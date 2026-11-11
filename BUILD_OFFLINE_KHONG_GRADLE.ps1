# BUILD APK WITH INSTALLED JDK AND ANDROID SDK. No Gradle or network required.
$ErrorActionPreference = 'Stop'
$project = Split-Path -Parent $MyInvocation.MyCommand.Path
$javaHome = 'E:\java'
$platformJar = 'E:\Androin\Sdk\platforms\android-37.0\android.jar'
$toolsCandidates = @(
  'E:\Androin\Sdk\build-tools\36.0.0',
  'C:\Users\M SI\AppData\Local\Android\Sdk\build-tools\36.0.0',
  'E:\Androin\Sdk\build-tools\34.0.0',
  'C:\Users\M SI\AppData\Local\Android\Sdk\build-tools\34.0.0'
)
$toolsDir = $toolsCandidates | Where-Object { Test-Path (Join-Path $_ 'aapt2.exe') } | Select-Object -First 1
if (!(Test-Path "$javaHome\bin\javac.exe")) { throw 'Chua thay javac.exe trong E:\java\bin' }
if (!(Test-Path $platformJar)) { throw 'Chua thay android.jar: E:\Androin\Sdk\platforms\android-37.0\android.jar' }
if (!$toolsDir) { throw 'Khong tim thay aapt2.exe trong Build Tools 34/36 o C hoac E.' }
foreach ($exe in @('aapt2.exe','d8.bat','zipalign.exe','apksigner.bat')) {
  if (!(Test-Path (Join-Path $toolsDir $exe))) { throw "Thieu $exe tai $toolsDir" }
}
$build = Join-Path $project 'apk_offline_build'
$gen = Join-Path $build 'generated'
$classes = Join-Path $build 'classes'
$dex = Join-Path $build 'dex'
foreach ($dir in @($build,$gen,$classes,$dex)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
$env:JAVA_HOME = $javaHome
$env:Path = "$javaHome\bin;$env:Path"
$res = Join-Path $project 'app\src\main\res'
$compiled = Join-Path $build 'compiled-res.zip'
$manifestText = Get-Content -Raw -Encoding UTF8 (Join-Path $project 'app\src\main\AndroidManifest.xml')
# AAPT2 requires a manifest package; AGP normally adds it automatically.
$manifestText = $manifestText -replace '<manifest\s', '<manifest package="com.example.nhom4_quanlycongviec" '
$manifest = Join-Path $build 'AndroidManifest.xml'
[System.IO.File]::WriteAllText($manifest, $manifestText, (New-Object System.Text.UTF8Encoding($false)))
Write-Host '[1/6] Compile XML resources' -ForegroundColor Cyan
& "$toolsDir\aapt2.exe" compile --dir $res -o $compiled
if ($LASTEXITCODE -ne 0) { throw 'aapt2 compile failed' }
Write-Host '[2/6] Link Android resources + generate R.java' -ForegroundColor Cyan
$unsigned = Join-Path $build 'unsigned.apk'
& "$toolsDir\aapt2.exe" link -o $unsigned --manifest $manifest -I $platformJar --java $gen --min-sdk-version 24 --target-sdk-version 28 $compiled
if ($LASTEXITCODE -ne 0) { throw 'aapt2 link failed' }
Write-Host '[3/6] Compile Java' -ForegroundColor Cyan
$javaFiles = @(Get-ChildItem (Join-Path $project 'app\src\main\java') -Recurse -Filter '*.java' -File | Select-Object -ExpandProperty FullName)
$javaFiles += @(Get-ChildItem $gen -Recurse -Filter '*.java' -File | Select-Object -ExpandProperty FullName)
& "$javaHome\bin\javac.exe" -encoding UTF-8 -source 8 -target 8 -classpath $platformJar -d $classes @javaFiles
if ($LASTEXITCODE -ne 0) { throw 'javac failed' }
Write-Host '[4/6] Generate classes.dex' -ForegroundColor Cyan
$classFiles = @(Get-ChildItem $classes -Recurse -Filter '*.class' -File | Select-Object -ExpandProperty FullName)
& "$toolsDir\d8.bat" --min-api 24 --lib $platformJar --output $dex @classFiles
if ($LASTEXITCODE -ne 0) { throw 'd8 failed' }
Write-Host '[5/6] Package + zipalign APK' -ForegroundColor Cyan
& "$javaHome\bin\jar.exe" uf $unsigned -C $dex classes.dex
if ($LASTEXITCODE -ne 0) { throw 'jar package failed' }
$aligned = Join-Path $build 'aligned.apk'
& "$toolsDir\zipalign.exe" -f 4 $unsigned $aligned
if ($LASTEXITCODE -ne 0) { throw 'zipalign failed' }
Write-Host '[6/6] Debug-sign APK' -ForegroundColor Cyan
$keystore = Join-Path $build 'debug.keystore'
if (!(Test-Path $keystore)) {
 & "$javaHome\bin\keytool.exe" -genkeypair -keystore $keystore -storepass android -keypass android -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 -dname 'CN=Android Debug,O=Android,C=US' -noprompt
 if ($LASTEXITCODE -ne 0) { throw 'keytool failed' }
}
$apk = Join-Path $build 'QuanLyCongViec-Nhom4-debug.apk'
& "$toolsDir\apksigner.bat" sign --ks $keystore --ks-key-alias androiddebugkey --ks-pass pass:android --key-pass pass:android --out $apk $aligned
if ($LASTEXITCODE -ne 0) { throw 'apksigner failed' }
& "$toolsDir\apksigner.bat" verify $apk
if ($LASTEXITCODE -ne 0) { throw 'apksigner verify failed' }
Write-Host "SUCCESS: $apk" -ForegroundColor Green
Write-Host 'Install APK by opening it on a device, or use adb install -r (APK path).'
