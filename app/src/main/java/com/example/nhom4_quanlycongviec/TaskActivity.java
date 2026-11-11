package com.example.nhom4_quanlycongviec;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;
import java.util.Locale;

public class TaskActivity extends Activity {
    private EditText edtName,edtDescription;
    private TextView txtDate,txtPageTitle,btnBack;
    private Spinner spinnerPriority;
    private Button btnSave,btnDelete;
    private DatabaseHelper database;
    private int taskId=-1;
    private String selectedDate="";
    private Task editingTask;
    private final String[] priorities={"Thấp","Trung bình","Cao"};
    @Override protected void onCreate(Bundle b){
        super.onCreate(b);setContentView(R.layout.activity_task);database=new DatabaseHelper(this);
        edtName=findViewById(R.id.edtName);edtDescription=findViewById(R.id.edtDescription);txtDate=findViewById(R.id.txtDate);
        txtPageTitle=findViewById(R.id.txtPageTitle);btnBack=findViewById(R.id.btnBack);spinnerPriority=findViewById(R.id.spinnerPriority);
        btnSave=findViewById(R.id.btnSave);btnDelete=findViewById(R.id.btnDelete);
        ArrayAdapter<String> a=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,priorities);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);spinnerPriority.setAdapter(a);spinnerPriority.setSelection(1);
        taskId=getIntent().getIntExtra("task_id",-1);
        if(taskId!=-1){editingTask=database.getTask(taskId);if(editingTask==null){Toast.makeText(this,"Không tìm thấy công việc",Toast.LENGTH_SHORT).show();finish();return;}loadExistingTask();}
        btnBack.setOnClickListener(v->finish());txtDate.setOnClickListener(v->showDatePicker());btnSave.setOnClickListener(v->saveTask());btnDelete.setOnClickListener(v->confirmDelete());
    }
    private void loadExistingTask(){
        txtPageTitle.setText("Chỉnh sửa công việc ✏️");edtName.setText(editingTask.name);edtDescription.setText(editingTask.description);
        selectedDate=editingTask.deadline==null?"":editingTask.deadline;updateDateText();
        for(int i=0;i<priorities.length;i++)if(priorities[i].equals(editingTask.priority)){spinnerPriority.setSelection(i);break;}
        btnSave.setText("Cập nhật công việc");btnDelete.setVisibility(View.VISIBLE);
    }
    private void showDatePicker(){
        Calendar c=Calendar.getInstance();
        if(selectedDate.matches("\\d{4}-\\d{2}-\\d{2}")){
            String[] p=selectedDate.split("-");c.set(Integer.parseInt(p[0]),Integer.parseInt(p[1])-1,Integer.parseInt(p[2]));
        }
        new DatePickerDialog(this,(view,y,m,d)->{selectedDate=String.format(Locale.US,"%04d-%02d-%02d",y,m+1,d);updateDateText();},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();
    }
    private void updateDateText(){
        if(selectedDate.isEmpty()){txtDate.setText("📅  Chọn ngày hết hạn");return;}
        String[] p=selectedDate.split("-");txtDate.setText("📅  "+p[2]+"/"+p[1]+"/"+p[0]);
    }
    private void saveTask(){
        String name=edtName.getText().toString().trim(),description=edtDescription.getText().toString().trim(),priority=spinnerPriority.getSelectedItem().toString();
        if(name.isEmpty()){edtName.setError("Vui lòng nhập tên công việc");edtName.requestFocus();return;}
        if(selectedDate.isEmpty()){Toast.makeText(this,"Vui lòng chọn ngày hết hạn",Toast.LENGTH_SHORT).show();return;}
        if(taskId==-1){
            long id=database.addTask(name,description,selectedDate,priority);
            if(id==-1){Toast.makeText(this,"Thêm công việc thất bại",Toast.LENGTH_SHORT).show();return;}
            Toast.makeText(this,"Đã thêm công việc!",Toast.LENGTH_SHORT).show();
        }else{
            editingTask.name=name;editingTask.description=description;editingTask.deadline=selectedDate;editingTask.priority=priority;
            if(database.updateTask(editingTask)==0){Toast.makeText(this,"Cập nhật thất bại",Toast.LENGTH_SHORT).show();return;}
            Toast.makeText(this,"Đã cập nhật công việc!",Toast.LENGTH_SHORT).show();
        }
        finish();
    }
    private void confirmDelete(){
        if(taskId==-1)return;
        new AlertDialog.Builder(this).setTitle("Xóa công việc?").setMessage("Công việc này sẽ bị xóa khỏi danh sách.").setNegativeButton("Hủy",null).setPositiveButton("Xóa",(dialog,which)->{
            if(database.deleteTask(taskId)>0){Toast.makeText(this,"Đã xóa công việc",Toast.LENGTH_SHORT).show();finish();}
            else Toast.makeText(this,"Không thể xóa công việc",Toast.LENGTH_SHORT).show();
        }).show();
    }
    @Override protected void onDestroy(){if(database!=null)database.close();super.onDestroy();}
}
