package com.cimraffi.android.birdeye.dbex;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.airbnb.android.airmapview.MapUtils.LatLng;
import com.cimraffi.android.birdeye.bean.ContinuePoint;
import com.cimraffi.android.birdeye.dbex.object.FlyLine;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import dji.sdk.media.MediaFile;

public class DBCursorWrapper extends CursorWrapper {
    public DBCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public FlyLine getFlyLine() {
        long id = getLong(getColumnIndex(DBSchema.FlyLineTable.cols.ID));
        String code = getString(getColumnIndex(DBSchema.FlyLineTable.cols.CODE));
        long svr_flyline_id = getLong(getColumnIndex(DBSchema.FlyLineTable.cols.SVR_FLYLINE_ID));
        int type = getInt(getColumnIndex(DBSchema.FlyLineTable.cols.TYPE));
        long date = getLong(getColumnIndex(DBSchema.FlyLineTable.cols.DATE));
        String address = getString(getColumnIndex(DBSchema.FlyLineTable.cols.ADDRESS));
        String json = getString(getColumnIndex(DBSchema.FlyLineTable.cols.JSON));

        FlyLine fly_line = new FlyLine();
        fly_line.setId(id);
        fly_line.setCode(code);
        fly_line.setSvrFlyLineId(svr_flyline_id);
        fly_line.setType(type);
        fly_line.setDate(date);
        fly_line.setAddress(address);
        fly_line.setJson(json);
        return fly_line;
    }

    public String getLog(){
        String log = getString(getColumnIndex(DBSchema.LogsTable.cols.LOG));
        return log;
    }

    public ContinuePoint getContinuePoint(){
        try {
            String boundaryId = getString(getColumnIndex(DBSchema.ContinuePointTable.cols.BOUNDARYID));
            String monitorTime = getString(getColumnIndex(DBSchema.ContinuePointTable.cols.MONITORTIME));
            long missionStartTime = getLong(getColumnIndex(DBSchema.ContinuePointTable.cols.MISSIONSTARTTIME));
            int type = getInt(getColumnIndex(DBSchema.ContinuePointTable.cols.TYPE));
            int index = getInt(getColumnIndex(DBSchema.ContinuePointTable.cols.INDEX));
            double lat = getDouble(getColumnIndex(DBSchema.ContinuePointTable.cols.LAT));
            double lng = getDouble(getColumnIndex(DBSchema.ContinuePointTable.cols.LNG));
            ContinuePoint continuePoint = new ContinuePoint();
            continuePoint.targetWaypointIndex = index;
            continuePoint.continuePosition = new LatLng(lat,lng);
            continuePoint.missionType = type;
            continuePoint.boundaryId = boundaryId;
            continuePoint.monitorTime = monitorTime;
            continuePoint.missionStartTime = missionStartTime;
            return continuePoint;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
