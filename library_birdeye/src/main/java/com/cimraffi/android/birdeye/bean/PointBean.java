package com.cimraffi.android.birdeye.bean;

import com.airbnb.android.airmapview.MapUtils.LatLng;

import java.io.Serializable;

public class PointBean implements Serializable {
    public double latitude;
    public double longitude;

    public PointBean(LatLng ll){
        this.latitude = ll.latitude;
        this.longitude = ll.longitude;
    }
}