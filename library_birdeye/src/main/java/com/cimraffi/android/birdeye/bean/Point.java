package com.cimraffi.android.birdeye.bean;

import com.airbnb.android.airmapview.MapUtils.LatLng;

import java.io.Serializable;

/*
 * 坐标点
 * by baoyh.
 * */
public class Point implements Serializable {
    public double latitude;
    public double longitude;

    public Point(LatLng ll){
        this.latitude = ll.latitude;
        this.longitude = ll.longitude;
    }
}