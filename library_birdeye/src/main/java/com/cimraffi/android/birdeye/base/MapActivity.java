package com.cimraffi.android.birdeye.base;

import static com.cimraffi.android.birdeye.utils.ToastUtils.setResultToToast;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.android.airmapview.AirMapInterface;
import com.airbnb.android.airmapview.AirMapMarker;
import com.airbnb.android.airmapview.AirMapView;
import com.airbnb.android.airmapview.LeafletGaodeMapType;
import com.airbnb.android.airmapview.MapType;
import com.airbnb.android.airmapview.MapUtils.LatLng;
import com.airbnb.android.airmapview.MapUtils.LatLngBounds;
import com.airbnb.android.airmapview.WebAirMapViewBuilder;
import com.airbnb.android.airmapview.listeners.OnMapInitializedListener;
import com.cimraffi.android.birdeye.ApplicationFactory;
import com.cimraffi.android.birdeye.bean.ContinuePoint;
import com.cimraffi.android.birdeye.bean.FlyLine;
import com.cimraffi.android.birdeye.bean.MissionConfig;
import com.cimraffi.android.birdeye.common.Constants;
import com.cimraffi.android.birdeye.common.Globle;
import com.cimraffi.android.birdeye.dbex.SomeLab;
import com.cimraffi.android.birdeye.utils.CoordinateTransformUtil;
import com.cimraffi.android.birdeye.utils.DensityUtils;
import com.cimraffi.android.birdeye.utils.DeviceUtil;
import com.cimraffi.android.birdeye.utils.SharedPreferenceUtils;
import com.cimraffi.android.birdeye.utils.ToastUtils;
import com.cimraffi.android.birdeye.utils.polygon.PolygonUtil;
import com.cimraffi.android.birdeye.view.VideoFeedView;
import java.util.ArrayList;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.payload.Payload;
import dji.sdk.sdkmanager.DJISDKManager;
import com.cimraffi.android.birdeye.R;

public class MapActivity extends FragmentActivity
        implements View.OnClickListener,
        OnMapInitializedListener{
    protected static final String TAG = MapActivity.class.getSimpleName();

    //顶部控件，大疆信息
    private Button btnBack;
    private TextView tvTitle;
    public TextView flightModeTV;
    public TextView gpsNumTV;
    public TextView batteryTV;
    public TextView rtkTV;
    public TextView hsTV;
    public TextView altitudeTV;
    public String mMode;

    //居中或全屏控件
    public AirMapView mMap;
    public boolean isProductConnected = false;
    //实时图传
    private VideoFeedView primaryVideoFeedView;
    private RelativeLayout coverlayout;
    //指南针图片
    private ImageView compassView;
    /** 传感器管理器 */
    private SensorManager manager;

    //查看直播状态控件
    private TextView tvLiveStatus;

    //探针参数设置
    private TextView tvPinSetting;

    //RTK设置
    private TextView tvRTKSetting;

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        private float predegree = 0;

        @Override
        public void onSensorChanged(SensorEvent event) {
            /**
             * values[0]: x-axis 方向加速度
             　　 values[1]: y-axis 方向加速度
             　　 values[2]: z-axis 方向加速度
             */
            float degree = event.values[0];// 存放了方向值
            /**动画效果*/
            RotateAnimation animation = new RotateAnimation(predegree, degree,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setDuration(200);
            compassView.startAnimation(animation);
            predegree = -degree;

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    /////////////////底部控件////////////////////
    public TextView tvPause;//暂停，继续，编辑，设为航点（航线任务，定居拍照的航点）
    public TextView tvStart;//开始暂停保存
    protected double curLat = 0;
    protected double curLng = 0;
    protected double airLat = 0;
    protected double airLng = 0;
    //无人机Marker
    private AirMapMarker droneMarker = null;

    // 记录地块多边形的顶点
    protected AreaUtils areaUtils;

    private LatLng curPosition;
    private RelativeLayout container;
    private boolean isFull = false;

    //已经开始起飞
    public boolean isStart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_birdeye_map);

        compassView = (ImageView) this.findViewById(R.id.compass);
        compassView.setKeepScreenOn(true);//屏幕高亮
        //获取系统服务（SENSOR_SERVICE)返回一个SensorManager 对象
        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        initUI();

        initMapView();
    }

    @Override
    protected void onResume() {
        /**
         * 获取方向传感器
         * 通过SensorManager对象获取相应的Sensor类型的对象
         */
        Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        //应用在前台时候注册监听器
        manager.registerListener(sensorEventListener, sensor,
                SensorManager.SENSOR_DELAY_GAME);
        super.onResume();

        BaseProduct product = ApplicationFactory.getProductInstance();
        if (product != null && product.isConnected()) {
            Payload payload = product.getPayload();
            if (null != payload){
                payload.setCommandDataCallback((byte[] data) -> {
                    String statusStr = new String(data);
                    if (null != statusStr) {
                        String[] split = statusStr.split(",");
                        if ("1".equals(split[0])) {
                            if (!"0".equals(split[1])) {
                                setResultToToast("未检测到光感");
                            }else {
                                setResultToToast("光感模块正常");
                            }
                            if (!"0".equals(split[2])) {
                                setResultToToast("未检测到光谱仪");
                            }else {
                                setResultToToast("光谱仪正常");
                            }
                        } else if ("2".equals(split[0])) {
                            if (!"0".equals(split[1])) {
                                setResultToToast("未接收到开始指令");
                            }else {
                                setResultToToast("接收到开始指令");
                            }
                        }else if ("3".equals(split[0])) {
                            if (!"0".equals(split[2])) {
                                setResultToToast("未接收到断点指令");
                            }else {
                                setResultToToast("接收到断点指令,本次采集数量:" + split[1] + "条");
                            }
                        }else if ("4".equals(split[0])) {
                            if (split.length == 3) {
                                if (!"0".equals(split[2])) {
                                    setResultToToast("未接收到结束指令");
                                }else {
                                    setResultToToast("接收到结束指令,本次采集数量:" + split[1] + "条");
                                }
                            } else if (split.length == 2) {
                                if (!"0".equals(split[1])) {
                                    setResultToToast("未接收到结束指令");
                                }else {
                                    setResultToToast("接收到结束指令");
                                }
                            }
                        }else if ("5".equals(split[0])) {
                            if ("2".equals(split[2])) {
                                setResultToToast("文件上传失败");
                            }else if ("1".equals(split[2])) {
                                setResultToToast("探针无网络");
                            }else {
                                setResultToToast("文件上传成功,本次采集数量:" + split[1] + "条");
                            }
                        }

                    }
                });
            }
        }
    }
    @Override
    protected void onPause() {
        //应用不在前台时候销毁掉监听器
        manager.unregisterListener(sensorEventListener);
        super.onPause();
    }
    private void initMapView() {
        mMap = findViewById(R.id.map);
        mMap.setOnMapInitializedListener(this);
//        mMap.initialize(getSupportFragmentManager());
        SharedPreferenceUtils sp = new SharedPreferenceUtils(ApplicationFactory.getInstance().getBaseApplication());
        String mapUrl = sp.getSaveStringData(Constants.KEY_MAP_URL, "");
        String subdomains = sp.getSaveStringData(Constants.KEY_SUBDOMAINS, "");;
        String tms = sp.getSaveStringData(Constants.KEY_TMS, "");;
        String attribution = sp.getSaveStringData(Constants.KEY_ATTRIBUTION, "");;
        AirMapInterface airMapInterface = new WebAirMapViewBuilder().withOptions(new LeafletGaodeMapType(mapUrl,subdomains,tms,attribution)).build();

        if (airMapInterface != null) {
            mMap.initialize(getSupportFragmentManager(), airMapInterface);
        }

        if (!DeviceUtil.isNetworkConnected(this) && areaUtils == null) {
            areaUtils = new AreaUtils(mMap);
        }
    }

    @Override
    public void onMapInitialized() {
        mMap.setMapType(MapType.MAP_TYPE_SATELLITE);
        areaUtils = new AreaUtils(mMap);
        SharedPreferenceUtils sp = new SharedPreferenceUtils(this.getApplicationContext());
        areaUtils.setUserId(sp.getSaveStringData(Constants.KEY_USERID, ""));

        initData();
    }

    public String fromFlyline(int type, FlyLine fl) {
        try {
            areaUtils.fromFlyLine(fl);
            MissionConfig.getInstance().setMissionType(fl.type);

            tvTitle.setText(fl.name);

            areaUtils.draw(this.getApplicationContext(), new Callback_AreaUtil() {
                @Override
                public void onClear(){
                }
                @Override
                public void onRefresh(String info) {
                }
            });
            ArrayList<LatLng> polygon = areaUtils.getAllPosition();

            final LatLngBounds latLngBounds = PolygonUtil.getPolygonLatLngBounds(polygon);
            mMap.animateCenterZoom(latLngBounds.getCenter(), 18);
            return areaUtils.getAreaID();
        } catch (Exception e) {
            return "";
        }
    }

    public void initData(){

        Globle.isGCJ02 = false;

        String areaID = getIntent().getStringExtra(Constants.KEY_AREAID);
        FlyLine flyLine = (FlyLine)getIntent().getSerializableExtra(Constants.KEY_FLYLINE);
        int missionType = getIntent().getIntExtra(Constants.KEY_MISSIONTYPE,0);

        if (!TextUtils.isEmpty(areaID) && null!=flyLine){
            fromFlyline(flyLine.type, flyLine);

            int h = MissionConfig.getInstance().getAltitude();
            int overlap = MissionConfig.getInstance().getOverlap();
            int photoInterval = MissionConfig.getInstance().getPhotoInterval();
            MissionConfig.getInstance().setOverlap(overlap);
            MissionConfig.getInstance().setAltitude(h);
            int space = flyLine.space;
            int speed = flyLine.speed;
//            int space = (int)SettingUtil.longkuan(h, overlap*1.0/100.0);
//            int speed = (int)SettingUtil.sudu(h, overlap*1.0/100.0, photoInterval);
            MissionConfig.getInstance().setSpeed(speed);
            SomeLab al = SomeLab.get(getBaseContext());
            ContinuePoint continuePoint = al.getContinuePoint(areaID);
            if (areaUtils!=null&&continuePoint!=null){
                areaUtils.setContinuePoint(continuePoint);
            }
        }
    }

    private void initUI() {
        findViewById(R.id.pre_flight_check_list).setVisibility(View.GONE);
        findViewById(R.id.pre_flight_check_list).bringToFront();

        findViewById(R.id.rtk_panel).setVisibility(View.GONE);
        findViewById(R.id.rtk_panel).bringToFront();

        btnBack = (Button) findViewById(R.id.map_back);
        tvTitle = (TextView) findViewById(R.id.map_title);
        btnBack.setOnClickListener(this);

        flightModeTV = findViewById(R.id.flight_mode_show);
        gpsNumTV = findViewById(R.id.gps_num);
        hsTV = findViewById(R.id.speed_show);
        altitudeTV = findViewById(R.id.alti_show);
        batteryTV = findViewById(R.id.battery_show);
        rtkTV = (TextView) findViewById(R.id.rtk_show);
        //实时图传
        container = (RelativeLayout) findViewById(R.id.container);
        primaryVideoFeedView = (VideoFeedView) findViewById(R.id.video_view_primary_video_feed);
        coverlayout = (RelativeLayout) findViewById(R.id.coverlayout);
        tvLiveStatus = (TextView) findViewById(R.id.btn_live_show_status);
        tvPinSetting = (TextView) findViewById(R.id.pin_setting);
        tvRTKSetting = (TextView) findViewById(R.id.rtk_setting);
        if (ApplicationFactory.getCameraInstance() != null && VideoFeeder.getInstance() != null) {
            primaryVideoFeedView.registerLiveVideo(VideoFeeder.getInstance().getPrimaryVideoFeed(), true);
            primaryVideoFeedView.setCoverLayout(coverlayout);
        } else {
            primaryVideoFeedView.setVisibility(View.GONE);
            coverlayout.setVisibility(View.GONE);
        }

        tvPause = (TextView) findViewById(R.id.pause);
        tvStart = (TextView) findViewById(R.id.start);
        coverlayout.setOnClickListener(this);
        tvPause.setOnClickListener(this);
        tvStart.setOnClickListener(this);
        tvLiveStatus.setOnClickListener(this);
        tvPinSetting.setOnClickListener(this);
        tvRTKSetting.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.map_back) {
            finish();
        } else if (id == R.id.btn_live_show_status) {
            isLiveShowOn();
        } else if (id == R.id.coverlayout) {
            if (isFull) {
                isFull = false;
                changeViewWidth(mMap, primaryVideoFeedView);
            } else {
                isFull = true;
                changeViewWidth(primaryVideoFeedView, mMap);
            }
        }
    }
    //view1切换成全屏，view2切换为右下角预览view
    private void changeViewWidth(View view1, View view2) {
        //放大view1
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        container.removeView(view1);
        container.addView(view1, params1);

        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) view2.getLayoutParams();
        // 设置宽为130dp
        params2.width = DensityUtils.dip2px(MapActivity.this, 130);
        // 设置高为100dp
        params2.height = DensityUtils.dip2px(MapActivity.this, 80);
        //底部对齐
        params2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        //右部对齐
        params2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //设置距离底部为0
            params2.bottomMargin = DensityUtils.dip2px(this, 15);
            params2.rightMargin = DensityUtils.dip2px(this, 66);
        } else {
            //设置距离底部为0
            params2.bottomMargin = DensityUtils.dip2px(this, 82);
            params2.rightMargin = DensityUtils.dip2px(this, 14);
        }
        // 根据布局参数的设置，重新设置view的大小
        container.removeView(view2);
        container.addView(view2, params2);
    }

    private void isLiveShowOn() {
        if (!isLiveStreamManagerOn()) {
            return;
        }
        ToastUtils.setResultToToast(DJISDKManager.getInstance().getLiveStreamManager().isStreaming()?"视频流:开启":"视频流:关闭");
//        setResultToToast("Is Live Show On:" + DJISDKManager.getInstance().getLiveStreamManager().isStreaming());
    }

    private boolean isLiveStreamManagerOn() {
        if (DJISDKManager.getInstance().getLiveStreamManager() == null) {
            setResultToToast("无视频流");
            return false;
        }
        return true;
    }

    public static boolean checkGpsCoordination(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f
                && longitude != 0f);
    }

    public void updateDroneLocation() {
        if (mMap.isInitialized() && droneMarker != null) {
            mMap.removeMarker(droneMarker);
            droneMarker = null;
        }
    }

    // Update the drone location based on states from MCU.
    // 在地图上刷新无人机的定位
    public void updateDroneLocation(final double droneLocationLat, final double droneLocationLng,
                                    final float droneHeading) {
        //Log.i(TAG, "location: " + droneLocationLat + "   " + droneLocationLng);
        double lat = droneLocationLat;
        double lng = droneLocationLng;
        if (Globle.isGCJ02) {
            double[] ll2 = CoordinateTransformUtil.wgs84togcj02(droneLocationLng, droneLocationLat);
            lat = ll2[1];
            lng = ll2[0];
        }
        airLat = lat;
        airLng = lng;
        final LatLng pos = new LatLng(lat, lng);
        if (areaUtils != null) {
            areaUtils.setDroneLocation(pos);
        }
        runOnUiThread(() -> {
            if (checkGpsCoordination(pos.latitude, pos.longitude)) {

                if (mMap.isInitialized() && droneMarker != null) {
                    //Log.d("UAV", "moveMarker:(" + pos.latitude + ")(" + pos.longitude + ")");
                    mMap.moveOverlay(droneMarker, pos, droneHeading);
                    //mMap.rotateOverlay(droneMarker, droneHeading);
                    //mMap.removeMarker(droneMarker);
                } else if (mMap.isInitialized() && droneMarker == null) {
                    //Log.d("UAV", "addMarker:(" + pos.latitude + ")(" + pos.longitude + ")");
                    //Create MarkerOptions object
                    droneMarker = new AirMapMarker.Builder()
                            .position(pos)
                            //.rotation(droneHeading)
                            .id(8094)
                            //.title("UAV")
                            .iconId(R.drawable.aircraft)
                            .divIconHtml("aircraft.png")
                            .anchor(0.5f, 0.5f).build();
                    mMap.addOverlay(droneMarker);
                    //    //droneMarker.setRotation(droneHeading);
                }
            }
        });

    }
}
