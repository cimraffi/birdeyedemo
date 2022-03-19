package com.airbnb.android.airmapview.MapUtils;

import androidx.annotation.NonNull;

public class LatLngBounds {
    public LatLng northeast;
    public LatLng southwest;

    public LatLngBounds(LatLng sw, LatLng ne){
        this.southwest = sw;
        this.northeast = ne;
    }

    /**
     * Returns the center of the lat/lng bounds.
     * @return
     */
    @NonNull
    public LatLng getCenter() {
        double midLat = (this.southwest.getLatitude() + this.northeast.getLatitude()) / 2.0D;
        double neLng = this.northeast.getLongitude();
        double swLng = this.southwest.getLongitude();
        double midLng;
        if (swLng <= neLng) {
            midLng = (neLng + swLng) / 2.0D;
        } else {
            midLng = (neLng + 360.0D + swLng) / 2.0D;
        }

        return new LatLng(midLat, midLng);
    }

    private static Builder builder;
    public static Builder builder(){
        if(null == builder){
            builder = new Builder();
            return builder;
        }else{
            return builder;
        }
    }

    /**
     * Builder class for {@link LatLngBounds}.
     */
    public static class Builder {

        private double northeastLat = 0;
        private double northeastLng = 0;
        private double southwestLat = 0;
        private double southwestLng = 0;

        /**
         * Includes this point for building of the bounds. The bounds will be extended in a minimum way
         * to include this point.
         * @param point
         * @return builder object
         */
        @NonNull public Builder include(@NonNull LatLng point) {
            if (northeastLat == 0) {
                northeastLat = point.getLatitude();
                northeastLng = point.getLongitude();
            }
            if (southwestLat == 0) {
                southwestLat = point.getLatitude();
                southwestLng = point.getLongitude();
            }
            if (point.getLatitude() > northeastLat) {
                northeastLat = point.getLatitude();
            } else if (point.getLatitude() < southwestLat) {
                southwestLat = point.getLatitude();
            }
            if (point.getLongitude() > northeastLng) {
                northeastLng = point.getLongitude();
            } else if (point.getLongitude() < southwestLng) {
                southwestLng = point.getLongitude();
            }
            return this;
        }

        /**
         * Constructs a new {@link LatLngBounds} from current boundaries.
         * @return
         */
        @NonNull public LatLngBounds build() {
            LatLng sw = new LatLng(southwestLat, southwestLng);
            LatLng ne = new LatLng(northeastLat, northeastLng);
            return new LatLngBounds(sw, ne);
        }
    }
}
