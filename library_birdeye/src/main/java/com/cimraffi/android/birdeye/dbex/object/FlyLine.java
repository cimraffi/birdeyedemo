package com.cimraffi.android.birdeye.dbex.object;

import java.io.Serializable;

public class FlyLine implements Serializable {
    private long id; //航线ID
    private String code; //航线识别码
    private Long svrFlyLineId; //服务器端航线id
    private int type; //生成航线的方式
    private long date; //航线生成日期
    private String address; //航线地址
    private String json; //航线详情

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public Long getSvrFlyLineId() {
        return svrFlyLineId;
    }

    public void setSvrFlyLineId(Long svrFlyLineId) {
        this.svrFlyLineId = svrFlyLineId;
    }
}
