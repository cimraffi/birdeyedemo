package com.cimraffi.android.birdeye.dbex;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 20;
    private static final String DATABASE_NAME = "birdseye.db";

    public DBBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table if not exists " + DBSchema.FlyLineTable.NAME
                + "(" + DBSchema.FlyLineTable.cols.ID + " integer primary key autoincrement, "
                + DBSchema.FlyLineTable.cols.CODE + ", "
                + DBSchema.FlyLineTable.cols.SVR_FLYLINE_ID + " integer, "
                + DBSchema.FlyLineTable.cols.TYPE + " integer, "
                + DBSchema.FlyLineTable.cols.DATE + ", "
                + DBSchema.FlyLineTable.cols.ADDRESS + ", "
                + DBSchema.FlyLineTable.cols.JSON+" text)" );

        db.execSQL("create table if not exists " + DBSchema.TaskTable.NAME
                + "(" + " _id integer primary key autoincrement, "
                + DBSchema.TaskTable.cols.CODE + ", "
                + DBSchema.TaskTable.cols.JSON + ", "
                + DBSchema.TaskTable.cols.SVR_TASK_ID + ", "
                + DBSchema.TaskTable.cols.STATUS + " , "
                + DBSchema.TaskTable.cols.ORIGIN_STATUS + " , "
                + DBSchema.TaskTable.cols.PREVIEW_STATUS  +" )" );

        db.execSQL("create table if not exists " + DBSchema.TaskPhotoTable.NAME
                + "(" + " _id integer primary key autoincrement, "
                + DBSchema.TaskPhotoTable.cols.TASK + ", "
                + DBSchema.TaskPhotoTable.cols.PHOTO + ", "
                + DBSchema.TaskPhotoTable.cols.PHOTO_TIME + " , "
                + DBSchema.TaskPhotoTable.cols.STATUS + " , "
                + DBSchema.TaskPhotoTable.cols.SVR_KEY + " , "
                + DBSchema.TaskPhotoTable.cols.MEDIAFILE + " BLOB, "
                + DBSchema.TaskPhotoTable.cols.PREVIEW_STATUS +" )" );

        db.execSQL("create table if not exists " + DBSchema.AreaLogTable.NAME
                + "(" + DBSchema.AreaLogTable.cols._ID + " integer primary key autoincrement, "
                + DBSchema.AreaLogTable.cols.AREAID + ", "
                + DBSchema.AreaLogTable.cols.CREATETIME + ")" );
        db.execSQL("create table if not exists " + DBSchema.LogsTable.NAME
                + "(" + DBSchema.LogsTable.cols._ID + " integer primary key autoincrement, "
                + DBSchema.LogsTable.cols.LOGID + ", "
                + DBSchema.LogsTable.cols.LOG + ")" );

        db.execSQL("create table if not exists " + DBSchema.ContinuePointTable.NAME
                + "(" + DBSchema.ContinuePointTable.cols._ID + " integer primary key autoincrement, "
                + DBSchema.ContinuePointTable.cols.BOUNDARYID + ", "
                + DBSchema.ContinuePointTable.cols.MONITORTIME + ", "
                + DBSchema.ContinuePointTable.cols.MISSIONSTARTTIME + ", "
                + DBSchema.ContinuePointTable.cols.INDEX + ", "
                + DBSchema.ContinuePointTable.cols.TYPE + ", "
                + DBSchema.ContinuePointTable.cols.LAT + ", "
                + DBSchema.ContinuePointTable.cols.LNG + ")" );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion==20&&oldVersion==18){
            db.execSQL("alter table " + DBSchema.TaskPhotoTable.NAME + " add column " + DBSchema.TaskPhotoTable.cols.MEDIAFILE+" BLOB" );
            db.execSQL("alter table " + DBSchema.ContinuePointTable.NAME + " add column " + DBSchema.ContinuePointTable.cols.MONITORTIME+" BLOB" );
        }
        //db.execSQL("alter table " + DBSchema.TaskTable.NAME + " add column " + DBSchema.TaskTable.cols.SVR_TASK_ID );
        //db.execSQL("alter table " + DBSchema.TaskTable.NAME + " add column " + DBSchema.TaskTable.cols.ORIGIN_STATUS );
    }
}
