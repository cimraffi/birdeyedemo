package com.cimraffi.android.birdeye.bean;

import java.io.Serializable;

public class MissionConfig implements Serializable {
    /**
     * 任务类型
     * 1、航线飞行
     * 2、定距拍照
     * 3、航点飞行
     */
    private int missionType;
    //飞行高度
    private int altitude;
    //飞行速度
    private int speed;
    //采集次数
    private int fetch_time;
    //误差
    private int delta;
    //积分时间1
    private int integralTime1;
    //积分时间2
    private int integralTime2;
    //存储格式
    private int format;
    //定标
    private boolean dingbiao;
    //悬停时间（积分时间1）
    private int stayTime;
    //拍照准备时间  单位毫秒
    private int readyTime;
    //相机控制
    private boolean cameraContorl = true;
    //监测时间
    private String monitorTime;
    //亩用量
    private int mudosage;
    //白板照片服务器地址
    private String whiteBoardUrl;
    //作物id
    private int cropId;
    //品种id
    private int varietyId;

    private int overlap = 0;

    //拍照间隔
    private int photoInterval;

    private static MissionConfig instance = new MissionConfig();

    private MissionConfig() {
        missionType = 1;
        stayTime = 5;
    }

    //获取唯一可用的对象
    public static MissionConfig getInstance() {
        return instance;
    }

    public int getMissionType() {
        return missionType;
    }

    public void setMissionType(int missionType) {
        this.missionType = missionType;
    }

    public int getStayTime() {
        return stayTime;
    }

    public void setStayTime(int stayTime) {
        this.stayTime = stayTime;
    }

    public int getReadyTime() {
        return readyTime;
    }

    public void setReadyTime(int readyTime) {
        this.readyTime = readyTime;
    }

    public boolean isCameraContorl() {
        return cameraContorl;
    }

    public void setCameraContorl(boolean cameraContorl) {
        this.cameraContorl = cameraContorl;
    }

    public int getAltitude() {
        return altitude;
    }

    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getFetch_time() {
        return fetch_time;
    }

    public void setFetch_time(int fetch_time) {
        this.fetch_time = fetch_time;
    }

    public int getDelta() {
        return delta;
    }

    public void setDelta(int delta) {
        this.delta = delta;
    }

    public int getIntegralTime1() {
        return integralTime1;
    }

    public void setIntegralTime1(int integralTime1) {
        this.integralTime1 = integralTime1;
    }

    public int getIntegralTime2() {
        return integralTime2;
    }

    public void setIntegralTime2(int integralTime2) {
        this.integralTime2 = integralTime2;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public boolean isDingbiao() {
        return dingbiao;
    }

    public void setDingbiao(boolean dingbiao) {
        this.dingbiao = dingbiao;
    }

    public String getMonitorTime() {
        return monitorTime;
    }

    public void setMonitorTime(String monitorTime) {
        this.monitorTime = monitorTime;
    }

    public int getMudosage() {
        return mudosage;
    }

    public void setMudosage(int mudosage) {
        this.mudosage = mudosage;
    }

    public String getWhiteBoardUrl() {
        return whiteBoardUrl;
    }

    public void setWhiteBoardUrl(String whiteBoardUrl) {
        this.whiteBoardUrl = whiteBoardUrl;
    }

    public int getCropId() {
        return cropId;
    }

    public void setCropId(int cropId) {
        this.cropId = cropId;
    }

    public int getVarietyId() {
        return varietyId;
    }

    public void setVarietyId(int varietyId) {
        this.varietyId = varietyId;
    }

    public int getOverlap() {
        return overlap;
    }
    public void setOverlap(int overlap) {
        this.overlap = overlap;
    }

    public int getPhotoInterval() {
        return photoInterval;
    }

    public void setPhotoInterval(int photoInterval) {
        this.photoInterval = photoInterval;
    }
}