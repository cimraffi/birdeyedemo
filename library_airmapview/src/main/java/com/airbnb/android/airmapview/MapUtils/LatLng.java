package com.airbnb.android.airmapview.MapUtils;

public class LatLng {

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double longitude;
    public double latitude;

    public LatLng(double lat, double lng){
        this.longitude = lng;
        this.latitude = lat;
    }
}
