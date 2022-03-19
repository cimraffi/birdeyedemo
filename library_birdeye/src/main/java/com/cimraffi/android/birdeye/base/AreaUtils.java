package com.cimraffi.android.birdeye.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.airbnb.android.airmapview.AirMapMarker;
import com.airbnb.android.airmapview.AirMapPolyline;
import com.airbnb.android.airmapview.AirMapView;
import com.airbnb.android.airmapview.MapUtils.LatLng;
import com.airbnb.android.airmapview.MapUtils.SphericalUtil;
import com.airbnb.android.airmapview.listeners.OnGenAirwayCallback;
import com.airbnb.android.airmapview.listeners.OnSnapshotReadyListener;
import com.cimraffi.android.birdeye.ApplicationFactory;
import com.cimraffi.android.birdeye.bean.ContinuePoint;
import com.cimraffi.android.birdeye.bean.FlyLine;
import com.cimraffi.android.birdeye.bean.MissionConfig;
import com.cimraffi.android.birdeye.bean.PointBean;
import com.cimraffi.android.birdeye.dbex.SomeLab;
import com.cimraffi.android.birdeye.utils.polygon.PolygonUtil;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.cimraffi.android.birdeye.R;
import static android.graphics.Bitmap.Config.ARGB_8888;


/*
 * 地块工具类
 * */
public class AreaUtils {
    private AirMapView mMap;

    // 地块ID
    private String areaID = "";

    //存储多边形每个顶点marker的id，并保持输入顺序
    private List<String> markersID = new ArrayList<String>();
    //以多边形顶点marker的id为健存储
    private Map<String, LatLng> markersLocation = new ConcurrentHashMap<>();

    //每个顶点
    private Map<String, AirMapMarker> markersMarker = new ConcurrentHashMap<>();
    // 每一条边
    private List<AirMapPolyline> mPolyline = new ArrayList<>();
    // 用来记录地块每条边长度的Marker
    private List<AirMapMarker> mPolylineMarker = new ArrayList<>();
    // 面积
    private AirMapMarker mAcreageMarker;

    // 航线规划
    private AirwayMissionUtils missionUtils;

    //集合旋转度数
    private LatLng startPoint;
    //使用凸包矩形规划航线
    private boolean useCovexHull;
    //最后一个标记的点
    private LatLng lastPoint;
    //是否翻转过
    private boolean isReverse = false;

    ///////////////续航点//////////////////////////
    private AirMapPolyline flyedAirway;
    //航线任务结束
    private boolean finishAirway = false;
    //航线结束时无人机所在得位置
    private LatLng droneFinishFlyPosition;
    //使用续航点飞行之前的续航点位置
    public int lastTargetWaypointIndex = 0;
    //目标航点
    private int targetWaypointIndex;
    //当前任务的航点总数
    private int totalWaypointCount;
    //结束的航点索引
    private int finishTargetIndex;
    //续航点
    private AirMapMarker continueMarker;
    //续航点实例
    private ContinuePoint continuePoint;
    //任务已经起飞
    private boolean flyingAirway = false;

    //记录航线的最后一个航点，用于判断安全性
    public LatLng last_point;
    private String acreage;
    private String collectDuration;
    private String collectPhoto;
    private long missionStartTime;
    private String monitorTime;
    private String userId;

    public void setAcreage(String acreage) {
        this.acreage = acreage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMonitorTime() {
        return monitorTime;
    }

    public void setMonitorTime(String monitorTime) {
        this.monitorTime = monitorTime;
    }

    public String getCollectPhoto() {
        return collectPhoto;
    }

    public long getMissionStartTime() {
        return missionStartTime;
    }
    public void setMissionStartTime(long time) {
        missionStartTime = time;
    }
    private long missionEndTime;
    public long getMissionEndTime() {
        return missionEndTime;
    }
    public void setMissionEndTime(long time) {
        missionEndTime = time;
    }

    public String getAcreage() {
        return acreage;
    }
    public String getCollectDuration() {
        return collectDuration;
    }

    public AreaUtils(AirMapView map) {
        this.mMap = map;
        missionUtils = new AirwayMissionUtils(this.mMap);
    }

    public void setMap(AirMapView map) {
        this.mMap = map;
        if (missionUtils != null) {
            missionUtils.setMap(map);
        } else {
            missionUtils = new AirwayMissionUtils(map);
        }
    }

    //当前矩形为凸包矩形
    public boolean isCovexHull() {
        ArrayList<LatLng> polygon = getAllPosition();
        if (polygon == null || polygon.size() < 3) {
            return true;
        }
        boolean isConvexHull = missionUtils.isConvexHull(polygon);
        return isConvexHull;
    }

    /**
     * 设置使用凸包矩形规划航线
     *
     * @param useCovexHull
     */
    public void setUseCovexHull(boolean useCovexHull) {
        this.useCovexHull = useCovexHull;
    }

    public void setSpace(int space) {
        missionUtils.setSpace(space);
    }

    public int getSpace() {
        return missionUtils.getSpace();
    }

    public int getStep() {
        return missionUtils.getStep();
    }

    public void setStep(int step) { missionUtils.setStep(step); }

    public int getSpeed() {
        return missionUtils.getSpeed();
    }

    public void setSpeed(int speed) { missionUtils.setSpeed(speed); }

    public String getAreaID() {
        return areaID;
    }

    public LatLng getLastPoint() {
        System.out.println("markersID.size():"+markersID.size());
        //if (lastPoint == null && markersID.size() > 0) {
            lastPoint = markersLocation.get(markersID.get(markersID.size() - 1));
        //}else{
        //    System.out.println("lastPoint is not null.");
        //}
        return lastPoint;
    }

    // 清空所有内容
    public void clear() {
        isReverse = false;
        useCovexHull = false;
        // 清除航线规划
        missionUtils.clear();

        // 清除所有边长
        for (AirMapMarker m : mPolylineMarker) {
            mMap.removeMarker(m);
        }
        mPolylineMarker.clear();

        // 清除所有边
        for (AirMapPolyline m : mPolyline) {
            mMap.removePolyline(m);
        }
        mPolyline.clear();

        // 清除所有顶点
        for (String id : markersID) {
            if (markersMarker.containsKey(id)) {
                mMap.removeMarker(markersMarker.get(id));
            }
        }
        markersID.clear();

        // 清除面积
        if (mAcreageMarker != null) {
            mMap.removeMarker(mAcreageMarker);
        }

        areaID = "";
    }

    // 清空地图显示
    public void clear_display() {
        // 清除航线规划
        missionUtils.clear_display();

        // 清除所有边长
        for (AirMapMarker m : mPolylineMarker) {
            mMap.removeMarker(m);
        }
        mPolylineMarker.clear();

        // 清除所有边
        for (AirMapPolyline m : mPolyline) {
            mMap.removePolyline(m);
        }
        mPolyline.clear();

        // 清除所有顶点
        for (String id : markersMarker.keySet()) {
            mMap.removeMarker(markersMarker.get(id));
        }
        markersMarker.clear();

        // 清除面积
        if (mAcreageMarker != null) {
            mMap.removeMarker(mAcreageMarker);
        }
    }

    public int size() {
        return markersID.size();
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

    public AirMapMarker drawPoint(LatLng point, int id) {
        int[] mm = {R.mipmap.p1, R.mipmap.p2,
                R.mipmap.p3, R.mipmap.p4,
                R.mipmap.p5, R.mipmap.p6,
                R.mipmap.p7, R.mipmap.p8,
                R.mipmap.p9, R.mipmap.p10,
                R.mipmap.p11, R.mipmap.p12,
                R.mipmap.p13, R.mipmap.p14,
                R.mipmap.p15, R.mipmap.p16,
                R.mipmap.p17, R.mipmap.p18,
                R.mipmap.p19, R.mipmap.p20,
                R.mipmap.p21, R.mipmap.p22,
                R.mipmap.p23, R.mipmap.p24,
                R.mipmap.p25, R.mipmap.p26,
                R.mipmap.p27, R.mipmap.p28,
                R.mipmap.p29, R.mipmap.p30,
                R.mipmap.p31, R.mipmap.p32,
                R.mipmap.p33, R.mipmap.p34,
                R.mipmap.p35, R.mipmap.p36,
                R.mipmap.p37, R.mipmap.p38,
                R.mipmap.p39, R.mipmap.p40,
                R.mipmap.p41, R.mipmap.p42,
                R.mipmap.p43, R.mipmap.p44,
                R.mipmap.p45, R.mipmap.p46,
                R.mipmap.p47, R.mipmap.p48,
                R.mipmap.p49, R.mipmap.p50,
                R.mipmap.p51, R.mipmap.p52,
                R.mipmap.p53, R.mipmap.p54,
                R.mipmap.p55, R.mipmap.p56,
                R.mipmap.p57, R.mipmap.p58,
                R.mipmap.p59, R.mipmap.p60,
                R.mipmap.p61, R.mipmap.p62,
                R.mipmap.p63, R.mipmap.p64,
                R.mipmap.p65, R.mipmap.p66,
                R.mipmap.p67, R.mipmap.p68,
                R.mipmap.p69, R.mipmap.p70,
                R.mipmap.p71, R.mipmap.p72,
                R.mipmap.p73, R.mipmap.p74,
                R.mipmap.p75, R.mipmap.p76,
                R.mipmap.p77, R.mipmap.p78,
                R.mipmap.p79, R.mipmap.p80,
                R.mipmap.p81, R.mipmap.p82,
                R.mipmap.p83, R.mipmap.p84,
                R.mipmap.p85, R.mipmap.p86,
                R.mipmap.p87, R.mipmap.p88,
                R.mipmap.p89, R.mipmap.p90,
                R.mipmap.p91, R.mipmap.p92,
                R.mipmap.p93, R.mipmap.p94,
                R.mipmap.p95, R.mipmap.p96,
                R.mipmap.p97, R.mipmap.p98,
                R.mipmap.p99};


        AirMapMarker.Builder markerOptions = new AirMapMarker.Builder();
        markerOptions.id(id);
        markerOptions.position(point);
        if (markersID.size() < 100) { //如果小于10，我们可以显示带数字的图标，如果大于10，则没有这样的图标了
            markerOptions.divIconHtml(id + ".png");
        } else {
            markerOptions.divIconHtml("");
        }
        markerOptions.iconId(mm[id - 1]);

        AirMapMarker marker = markerOptions.build();
        mMap.addMarker(marker);
        return marker;
    }

    public void genAreaID() {
        Date d = new Date();
        long curTimestamp = d.getTime();

        ArrayList<LatLng> polygon = getAllPosition();

        LatLng ll = polygon.get(0);//PolygonUtil.getCenterOfGravity(polygon);

        StringBuilder id = new StringBuilder();
        //DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
        id.append(curTimestamp)
                .append(String.format("%.7f", ll.latitude))
                .append(String.format("%.7f", ll.longitude));
        areaID = id.toString().replace(".", "");
    }

    public void add(LatLng point) {
        ArrayList<LatLng> polygon = getAllPosition1();
        for (LatLng l : polygon) {
            if (PolygonUtil.distance(l, point) < 1) {// 如果新增加的点距离任何一个已有点少于1m，则不增加。
                return;
            }
        }

        String id = Long.toString(markersID.size() + 1);
        //如果翻转过航线，先翻转回来添加航点，添加完航点再翻转航线
        if (isReverse && markersID != null && markersID.size() > 2) {
            Collections.reverse(markersID.subList(1, markersID.size()));
        }
        markersID.add(id);
        if (isReverse && markersID != null && markersID.size() > 2) {
            Collections.reverse(markersID.subList(1, markersID.size()));
        }
        markersLocation.put(id, point);
        lastPoint = point;
        if (areaID.equals("")) {
            genAreaID();
        }
    }

    public boolean contains(String id) {
        return markersID.contains(id);
    }

    public int indexOf(String id) {
        return markersID.indexOf(id);
    }

    public LatLng get(int i) {
        return markersLocation.get(markersID.get(i));
    }

    public void remove(String id) {
        markersID.remove(id);
        markersLocation.remove(id);
    }

    public void removeLast() {
        if (size() > 0) {
            if (size() > 2 && isReverse) {
                Collections.reverse(markersID.subList(1, markersID.size()));
            }
            String id = markersID.remove(size() - 1);
            if (size() > 2) {
                if (isReverse) {
                    Collections.reverse(markersID.subList(1, markersID.size()));
                }
            } else {
                isReverse = false;
            }
            markersLocation.remove(id);
        } else {
            // todo clear
            isReverse = false;
        }
    }

    public void setStartPoint(LatLng startPoint) {
        this.startPoint = startPoint;
    }

    public LatLng getStartPoint() {
        if (startPoint != null) {
            return startPoint;
        }
        return missionUtils.getStartPoint();
    }

    public void reverse(int start) {
        if (markersID == null || markersID.size() < 3) {
            return;
        }
        isReverse = !isReverse;
        Collections.reverse(markersID.subList(start, markersID.size()));
    }

    /**
     * 按顺序打点标记的所有的航点集合
     *
     * @return
     */
    public ArrayList<LatLng> getAllPosition1() {
        ArrayList<LatLng> polygon = new ArrayList<LatLng>();
        if (markersID == null || markersID.size() == 0) {
            return polygon;
        }
        for (String id : markersID) {
            polygon.add(markersLocation.get(id));
        }
        return polygon;
    }

    /**
     * 使用凸包矩形和设置起点规划后的所有的航点集合
     *
     * @return
     */
    public ArrayList<LatLng> getAllPosition() {
        ArrayList<LatLng> polygon = new ArrayList<LatLng>();
        if (markersID == null || markersID.size() == 0) {
            return polygon;
        }
        for (String id : markersID) {
            polygon.add(markersLocation.get(id));
        }
        if (markersID.size() < 3) {
            return polygon;
        }

        if (!useCovexHull) {
            //不使用凸包矩形规划航线
            int distance = 0;
            if (startPoint != null) {
                for (int i = 0; i < polygon.size(); i++) {
                    LatLng l = polygon.get(i);
                    if (PolygonUtil.distance(l, startPoint) < 1) {
                        distance = i;
                    }
                }
            }
            //设置起点后规划的航线
            if (distance != 0) {
                Collections.rotate(polygon, (-distance));
            }
            return polygon;
        }
        //使用凸包矩形规划
        //获取新矩形的所有坐标点（最小外包矩形/凸包矩形）
        ArrayList<LatLng> newPolygon = missionUtils.getNewPolygon(polygon);
        int distance = 0;
        if (startPoint != null) {
            for (int i = 0; i < newPolygon.size(); i++) {
                LatLng l = newPolygon.get(i);
                if (PolygonUtil.distance(l, startPoint) < 1) {
                    distance = i;
                }
            }
        }
        //设置起点后规划的航线
        if (distance != 0) {
            Collections.rotate(newPolygon, (-distance));
        }
        return newPolygon;
    }

    public List<LatLng> getAirwayPoints() {
        List<LatLng> result = missionUtils.getAirwayPoints();
        return result;
    }

    public List<LatLng> getMissionPoints() {
        List<LatLng> result = missionUtils.getMissionPoints();
        return result;
    }

    /**
     * 获取续航航线任务集合
     *
     * @return
     */
    public List<LatLng> getContinueAirwayPoints() {
        List<LatLng> airwayPoints = missionUtils.getAirwayPoints();
        if (hasContinuePoint()) {

            List<LatLng> result = new ArrayList<>();
            // 从上一次飞完之后的目标点开始取点
            for (int i = lastTargetWaypointIndex; i < airwayPoints.size(); i++) {
                result.add(airwayPoints.get(i));
            }
            if (result.size() > 0) {
                double distance = PolygonUtil.distance(continuePoint.continuePosition, result.get(0));
                if (distance >= 1) {
                    result.add(0, continuePoint.continuePosition);
                    lastTargetWaypointIndex -=1;
                }
            }
//            result.add(0, continuePoint.continuePosition);
            /*
            if (result.size() > 0) {
                double distance = PolygonUtil.distance(droneFinishFlyPosition, result.get(0));
                if (distance >= 1) {
                    result.add(0, droneFinishFlyPosition);
                }
            }*/

            return result;
        }
        return airwayPoints;
    }

    public void draw(Context c, Callback_AreaUtil cb) {
        int size = markersID.size();
        clear_display(); // 绘制之前先清除
        cb.onClear();

        List<LatLng> pts = new ArrayList<LatLng>();
        for (int i = 0; i < size; i++) {
            int next_i = (i + 1) % size;
            List<LatLng> latLngs = new ArrayList<LatLng>();

            // 绘制当前顶点
            LatLng ll = markersLocation.get(markersID.get(i));
            AirMapMarker m = drawPoint(markersLocation.get(markersID.get(i)),
                    Integer.parseInt(markersID.get(i)));
            markersMarker.put(markersID.get(i), m);

            // 在一个顶点的情况下，可以结束了
            if (next_i == i) {
                break;
            }

            pts.add(ll);
            latLngs.add(ll);

            // 绘制当前顶点到下一个顶点的边
            LatLng next_ll = markersLocation.get(markersID.get(next_i));
            latLngs.add(next_ll);
            AirMapPolyline polyline = new AirMapPolyline(null, latLngs,
                    i + 1, 3, 0xFFC65F);
            mMap.addPolyline(polyline);
            mPolyline.add(polyline);

            // 边长
            double distance = PolygonUtil.distance(ll, next_ll);
            DecimalFormat fnum = new DecimalFormat("###.##");
            String dd = fnum.format(distance);
            LatLng pos = new LatLng((ll.latitude + next_ll.latitude) / 2,
                    (ll.longitude + next_ll.longitude) / 2);
            String title = dd + "M";
            Bitmap bmp = genBitmap(c, title, 0, 14);
            AirMapMarker marker = new AirMapMarker.Builder()
                    .position(pos)
                    .id(100000 + i + 1)
                    .title(title)
                    .bitmap(bmp)
                    .anchor(0.5f, 1).build();
            mMap.addText(marker);
            mPolylineMarker.add(marker);
        }

        if (size >= 3 && MissionConfig.getInstance().getMissionType() != 3) { // 如果达到多边形的条件，则绘制面积

            // 绘制航线
            missionUtils.genAirway(c, getAllPosition(), false, new OnGenAirwayCallback() {
                @Override
                public void onAirwayReady(List<LatLng> points) {
                    double area = PolygonUtil.area(pts);
                    DecimalFormat fnum = new DecimalFormat("###.##");
                    String dd = fnum.format(area / 666.7);
                    //航线飞行时间+每个航点3秒的转向
                    String min = fnum.format(SphericalUtil.computeLength(points) / (getSpeed() * 60)
                            + (points.size()*2)/60) ;
                    Double second = Double.valueOf(min) * 60;
                    acreage = dd;
                    collectDuration = (min+3) + "分钟"; //加3分钟的起飞与返航时间
                    collectPhoto = (second.intValue())/2 + "张";
                    String taskInfo = "预计飞行面积:"+dd+"亩\n" +
                            "预计飞行时间:"+ min +"分钟\n" +
                            "预计采集照片:"+(second.intValue())/2+"张";
                    cb.onRefresh(taskInfo);
                    last_point = points.get(points.size()-1);
                }

            });
        }
    }

    /**
     * 航线任务字符串
     *
     * @return
     */
    public String airwayToJson() {
        try {
            JSONArray latlngs = new JSONArray();
            List<LatLng> missions = getAirwayPoints();
            for (int i = 0; i < missions.size(); i++) {
                LatLng ll = missions.get(i);
                JSONObject jll = new JSONObject();
                jll.put("lat", ll.latitude);
                jll.put("lng", ll.longitude);
                latlngs.put(jll);
            }
            return latlngs.toString(1);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 定距任务字符串
     *
     * @return
     */
    public String missionToJson() {
        try {
            JSONArray latlngs = new JSONArray();
            List<LatLng> missions = getMissionPoints();
            for (int i = 0; i < missions.size(); i++) {
                LatLng ll = missions.get(i);
                JSONObject jll = new JSONObject();
                jll.put("lat", ll.latitude);
                jll.put("lng", ll.longitude);
                latlngs.put(jll);
            }
            return latlngs.toString(1);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 航点任务字符串
     *
     * @return
     */
    public String pointToJson() {
        try {
            JSONArray latlngs = new JSONArray();
            for (String s : markersID) {
                LatLng ll = markersLocation.get(s);
                JSONObject jll = new JSONObject();
                jll.put("lat", ll.latitude);
                jll.put("lng", ll.longitude);
                latlngs.put(jll);
            }
            return latlngs.toString(1);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String toJson() {
//        if (size() < 3)
//            return "";

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("areaid", areaID);
//            jsonObject.put("date", new Date());
            jsonObject.put("space", missionUtils.getSpace());
            jsonObject.put("step", missionUtils.getStep());

            JSONArray latlngs = new JSONArray();
            for (String s : markersID) {
                LatLng ll = markersLocation.get(s);
                JSONObject jll = new JSONObject();
                jll.put("lat", ll.latitude);
                jll.put("lng", ll.longitude);
                latlngs.put(jll);
            }

            jsonObject.put("latlngs", latlngs);
            return jsonObject.toString(1);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void airwayFromJson(String json) {
        List<LatLng> airway = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jll = jsonArray.getJSONObject(i);
                double lat = jll.getDouble("lat");
                double lng = jll.getDouble("lng");

                LatLng pos = new LatLng(lat, lng);
                airway.add(pos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        missionUtils.setAirwayPoints(airway);
    }

    public void missionFromJson(String json) {
        List<LatLng> mission = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jll = jsonArray.getJSONObject(i);
                double lat = jll.getDouble("lat");
                double lng = jll.getDouble("lng");

                LatLng pos = new LatLng(lat, lng);
                mission.add(pos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        missionUtils.setMissionPoints(mission);
    }

    public void pointsFromJson(String json) {

    }

    public void fromJson(String json) {
        clear();
        try {
            JSONObject jsonObject = new JSONObject(json);
            areaID = jsonObject.getString("areaid");
            missionUtils.setSpace(jsonObject.has("space") ? jsonObject.getInt("space") : 5);
            missionUtils.setStep(jsonObject.has("step") ? jsonObject.getInt("step") : 4);

            JSONArray latlngs = jsonObject.getJSONArray("latlngs");
            for (int i = 0; i < latlngs.length(); i++) {
                JSONObject jll = latlngs.getJSONObject(i);
                double lat = jll.getDouble("lat");
                double lng = jll.getDouble("lng");

                LatLng pos = new LatLng(lat, lng);
                String id = Long.toString(markersID.size() + 1);
                markersID.add(id);
                markersLocation.put(id, pos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fromFlyLine(FlyLine flyLine) {
        clear();
        try {
            areaID = flyLine.boundaryId;
            userId = flyLine.userId;
            int space = flyLine.space;
//            double space = SettingUtil.longkuan(flyLine.height, flyLine.overlap/100.0);
            missionUtils.setSpace(space);
//            double speed = SettingUtil.sudu(flyLine.height, flyLine.overlap/100.0, 2);
            int speed = flyLine.speed;
            missionUtils.setSpeed(speed);
            MissionConfig.getInstance().setAltitude(flyLine.height);
            MissionConfig.getInstance().setOverlap(flyLine.overlap);
            MissionConfig.getInstance().setPhotoInterval(flyLine.photoInterval);

            for (int i = 0; i < flyLine.boundary.size(); i++) {
                PointBean jll = flyLine.boundary.get(i);
                double lat = jll.latitude;
                double lng = jll.longitude;

                LatLng pos = new LatLng(lat, lng);
                String id = Long.toString(markersID.size() + 1);
                markersID.add(id);
                markersLocation.put(id, pos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //设置飞机在航线中的目标点索引及航线中航点总数
    public void setTargetWaypointIndex(int index, int total) {
        //if (finishAirway) {
        //    return;
        //}

        int type = MissionConfig.getInstance().getMissionType(); // 任务类型

        // 总采集任务的总航点数
        int countOfTotalPoints = 0;
        if (type == 1) {
            countOfTotalPoints = getAirwayPoints().size();
        } else if (type == 2) {
            countOfTotalPoints = getMissionPoints().size();
        } else if (type == 3) {
            countOfTotalPoints = getAllPosition1().size();
        }

        // 计算当前子任务目标航点在总采集任务中的编号的起算值
        // 例1：总采集任务有80个航点，第一次子任务有80个航点，则起算值为0
        // 例2：第一次子任务有80个航点，但只飞了35个就断点了，则第二次子任务的航点数为55个，起算点为80-55=35个，也就是把第一次子任务的35个航点跳过去了。
        //int indexBaseOfCurTask = countOfTotalPoints - lastTargetWaypointIndex;

        // 在当前起算值基础上+已完成航点索引+1，即为当前子任务目标航点在总采集任务中的编号
        if(index<total-1){
            this.targetWaypointIndex = lastTargetWaypointIndex + index + 1;
        }else{
            this.targetWaypointIndex = lastTargetWaypointIndex + index ;
        }

        Log.d("cp_test", String.format("targetWaypointIndex:" + targetWaypointIndex + "lastTargetWaypointIndex:" + lastTargetWaypointIndex + "index:" + index + "total:" + total));
        //
        //if (index == total - 1){
        //    this.targetWaypointIndex = -1;
        //}
    }

    public void setFinishTargetWaypointIndex(int finishIndex, int total) {
        //if (finishAirway) {
        //    return;
        //}
        this.finishTargetIndex = finishIndex;
        this.totalWaypointCount = total;
    }

    /**
     * 设置飞机位置
     *
     * @param latLng
     */
    public void setDroneLocation(LatLng latLng) {
        if (finishAirway || latLng == null) {
            return;
        }
//        ToastUtils.setResultToToast(latLng.latitude+";"+latLng.longitude);
//        if(!this.hasContinuePoint()){
            droneFinishFlyPosition = latLng;
//        }

    }

    /**
     * 结束航线，判断是否没有飞完航线
     *
     * @param finishAirway
     */
    public void setFinishAirway(boolean finishAirway) {
        this.finishAirway = finishAirway;
    }
    public boolean getFinishAirway() {
        return this.finishAirway;
    }

    public boolean DoFinish(){
        if (finishAirway) {
            flyingAirway = false;
        } else {
            flyingAirway = true;
        }
        int type = MissionConfig.getInstance().getMissionType();
        Log.d("cp_test", String.format("DoFinish"));
        Log.d("cp_test", String.format("finishTargetIndex:" + finishTargetIndex));
        Log.d("cp_test", String.format("totalWaypointCount:" + totalWaypointCount));
        Log.d("cp_test", String.format("targetWaypointIndex:" + targetWaypointIndex));
        Log.d("cp_test", String.format("missionUtils.getAirwayPoints().size():" + missionUtils.getAirwayPoints().size()));
        if (targetWaypointIndex != 0
                && ((finishTargetIndex != totalWaypointCount - 1) //未到达最后一个任务点
                    ||(finishTargetIndex == totalWaypointCount - 1 && targetWaypointIndex<missionUtils.getAirwayPoints().size()-1))) { //到达最后一个任务点，但任务点不是最终航点
            Log.d("cp_test", String.format("DoFinish: into if"));

            // 总任务尚未结束
            setFinishAirway(false);

            LatLng continuePosition = droneFinishFlyPosition;
            if (type == 1) {
                continuePosition = droneFinishFlyPosition;
            } else if (type == 2) {
                continuePosition = getMissionPoints().get(targetWaypointIndex);
            } else if (type == 3) {
                continuePosition = getAllPosition1().get(targetWaypointIndex);
            }

            if (continueMarker != null) {
                mMap.removeMarker(continueMarker);
                continueMarker = null;
            }

            if (continuePosition == null) {
                Log.d("cp_test", String.format("DoFinish: continuePosition == null"));
                return false;
            }

            Log.d("cp_test", String.format("DoFinish: add continueMarker"));
            continueMarker = new AirMapMarker.Builder()
                    .position(continuePosition)
                    .divIconHtml("continue_point.png")
                    .iconId(R.mipmap.continue_point)
                    .id(1000)
                    .build();
            mMap.addMarker(continueMarker);

            lastTargetWaypointIndex = this.targetWaypointIndex;
            continuePoint = new ContinuePoint();
            continuePoint.boundaryId = areaID;
            continuePoint.monitorTime = monitorTime;
            continuePoint.continuePosition = continuePosition;
            continuePoint.missionType = type;
            continuePoint.targetWaypointIndex = lastTargetWaypointIndex;
            continuePoint.missionStartTime = missionStartTime;
            SomeLab areaLab = SomeLab.get(ApplicationFactory.getInstance().getBaseContext());
            areaLab.addContinuePoint(continuePoint);
            //TODO 发送暂停任务指令
            return true;
        }
        setFinishAirway(true);
        return false;
    }

    public void deleteContinuePoint() {
        Log.d("cp_test", String.format("deleteContinuePoint: continueMarker"));
        if (continueMarker != null) {
            Log.d("cp_test", String.format("deleteContinuePoint: continueMarker != null"));
            mMap.removeMarker(continueMarker);
            continueMarker = null;
        }
        Log.d("cp_test", String.format("deleteContinuePoint: continuePoint"));
        if (continuePoint != null) {
            Log.d("cp_test", String.format("deleteContinuePoint: continuePoint != null"));
            SomeLab areaLab = SomeLab.get(ApplicationFactory.getInstance().getBaseContext());
            areaLab.deleteContinuePoint(areaID);
            continuePoint = null;
            //TODO 发送继续上传任务指令
        }
    }

   /* public int getContinuePointType() {
        if (continuePoint != null) {
            return continuePoint.missionType;
        } else {
            return 1;
        }
    }*/

    /**
     * 该任务中是否有续航点
     *
     * @return
     */
    public boolean hasContinuePoint() {
        if (continuePoint != null) {
            return true;
        } else {
            return false;
        }
    }

    public void drawContinuePoint() {
        if (continuePoint != null) {
            setContinuePoint(continuePoint);
        }
    }

    public void setContinuePoint(ContinuePoint continuePoint) {
        deleteContinuePoint();

        LatLng continuePosition = continuePoint.continuePosition;

        if (continuePosition == null) {
            return;
        }
        continueMarker = new AirMapMarker.Builder()
                .position(continuePosition)
                .divIconHtml("continue_point.png")
                .iconId(R.mipmap.continue_point)
                .id(1000)
                .build();
        mMap.addMarker(continueMarker);

        this.continuePoint = continuePoint;
        this.missionStartTime = continuePoint.missionStartTime;
        this.lastTargetWaypointIndex = continuePoint.targetWaypointIndex;
        this.droneFinishFlyPosition = continuePosition;
        Log.d("cp_test", "setContinuePoint: lastTargetWaypointIndex("+lastTargetWaypointIndex+")");
        finishAirway = true;
    }

    public void productDisconnected() {
        if (!flyingAirway) {
            return;
        }
        setFinishAirway(true);
    }

    /**
     * 平移1米
     * 经度（东西方向）1M实际度：360°/31544206M=1.141255544679108e-5=0.00001141
     * 纬度（南北方向）1M实际度：360°/40030173M=8.993216192195822e-6=0.00000899
     *
     * @param id
     */
    public void translate(Context context, int id) {
        /*
        double transLat = 0;
        double transLng = 0;
        if (id == R.id.transleft) {
            transLat = 0;
            transLng = -0.00001141;
        } else if (id == R.id.transright) {
            transLat = 0;
            transLng = 0.00001141;
        } else if (id == R.id.transbottom) {
            transLat = -0.00000899;
            transLng = 0;
        } else if (id == R.id.transtop) {
            transLat = 0.00000899;
            transLng = 0;
        }
//        int missionType = MissionConfig.getInstance().getMissionType();
//        switch (missionType){
//            case 1:
//                break;
//            case 2:
//                break;
//            case 3:
//                break;
//        }
        if (hasContinuePoint()) {
            transContinuePoint(transLat, transLng);
        }
        for (String markerId : markersID) {
            LatLng ll = markersLocation.get(markerId);
            markersLocation.put(markerId, new LatLng(ll.latitude + transLat, ll.longitude + transLng));
        }

        List<LatLng> airwayPoints = missionUtils.getAirwayPoints();
        if (airwayPoints != null && airwayPoints.size() > 0) {
            for (int i = 0; i < airwayPoints.size(); i++) {
                LatLng ll = airwayPoints.remove(i);
                LatLng ll2 = new LatLng(ll.latitude + transLat, ll.longitude + transLng);
                airwayPoints.add(i, ll2);
            }
            missionUtils.setAirwayPoints(airwayPoints);
        }
        List<LatLng> missionPoints = missionUtils.getMissionPoints();
        if (missionPoints != null && missionPoints.size() > 0) {
            for (int i = 0; i < missionPoints.size(); i++) {
                LatLng ll = missionPoints.remove(i);
                LatLng ll2 = new LatLng(ll.latitude + transLat, ll.longitude + transLng);
                missionPoints.add(i, ll2);
            }
            missionUtils.setMissionPoints(missionPoints);
        }

        drawPoints(context);
        missionUtils.draw(context);*/
    }

    private void drawPoints(Context c) {
        int size = markersID.size();
        clear_display(); // 绘制之前先清除

        List<LatLng> pts = new ArrayList<LatLng>();
        for (int i = 0; i < size; i++) {
            int next_i = (i + 1) % size;
            List<LatLng> latLngs = new ArrayList<LatLng>();

            // 绘制当前顶点
            LatLng ll = markersLocation.get(markersID.get(i));
            AirMapMarker m = drawPoint(markersLocation.get(markersID.get(i)),
                    Integer.parseInt(markersID.get(i)));
            markersMarker.put(markersID.get(i), m);

            // 在一个顶点的情况下，可以结束了
            if (next_i == i) {
                break;
            }

            pts.add(ll);
            latLngs.add(ll);

            // 绘制当前顶点到下一个顶点的边
            LatLng next_ll = markersLocation.get(markersID.get(next_i));
            latLngs.add(next_ll);
            AirMapPolyline polyline = new AirMapPolyline(null, latLngs,
                    i + 1, 3, 0xFFC65F);
            mMap.addPolyline(polyline);
            mPolyline.add(polyline);

            // 边长
            double distance = PolygonUtil.distance(ll, next_ll);
            DecimalFormat fnum = new DecimalFormat("###.##");
            String dd = fnum.format(distance);
            LatLng pos = new LatLng((ll.latitude + next_ll.latitude) / 2,
                    (ll.longitude + next_ll.longitude) / 2);
            String title = dd + "M";
            Bitmap bmp = genBitmap(c, title, 0, 14);
            AirMapMarker marker = new AirMapMarker.Builder()
                    .position(pos)
                    .id(100000 + i + 1)
                    .title(title)
                    .bitmap(bmp)
                    .anchor(0.5f, 1).build();
            mMap.addText(marker);
            mPolylineMarker.add(marker);
        }
    }

}
