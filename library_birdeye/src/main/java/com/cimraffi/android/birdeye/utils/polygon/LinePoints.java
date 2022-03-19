package com.cimraffi.android.birdeye.utils.polygon;

import com.airbnb.android.airmapview.MapUtils.LatLng;

import java.util.ArrayList;

public class LinePoints {

    private ArrayList<LatLng> points;
    private double remain;

    public LinePoints(ArrayList<LatLng> points, double remain) {
        this.points = points;
        this.remain = remain;
    }

    public double getRemain() {
        return remain;
    }

    public void setRemain(double remain) {
        this.remain = remain;
    }

    public ArrayList<LatLng> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<LatLng> points) {
        this.points = points;
    }

}
