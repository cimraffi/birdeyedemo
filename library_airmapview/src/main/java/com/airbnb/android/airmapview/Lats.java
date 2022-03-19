package com.airbnb.android.airmapview;

public class Lats {

    private int len;
    private double lat;

    public Lats(int len, double lat){
        this.len = len;
        this.lat = lat;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }
}
