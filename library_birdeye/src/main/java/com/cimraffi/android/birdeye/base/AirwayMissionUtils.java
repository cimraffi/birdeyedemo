package com.cimraffi.android.birdeye.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.TextView;
import com.cimraffi.android.birdeye.R;
import com.airbnb.android.airmapview.AirMapMarker;
import com.airbnb.android.airmapview.AirMapPolygon;
import com.airbnb.android.airmapview.AirMapPolyline;
import com.airbnb.android.airmapview.AirMapView;
import com.airbnb.android.airmapview.MapUtils.LatLng;
import com.airbnb.android.airmapview.MapUtils.SphericalUtil;
import com.airbnb.android.airmapview.listeners.OnGenAirwayCallback;
import com.cimraffi.android.birdeye.bean.MissionConfig;
import com.cimraffi.android.birdeye.utils.CoordinateTransformUtil;
import com.cimraffi.android.birdeye.utils.polygon.LinePoints;
import com.cimraffi.android.birdeye.utils.polygon.MinimumBoundingRectangle;
import com.cimraffi.android.birdeye.utils.polygon.PolygonUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.graphics.Bitmap.Config.ARGB_8888;

/*
 * 地块工具类
 * */
public class AirwayMissionUtils {
    private AirMapView mMap;

    //存储航线飞行的航点List
    private List<LatLng> airway_points = new ArrayList<>();
    //存储定距飞行的任务点List
    private List<LatLng> mission_points = new ArrayList<>();

    public List<LatLng> getAirwayPoints(){
        return airway_points;
    }

    public void setAirwayPoints(List<LatLng> airway){
        this.airway_points = airway;
    }

    public void setMissionPoints(List<LatLng> mission){
        this.mission_points = mission;
    }

    public LatLng getStartPoint(){
        if (airway_points!=null&&airway_points.size()>0){
            return airway_points.get(0);
        }else{
            return null;
        }
    }

    public List<LatLng> getMissionPoints(){
        return mission_points;
    }

    public int getSpace() {
        return space;
    }

    public void setSpace(int space) {
        this.space = space;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    //陇宽， 点距， 是否旋转， 速度
    private int space, step, speed;
    //起点
    private LatLng start_point;

    //航线起点
    private AirMapMarker map_start_point = null;
    //飞行航线
    private AirMapPolyline map_airway_points;
    //用来记录定距标点的Marker
    private List<AirMapMarker> map_mission_points = new ArrayList<>();

    private AirMapPolyline min_rect_line;

    AirwayMissionUtils(AirMapView map) {
        space = 31;
        step = 31;
        speed = 11;
        mMap = map;
    }

    public void setMap(AirMapView map){
        this.mMap = map;
    }

    public void clear(){
        airway_points.clear();
        mission_points.clear();
        start_point = null;

        if (map_start_point!=null){
            mMap.removeMarker(map_start_point);
        }

        if (map_airway_points!=null){
            mMap.removePolyline(map_airway_points);
        }

        for (AirMapMarker marker : map_mission_points) {
            mMap.removeMarker(marker);
        }


        if (min_rect_line!=null){
            mMap.removePolyline(min_rect_line);
        }
    }

    public LinePoints computeNode(LatLng s, LatLng e,
                                  double start, double step) {
        double distance = PolygonUtil.distance(s, e);
        int n = (int)((distance - start)/step);
        double heading = SphericalUtil.computeHeading(s, e);

        ArrayList<LatLng> result = new ArrayList<>();
        for (int i = 0; i < n+1; i++) {
            result.add(SphericalUtil.computeOffset(s, start + i*step, heading));
        }
        double remain = step - (distance - start - step * n);
        return new LinePoints(result, remain);
    }

    public String getWKT(ArrayList<LatLng> polygon){
        // POLYGON ((35 10, 45 45, 15 40, 10 20, 35 10))
        StringBuffer sb = new StringBuffer("POLYGON ((");
        for (int i = 0; i < polygon.size(); i++) {
            LatLng latLng = polygon.get(i);
            sb.append(latLng.longitude);
            sb.append(" ");
            sb.append(latLng.latitude);
            sb.append(",");
        }
        sb.append(polygon.get(0).longitude);
        sb.append(" ");
        sb.append(polygon.get(0).latitude);
        sb.append("))");
        return sb.toString();
    }
    //规划的矩形为凸包矩形
    public boolean isConvexHull(ArrayList<LatLng> polygon){
        String wkt = getWKT(polygon);
        try {
            Geometry read = new WKTReader().read(wkt);
            return MinimumBoundingRectangle.isConvexHull(read);
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 获取航线规划多边矩形的最小外包矩形或者凸包矩形
     * MinimumBoundingRectangle.getConvexHull(read, gf);凸包矩形
     * MinimumBoundingRectangle.get(read, gf);最小外接矩形
     * @param polygon
     * @return
     */
    public ArrayList<LatLng> getNewPolygon(ArrayList<LatLng> polygon){
        String wkt = getWKT(polygon);
        Polygon p = null;
        try {
            Geometry read = new WKTReader().read(wkt);
            GeometryFactory gf = new GeometryFactory();
            p = MinimumBoundingRectangle.getConvexHull(read, gf);
        }catch (Exception e){
            e.printStackTrace();
        }
        ArrayList<LatLng> result = new ArrayList<>();
        if (p!=null){
            Coordinate[] coordinates = p.getCoordinates();
            List<LatLng> latLngs = new ArrayList<LatLng>();
            for (int i = 0; i < coordinates.length; i++) {
                if (i == coordinates.length-1){
                    break;
                }
                int next_i = (i + 1) % coordinates.length;

                Coordinate coordinate = coordinates[i];
                Coordinate coordinateNext = coordinates[next_i];
                LatLng ll = new LatLng(coordinate.y,coordinate.x);
                LatLng next_ll = new LatLng(coordinateNext.y,coordinateNext.x);
                latLngs.add(ll);
                latLngs.add(next_ll);
            }
            if (min_rect_line!=null){
                mMap.removePolyline(min_rect_line);
            }
            min_rect_line = new AirMapPolyline(null, latLngs,
                    100, 5, 0xEC5B0D);
            mMap.addPolyline(min_rect_line);

            for (int i = 1; i < coordinates.length; i++) {
                Coordinate coordinate = coordinates[i];
                LatLng latLng = new LatLng(coordinate.y,coordinate.x);
                result.add(latLng);
            }
            return result;
        }else{
            return polygon;
        }
    }

    /**
     * 计算多边形内的航线规划
     */
public void genAirway(Context context, ArrayList<LatLng> polygon, boolean isGcj02, OnGenAirwayCallback c) {
        mMap.setOptions(new AirMapPolygon.Builder().addAll(polygon).build(),
                0, space, (String point) -> {
            try{
                JSONArray latlngs = new JSONArray(point);
                airway_points.clear();
                for (int jj = 0; jj < latlngs.length(); jj++) {
                    JSONObject jll = latlngs.getJSONObject(jj);
                    double lat = jll.getDouble("lat");
                    double lng = jll.getDouble("lng");

                    LatLng pos = new LatLng(lat, lng);
                    airway_points.add(pos);
                }

                if (MissionConfig.getInstance().getMissionType()==2){
                    //跳棋模式（定距）
                    boolean isToStop = false;
                    double remain = 0.0;

                    mission_points.clear();
                    for (int i = 0; i < airway_points.size() - 1; i++) {
                        if (isToStop) {
                            break;
                        }
                        LatLng s = airway_points.get(i);
                        LatLng e = airway_points.get(i+1);
                        LinePoints lps = computeNode(s, e, i==0?0.0:remain, step);
                        remain = lps.getRemain();

                        for (int j = 0; j< lps.getPoints().size(); j++) {
                            LatLng ll = lps.getPoints().get(j);
                            if (isGcj02) {
                                double[] ll2 = CoordinateTransformUtil.gcj02towgs84(ll.longitude, ll.latitude);
                                ll = new LatLng(ll2[1], ll2[0]);
                            }
                            mission_points.add(ll);
                            if(mission_points.size() >= 99 ) {
                                isToStop = true;
                                break;
                            }
                        }
                    }
                }

                draw(context);
                c.onAirwayReady(airway_points);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void clear_display(){
        if (map_start_point!=null){
            mMap.removeMarker(map_start_point);
        }

        if (map_airway_points!=null){
            mMap.removePolyline(map_airway_points);
        }

        for (AirMapMarker marker : map_mission_points) {
            mMap.removeMarker(marker);
        }

        if (min_rect_line!=null){
            mMap.removePolyline(min_rect_line);
        }
    }

    public static Bitmap genBitmap(Context c, final String text,
                                   final int padding, final int fontSize) {

        final TextView textView = new TextView(c);
        textView.setText(text);
        textView.setTextSize(fontSize);

        final Paint paintText = textView.getPaint();

        final android.graphics.Rect boundsText = new android.graphics.Rect();
        paintText.getTextBounds(text, 0, textView.length(), boundsText);
        paintText.setTextAlign(Paint.Align.CENTER);

        final Bitmap.Config conf = ARGB_8888;
        final Bitmap bmpText = Bitmap.createBitmap(boundsText.width() + 2
                * padding, boundsText.height() + 2 * padding, conf);

        final Canvas canvasText = new Canvas(bmpText);
        paintText.setColor(Color.GREEN);

        canvasText.drawText(text, canvasText.getWidth() / 2,
                canvasText.getHeight() - padding - boundsText.bottom, paintText);

        return bmpText;
    }

    public AirMapMarker drawRPoint(LatLng point, int id) {
        LatLng position = new LatLng(point.latitude-0.000003,point.longitude);
        AirMapMarker.Builder markerOptions = new AirMapMarker.Builder();
        markerOptions.id(id);
        markerOptions.position(position);
        markerOptions.divIconHtml("i_r.png");
        markerOptions.iconId(R.drawable.i_r);
        //markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        //markerOptions.draggable(true);
        AirMapMarker marker = markerOptions.build();
        mMap.addMarker(marker);
        return marker;
    }

    public void draw(Context context){
        if (airway_points.size()==0){
            return;
        }
        clear_display(); // 绘制之前先清除
        Bitmap bmp = genBitmap(context, context.getResources().getString(R.string.start_point), 0, 14);
        map_start_point = new AirMapMarker.Builder()
                .position(airway_points.get(0))
                .id(200000)
                .title(context.getResources().getString(R.string.start_point))
                .bitmap(bmp)
                .anchor(0.5f, 1).build();
        mMap.addText(map_start_point);

        map_airway_points = new AirMapPolyline(null, airway_points,
                0, 5, 0xF5497A);
        mMap.addPolyline(map_airway_points);

        int i = 0;
        for (LatLng ll: mission_points) {
            map_mission_points.add(drawRPoint(ll, (i++)+1+5000));
        }
    }
}
