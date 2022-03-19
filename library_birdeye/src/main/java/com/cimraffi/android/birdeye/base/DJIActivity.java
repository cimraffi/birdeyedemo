package com.cimraffi.android.birdeye.base;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.airbnb.android.airmapview.MapUtils.LatLng;
import com.airbnb.android.airmapview.MapUtils.SphericalUtil;
import com.cimraffi.android.birdeye.ApplicationFactory;
import com.cimraffi.android.birdeye.bean.ContinuePoint;
import com.cimraffi.android.birdeye.bean.MissionConfig;
import com.cimraffi.android.birdeye.common.Constants;
import com.cimraffi.android.birdeye.common.Globle;
import com.cimraffi.android.birdeye.dbex.SomeLab;
import com.cimraffi.android.birdeye.utils.SharedPreferenceUtils;
import com.cimraffi.android.birdeye.utils.ToastUtils;
import com.cimraffi.android.birdeye.utils.Utils;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import dji.common.battery.BatteryState;
import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.flightcontroller.Attitude;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.RemoteControllerFlightMode;
import dji.common.flightcontroller.rtk.NetworkServiceState;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointExecutionProgress;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecuteState;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.common.model.LocationCoordinate2D;
import dji.common.product.Model;
import dji.common.remotecontroller.HardwareState;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.battery.Battery;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.flightcontroller.RTK;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.media.MediaFile;
import dji.sdk.mission.timeline.triggers.Trigger;
import dji.sdk.mission.timeline.triggers.WaypointReachedTrigger;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.network.RTKNetworkServiceProvider;
import dji.sdk.products.Aircraft;
import dji.sdk.remotecontroller.RemoteController;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.sdkmanager.LiveVideoBitRateMode;
import dji.sdk.useraccount.UserAccountManager;
import com.cimraffi.android.birdeye.R;

public class DJIActivity extends MapActivity implements Callback_DroneUtil {

    protected static final String TAG = DJIActivity.class.getSimpleName();

    //无人机坐标
    protected double droneLocationLat = 181, droneLocationLng = 181;
    //无人机机头朝向
    protected float droneHeading = 0.0f;

    protected List<Waypoint> waypointList = new ArrayList<>();
    public static WaypointMission.Builder waypointMissionBuilder;
    private FlightController mFlightController;
    private WaypointMissionOperator instance;
    private WaypointMissionFinishedAction mFinishedAction = WaypointMissionFinishedAction.NO_ACTION;
    //任务航向模式 使用下一航点的方向
    private WaypointMissionHeadingMode mHeadingMode = WaypointMissionHeadingMode.USING_WAYPOINT_HEADING;

    //正在飞行...
    private boolean isFlying;
    //已经上传结束飞行时间
    private boolean hadUploadFinishFlyTime = false;
    //开始任务
    private boolean hadStart = false;
    //
    private boolean hadFly = false;

    public boolean useContinuePoint = false;

    public boolean isFlying(){
        return isFlying;
    }

    private boolean stop_send_photo = false;

    private String task_code; //飞机起飞后会为该变量生成一个值

    private ProgressDialog mDownloadDialog;

    private String liveShowUrl;
    @Override
    public void ShowDownloadProgressDialog(String info) {
        if (mDownloadDialog != null) {
            runOnUiThread(new Runnable() {
                public void run() {
                    mDownloadDialog.incrementProgressBy(-mDownloadDialog.getProgress());
                    mDownloadDialog.setTitle(info);
                    mDownloadDialog.show();
                }
            });
        }
    }
    @Override
    public void SetDownloadProgressDialogMsg(String info, double p){
        if (mDownloadDialog != null) {
            runOnUiThread(new Runnable() {
                public void run() {
                    mDownloadDialog.setTitle(info);
                    if (p>0){
                        int tmpProgress = (int) (1.0 * p * 100);
                        mDownloadDialog.setProgress(tmpProgress);
                    }
                }
            });
        }
    }

    @Override
    public void HideDownloadProgressDialog() {
        if (null != mDownloadDialog && mDownloadDialog.isShowing()) {
            runOnUiThread(new Runnable() {
                public void run() {
                    mDownloadDialog.dismiss();
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        EventBus.getDefault().register(this);

//        initCameraManager();

        addListener();

        droneLocationLat = 40;
        droneLocationLng = 116;

        //Init Download Dialog
        mDownloadDialog = new ProgressDialog(DJIActivity.this);
        mDownloadDialog.setTitle("处理照片");
        mDownloadDialog.setIcon(android.R.drawable.ic_dialog_info);
        mDownloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDownloadDialog.setCanceledOnTouchOutside(false);
        mDownloadDialog.setCancelable(true);
        mDownloadDialog.setOnCancelListener((DialogInterface dialog) -> {
                    stop_send_photo = true;
                    DroneUtil.getInstance().setStopSendPhoto(true);
                }
        );

        SharedPreferenceUtils sp = new SharedPreferenceUtils(ApplicationFactory.getInstance().getBaseApplication());
        liveShowUrl = sp.getSaveStringData(Constants.KEY_VIDEO_ADDRESS, "");
    }

    @Override
    protected void onResume() {
        super.onResume();
        initFlightController();
    }

    @Override
    protected void onDestroy() {
        //Activity生命周期结束，做回收工作
        EventBus.getDefault().unregister(this);

        removeListener();

        super.onDestroy();
    }

    public ArrayList<String> logs = new ArrayList<>();
    /**
     * 日志列表追加文本日志
     *
     * @param msg
     */
    protected void appendLog(String msg) {
        if (logs==null){
            logs = new ArrayList<>();
        }
        logs.add(msg);
    }

    /**
     * 显示日志
     *
     * @param string
     */
    public void setResultToToast(final String string) {
        runOnUiThread(() -> {
//            ToastUtils.showToast(string);
            appendLog(string);
            Log.d("MapActivity", string);
        });
    }

    /*
     * **************************************************
     * DJI设置开始
     * */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProductConnectionChange(ApplicationFactory.ConnectivityChangeEvent event) {
        //TODO 这里如果相机未连接，不初始化飞控，就飞不起来？
        if (event == ApplicationFactory.ConnectivityChangeEvent.CameraConnect) {
            mFlightController.setStateCallback((@NonNull FlightControllerState flightControllerState) -> {
                boolean flying = flightControllerState.isFlying();
            });

            initFlightController();
        } else if (event == ApplicationFactory.ConnectivityChangeEvent.ProductConnected) {
            isProductConnected = true;
            mMap.setMyLocationEnabled(false);
            loginAccount();
        } else if (event == ApplicationFactory.ConnectivityChangeEvent.CameraDisconnect) {
            setResultToToast(getResources().getString(R.string.camera_disconnected));
        } else if (event == ApplicationFactory.ConnectivityChangeEvent.ProductDisconnected) {
            setResultToToast(getResources().getString(R.string.product_disconnected));
            isProductConnected = false;
            mMap.setMyLocationEnabled(true);
            if (areaUtils != null) {
                areaUtils.productDisconnected();
            }
            if (hadStart) {
                hadStart = false;
                hadFly = false;
                hadUploadFinishFlyTime = true;
                //如果已经开始飞行，断开连接后上传飞行结束时间
            }
            hadStopedWaypointMission(true, "");
        }
    }

    private void loginAccount() {
        //MSDK登录逻辑
        UserAccountManager.getInstance().logIntoDJIUserAccount(this,
                new CommonCallbacks.CompletionCallbackWith<UserAccountState>() {
                    @Override
                    public void onSuccess(final UserAccountState userAccountState) {
                        setResultToToast(getResources().getString(R.string.login_success));
                    }

                    @Override
                    public void onFailure(DJIError error) {
                        setResultToToast(String.format(getResources().getString(R.string.login_error), error.getDescription()));
                    }
                });
    }

    public void initFlightController() {
        //初始化FlightController模块
        BaseProduct product = ApplicationFactory.getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                mFlightController = ((Aircraft) product).getFlightController();
                Battery battery = ((Aircraft) product).getBattery();
                droneBatteryUpdate(battery);
            }
        }

        if (mFlightController != null) {
            getSerialNumber();
            setMaxFlightRadiusLimitationEnabled(true);
            setMaxFlightHeight(500);
            setReturnHomeHeight(20);
            droneStateUpdate();
            getRCSwitchFlightModeMapping();
        }
    }

    @Override
    public void updateDroneLocation() {
        super.updateDroneLocation();
        updateDroneLocation(droneLocationLat, droneLocationLng, droneHeading);
    }

    private void getSerialNumber() {
        mFlightController.getSerialNumber(new CommonCallbacks.CompletionCallbackWith<String>() {
            @Override
            public void onSuccess(String s) {
                Globle.serialNumber = s;
                setResultToToast(String.format(getResources().getString(R.string.serialnumber), s));
            }

            @Override
            public void onFailure(DJIError djiError) {

            }
        });
    }

    private void initCameraManager() {
        BaseProduct product = ApplicationFactory.getProductInstance();
        setCameraShootPhotoMode();
    }

    private void addListener() {
        if (getWaypointMissionOperator() != null) {
            getWaypointMissionOperator().addListener(eventNotificationListener);
        }
    }

    private void removeListener() {
        if (getWaypointMissionOperator() != null) {
            getWaypointMissionOperator().removeListener(eventNotificationListener);
        }
    }

    protected void genTaskCode(){
        task_code = "ID"+Calendar.getInstance().getTimeInMillis();
    }

    private WaypointMissionOperatorListener eventNotificationListener = new WaypointMissionOperatorListener() {
        @Override
        public void onDownloadUpdate(WaypointMissionDownloadEvent downloadEvent) {

        }

        @Override
        public void onUploadUpdate(WaypointMissionUploadEvent uploadEvent) {

        }

        @Override
        public void onExecutionUpdate(WaypointMissionExecutionEvent executionEvent) {
            WaypointExecutionProgress p = executionEvent.getProgress();
            if (p == null) {
                System.out.println("cp_test  returnrrrr.");
                return;
            }
            boolean status = p.isWaypointReached;
            int index = p.targetWaypointIndex;
            int total = p.totalWaypointCount;
            WaypointMissionExecuteState s = p.executeState;
            System.out.println("onExecutionUpdate   " + index+"  "+total);

            if (status && s == WaypointMissionExecuteState.BEGIN_ACTION) { //每个航点开始动作，输出信息
                setResultToToast(String.format(getResources().getString(R.string.Index_Begin), index));
            }

            if (index == 0){ //第一个航点
                if (status && s == WaypointMissionExecuteState.BEGIN_ACTION) { //摄像头低头前把必要的信息入库保存
                    setResultToToast(String.format(getResources().getString(R.string.Total_Count), total));
                    missionStartPrepare();
                } else if (status && s == WaypointMissionExecuteState.FINISHED_ACTION) { //摄像头低头后开始拍照
                    setResultToToast("开始结束，启动拍照");
                    if (MissionConfig.getInstance().isCameraContorl()) {
                        startShootPhoto();
                    }
                }
            }

            //更新每个航点信息
            if (s == WaypointMissionExecuteState.FINISHED_ACTION){
                missionUpdate(index, total);
            }

            if (index == (total - 1)) { //最后一个航点
                if (status && s == WaypointMissionExecuteState.BEGIN_ACTION) { //摄像头抬头前停止拍照
                    setResultToToast("任务结束，停止拍照");
                    if (MissionConfig.getInstance().isCameraContorl()) {
                        stopShootPhoto();
                    }
                }
                if (status && s == WaypointMissionExecuteState.FINISHED_ACTION) { //相机抬头后记录时间
                    missionStopPrepare();
                }
            }
        }

        @Override
        public void onExecutionStart() {
            setResultToToast(getResources().getString(R.string.start_task));
            missionStart();
        }

        @Override
        public void onExecutionFinish(@Nullable final DJIError error) {
            //停止定时间隔拍照
            if (MissionConfig.getInstance().isCameraContorl()) {
                stopShootPhoto();
            }
            missionStop();
        }
    };

    public WaypointMissionOperator getWaypointMissionOperator() {
        if (instance == null) {
            if (DJISDKManager.getInstance() != null && DJISDKManager.getInstance().getMissionControl() != null) {
                instance = DJISDKManager.getInstance().getMissionControl().getWaypointMissionOperator();
            }
        }
        return instance;
    }

    //todo 与无人机的连接发生变化时需要做的工作
    public void workWhenConnectChange() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectChange(ApplicationFactory.ConnectivityChangeEvent event) {
        if (event == ApplicationFactory.ConnectivityChangeEvent.ProductConnected) {
            workWhenConnectChange();
        }
    }

    /*
     * DJI设置结束
     * **************************************************
     * */

    public void missionStartPrepare(){
        SomeLab al = SomeLab.get(getBaseContext());
        ContinuePoint continuePoint = al.getContinuePoint(areaUtils.getAreaID());
        long timeStamp = continuePoint != null ? Utils.getTimestamp10(continuePoint.missionStartTime) : Utils.getTimestamp10(Calendar.getInstance().getTimeInMillis());

        System.out.println("----------------" + timeStamp);
        /*
        // 删除续航点，因为onExecutionStart回调偶尔会不调用，所以放在这里，
        // 而且飞机到到第一个任务点才删除续航点，也比较合理
        if (areaUtils != null) {
            Log.d("cp_test", String.format("missionStart: (areaUtils != null)"));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("cp_test", String.format("missionStart: areaUtils.deleteContinuePoint()"));
                    areaUtils.deleteContinuePoint();
                }
            });
        }*/

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        task_code = sdf.format(date);

        if (null != ApplicationFactory.getCameraInstance()) {
            ApplicationFactory.getCameraInstance().setMediaFileCallback((MediaFile mediaFile) -> {
                Log.d("newMediaFile", mediaFile.getFileName()+"|"+mediaFile.getIndex());
                if (mediaFile.getMediaType() == MediaFile.MediaType.JPEG || mediaFile.getMediaType() == MediaFile.MediaType.TIFF){
                    //DroneUtil.getInstance().addMediaFile(mediaFile.clone());
                }
            });
        }
    }

    public void missionUpdate(int index, int total){
        if (areaUtils != null) {
            Log.d("cp_test", String.format("missionUpdate: index(%d) total(%d)", index, total));
            areaUtils.setTargetWaypointIndex(index, total);
            areaUtils.setFinishTargetWaypointIndex(index, total);
        }
    }

    public void missionStopPrepare(){
        if(null!=ApplicationFactory.getCameraInstance()){
            ApplicationFactory.getCameraInstance().setMediaFileCallback(null);
        }

        //areaUtils.setFinishAirway(true);
        long timeStamp = Utils.getTimestamp10(Calendar.getInstance().getTimeInMillis());
        areaUtils.setMissionEndTime(timeStamp+10);
    }

    public void missionStart() {
        Log.d("cp_test", String.format("missionStart"));
    }

    public void missionStop(){
        Log.d("cp_test", String.format("missionStop"));

        // 删除续航点，策略是本次任务结束（或完成、或断点）才会删除本次飞行开始时所采用的断点，
        // 否则容易出现的情况是飞行途中出现问题，APP退出或重启，则断点也没了，整个任务就作废了。
        if (areaUtils != null) {
            Log.d("cp_test", String.format("missionStop: (areaUtils != null)"));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("cp_test", String.format("missionStop: areaUtils.deleteContinuePoint()"));
                    areaUtils.deleteContinuePoint();
                }
            });
        }

        if (areaUtils != null) {
            setResultToToast(String.format("starttime:%s   endtime:%s",
                    areaUtils.getMissionStartTime(),
                    areaUtils.getMissionEndTime()));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("genPauseCmd", "run");
//                    areaUtils.DoFinish();
                    if (areaUtils.DoFinish()) {
                        Log.d("genPauseCmd", "DoFinish is true");
                        hadStopedWaypointMission(true, "");
                    }else{
                        Log.d("genPauseCmd", "DoFinish is false");
                        hadStopedWaypointMission(true, "");
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date curDate = new Date(System.currentTimeMillis());
                        String createTime = formatter.format(curDate);
                        setResultToToast(getResources().getString(R.string.end_mission) + createTime);
                        /*定距拍照任务从无人机上下载图片上传到服务器*/
                        int missionType = MissionConfig.getInstance().getMissionType();
                        System.out.println("cp_test:-------------" + areaUtils.getFinishAirway());
                        if (areaUtils != null && areaUtils.getFinishAirway()) {
                            System.out.println("cp_test:-------------into");
                        }
                    }
                }
            });
        }
    }

    //设置飞机参数
    public void configeMission() {

        float altitude = MissionConfig.getInstance().getAltitude();
        float mSpeed = areaUtils.getSpeed();
        waypointMissionBuilder = null;
        SyncWayPoint(altitude);

        configWayPointMission(mSpeed, altitude);
    }

    protected String nulltoIntegerDefalt(String value) {
        if (!isIntValue(value)) {
            value = "0";
        }
        return value;
    }

    private boolean isIntValue(String val) {
        try {
            val = val.replace(" ", "");
            Integer.parseInt(val);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    protected String nulltoFloatDefalt(String value) {
        if (!isFloatValue(value)) {
            value = "0.0";
        }
        return value;
    }

    private boolean isFloatValue(String val) {
        try {
            val = val.replace(" ", "");
            Float.parseFloat(val);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void configWayPointMission(float mSpeed, float altitude) {
        DecimalFormat df = new DecimalFormat("#.#");
        setResultToToast(String.format(getResources().getString(R.string.Speed_Altitude), df.format(mSpeed), df.format(altitude)));

        if (ApplicationFactory.getCameraInstance() != null && MissionConfig.getInstance().isCameraContorl()) {//手机控制无人机每隔2s自动拍照一次
            setCameraShootPhotoMode();
            setPhotoTimeIntervalSettings();
        }

        if (waypointMissionBuilder == null) {
            waypointMissionBuilder = new WaypointMission.Builder();
        }

        mFinishedAction = WaypointMissionFinishedAction.GO_HOME;
        //if (AppUtils.isPlantProtectionApp(this)) {
            mHeadingMode = WaypointMissionHeadingMode.AUTO;
        //} else {
        //    mHeadingMode = WaypointMissionHeadingMode.USING_WAYPOINT_HEADING;
        //}

        waypointMissionBuilder.finishedAction(mFinishedAction)
                .headingMode(mHeadingMode)
                .autoFlightSpeed(mSpeed)
                .maxFlightSpeed(mSpeed)
                .flightPathMode(WaypointMissionFlightPathMode.NORMAL);

        if (getWaypointMissionOperator() != null) {
            DJIError error = getWaypointMissionOperator().loadMission(waypointMissionBuilder.build());
            if (error == null) {
                setResultToToast(getResources().getString(R.string.LoadMission_Success));
                uploadWayPointMission();
            } else {
                //setResultToToast(String.format(getResources().getString(R.string.LoadMission_Failed), error.getDescription()));
                String msg = String.format(getResources().getString(R.string.LoadMission_Failed), error.getDescription());
                hadNotUploadedWaypointMisssion(msg);
                setResultToToast(msg);
            }
        } else {
            //setResultToToast(String.format(getResources().getString(R.string.LoadMission_Failed), "WaypointMissionOperator is null"));
            String msg = String.format(getResources().getString(R.string.LoadMission_Failed), "WaypointMissionOperator is null") ;
            hadNotUploadedWaypointMisssion(msg);
            setResultToToast(msg);
        }

    }

    private void setMaxFlightRadiusLimitationEnabled(final boolean isEnabled) {
        if (null != mFlightController) {
            mFlightController.setMaxFlightRadiusLimitationEnabled(isEnabled, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (isEnabled) {
                        setMaxFlightRadius(5000);
                    }
                }
            });
        }
    }

    // 设置最大飞行半径
    private void setMaxFlightRadius(final int r) {
        if (null != mFlightController) {
            mFlightController.setMaxFlightRadius(r, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        setResultToToast(getResources().getString(R.string.set_radius_success) + "(" + r + "m)。");
                    } else {
                        setResultToToast(getResources().getString(R.string.set_radius_failed) + "(" + r + "m) " + djiError.getDescription());
                    }
                }
            });
        }
    }

    // 设置最大飞行高度
    private void setMaxFlightHeight(int h) {
        if (null != mFlightController) {
            mFlightController.setMaxFlightHeight(h, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {

                }
            });
        }
    }

    // 设置返航高度
    private void setReturnHomeHeight(final int h) {
        if (null != mFlightController) {
            mFlightController.setGoHomeHeightInMeters(h, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        setResultToToast(String.format(getResources().getString(R.string.Set_GoHomeHeight_Success), h));
                    } else {
                        setResultToToast(String.format(getResources().getString(R.string.Set_GoHomeHeight_Failed), djiError.getDescription()));
                    }
                }
            });
        }
    }

    public void setHome(LatLng home){
        if (null != mFlightController) {
            mFlightController.setHomeLocation(new LocationCoordinate2D(home.latitude, home.longitude),
                    (DJIError djiError) -> { });
        }
    }

    private RTK mRtk;
    private RTKNetworkServiceProvider mRTKNetworkServiceProvider;
    private boolean hadSetNetServiceCallback = false;

    private void droneStateUpdate() {
        if (null != mFlightController) {
            mFlightController.setStateCallback((@NonNull FlightControllerState flightControllerState) -> {
                isFlying = flightControllerState.isFlying();
                if (hadStart && hadFly && !isFlying && !hadUploadFinishFlyTime) {
                    hadStart = false;
                    hadUploadFinishFlyTime = true;
                }
                if (hadFly&&!isFlying) {
                    //停止直播
                    stopLiveShow();
                    updateUAVStatus(Constants.STATUS_FALL);
                }
                hadFly = isFlying;
                int satelliteCount = flightControllerState.getSatelliteCount();
                updateSatelliteCount(satelliteCount);

                float velo_x = flightControllerState.getVelocityX();
                float velo_y = flightControllerState.getVelocityY();
                float velo_h = (float) Math.sqrt(velo_x * velo_x + velo_y * velo_y);
                updateVelocity(velo_h);

                float alt = flightControllerState.getAircraftLocation().getAltitude();
                updateAltitude(alt);

                droneLocationLat = flightControllerState.getAircraftLocation().getLatitude();
                droneLocationLng = flightControllerState.getAircraftLocation().getLongitude();
                Attitude attitude = flightControllerState.getAttitude();
                droneHeading = (float) attitude.yaw;
                updateDroneLocation(droneLocationLat, droneLocationLng, droneHeading);
            });
        }
    }

    protected void changeRTKShow(final String newDescription) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rtkTV.setText(newDescription);
            }
        });
    }

    private void getRCSwitchFlightModeMapping() {
        if (ApplicationFactory.isRemoteControllerAvailable()) {
            RemoteController remoteController = ((Aircraft) ApplicationFactory.getProductInstance()).getRemoteController();
            remoteController.setHardwareStateCallback(new HardwareState.HardwareStateCallback() {
                @Override
                public void onUpdate(@NonNull HardwareState rcHardwareState) {
                    if (null != mFlightController) {
                        mFlightController.getRCSwitchFlightModeMapping(new CommonCallbacks.CompletionCallbackWith<RemoteControllerFlightMode[]>() {
                            @Override
                            public void onSuccess(RemoteControllerFlightMode[] remoteControllerFlightModes) {
                                try {
                                    int position = rcHardwareState.getFlightModeSwitch().value();
                                    String mode = remoteControllerFlightModes[position].name();
                                    updateFlightMode(mode);
                                } catch (Exception e) {
                                }
                            }

                            @Override
                            public void onFailure(DJIError djiError) {
                            }
                        });
                    }
                }
            });
        }

    }

    private void droneBatteryUpdate(Battery battery) {
        if (battery != null) {
            battery.setStateCallback((BatteryState state) -> {
                int chargeRemaining = state.getChargeRemaining();
                int fullChargeCapacity = state.getFullChargeCapacity();
                int voltage = state.getVoltage();
                int percent = 101;
                if (fullChargeCapacity != 0 && chargeRemaining != 0) {
                    percent = chargeRemaining * 100 / fullChargeCapacity;
                }
                updateBattery(voltage, percent, isFlying);
            });
        }
    }

    //todo 上传任务完成
    public void hadUploadedWaypointMission() {

    }

    //todo 上传任务失败
    public void hadNotUploadedWaypointMisssion(String err) {

    }

    //todo 撤离至安全区域
    public void evacuation2SafeArea() {

    }

    //todo
    public void hadStartedWaypointMission(boolean result, String errr) {

    }

    //todo
    public void hadStopedWaypointMission(boolean isOk, String err) {

    }

    public void hadPauseWaypointMission(boolean isOk, String err) {
    }

    public void hadResumeWaypointMission(boolean isOk, String err) {
    }

    //todo
    public void updateFlightMode(final String mode) {

    }

    //todo
    public void updateSatelliteCount(final int satelliteCount) {

    }

    //todo
    public void updateVelocity(final float vh) {

    }

    //todo
    public void updateAltitude(final float altitude) {

    }

    public void updateBattery(final int voltage, final int persent, final boolean isFlying) {

    }

    public void safeCheck(LatLng ll, Callback_SafeCheck c){
        if (null != mFlightController) {
            mFlightController.getHomeLocation(new CommonCallbacks.CompletionCallbackWith<LocationCoordinate2D>() {
                @Override
                public void onSuccess(LocationCoordinate2D l) {
                    if ((l.getLatitude()+"").toLowerCase().equals("nan")) {
                        ToastUtils.setResultToToast("请检查无人机是否连接");
                    }else{
                        Double d = SphericalUtil.computeDistanceBetween(new LatLng(l.getLatitude(), l.getLongitude()), ll);
                        if (d<=25){
                            c.onFinish(true);
                        }else{
                            c.onFinish(false);
                        }
                    }
                }

                @Override
                public void onFailure(DJIError djiError) {
                    c.onFinish(false);
                }
            });
        }else{
            c.onFinish(false);
        }
    }

    /**
     * 上传任务
     */
    protected void uploadWayPointMission() {
        if (mFlightController == null || !mFlightController.isConnected()) {
            setResultToToast(getResources().getString(R.string.unconnected_aircraft));
            hadNotUploadedWaypointMisssion(getResources().getString(R.string.unconnected_aircraft));
            return;
        }
        if (getWaypointMissionOperator() != null) {
            getWaypointMissionOperator().uploadMission((DJIError error) -> {
                if (error == null) {
                    //setResultToToast("Mission upload successfully!");
                    hadUploadedWaypointMission();
                } else {
                    String msg = String.format(getResources().getString(R.string.Mission_Upload_Failed), error.getDescription()) + ",retrying...";
                    hadNotUploadedWaypointMisssion(msg);
                    setResultToToast(msg);
//                    getWaypointMissionOperator().retryUploadMission(null);
                }
            });
        } else {
            String err = String.format(getResources().getString(R.string.Mission_Upload_Failed), "WaypointMissionOperator is null");
            hadNotUploadedWaypointMisssion(err);
            setResultToToast(err);
        }
    }

    /**
     * 开始任务
     */
    protected void startWaypointMission() {
        startLiveShow();
        updateUAVStatus(Constants.STATUS_FLYING);
        tvStart.setClickable(true);
        if (getWaypointMissionOperator() != null) {
            getWaypointMissionOperator().startMission((DJIError error) -> {
                if (error == null) {
                    if (ApplicationFactory.getCameraInstance() == null) {
                        ToastUtils.setResultToToast(getResources().getString(R.string.no_camera));
                    }
                    String msg = getResources().getString(R.string.Mission_Start_Success);
                    hadStartedWaypointMission(true, msg);
                    setResultToToast(msg);
                    hadStart = true;
                    hadUploadFinishFlyTime = false;
                } else {
                    String err = String.format(getResources().getString(R.string.Mission_Start_Failed), error.getDescription());
                    hadStartedWaypointMission(false, err);
                    setResultToToast(err);
                }
            });
        } else {
            String err = String.format(getResources().getString(R.string.Mission_Start_Failed), "WaypointMissionOperator is null");
            hadStartedWaypointMission(false, err);
            setResultToToast(err);
        }

    }

    private void updateUAVStatus(int status) {
        SharedPreferenceUtils sp = new SharedPreferenceUtils(DJIActivity.this);
        String uavId = Globle.serialNumber;
        String customerCode = sp.getSaveStringData(Constants.KEY_CUSTOMER_CODE, "");
        String userId = sp.getSaveStringData(Constants.KEY_USERID, "");
        String url = String.format(Constants.URL_UPDATE_STATUS, userId, customerCode,uavId,status);
    }

    /**
     * 停止任务
     */
    protected void stopWaypointMission() {
        if (getWaypointMissionOperator() != null) {
            getWaypointMissionOperator().stopMission((DJIError error) -> {
                if (error == null) {
                    String msg = getResources().getString(R.string.Mission_Stop_Success);
                    setResultToToast(msg);
                    hadStopedWaypointMission(true, msg);
                } else {
                    String err = String.format(getResources().getString(R.string.Mission_Stop_Failed), error.getDescription());
                    hadStopedWaypointMission(false, err);
                    setResultToToast(err);
                }
            });
        } else {
            String err = String.format(getResources().getString(R.string.Mission_Stop_Failed), "WaypointMissionOperator is null");
            hadStopedWaypointMission(false, err);
            setResultToToast(err);
        }

    }

    /**
     * 暂停任务
     */
    protected void pauseWaypointMission() {
        if (getWaypointMissionOperator() != null) {
            getWaypointMissionOperator().pauseMission((DJIError error) -> {
                if (error == null) {
                    String msg = getResources().getString(R.string.Mission_Pause_Success);
                    hadPauseWaypointMission(true, msg);
                    setResultToToast(msg);
                } else {
                    String err = String.format(getResources().getString(R.string.Mission_Pause_Failed), error.getDescription());
                    hadPauseWaypointMission(false, err);
                    setResultToToast(err);
                }

            });
        } else {
            String err = String.format(getResources().getString(R.string.Mission_Pause_Failed), "WaypointMissionOperator is null");
            hadPauseWaypointMission(false, err);
            setResultToToast(err);
        }

    }

    /**
     * 继续任务
     */
    protected void resumeWaypointMission() {
        if (getWaypointMissionOperator() != null) {
            getWaypointMissionOperator().resumeMission((DJIError error) -> {
                if (error == null) {
                    String msg = getResources().getString(R.string.Mission_Resume_Success);
                    hadResumeWaypointMission(true, msg);
                    setResultToToast(msg);
                } else {
                    String err = String.format(getResources().getString(R.string.Mission_Resume_Failed), error.getDescription());
                    hadResumeWaypointMission(false, err);
                    setResultToToast(err);
                }

            });
        } else {
            String err = String.format(getResources().getString(R.string.Mission_Resume_Failed), "WaypointMissionOperator is null");
            hadResumeWaypointMission(false, err);
            setResultToToast(err);
        }

    }

    private LatLng transLatLng(LatLng l, boolean isGcj02) {
        double lat = l.latitude;
        double lng = l.longitude;
//        double[] ll2 = CoordinateTransformUtil.gcj02towgs84(lng, lat);
//        lat = ll2[1];
//        lng = ll2[0];
        return new LatLng(lat, lng);
    }

    private List<LatLng> transLatLngs(List<LatLng> ls, boolean isGcj02) {
        List<LatLng> r = new ArrayList<>();
        for (LatLng l : ls) {
            r.add(transLatLng(l, isGcj02));
        }
        return r;
    }

    private int getHeading(List<LatLng> ls) {
        if (ls == null || ls.size() < 2) {
            return 0;
        }
        double heading = SphericalUtil.computeHeading(ls.get(0), ls.get(1));
        return (int) heading;
    }

    public void SyncWayPoint(float altitude) {
        if (mFlightController == null || !mFlightController.isConnected()) {
            return;
        }
        int nMissionType = MissionConfig.getInstance().getMissionType();
        if (nMissionType == 1) {
            setResultToToast(getResources().getString(R.string.task_type) + "航线");
        } else if (nMissionType == 2) {
            setResultToToast(getResources().getString(R.string.task_type) + "跳棋");
        } else {
            setResultToToast(getResources().getString(R.string.task_type) + "航点");
        }
//        ToastUtils.setResultToToast("useContinuePoint "+useContinuePoint);
        waypointList.clear();
        if (nMissionType == 1) {
            int i = 0;
            List<LatLng> missionList;
            if (useContinuePoint) {
                missionList = areaUtils.getContinueAirwayPoints();
            } else {
                missionList = areaUtils.getAirwayPoints();
//                ToastUtils.setResultToToast(useContinuePoint+"   "+missionList.size());
            }
            genWayPoint(altitude, missionList);
        }
    }

    class tAction implements Trigger.Action {
        @Override
        public void onCall(){
            ApplicationFactory.getCameraInstance().startShootPhoto(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if(null==djiError){
                        Log.d("tAction", "航点拍照成功.");
                    }else{
                        Log.d("tAction", "航点拍照失败."+djiError.getDescription());
                    }
                }
            });
        }
    }

    public void genWayPoint(float altitude, List<LatLng> missionList) {
        if (mFlightController == null || !mFlightController.isConnected()) {
            return;
        }
//        ToastUtils.setResultToToast("useContinuePoint "+useContinuePoint);
        waypointList.clear();
        int i = 0;
        List<LatLng> ls = transLatLngs(missionList, Globle.isGCJ02);
        int heading = getHeading(missionList);
        setResultToToast(getResources().getString(R.string.flight_heading) + heading);
        for (LatLng point : ls) {
            if(i>=90){
                break; //大疆的限制是99个航点，稳妥起见，我们限制为95个航点
            }
            Waypoint mWaypoint = new Waypoint(point.latitude, point.longitude, altitude);
            WaypointReachedTrigger t = new WaypointReachedTrigger();
            t.setWaypointIndex(i);
            t.setAction(new tAction());
            //mWaypoint.heading = heading;//航向正北
            System.out.println("进入航线之前");
            List<Gimbal> gimbals = ApplicationFactory.getGimbals();
            if (null != gimbals) {
                for (Gimbal gimbal : gimbals) {
                    System.out.println("gimbalName:" + gimbal.getDisplayName());
                }
            }
            if (null!= gimbals) {//手机控制无人机每隔2s自动拍照一次
                //添加拍照任务开始
                if (i == 0) {
                    for (Gimbal gimbal : gimbals) { //TODO 旋转相机的工作应该放在任务开始前，不是这里，以后再调吧，因为还要考虑回调延时
                        if (null != gimbal) {
                            if (!"Gimbal DJI_X_PORT".equals(gimbal.getDisplayName())) {
                                Rotation.Builder builder = new Rotation.Builder().mode(RotationMode.ABSOLUTE_ANGLE).time(2);
                                builder.pitch(-90f);
                                gimbal.rotate(builder.build(), new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {

                                    }
                                });
                            }
                        }
                    }
                }
//                    mWaypoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 0));
                /*if (i == missionList.size() - 1) {
                    mWaypoint.addAction(new WaypointAction(WaypointActionType.GIMBAL_PITCH, 0));
                }*/
            }
            i++;
            waypointList.add(mWaypoint);
        }
        // 把路径点添加到路径队列
        if (waypointMissionBuilder != null) {
            waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
        } else {
            waypointMissionBuilder = new WaypointMission.Builder();
            waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
        }
    }

    private Gimbal gimbal = null;
    private int currentGimbalId = 0;
    private Gimbal getGimbalInstance() {
        if (gimbal == null) {
            initGimbal();
        }
        return gimbal;
    }
    private void initGimbal() {
        if (DJISDKManager.getInstance() != null) {
            BaseProduct product = DJISDKManager.getInstance().getProduct();
            if (product != null) {
                if (product instanceof Aircraft) {
                    gimbal = ((Aircraft) product).getGimbals().get(currentGimbalId);
                } else {
                    gimbal = product.getGimbal();
                }
            }
        }
    }
    private void setCameraShootPhotoMode() {
        if (null != ApplicationFactory.getCameraInstance()) {
            ApplicationFactory.getCameraInstance().setMode(SettingsDefinitions.CameraMode.SHOOT_PHOTO, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError mError) {
                    if (mError != null) {
                        setResultToToast(String.format(getResources().getString(R.string.Set_CMode_Failed), mError.getDescription()));
                    }
                }
            });
        } else {
            setResultToToast(String.format(getResources().getString(R.string.Set_CMode_Failed), getResources().getString(R.string.camera_disconnected)));
        }
    }

    /**
     * 开始航线任务定时拍照
     * 每2s拍一张
     */
    private void startShootPhoto() {
        if (ApplicationFactory.getCameraInstance() == null) {
            setResultToToast(String.format(getResources().getString(R.string.Start_SP_Failed), getResources().getString(R.string.camera_disconnected)));
            return;
        }
        ApplicationFactory.getCameraInstance().getShootPhotoMode(new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.ShootPhotoMode>() {
            @Override
            public void onSuccess(SettingsDefinitions.ShootPhotoMode shootPhotoMode) {
                if (shootPhotoMode == SettingsDefinitions.ShootPhotoMode.INTERVAL) {
                    ApplicationFactory.getCameraInstance().startShootPhoto((DJIError djiError)-> {
                                if (djiError == null) {
                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    Date curDate = new Date(System.currentTimeMillis());
                                    String createTime = formatter.format(curDate);
                                    setResultToToast(getResources().getString(R.string.Start_SP_Success) + createTime);
                                } else {
                                    setResultToToast(String.format(getResources().getString(R.string.Start_SP_Failed), djiError.getDescription()));
                                }
                            }
                    );
                } else {
                    setResultToToast(String.format(getResources().getString(R.string.Start_SP_Failed), getResources().getString(R.string.SPMode_Error)));
                }
            }

            @Override
            public void onFailure(DJIError djiError) {
                setResultToToast(String.format(getResources().getString(R.string.Start_SP_Failed), djiError.getDescription()));
            }
        });

    }

    /**
     * 结束航线任务定时拍照
     */
    private void stopShootPhoto() {
        if (ApplicationFactory.getCameraInstance() == null) {
            setResultToToast(String.format(getResources().getString(R.string.Stop_SP_Failed), getResources().getString(R.string.camera_disconnected)));
            return;
        }
        ApplicationFactory.getCameraInstance().stopShootPhoto((DJIError djiError) -> {
                    if (djiError == null) {
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date curDate = new Date(System.currentTimeMillis());
                        String createTime = formatter.format(curDate);
                        setResultToToast(getResources().getString(R.string.Stop_SP_Success) + createTime);
                    } else {
                        setResultToToast(String.format(getResources().getString(R.string.Stop_SP_Failed), djiError.getDescription()));
                    }
                    //将拍照模式切换回单张拍照模式
                    setCameraShootPhotoMode();
//                MainApplication.getCameraInstance().setShootPhotoMode(SettingsDefinitions.ShootPhotoMode.SINGLE, new CommonCallbacks.CompletionCallback() {
//                    @Override
//                    public void onResult(DJIError djiError) {
//                        if (djiError==null){
//                            setResultToToast("Set ShootPhotoMode success");
//                        }else{
//                            setResultToToast("Set ShootPhotoMode failed "+djiError.getDescription());
//                        }
//                    }
//                });
                }
        );

    }

    /**
     * 定时间隔拍照功能
     */
    private void setPhotoTimeIntervalSettings() {
        if (ApplicationFactory.getCameraInstance() == null) {
            setResultToToast(String.format(getResources().getString(R.string.Set_PTIS_Failed), getResources().getString(R.string.camera_disconnected)));
            return;
        }
        ApplicationFactory.getCameraInstance().setPhotoFileFormat(SettingsDefinitions.PhotoFileFormat.JPEG, (DJIError djiError) -> {
                    if (djiError == null) {
                        setResultToToast(getResources().getString(R.string.Set_JPG_Success));
                    } else {
                        setResultToToast(String.format(getResources().getString(R.string.Set_JPG_Failed), djiError.getDescription()));
                    }
                }
        );
        ApplicationFactory.getCameraInstance().setShootPhotoMode(SettingsDefinitions.ShootPhotoMode.INTERVAL, (DJIError error) -> {
                    if (error == null) {
                        setResultToToast(getResources().getString(R.string.Set_SPMode_Success));
                        //TODO 设置拍照间隔
                        int photoInterval = MissionConfig.getInstance().getPhotoInterval();
                        SettingsDefinitions.PhotoTimeIntervalSettings ptis = new SettingsDefinitions.PhotoTimeIntervalSettings(255, photoInterval);

                        ApplicationFactory.getCameraInstance().setPhotoTimeIntervalSettings(ptis, new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (error == null) {
                                    setResultToToast(getResources().getString(R.string.Set_PTIS_Success));
                                } else {
                                    setResultToToast(String.format(getResources().getString(R.string.Set_PTIS_Failed), error.getDescription()));
                                }
                            }
                        });
                    } else {
                        setResultToToast(String.format(getResources().getString(R.string.Set_SPMode_Failed), error.getDescription()));
                    }
                }
        );
    }

    //御
    public boolean isMavicProduct() {
        BaseProduct baseProduct = ApplicationFactory.getProductInstance();
        if (baseProduct != null) {
            Model model = baseProduct.getModel();
            return model == Model.MAVIC_AIR
                    || model == Model.MAVIC_2_PRO || model == Model.MAVIC_2_ZOOM
                    || model == Model.MAVIC_2 || model == Model.MAVIC_2_ENTERPRISE
                    || model == Model.MAVIC_2_ENTERPRISE_DUAL
                    || model == Model.MAVIC_PRO;
        }
        return false;
    }
    //精灵系列
    public boolean isPhantom() {
        BaseProduct baseProduct = ApplicationFactory.getProductInstance();
        if (baseProduct != null) {
            Model model = baseProduct.getModel();
            return model == Model.Phantom_3_4K
                    || model == Model.PHANTOM_3_ADVANCED || model == Model.PHANTOM_3_PROFESSIONAL
                    || model == Model.PHANTOM_3_STANDARD || model == Model.PHANTOM_4
                    || model == Model.PHANTOM_4_ADVANCED|| model == Model.PHANTOM_4_PRO
                    || model == Model.PHANTOM_4_PRO_V2|| model == Model.PHANTOM_4_RTK
                    ||model==Model.P_4_MULTISPECTRAL;
        }
        return false;
    }
    //MATRICE系列
    public boolean isMatricProduct() {
        BaseProduct baseProduct = ApplicationFactory.getProductInstance();
        if (baseProduct != null) {
            Model model = baseProduct.getModel();
            return model == Model.MATRICE_100
                    || model == Model.MATRICE_200 || model == Model.MATRICE_200_V2
                    || model == Model.MATRICE_210 || model == Model.MATRICE_210_RTK
                    || model == Model.MATRICE_210_RTK_V2|| model == Model.MATRICE_210_V2
                    || model == Model.MATRICE_300_RTK|| model == Model.MATRICE_600|| model == Model.MATRICE_600_PRO;
        }
        return false;
    }

    void startLiveShow() {
        if(!Globle.isPushVideo){
            ToastUtils.setResultToToast("视频流推送开关未打开.");
            return;
        }
        if ("".equals(liveShowUrl)) {
            ToastUtils.setResultToToast(getResources().getString(R.string.config_exception));
            return;
        }
        ToastUtils.setResultToToast("开始直播"+liveShowUrl);
        if (!isLiveStreamManagerOn()) {
            return;
        }
        if (DJISDKManager.getInstance().getLiveStreamManager().isStreaming()) {
            ToastUtils.setResultToToast("直播已开始");
            return;
        }
        new Thread() {
            @Override
            public void run() {
                DJISDKManager.getInstance().getLiveStreamManager().setLiveUrl(liveShowUrl);
                //DJISDKManager.getInstance().getLiveStreamManager().setLiveVideoBitRate();
                DJISDKManager.getInstance().getLiveStreamManager().setLiveVideoBitRateMode(LiveVideoBitRateMode.AUTO);
                int result = DJISDKManager.getInstance().getLiveStreamManager().startStream();
                DJISDKManager.getInstance().getLiveStreamManager().setStartTime();
                ToastUtils.setResultToToast("开始直播:" + result +
                        "\n isVideoStreamSpeedConfigurable:" + DJISDKManager.getInstance().getLiveStreamManager().isVideoStreamSpeedConfigurable() +
                        "\n isLiveAudioEnabled:" + DJISDKManager.getInstance().getLiveStreamManager().isLiveAudioEnabled());
            }
        }.start();
    }

    void stopLiveShow() {
        if (!isLiveStreamManagerOn()) {
            return;
        }
        DJISDKManager.getInstance().getLiveStreamManager().stopStream();
        ToastUtils.setResultToToast("停止直播");
    }

    private boolean isLiveStreamManagerOn() {
        if (DJISDKManager.getInstance().getLiveStreamManager() == null) {
            ToastUtils.setResultToToast("无视频流");
            return false;
        }
        return true;
    }
}
