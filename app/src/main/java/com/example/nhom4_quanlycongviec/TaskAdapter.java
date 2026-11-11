package com.example.nhom4_quanlycongviec;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import java.util.ArrayList;

public class TaskAdapter extends BaseAdapter {
    public interface TaskListener { void onTaskClick(Task task); void onTaskChecked(Task task,boolean checked); }
    private final Context context;
    private ArrayList<Task> tasks;
    private final TaskListener listener;
    public TaskAdapter(Context c,ArrayList<Task> data,TaskListener l){context=c;tasks=data;listener=l;}
    public void updateData(ArrayList<Task> data){tasks=data;notifyDataSetChanged();}
    @Override public int getCount(){return tasks.size();}
    @Override public Task getItem(int position){return tasks.get(position);}
    @Override public long getItemId(int position){return getItem(position).id;}
    @Override public View getView(int position,View convertView,ViewGroup parent){
        View view=convertView==null?LayoutInflater.from(context).inflate(R.layout.item_task,parent,false):convertView;
        Task t=getItem(position);
        TextView name=view.findViewById(R.id.txtTaskName),description=view.findViewById(R.id.txtDescription),deadline=view.findViewById(R.id.txtDeadline),priority=view.findViewById(R.id.txtPriority);
        CheckBox cb=view.findViewById(R.id.checkDone);
        name.setText(t.name);
        String desc=t.description==null?"":t.description;description.setText(desc);description.setVisibility(desc.trim().isEmpty()?View.GONE:View.VISIBLE);
        String date=t.deadline==null?"":t.deadline;
        if(date.matches("\\d{4}-\\d{2}-\\d{2}"))date=date.substring(8,10)+"/"+date.substring(5,7)+"/"+date.substring(0,4);
        deadline.setText("Hạn: "+(date.isEmpty()?"Chưa đặt":date));
        String level=t.priority==null?"Trung bình":t.priority;priority.setText(level);
        priority.setTextColor(context.getColor("Cao".equals(level)?R.color.red:"Thấp".equals(level)?R.color.green:R.color.orange));
        cb.setOnCheckedChangeListener(null);cb.setChecked(t.completed);
        int flags=name.getPaintFlags();name.setPaintFlags(t.completed?flags|Paint.STRIKE_THRU_TEXT_FLAG:flags&~Paint.STRIKE_THRU_TEXT_FLAG);name.setAlpha(t.completed?0.55f:1f);
        cb.setOnCheckedChangeListener((buttonView,checked)->listener.onTaskChecked(t,checked));
        view.setOnClickListener(v->listener.onTaskClick(t));
        return view;
    }
}
