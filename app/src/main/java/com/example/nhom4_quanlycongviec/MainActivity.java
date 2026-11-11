package com.example.nhom4_quanlycongviec;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class MainActivity extends Activity {
    private DatabaseHelper database;
    private TaskAdapter adapter;
    private TextView txtTotal,txtDone,txtPending,txtEmpty;
    private EditText edtSearch;
    private ListView listTasks;
    private Button btnAll,btnPending,btnDone,btnAdd;
    private int currentFilter=0; // 0=tất cả, 1=chưa hoàn thành, 2=đã hoàn thành
    @Override protected void onCreate(Bundle b){
        super.onCreate(b);setContentView(R.layout.activity_main);database=new DatabaseHelper(this);
        txtTotal=findViewById(R.id.txtTotal);txtDone=findViewById(R.id.txtDone);txtPending=findViewById(R.id.txtPending);txtEmpty=findViewById(R.id.txtEmpty);
        edtSearch=findViewById(R.id.edtSearch);listTasks=findViewById(R.id.listTasks);
        btnAll=findViewById(R.id.btnAll);btnPending=findViewById(R.id.btnPending);btnDone=findViewById(R.id.btnDone);btnAdd=findViewById(R.id.btnAdd);
        adapter=new TaskAdapter(this,new ArrayList<>(),new TaskAdapter.TaskListener(){
            @Override public void onTaskClick(Task task){Intent i=new Intent(MainActivity.this,TaskActivity.class);i.putExtra("task_id",task.id);startActivity(i);}
            @Override public void onTaskChecked(Task task,boolean checked){if(database.setCompleted(task.id,checked)==0)Toast.makeText(MainActivity.this,"Không thể cập nhật trạng thái",Toast.LENGTH_SHORT).show();loadTasks();}
        });
        listTasks.setAdapter(adapter);
        btnAdd.setOnClickListener(v->startActivity(new Intent(this,TaskActivity.class)));
        btnAll.setOnClickListener(v->{currentFilter=0;loadTasks();});
        btnPending.setOnClickListener(v->{currentFilter=1;loadTasks();});
        btnDone.setOnClickListener(v->{currentFilter=2;loadTasks();});
        edtSearch.addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            public void onTextChanged(CharSequence s,int start,int before,int count){loadTasks();}
            public void afterTextChanged(Editable s){}
        });
        loadTasks();
    }
    private void loadTasks(){
        String keyword=edtSearch.getText().toString().trim();
        ArrayList<Task> tasks=database.getTasks(keyword,currentFilter);adapter.updateData(tasks);
        boolean empty=tasks.isEmpty();listTasks.setVisibility(empty?View.GONE:View.VISIBLE);txtEmpty.setVisibility(empty?View.VISIBLE:View.GONE);
        if(empty)txtEmpty.setText(!keyword.isEmpty()?"Không tìm thấy công việc phù hợp":currentFilter!=0?"Không có công việc ở trạng thái này":"Chưa có công việc nào.\nHãy thêm công việc đầu tiên!");
        txtTotal.setText(String.valueOf(database.countAll()));txtDone.setText(String.valueOf(database.countByStatus(1)));txtPending.setText(String.valueOf(database.countByStatus(0)));
        Button[] buttons={btnAll,btnPending,btnDone};
        for(int i=0;i<buttons.length;i++){
            boolean chosen=i==currentFilter;
            buttons[i].setBackgroundTintList(ColorStateList.valueOf(getColor(chosen?R.color.primary:R.color.primary_light)));
            buttons[i].setTextColor(getColor(chosen?R.color.white:R.color.primary_dark));
        }
    }
    @Override protected void onResume(){super.onResume();if(database!=null&&adapter!=null)loadTasks();}
    @Override protected void onDestroy(){if(database!=null)database.close();super.onDestroy();}
}
