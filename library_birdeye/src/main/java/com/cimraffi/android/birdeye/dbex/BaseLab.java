package com.cimraffi.android.birdeye.dbex;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.cimraffi.android.birdeye.ApplicationFactory;

public class BaseLab {
    private Context mContext;
    protected SQLiteDatabase mDatabase;

    public BaseLab() {
        mContext = ApplicationFactory.getInstance().getBaseApplication();
        mDatabase = new DBBaseHelper(mContext).getWritableDatabase();
    }

    protected DBCursorWrapper queryDB(String tableName, String whereClause, String[] whereArgs) {
//        String orderBy = null;
//        if (tableName.equals(DBSchema.FlyLineTable.NAME)){
//            orderBy = "date desc";
//        }
        Cursor cursor = mDatabase.query(tableName,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null);

        return new DBCursorWrapper(cursor);
    }

//    private

    protected DBCursorWrapper queryDB(String tableName, String where, String orderBy) {
        String condition = "";
        if (!TextUtils.isEmpty(where)){
            condition = " "+where;
        }
        if (!TextUtils.isEmpty(orderBy)){
            condition += " order by "+orderBy;
        }
        Cursor cursor = mDatabase.rawQuery("select * from "+tableName+condition,null);//" where type="+" and date>? and date<?",whereArgs);
//        Cursor cursor = mDatabase.query(tableName,
//                null,
//                whereClause,
//                whereArgs,
//                null,
//                null,
//                orderBy);

        return new DBCursorWrapper(cursor);
    }
}
