package com.airbnb.android.airmapview;

import android.graphics.Point;

import com.airbnb.android.airmapview.MapUtils.LatLng;
import com.airbnb.android.airmapview.MapUtils.LatLngBounds;
import com.airbnb.android.airmapview.MapUtils.SphericalUtil;

import java.util.List;

public class PolygonUtil {

    /**
     * 获取不规则多边形重心点
     *
     * @param mPoints
     * @return
     */
    public static LatLng getCenterOfGravity(List<LatLng> mPoints) {
        double area = 0.0;//多边形面积
        double Gx = 0.0, Gy = 0.0;// 重心的x、y
        for (int i = 1; i <= mPoints.size(); i++) {
            double iLat = mPoints.get(i % mPoints.size()).latitude;
            double iLng = mPoints.get(i % mPoints.size()).longitude;
            double nextLat = mPoints.get(i - 1).latitude;
            double nextLng = mPoints.get(i - 1).longitude;
            double temp = (iLat * nextLng - iLng * nextLat) / 2.0;
            area += temp;
            Gx += temp * (iLat + nextLat) / 3.0;
            Gy += temp * (iLng + nextLng) / 3.0;
        }
        Gx = Gx / area;
        Gy = Gy / area;
        return new LatLng(Gx, Gy);
    }

    public static Point transform(int x, int y,
                           int tx, int ty,
                           double deg,
                           double sx, double sy) {

        double local_deg = deg * Math.PI / 180;

        int local_x = (int)(sx * ((x - tx) * Math.cos(local_deg) - (y - ty) * Math.sin(local_deg)) + tx);
        int local_y = (int)(sy * ((x - tx) * Math.sin(local_deg) + (y - ty) * Math.cos(local_deg)) + ty);

        return new Point(local_x, local_y);
    }

    public static double distance(LatLng p1LL, LatLng p2LL) {
        return SphericalUtil.computeDistanceBetween(p1LL, p2LL);
    }

    public static double area(List<LatLng> l) {
        return SphericalUtil.computeArea(l);
    }

    public static LatLngBounds getPolygonLatLngBounds(final List<LatLng> polygon) {
        final LatLngBounds.Builder centerBuilder = LatLngBounds.builder();
        for (LatLng point : polygon) {
            centerBuilder.include(point);
        }
        return centerBuilder.build();
    }
}
