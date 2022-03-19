package com.cimraffi.android.birdeye.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import com.airbnb.android.airmapview.AirMapInterface;
import com.airbnb.android.airmapview.AirMapMarker;
import com.airbnb.android.airmapview.AirMapPolyline;
import com.airbnb.android.airmapview.AirMapView;
import com.airbnb.android.airmapview.LeafletGaodeMapType;
import com.airbnb.android.airmapview.MapType;
import com.airbnb.android.airmapview.MapUtils.LatLng;
import com.airbnb.android.airmapview.MapUtils.LatLngBounds;
import com.airbnb.android.airmapview.WebAirMapViewBuilder;
import com.airbnb.android.airmapview.listeners.OnCameraChangeListener;
import com.airbnb.android.airmapview.listeners.OnMapInitializedListener;
import com.airbnb.android.airmapview.listeners.OnMapMarkerClickListener;
import com.cimraffi.android.birdeye.ApplicationFactory;
import com.cimraffi.android.birdeye.base.AreaUtils;
import com.cimraffi.android.birdeye.base.Callback_AreaUtil;
import com.cimraffi.android.birdeye.bean.DeviceList;
import com.cimraffi.android.birdeye.bean.MissionConfig;
import com.cimraffi.android.birdeye.common.Constants;
import com.cimraffi.android.birdeye.common.Globle;
import com.cimraffi.android.birdeye.dialog.CustomDialog;
import com.cimraffi.android.birdeye.utils.CoordinateTransformUtil;
import com.cimraffi.android.birdeye.utils.SettingUtil;
import com.cimraffi.android.birdeye.utils.SharedPreferenceUtils;
import com.cimraffi.android.birdeye.utils.ToastUtils;
import com.cimraffi.android.birdeye.utils.polygon.PolygonUtil;
import com.yayandroid.locationmanager.base.LocationBaseActivity;
import com.yayandroid.locationmanager.configuration.Configurations;
import com.yayandroid.locationmanager.configuration.LocationConfiguration;
import com.yayandroid.locationmanager.constants.FailType;
import com.yayandroid.locationmanager.constants.ProcessType;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import dji.common.flightcontroller.Attitude;
import dji.common.flightcontroller.FlightControllerState;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import com.cimraffi.android.birdeye.R;

public class FlyLineActivity extends LocationBaseActivity
        implements View.OnClickListener,
        OnMapInitializedListener,
        OnMapMarkerClickListener,
        OnCameraChangeListener,
        AdapterView.OnItemSelectedListener {

    private static final String TAG = FlyLineActivity.class.getSimpleName();

    private AirMapView mMap; // 地图控件

    // 右侧控件
    private DrawerLayout drawerLayout;
    private ImageView drawerHandleO;

    private ImageView compassView; // 指南针图片
    private SensorManager manager; // 传感器管理器

    private TextView taskInfoView; // 航线信息

    private LinearLayout drawerRightLayout;

    //高度
    private TextView tvAltitudeNum;
    //拍照间隔
    private TextView tvPhotoIntervalNum;
    //垄宽
    private TextView tvSpaceNum;
    //速度
    private TextView tvSpeedNum;
    //凹凸
    private TextView tvAoTo;

    private Spinner deviceSpinner; // 相机选择
    private TextView tvSideOverlapNum; // 旁向重叠度
    private TextView tvFrontOverlapNum; // 前向重叠度

    //地块多边形
    protected AreaUtils areaUtils;

    //当前地图中央的坐标
    private LatLng center;

    //最后一个航点与当前地图中心的连线
    private AirMapPolyline polyline;
    //最后一个航点与当前地图中心的距离maker
    private AirMapMarker marker;

    private boolean useTO = false;

    //当前坐标与地址
    private double curLat = 0;
    private double curLng = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_birdeye_flyline);

        EventBus.getDefault().register(this);

        compassView = this.findViewById(R.id.compass);
        compassView.setKeepScreenOn(true);//屏幕高亮
        //获取系统服务（SENSOR_SERVICE)返回一个SensorManager 对象
        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //locateCurPosition();
        initUI();
        initMapView();
        getLocation();
    }

    @Override
    protected void onDestroy() {
        //Activity生命周期结束，做回收工作
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        initFlightController();
        /**
         * 获取方向传感器
         * 通过SensorManager对象获取相应的Sensor类型的对象
         */
        Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        //应用在前台时候注册监听器
        manager.registerListener(sensorEventListener, sensor,
                SensorManager.SENSOR_DELAY_GAME);
        super.onResume();
    }

    @Override
    protected void onPause() {
        //应用不在前台时候销毁掉监听器
        manager.unregisterListener(sensorEventListener);
        super.onPause();
    }

    private int cameraType = 0;

    public void initData(){

        Globle.isGCJ02 = false;

        int altitude = 80;
        int speed = 15;
        int space = 20;
        //int overlap = 70;
        int photoInterval = 2;
       // MissionConfig.getInstance().setOverlap(overlap);
        MissionConfig.getInstance().setAltitude(altitude);
        MissionConfig.getInstance().setPhotoInterval(photoInterval);
        tvAltitudeNum.setText(""+altitude);
        //int space = (int)SettingUtil.longkuan(h, overlap*1.0/100.0);
        //int speed = (int)SettingUtil.sudu(h, overlap*1.0/100.0, photoInterval);
        tvSpaceNum.setText(""+space);
        tvSpeedNum.setText(""+speed);
        tvPhotoIntervalNum.setText("" + photoInterval);
        MissionConfig.getInstance().setSpeed(speed);

        MissionConfig.getInstance().setAltitude(altitude);
        int side_overlap = (int) SettingUtil.overlap_side(altitude, space, cameraType);
        int front_overlap = (int)SettingUtil.overlap_front(altitude, speed, photoInterval, cameraType);
        tvSideOverlapNum.setText(""+side_overlap);
        tvFrontOverlapNum.setText(""+front_overlap);
    }

    private void initUI() {
        findViewById(R.id.map_save).setOnClickListener(this); //保存按钮

        findViewById(R.id.imageView1).setOnClickListener(this); //地图中点

        taskInfoView = findViewById(R.id.taskInfo);
        findViewById(R.id.locate).setOnClickListener(this); //左侧：定位
        findViewById(R.id.gps).setOnClickListener(this); //左侧：定位
        findViewById(R.id.clear).setOnClickListener(this); //左侧：清除
        findViewById(R.id.delete).setOnClickListener(this); //左侧：回退
        findViewById(R.id.fanzhuan).setOnClickListener(this); //左侧：翻转
        taskInfoView.setOnClickListener(this);

        drawerLayout = findViewById(R.id.map_drawerlayout);
        drawerRightLayout = findViewById(R.id.right_layout);
        ImageView drawerHandleC = findViewById(R.id.drawer_handle_c);
        drawerHandleO = findViewById(R.id.drawer_handle_o);
        drawerHandleO.setOnClickListener(this);
        drawerHandleC.setOnClickListener(this);
        //去阴影
        drawerLayout.setScrimColor(Color.TRANSPARENT);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                if (slideOffset == 0) {
                    drawerHandleO.setVisibility(View.VISIBLE);

                } else {
                    drawerHandleO.setVisibility(View.GONE);
                }
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        tvSpaceNum = findViewById(R.id.space_text);
        tvSpeedNum = findViewById(R.id.speed_text);
        deviceSpinner = findViewById(R.id.spinner_device);
        deviceSpinner.setOnItemSelectedListener(FlyLineActivity.this);
        tvAltitudeNum = findViewById(R.id.altitude_text);
        TextView addAltitude = findViewById(R.id.altitude_add);
        TextView subAltitude = findViewById(R.id.altitude_sub);
        addAltitude.setOnClickListener(this);
        subAltitude.setOnClickListener(this);

        tvPhotoIntervalNum = findViewById(R.id.interval_text);
        TextView addPhotoInterval = findViewById(R.id.interval_add);
        TextView subPhotoInterval = findViewById(R.id.interval_sub);
        addPhotoInterval.setOnClickListener(this);
        subPhotoInterval.setOnClickListener(this);

        TextView addSpace = findViewById(R.id.space_add);
        TextView subSpace = findViewById(R.id.space_sub);
        addSpace.setOnClickListener(this);
        subSpace.setOnClickListener(this);
        TextView addSpeed = findViewById(R.id.speed_add);
        TextView subSpeed = findViewById(R.id.speed_sub);
        addSpeed.setOnClickListener(this);
        subSpeed.setOnClickListener(this);

        tvAoTo = findViewById(R.id.aoto_view);
        tvAoTo.setOnClickListener(this);

        // 打点控件
        TextView tvPoint = findViewById(R.id.point);
        tvPoint.setOnClickListener(this);

        tvSideOverlapNum = findViewById(R.id.overlap_side_text);
        tvFrontOverlapNum = findViewById(R.id.overlap_front_text);

        addTextWatcher();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        cameraType = ((DeviceList.DeviceData) this.deviceSpinner.getSelectedItem()).cameraType;
        String deviceName = ((DeviceList.DeviceData) this.deviceSpinner.getSelectedItem()).name;
        if ("Mavic".equals(deviceName)) {
            tvAltitudeNum.setText("30");
            tvSpaceNum.setText("13");
            tvSpeedNum.setText("13");
        }else if ("Matric".equals(deviceName)){
            tvAltitudeNum.setText("200");
            tvSpaceNum.setText("33");
            tvSpeedNum.setText("13");
        }else {
            tvAltitudeNum.setText("80");
            tvSpaceNum.setText("20");
            tvSpeedNum.setText("15");
        }
        if (areaUtils != null) {
            int altitude = MissionConfig.getInstance().getAltitude();
            int space = areaUtils.getSpace();
            int speed = areaUtils.getSpeed();
            int photoInterval = MissionConfig.getInstance().getPhotoInterval();
            int side_overlap = (int)SettingUtil.overlap_side(altitude, space, cameraType);
            int front_overlap = (int)SettingUtil.overlap_front(altitude, speed, photoInterval, cameraType);
            tvSideOverlapNum.setText(""+side_overlap);
            tvFrontOverlapNum.setText(""+front_overlap);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void addTextWatcher(){
        tvAltitudeNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(charSequence.toString())){
                    try {
                        float value = Float.parseFloat(charSequence.toString());
                        if (value<1){
                            tvAltitudeNum.setText("1");
                        }else if(value>500){
                            tvAltitudeNum.setText("500");
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        tvAltitudeNum.setText("5");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    int altitude = Integer.parseInt(editable.toString());
                    MissionConfig.getInstance().setAltitude(altitude);

                    int space = areaUtils.getSpace();
                    int speed = areaUtils.getSpeed();
                    int photoInterval = MissionConfig.getInstance().getPhotoInterval();
                    int side_overlap = (int)SettingUtil.overlap_side(altitude, space, cameraType);
                    int front_overlap = (int)SettingUtil.overlap_front(altitude, speed, photoInterval, cameraType);
                    tvSideOverlapNum.setText(""+side_overlap);
                    tvFrontOverlapNum.setText(""+front_overlap);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        tvPhotoIntervalNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(charSequence.toString())){
                    try {
                        float value = Float.parseFloat(charSequence.toString());
                        if (value<2){
                            tvPhotoIntervalNum.setText("2");
                        }else if(value>10){
                            tvPhotoIntervalNum.setText("10");
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        tvPhotoIntervalNum.setText("2");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    int altitude = MissionConfig.getInstance().getAltitude();
                    int photoInterval = Integer.parseInt(editable.toString());
                    int space = areaUtils.getSpace();
                    int speed = areaUtils.getSpeed();
                    int side_overlap = (int)SettingUtil.overlap_side(altitude, space, cameraType);
                    int front_overlap = (int)SettingUtil.overlap_front(altitude, speed, photoInterval, cameraType);
                    tvFrontOverlapNum.setText(""+front_overlap);

                    MissionConfig.getInstance().setPhotoInterval(photoInterval);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        tvSpeedNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(charSequence.toString())){
                    try {
                        float value = Float.parseFloat(charSequence.toString());
                        if (value<2){
                            tvSpeedNum.setText("2");
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        tvSpeedNum.setText("2");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    int speed = Integer.parseInt(editable.toString());
                    areaUtils.setSpeed(speed);
                    MissionConfig.getInstance().setSpeed(speed);


                    int altitude = MissionConfig.getInstance().getAltitude();
                    int photoInterval = MissionConfig.getInstance().getPhotoInterval();
                    int space = areaUtils.getSpace();
                    int side_overlap = (int)SettingUtil.overlap_side(altitude, space, cameraType);
                    int front_overlap = (int)SettingUtil.overlap_front(altitude, speed, photoInterval, cameraType);
                    tvFrontOverlapNum.setText(""+front_overlap);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        tvSpaceNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(charSequence.toString())){
                    try {
                        float value = Float.parseFloat(charSequence.toString());
                        if (value<2){
                            tvSpaceNum.setText("2");
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        tvSpaceNum.setText("2");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    int space = Integer.parseInt(editable.toString());
                    areaUtils.setSpace(space);

                    int altitude = MissionConfig.getInstance().getAltitude();
                    int photoInterval = MissionConfig.getInstance().getPhotoInterval();
                    int speed = areaUtils.getSpeed();
                    int side_overlap = (int)SettingUtil.overlap_side(altitude, space, cameraType);
                    int front_overlap = (int)SettingUtil.overlap_front(altitude, speed, photoInterval, cameraType);
                    tvSideOverlapNum.setText(""+side_overlap);

                    areaUtils.draw(FlyLineActivity.this.getApplicationContext(), new Callback_AreaUtil() {
                        @Override
                        public void onClear(){
                        }
                        @Override
                        public void onRefresh(String info) {
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void useAOTO(boolean useTo) {
        this.useTO = useTo;
        if (useTo) {
            tvAoTo.setText(getResources().getString(R.string.to));
        } else {
            tvAoTo.setText(getResources().getString(R.string.ao));
        }
        try {
            areaUtils.setUseCovexHull(useTo);
            areaUtils.draw(this.getApplicationContext(), new Callback_AreaUtil() {
                @Override
                public void onClear(){
                    taskInfoView.setVisibility(View.GONE);
                }
                @Override
                public void onRefresh(String info) {
                    taskInfoView.setVisibility(View.VISIBLE);
                    taskInfoView.setText(info);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cameraUpdate(double lat, double lnt) {
        LatLng location = new LatLng(lat, lnt);
        mMap.animateCenterZoom(location, 18);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.locate){
            Log.d(TAG, ""+isProductConnected);
            //if (isProductConnected) {
                if (checkGpsCoordination(droneLocationLat, droneLocationLng)) {
                    cameraUpdate(droneLocationLat, droneLocationLng); // Locate the drone's place
                }else {
                    ToastUtils.setResultToToast("未连接飞机！");
                    return;
                }
            //}
        } else if (id == R.id.gps) {
            double[] ll2 = {curLng, curLat};
//            ll2 = CoordinateTransformUtil.wgs84togcj02(curLng,
//                    curLat);
            LatLng ll = new LatLng(ll2[1], ll2[0]);
            mMap.setCenter(ll);
        } else if (id == R.id.map_save) {
            // 起飞

        } else if (id == R.id.imageView1) {
            addPoint();
        } else if (id == R.id.point) {
            addPoint();
        } else if (id == R.id.drawer_handle_c) {
            drawerLayout.closeDrawers();
        } else if (id == R.id.drawer_handle_o) {
            drawerLayout.openDrawer(Gravity.RIGHT);
        } else if (id == R.id.altitude_add) {
            String s = tvAltitudeNum.getText().toString();
            int num = Integer.parseInt(s) + 5;
            if (num > 500) {
                return;
            }
            tvAltitudeNum.setText(String.valueOf(num));
        } else if (id == R.id.altitude_sub) {
            String s = tvAltitudeNum.getText().toString();
            int num = Integer.parseInt(s) - 5;
            if (num <= 0) {
                return;
            }
            tvAltitudeNum.setText(String.valueOf(num));
        } else if (id == R.id.interval_add) {
            String s = tvPhotoIntervalNum.getText().toString();
            int num = Integer.parseInt(s) + 1;
            if (num > 10) {
                return;
            }
            tvPhotoIntervalNum.setText(String.valueOf(num));
        } else if (id == R.id.interval_sub) {
            String s = tvPhotoIntervalNum.getText().toString();
            int num = Integer.parseInt(s) - 1;
            if (num < 2) {
                return;
            }
            tvPhotoIntervalNum.setText(String.valueOf(num));
        }else if (id == R.id.aoto_view) {
            useAOTO(!useTO);
        } else if (id == R.id.delete) {
            if (areaUtils != null) {
                areaUtils.removeLast();
                areaUtils.draw(this.getApplicationContext(), new Callback_AreaUtil() {
                    @Override
                    public void onClear() {
                        taskInfoView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onRefresh(String info) {
                        taskInfoView.setVisibility(View.VISIBLE);
                        taskInfoView.setText(info);
                    }
                });
//                    if (areaUtils.size() > 2) {
//                        setPolygonEditable(true);
//                    } else {
//                        setPolygonEditable(false);
//                    }
            }
        } else if (id == R.id.clear) {//                setPolygonEditable(false);
            if (areaUtils != null) {
                areaUtils.clear();
            }
        } else if (id == R.id.fanzhuan) {
            if (areaUtils != null) {
                areaUtils.reverse(1);
                areaUtils.draw(this.getApplicationContext(), new Callback_AreaUtil() {
                    @Override
                    public void onClear() {
                        taskInfoView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onRefresh(String info) {
                        taskInfoView.setVisibility(View.VISIBLE);
                        taskInfoView.setText(info);
                    }
                });
            }
        }else if (id == R.id.space_add) {
            String s = tvSpaceNum.getText().toString();
            int num = Integer.parseInt(s) + 1;
            //if (num > 99) {
            //    return;
            //}
            tvSpaceNum.setText(String.valueOf(num));
            areaUtils.setSpace(num);
            areaUtils.draw(this.getApplicationContext(), new Callback_AreaUtil() {
                @Override
                public void onClear(){
                    taskInfoView.setVisibility(View.GONE);
                }
                @Override
                public void onRefresh(String info) {
                    taskInfoView.setVisibility(View.VISIBLE);
                    taskInfoView.setText(info);
                }
            });
        }else if (id == R.id.space_sub) {
            String s = tvSpaceNum.getText().toString();
            int num = Integer.parseInt(s) - 1;
            if (num == 0) {
                return;
            }
            tvSpaceNum.setText(String.valueOf(num));
            areaUtils.setSpace(num);
            areaUtils.draw(this.getApplicationContext(), new Callback_AreaUtil() {
                @Override
                public void onClear(){
                    taskInfoView.setVisibility(View.GONE);
                }
                @Override
                public void onRefresh(String info) {
                    taskInfoView.setVisibility(View.VISIBLE);
                    taskInfoView.setText(info);
                }
            });
        }else if (id == R.id.speed_add) {
            String s = tvSpeedNum.getText().toString();
            int num = Integer.parseInt(s) + 1;
            if (num > 25) {
                return;
            }
            tvSpeedNum.setText(String.valueOf(num));
            areaUtils.setSpeed(num);
        }else if (id == R.id.speed_sub) {
            String s = tvSpeedNum.getText().toString();
            int num = Integer.parseInt(s) - 1;
            if (num == 1) {
                return;
            }
            tvSpeedNum.setText(String.valueOf(num));
            areaUtils.setSpeed(num);
        }
    }

    private void addPoint() {
        if (areaUtils == null || areaUtils.size() >= 99) {
            //最多99个点
            return;
        }

        if (center == null) {
            return;
        }
        areaUtils.add(center);
        if (MissionConfig.getInstance().getMissionType()!=3&&!areaUtils.isCovexHull()) {
            showUseConvexHullDialog();
        } else {
            try {
                areaUtils.draw(this.getApplicationContext(), new Callback_AreaUtil() {
                    @Override
                    public void onClear(){
                        taskInfoView.setVisibility(View.GONE);
                    }
                    @Override
                    public void onRefresh(String info) {
                        taskInfoView.setVisibility(View.VISIBLE);
                        taskInfoView.setText(info);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showUseConvexHullDialog() {
        CustomDialog dialog = new CustomDialog(this);
        dialog.setMessage(getResources().getString(R.string.tip_aoto));
        dialog.addButton(getResources().getString(R.string.no_use), getResources().getString(R.string.use), new CustomDialog.DialogButtonClickListener() {
            @Override
            public void leftClick() {
                useAOTO(false);
            }

            @Override
            public void rightClic() {
                useAOTO(true);
            }
        });
        dialog.show();
    }

    private void addPhoneMarker( LatLng latLng ) {
        AirMapMarker marker = new AirMapMarker.Builder()
                .position(latLng)
                .divIconHtml("phone.png")
                .iconId(R.mipmap.curmark)
                .id(1000)
                .build();
        mMap.setPhoneMarker(marker);
    }

    //////地图操作：开始////////////////////
    private void initMapView() {
        mMap = findViewById(R.id.map);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCameraChangeListener(this);
        mMap.setOnMapInitializedListener(this);
        //mMap.initialize(getSupportFragmentManager());
        SharedPreferenceUtils sp = new SharedPreferenceUtils(ApplicationFactory.getInstance().getBaseApplication());
        String mapUrl = sp.getSaveStringData(Constants.KEY_MAP_URL, "");
        String subdomains = sp.getSaveStringData(Constants.KEY_SUBDOMAINS, "");
        String tms = sp.getSaveStringData(Constants.KEY_TMS, "");
        String attribution = sp.getSaveStringData(Constants.KEY_ATTRIBUTION, "");
        AirMapInterface airMapInterface = new WebAirMapViewBuilder().withOptions(new LeafletGaodeMapType(mapUrl,subdomains,tms,attribution)).build();

        if (airMapInterface != null) {
            mMap.initialize(getSupportFragmentManager(), airMapInterface);
        }
    }

    @Override
    public void onMapInitialized() {
        mMap.setMapType(MapType.MAP_TYPE_SATELLITE);
        if (curLng > 0 && curLat > 0){
            LatLng ll = new LatLng(curLat, curLng);
            mMap.setCenter(ll);
        }
        if (areaUtils == null) {
            areaUtils = new AreaUtils(mMap);
            SharedPreferenceUtils sp = new SharedPreferenceUtils(this.getApplicationContext());
            areaUtils.setUserId(sp.getSaveStringData(Constants.KEY_USERID, ""));

            initData();
        }
    }

    @Override
    public void onMapMarkerClick(AirMapMarker<?> marker) {
        //被点击的顶点设置为起点
        if (areaUtils.contains(Long.toString(marker.getId()))) {
            LatLng position = marker.getLatLng();
            areaUtils.setStartPoint(position);
            areaUtils.draw(this.getApplicationContext(), new Callback_AreaUtil() {
                @Override
                public void onClear(){
                    taskInfoView.setVisibility(View.GONE);
                }
                @Override
                public void onRefresh(String info) {
                    taskInfoView.setVisibility(View.VISIBLE);
                    taskInfoView.setText(info);
                }
            });
        }
    }

    @Override
    public void onCameraChanged(LatLng latLng, int zoom) {
        center = mMap.getCenter();
        if (polyline != null) {
            mMap.removePolyline(polyline);
        }
        if (marker != null) {
            mMap.removeMarker(marker);
        }

        if (center == null) {
            return;
        }

        //重绘连接线
        if (areaUtils.size() >= 1) {
            List<LatLng> latLngs = new ArrayList<LatLng>();
            LatLng last_ll = areaUtils.getLastPoint();
            if (last_ll == null) {
                return;
            }
            LatLng cur_ll = center;
            latLngs.add(last_ll);
            latLngs.add(cur_ll);
            polyline = new AirMapPolyline(null, latLngs,
                    8092, 3, 0xFFC65F);
            mMap.addPolyline(polyline);

            double distance = PolygonUtil.distance(last_ll, cur_ll);
            DecimalFormat fnum = new DecimalFormat("##0.00");
            String dd = fnum.format(distance);
            LatLng pos = new LatLng((last_ll.latitude + cur_ll.latitude) / 2,
                    (last_ll.longitude + cur_ll.longitude) / 2);
            String title = dd + "M";
            Bitmap bmp = AreaUtils.genBitmap(this.getApplicationContext(), title, 0, 14);
            AirMapMarker.Builder markerOptions = new AirMapMarker.Builder()
                    .position(pos)
                    .id(8093)
                    .title(title)
                    .bitmap(bmp)
                    .anchor(0.5f, 1);

            marker = markerOptions.build();
            mMap.addText(marker);
        }
    }
    //////地图操作：结束////////////////////

    //////无人机控制：开始//////////////////
    private FlightController mFlightController;
    //无人机坐标
    protected double droneLocationLat = 181, droneLocationLng = 181;
    //无人机Marker
    private AirMapMarker droneMarker = null;

    public boolean isProductConnected = false;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProductConnectionChange(ApplicationFactory.ConnectivityChangeEvent event) {
        //TODO 这里如果相机未连接，不初始化飞控，就飞不起来？
        if (event == ApplicationFactory.ConnectivityChangeEvent.CameraConnect) {
            initFlightController();
        } else if (event == ApplicationFactory.ConnectivityChangeEvent.ProductConnected) {
            isProductConnected = true;
        } else if (event == ApplicationFactory.ConnectivityChangeEvent.CameraDisconnect) {
        } else if (event == ApplicationFactory.ConnectivityChangeEvent.ProductDisconnected) {
            isProductConnected = false;
        }
    }

    public void initFlightController() {
        //初始化FlightController模块
        BaseProduct product = ApplicationFactory.getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                mFlightController = ((Aircraft) product).getFlightController();
                if (mFlightController != null) {
                    droneStateUpdate();
                }
            }
        }
    }

    private void droneStateUpdate() {
        if (null != mFlightController) {
            mFlightController.setStateCallback((@NonNull FlightControllerState flightControllerState) -> {
                droneLocationLat = flightControllerState.getAircraftLocation().getLatitude();
                droneLocationLng = flightControllerState.getAircraftLocation().getLongitude();
                Attitude attitude = flightControllerState.getAttitude();
                double droneHeading = (float) attitude.yaw;
                updateDroneLocation(droneLocationLat, droneLocationLng, droneHeading);
            });
        }
    }

    public static boolean checkGpsCoordination(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180)
                && (latitude != 0f && longitude != 0f);
    }

    // 在地图上刷新无人机的定位
    public void updateDroneLocation(final double droneLocationLat, final double droneLocationLng, final double droneHeading) {
        //Log.i(TAG, "location: " + droneLocationLat + "   " + droneLocationLng);
        double lat = droneLocationLat;
        double lng = droneLocationLng;
        if (Globle.isGCJ02) {
            double[] ll2 = CoordinateTransformUtil.wgs84togcj02(droneLocationLng, droneLocationLat);
            lat = ll2[1];
            lng = ll2[0];
        }
        final LatLng pos = new LatLng(lat, lng);
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
    //////无人机控制：结束//////////////////

    //////方向传感器：开始//////////////////
    // 方向传感器监听者
    private final SensorEventListener sensorEventListener = new SensorEventListener() {
            private float pre_degree = 0;

        @Override
        public void onSensorChanged(SensorEvent event) {
            // values[0]: x-axis 方向加速度
            // values[1]: y-axis 方向加速度
            // values[2]: z-axis 方向加速度
            float degree = event.values[0];// 存放了方向值
            // 动画效果
            RotateAnimation animation = new RotateAnimation(pre_degree, degree,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setDuration(200);
            compassView.startAnimation(animation);
            pre_degree = -degree;

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    //////方向传感器：结束//////////////////

    //////手机定位：开始//////////////////
    @Override
    public LocationConfiguration getLocationConfiguration() {
        return Configurations.defaultConfiguration("Gimme the permission!", "Would you mind to turn GPS on?");
    }

    @Override
    public void onLocationChanged(Location location) {
        //samplePresenter.onLocationChanged(location);
        Log.d(TAG, location.toString());
        curLat = location.getLatitude();
        curLng = location.getLongitude();
        if (mMap.isInitialized()){
            LatLng ll = new LatLng(curLat, curLng);
            mMap.setCenter(ll);
        }else{
            Log.d(TAG, "mMap is not initialized now.");
        }
    }

    @Override
    public void onLocationFailed(@FailType int failType) {
        //samplePresenter.onLocationFailed(failType);

    }

    @Override
    public void onProcessTypeChanged(@ProcessType int processType) {
        //samplePresenter.onProcessTypeChanged(processType);
    }
    //////手机定位：结束//////////////////
}
