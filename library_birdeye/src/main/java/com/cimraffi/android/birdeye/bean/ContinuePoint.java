package com.cimraffi.android.birdeye.bean;

import com.airbnb.android.airmapview.MapUtils.LatLng;

import java.io.Serializable;

public class ContinuePoint implements Serializable {
    //地块id
    public String boundaryId;
    //监测日期
    public String monitorTime;
    //任务类型
    public int missionType;
    //下一个航点索引
    public int targetWaypointIndex;
    //续航点经纬度
    public LatLng continuePosition;
    //本次飞行开始时的起始时间
    public long missionStartTime;
}
