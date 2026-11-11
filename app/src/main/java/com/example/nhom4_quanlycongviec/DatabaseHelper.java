package com.example.nhom4_quanlycongviec;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TABLE="congviec";
    public DatabaseHelper(Context context) { super(context,"quanlycongviec.db",null,1); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (id INTEGER PRIMARY KEY AUTOINCREMENT,ten_cong_viec TEXT NOT NULL,mo_ta TEXT,ngay_het_han TEXT,muc_do TEXT,trang_thai INTEGER NOT NULL DEFAULT 0)");
    }
    @Override public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion) { }
    public long addTask(String name,String description,String deadline,String priority) {
        ContentValues v=new ContentValues();v.put("ten_cong_viec",name);v.put("mo_ta",description);v.put("ngay_het_han",deadline);v.put("muc_do",priority);v.put("trang_thai",0);
        return getWritableDatabase().insert(TABLE,null,v);
    }
    public ArrayList<Task> getTasks(String keyword,int filter) {
        ArrayList<Task> list=new ArrayList<>();
        String sql="SELECT * FROM "+TABLE+" WHERE ten_cong_viec LIKE ?";
        if(filter==1)sql+=" AND trang_thai=0";
        if(filter==2)sql+=" AND trang_thai=1";
        sql+=" ORDER BY trang_thai ASC,id DESC";
        // Escape wildcards so search matches the entered text literally.
        try(Cursor c=getReadableDatabase().rawQuery(sql,new String[]{"%"+keyword+"%"})) {
            while(c.moveToNext())list.add(readTask(c));
        }
        return list;
    }
    public Task getTask(int id) {
        try(Cursor c=getReadableDatabase().rawQuery("SELECT * FROM "+TABLE+" WHERE id=?",new String[]{String.valueOf(id)})) {
            if(c.moveToFirst())return readTask(c);
        }
        return null;
    }
    private Task readTask(Cursor c) {
        return new Task(c.getInt(c.getColumnIndexOrThrow("id")),c.getString(c.getColumnIndexOrThrow("ten_cong_viec")),c.getString(c.getColumnIndexOrThrow("mo_ta")),c.getString(c.getColumnIndexOrThrow("ngay_het_han")),c.getString(c.getColumnIndexOrThrow("muc_do")),c.getInt(c.getColumnIndexOrThrow("trang_thai"))==1);
    }
    public int updateTask(Task t) {
        ContentValues v=new ContentValues();v.put("ten_cong_viec",t.name);v.put("mo_ta",t.description);v.put("ngay_het_han",t.deadline);v.put("muc_do",t.priority);v.put("trang_thai",t.completed?1:0);
        return getWritableDatabase().update(TABLE,v,"id=?",new String[]{String.valueOf(t.id)});
    }
    public int deleteTask(int id) { return getWritableDatabase().delete(TABLE,"id=?",new String[]{String.valueOf(id)}); }
    public int setCompleted(int id,boolean done) {
        ContentValues v=new ContentValues();v.put("trang_thai",done?1:0);
        return getWritableDatabase().update(TABLE,v,"id=?",new String[]{String.valueOf(id)});
    }
    public int countAll() { return count("SELECT COUNT(*) FROM "+TABLE); }
    public int countByStatus(int status) { return count("SELECT COUNT(*) FROM "+TABLE+" WHERE trang_thai="+(status==1?1:0)); }
    private int count(String sql) {
        try(Cursor c=getReadableDatabase().rawQuery(sql,null)){return c.moveToFirst()?c.getInt(0):0;}
    }
}
