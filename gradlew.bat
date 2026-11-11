@echo off
setlocal
set "JAVA_HOME=E:\java"
set "GRADLE_USER_HOME=E:\GradleCache"
set "PATH=%JAVA_HOME%\bin;%PATH%"
if not exist "E:\grade\gradle-8.9\bin\gradle.bat" (
 echo KHONG TIM THAY: E:\grade\gradle-8.9\bin\gradle.bat
 exit /b 1
)
call "E:\grade\gradle-8.9\bin\gradle.bat" %*
exit /b %ERRORLEVEL%
