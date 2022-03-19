package com.airbnb.android.airmapview;

/*
* longitude：经度
* latitude：纬度
* */

import com.airbnb.android.airmapview.MapUtils.LatLng;

public class PolygonBound {

    private LatLng center;
    private LatLng nw, ne, sw, se;

    private double northLat;

    public LatLng getCenter() {
        return center;
    }

    public void setCenter(LatLng center) {
        this.center = center;
    }

    public LatLng getNw() {
        return nw;
    }

    public void setNw(LatLng nw) {
        this.nw = nw;
    }

    public LatLng getNe() {
        return ne;
    }

    public void setNe(LatLng ne) {
        this.ne = ne;
    }

    public LatLng getSw() {
        return sw;
    }

    public void setSw(LatLng sw) {
        this.sw = sw;
    }

    public LatLng getSe() {
        return se;
    }

    public void setSe(LatLng se) {
        this.se = se;
    }

    public double getNorthLat() {
        return northLat;
    }

    public void setNorthLat(double northLat) {
        this.northLat = northLat;
    }
}
