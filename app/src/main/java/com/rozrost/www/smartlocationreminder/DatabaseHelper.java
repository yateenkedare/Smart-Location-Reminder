package com.rozrost.www.smartlocationreminder;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.renderscript.Double2;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Geo.db";
    public static final String TABLE_NAME = "places_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "NAME";
    public static final String COL_3 = "LATITUDE";
    public static final String COL_4 = "LONGITUDE";
    public static final String COL_5 = "RANGE";
    public static final String COL_6 = "TIMEOUT";
    public static final String COL_7 = "STATUS";
    public static final String COL_8 = "STARTTIME";
    public static final String COL_9 = "ENDTIME";
    public static final String COL_10 = "TASK";



    public static final Integer SNZ = 36000;



    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,NAME TEXT,LATITUDE TEXT,LONGITUDE TEXT, RANGE TEXT, TIMEOUT TEXT, STATUS TEXT, STARTTIME TEXT, ENDTIME TEXT, TASK TEXT )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }



    public String addToDatabase(String Name, double latitude, double longitude, int duration, int radius, int sh, int sm, int eh, int em, String task) throws ParseException {
        SQLiteDatabase db = this.getWritableDatabase();
        Date start = convertintoDate(sh,sm);
        Date end = convertintoDate(eh,em);
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2,Name);
        contentValues.put(COL_3,latitude);
        contentValues.put(COL_4,longitude);
        contentValues.put(COL_5,radius);
        contentValues.put(COL_6,duration);
        contentValues.put(COL_7,"false");
        contentValues.put(COL_8,start.toString());
        contentValues.put(COL_9,end.toString());
        contentValues.put(COL_10,task);
        Log.i("INCLASS",start.toString());
        Log.i("INCLASS",end.toString());
        long result = db.insert(TABLE_NAME,null ,contentValues);
        return String.valueOf(result);
    }

    public Date convertintoDate(int h,int m) throws ParseException {
        SimpleDateFormat lsd = new SimpleDateFormat("H:mm");
        Date n = lsd.parse(Integer.toString(h)+':'+Integer.toString(m));
        return n;
    }

    public String getTime(String primary) throws ParseException {
        SimpleDateFormat lsd = new SimpleDateFormat("H:mm");
        String timeString;
        timeString = lsd.format(getStart(primary))+" : "+lsd.format(getEnd(primary));
        return timeString;
    }

    public boolean CheckInBetween(String primary) throws ParseException {
        Date now = new Date();
        now = changeFormat(now);
        Date start = changeFormat(getStart(primary));
        Date end = changeFormat(getEnd(primary));
        Log.i("DATE","NOW "+now.toString());
        Log.i("DATE","START "+start.toString());
        Log.i("DATE","END "+end.toString());
        if (now.after(start) && now.before(end)){
            return true;
        }
        else
            return false;
    }

    private Date changeFormat(Date x) throws ParseException {
        Date date = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH).parse(x.toString());
        SimpleDateFormat lsd = new SimpleDateFormat("H:mm");
        String newString = lsd.format(date);
        Date n = lsd.parse(newString);
        return n;
    }

    public Date getStart(String primaryKey) throws ParseException {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[] { COL_8 }, COL_1 + "=?",
                new String[] { primaryKey }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        SimpleDateFormat dateFormat = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        Date t;
        t = dateFormat.parse(cursor.getString(0));
        return t;
    }


    public String getStartStr(String primaryKey) throws ParseException {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[] { COL_8 }, COL_1 + "=?",
                new String[] { primaryKey }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        SimpleDateFormat dateFormat = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        //Date t;
        // t = dateFormat.parse(cursor.getString(0));
        return cursor.getString(0);
    }



    public Date getEnd(String primaryKey) throws ParseException {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[] { COL_9 }, COL_1 + "=?",
                new String[] { primaryKey }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        Date t;
        t = dateFormat.parse(cursor.getString(0));
        return t;
    }


    public boolean changeOnOffInDatabase(String primaryKey, Boolean onOff){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_7,onOff.toString());
        if (db.update(TABLE_NAME, contentValues, "ID = ?",new String[] { primaryKey })>0)
            return true;
        else
            return false;
    }

    public String getNameFromDatabase(String primaryKey){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[] { COL_2 }, COL_1 + "=?",
                new String[] { primaryKey }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        return cursor.getString(0);
    }

    public double getLatitudeFromDatabase(String primaryKey){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[] { COL_3 }, COL_1 + "=?",
                new String[] { primaryKey }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        return Double.parseDouble(cursor.getString(0));
    }

    public double getLongitudeFromDatabase(String primaryKey){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[] { COL_4 }, COL_1 + "=?",
                new String[] { primaryKey }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        return Double.parseDouble(cursor.getString(0));
    }

    public int getDurationFromDatabase(String primaryKey){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[] { COL_6 }, COL_1 + "=?",
                new String[] { primaryKey }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        return Integer.parseInt(cursor.getString(0));
    }

    public int getRadiusFromDatabase(String primaryKey){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[] { COL_5 }, COL_1 + "=?",
                new String[] { primaryKey }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        return Integer.parseInt(cursor.getString(0));
    }

    public boolean getStatusFromDatabase(String primaryKey) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COL_7}, COL_1 + "=?",
                new String[]{primaryKey}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        return Boolean.parseBoolean(cursor.getString(0));
    }

    public  boolean deleteData(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        if( db.delete(TABLE_NAME, "ID = ?",new String[] { id }) > 0)
            return true;
        else
            return false;
    }

    public boolean changeDuration(String primaryKey, int duration){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_6,duration);
        if (db.update(TABLE_NAME, contentValues, "ID = ?", new String[]{primaryKey})>0)
            return true;
        else
            return false;


    }


    public String getTaskFromDatabase(String primaryKey){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[] { COL_10 }, COL_1 + "=?",
                new String[] { primaryKey }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        return cursor.getString(0);
    }




    /*ignore the funcitons below*/
    public boolean insertData(String name,String latitude,String longitude,String range, String timeout, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2,name);
        contentValues.put(COL_3,latitude);
        contentValues.put(COL_4,longitude);
        contentValues.put(COL_5,range);
        contentValues.put(COL_6,timeout);
        contentValues.put(COL_7,status);

        long result = db.insert(TABLE_NAME,null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }
    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME,null);
        return res;
    }
    public boolean updateData(String id,String name,String latitude,String longitude,String range, String timeout, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1,id);
        contentValues.put(COL_2,name);
        contentValues.put(COL_3,latitude);
        contentValues.put(COL_4,longitude);
        contentValues.put(COL_5,range);
        contentValues.put(COL_6,timeout);
        contentValues.put(COL_7,status);
        db.update(TABLE_NAME, contentValues, "ID = ?",new String[] { id });
        return true;
    }
    public Cursor getOne(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME+" where id ="+id,null);
        return res;

    }
}
