package com.cimraffi.android.birdeye.dbex;

import android.content.ContentValues;
import android.content.Context;

import com.cimraffi.android.birdeye.bean.ContinuePoint;

public class SomeLab extends BaseLab {
    private static SomeLab sLab;
    public static SomeLab get(Context context) {
        if (sLab == null) {
            sLab = new SomeLab();
        }
        return sLab;
    }

    public void addContinuePoint(ContinuePoint c) {
        ContentValues values = getContentValues(c);
        mDatabase.insert(DBSchema.ContinuePointTable.NAME, null, values);
    }

    private static ContentValues getContentValues(ContinuePoint c) {
        ContentValues values = new ContentValues();
        values.put(DBSchema.ContinuePointTable.cols.BOUNDARYID, c.boundaryId);
        values.put(DBSchema.ContinuePointTable.cols.MONITORTIME, c.monitorTime);
        values.put(DBSchema.ContinuePointTable.cols.MISSIONSTARTTIME, c.missionStartTime);
        values.put(DBSchema.ContinuePointTable.cols.TYPE, c.missionType);
        values.put(DBSchema.ContinuePointTable.cols.INDEX, c.targetWaypointIndex);
        values.put(DBSchema.ContinuePointTable.cols.LAT, c.continuePosition.latitude);
        values.put(DBSchema.ContinuePointTable.cols.LNG, c.continuePosition.longitude);
        return values;
    }

    public ContinuePoint getContinuePoint(String boundaryId) {
        DBCursorWrapper cursor = queryDB(DBSchema.ContinuePointTable.NAME, DBSchema.ContinuePointTable.cols.BOUNDARYID + " = ?",
                new String[]{boundaryId});
        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getContinuePoint();
        } finally {
            cursor.close();
        }
    }

    public void deleteContinuePoint(String boundaryId) {
        try {
            mDatabase.delete(DBSchema.ContinuePointTable.NAME, DBSchema.ContinuePointTable.cols.BOUNDARYID + " = ?",
                    new String[]{boundaryId});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
