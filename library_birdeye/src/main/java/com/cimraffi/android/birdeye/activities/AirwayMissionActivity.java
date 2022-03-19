package com.cimraffi.android.birdeye.activities;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;

import com.cimraffi.android.birdeye.base.DJIActivity;
import com.cimraffi.android.birdeye.bean.ContinuePoint;
import com.cimraffi.android.birdeye.bean.MissionConfig;
import com.cimraffi.android.birdeye.dbex.SomeLab;
import com.cimraffi.android.birdeye.dialog.CustomDialog;
import com.cimraffi.android.birdeye.dialog.DialogUtil;
import com.cimraffi.android.birdeye.dialog.LoadingDialog;
import com.cimraffi.android.birdeye.utils.ToastUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.cimraffi.android.birdeye.R;

public class AirwayMissionActivity extends DJIActivity
        implements View.OnClickListener {

    protected static final String TAG = "AirwayMissionActivity";
    /**
     * 开启任务确认弹框
     */
    private CustomDialog startMissionDialog;
    private LoadingDialog loadingDialog;
    @Override
    public void onClick(View v) {
        int id = v.getId();
        /*if (id == R.id.locate) {
            if (isProductConnected) {
                if (checkGpsCoordination(droneLocationLat, droneLocationLng)) {
                    updateDroneLocation(droneLocationLat, droneLocationLng, droneHeading);
                    cameraUpdate2(droneLocationLat, droneLocationLng); // Locate the drone's place
                    setHome(new LatLng(droneLocationLat, droneLocationLng));
                } else {
                    setResultToToast(getResources().getString(R.string.tip_nodronelocation));
                }
            }
        } else*/ if (id == R.id.start) {
            if (tvStart.getText().toString().equals(getResources().getString(R.string.Start))) {
                ToastUtils.setResultToToast("拍照间隔:"+ MissionConfig.getInstance().getPhotoInterval());
                if (isFlying()) {
                    ToastUtils.setResultToToast("飞机正在飞行");
                }else {
                    showLoading();
                    showCheckFlightMode();
                }
            } else if (tvStart.getText().toString().equals(getResources().getString(R.string.Stop))) {
                showStopMissionConfirmDialog();
            } else {
                super.onClick(v);
            }
        } else if (id == R.id.pause) {
            if (tvPause.getText().toString().equals(getResources().getString(R.string.Pause))) {
                showPauseMissionConfirmDialog(true);
            } else if (tvPause.getText().toString().equals(getResources().getString(R.string.Resume))) {
                showPauseMissionConfirmDialog(false);
            } else {
                super.onClick(v);
            }
        } else {
            super.onClick(v);
        }
    }

    private void showLoading() {
        loadingDialog = DialogUtil.showLoadingDialog(this, "正在上传任务信息");
    }

    private void dismissLoading() {
        try {
            loadingDialog.dismiss();
            loadingDialog = null;
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 显示结束任务确认弹框
     */
    private void showStopMissionConfirmDialog() {
        CustomDialog stopDialog = new CustomDialog(this);
        stopDialog.setMessage(getResources().getString(R.string.confirm_stop_mission));
        stopDialog.addButton(getResources().getString(R.string.cancle), getResources().getString(R.string.stop), new CustomDialog.DialogButtonClickListener() {
            @Override
            public void leftClick() {
            }

            @Override
            public void rightClic() {
                stopWaypointMission();
            }
        });
        stopDialog.show();
    }

    private void showPauseMissionConfirmDialog(boolean isP) {
        String msg;
        if (isP) {
            msg = getResources().getString(R.string.confirm_pause_mission);
        } else {
            msg = getResources().getString(R.string.confirm_resume_mission);
        }
        CustomDialog dialog = new CustomDialog(this);
        dialog.setMessage(msg);
        dialog.addButton(getResources().getString(R.string.cancle), getResources().getString(R.string.confirm), new CustomDialog.DialogButtonClickListener() {
            @Override
            public void leftClick() {
            }

            @Override
            public void rightClic() {
                if (isP) {
                    pauseWaypointMission();
                } else {
                    resumeWaypointMission();
                }
            }
        });
        dialog.show();
    }

    @Override
    public void hadNotUploadedWaypointMisssion(String err) {
        ToastUtils.setResultToToast(err);
        startMissionDialog.dismiss();
    }

    public void hadStartedWaypointMission(boolean result, String errr) {
        if (result) {
            runOnUiThread(() -> {
                areaUtils.setFinishAirway(false);
                isStart = true;

                tvStart.setText(getResources().getString(R.string.Stop));
                Drawable stopLeft = getResources().getDrawable(R.mipmap.ic_stop);
                stopLeft.setBounds(0, 0, stopLeft.getMinimumWidth(), stopLeft.getMinimumHeight());
                tvStart.setCompoundDrawables(stopLeft, null, null, null);

                tvPause.setText(getResources().getString(R.string.Pause));
                Drawable pauseLeft = getResources().getDrawable(R.mipmap.ic_pause);
                pauseLeft.setBounds(0, 0, pauseLeft.getMinimumWidth(), pauseLeft.getMinimumHeight());
                tvPause.setCompoundDrawables(pauseLeft, null, null, null);
            });
        } else {
            ToastUtils.setResultToToast(errr);
            hadStopedWaypointMission(true, "");
        }
    }

    @Override
    public void hadStopedWaypointMission(boolean isOk, String err) {
        runOnUiThread(() -> {
            if (isOk) {
                isStart = false;
                tvStart.setText(getResources().getString(R.string.Start));
                Drawable startLeft = getResources().getDrawable(R.mipmap.ic_start);
                startLeft.setBounds(0, 0, startLeft.getMinimumWidth(), startLeft.getMinimumHeight());
                tvStart.setCompoundDrawables(startLeft, null, null, null);
            } else {
                ToastUtils.setResultToToast("");
            }
        });
    }

    @Override
    public void hadPauseWaypointMission(boolean isOk, String err) {
        runOnUiThread(() -> {
            if (isOk) {
                tvPause.setText(getResources().getString(R.string.Resume));
                Drawable resumeLeft = getResources().getDrawable(R.mipmap.ic_start);
                resumeLeft.setBounds(0, 0, resumeLeft.getMinimumWidth(), resumeLeft.getMinimumHeight());
                tvPause.setCompoundDrawables(resumeLeft, null, null, null);
            } else {
                ToastUtils.setResultToToast(err);
            }
        });
    }

    @Override
    public void hadResumeWaypointMission(boolean isOk, String err) {
        runOnUiThread(() -> {
            if (isOk) {
                tvPause.setText(getResources().getString(R.string.Pause));
                Drawable pauseLeft = getResources().getDrawable(R.mipmap.ic_pause);
                pauseLeft.setBounds(0, 0, pauseLeft.getMinimumWidth(), pauseLeft.getMinimumHeight());
                tvPause.setCompoundDrawables(pauseLeft, null, null, null);
            } else {
                ToastUtils.setResultToToast(err);
            }
        });
    }

    /**
     * 检查遥控挡位
     */
    private void showCheckFlightMode() {
        if (!TextUtils.isEmpty(mMode)
                && (mMode.equalsIgnoreCase("P")
                    || mMode.equalsIgnoreCase("F"))) {
            if (areaUtils != null) {

                //安全检查，判断最后一个航点是否距离起飞点少于20M
                safeCheck(areaUtils.last_point, (boolean b)->{
                    if (b){
                        CustomDialog dialog = new CustomDialog(this);
                        dialog.setMessage("最后一个任务点距离起飞点距离小于25米，建议重新设置，是否继续起飞？");
                        dialog.addButton("取消","确认", new CustomDialog.DialogButtonClickListener() {
                            @Override
                            public void leftClick() {
                            }

                            @Override
                            public void rightClic() {
                                showUseContinuePointDialog();
                            }
                        });
                        dialog.show();
                    }else{
                        showUseContinuePointDialog();
                    }
                });
            }
        } else {
            CustomDialog dialog = new CustomDialog(this);
            dialog.setMessage(getResources().getString(R.string.flightmodeerror));
            dialog.addButton(getResources().getString(R.string.confirm),(View view) -> { });
            dialog.show();
            dismissLoading();
        }
    }

    /**
     * 显示使用续航点弹框
     */
    private void showUseContinuePointDialog() {
        if (areaUtils.hasContinuePoint()) {
            CustomDialog dialog = new CustomDialog(this);
            dialog.setMessage(getResources().getString(R.string.use_continuepoint));
            dialog.addButton(getResources().getString(R.string.no_use), getResources().getString(R.string.use), new CustomDialog.DialogButtonClickListener() {
                @Override
                public void leftClick() {
                    useContinuePoint = false;
                    showStartMissionConfirmDialog();
                }

                @Override
                public void rightClic() {
                    useContinuePoint = true;
                    showStartMissionConfirmDialog();
                }
            });
            dialog.show();
        }else{
            useContinuePoint = false;
            showStartMissionConfirmDialog();
        }
    }

    /**
     * 显示开始任务确认弹框
     */
    private void showStartMissionConfirmDialog() {
        genTaskCode(); //准备起飞，生成任务码
        //MissionConfig.getInstance().setCameraContorl(false);
        //监测时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        String monitorTime = sdf.format(curDate);
        areaUtils.setMonitorTime(monitorTime);
        MissionConfig.getInstance().setMonitorTime(monitorTime);
        if (useContinuePoint) {
            SomeLab al = SomeLab.get(getBaseContext());
            ContinuePoint continuePoint = al.getContinuePoint(areaUtils.getAreaID());
            MissionConfig.getInstance().setMonitorTime(continuePoint.monitorTime);
            areaUtils.setMonitorTime(continuePoint.monitorTime);
        }

        startMissionDialog = new CustomDialog(this);
        /*startMissionDialog.setTitle(getResources().getString(R.string.start_mission));
        startMissionDialog.setMessage(getResources().getString(R.string.upload_mission_config));
        // Access the button and set it to invisible
        startMissionDialog.setRightButtonClickable(false);*/
        configeMission();
    }

    public void hadUploadedWaypointMission() {
        runOnUiThread(() -> {
            Handler handler = new Handler();
//            startMissionDialog = new CustomDialog(this);
            startMissionDialog.setRightButtonClickable(false);
            startMissionDialog.setTitle(getResources().getString(R.string.start_mission));
            startMissionDialog.setMessage(getResources().getString(R.string.upload_finish));
            startMissionDialog.addButton(getResources().getString(R.string.cancle), getResources().getString(R.string.confirm), new CustomDialog.DialogButtonClickListener() {
                @Override
                public void leftClick() {
                    dismissLoading();
                }

                @Override
                public void rightClic() {
                    dismissLoading();
                    startWaypointMission();
                }
            });
            startMissionDialog.show();
            // 撤离到安全区域
            handler.postDelayed(() -> {
                startMissionDialog.setRightButtonClickable(true);
            }, 5000);
        });
    }

    @Override
    public void onBackPressed() {
        if(isFlying()){
            CustomDialog c = new CustomDialog(this);
            c.setTitle("请确认");
            c.setMessage("飞机正在飞行，您退出飞行界面可能会导致数据丢失，确认退出飞行界面嘛？");
            c.addButton(getResources().getString(R.string.cancle), getResources().getString(R.string.confirm), new CustomDialog.DialogButtonClickListener() {
                @Override
                public void leftClick() {
                }

                @Override
                public void rightClic() {
                    finish();
                }
            });
            c.show();

        }else{
            super.onBackPressed();
        }
    }

    @Override
    public void updateFlightMode(final String mode) {
        mMode = mode;
        flightModeTV.post(new Runnable() {
            @Override
            public void run() {
                flightModeTV.setText(mode);
            }
        });
    }

    @Override
    public void updateSatelliteCount(final int satelliteCount) {
        gpsNumTV.post(new Runnable() {
            @Override
            public void run() {
                gpsNumTV.setText(String.valueOf(satelliteCount));
            }
        });
    }

    @Override
    public void updateVelocity(final float vh) {
        final DecimalFormat df = new DecimalFormat("##0.0");
        hsTV.post(new Runnable() {
            @Override
            public void run() {
                String svh = df.format(vh);
                hsTV.setText(svh + " m/s");
            }
        });
    }

    @Override
    public void updateAltitude(final float altitude) {
        final DecimalFormat df = new DecimalFormat("##0.0");
        altitudeTV.post(new Runnable() {
            @Override
            public void run() {
                String s_alt = df.format(altitude);
                altitudeTV.setText(s_alt + " m");
            }
        });
    }

    @Override
    public void updateBattery(int voltage, int persent, boolean isFlying) {
        if (persent <= 100) {
            batteryTV.post(() -> {
                batteryTV.setText(persent + "%");
            });
        } else {
            float battery = voltage / 1000f;
            DecimalFormat df = new DecimalFormat("###.##");
            String batteryStr = df.format(battery);
            batteryTV.post(() -> {
                batteryTV.setText(batteryStr + "V");
            });
        }
    }

    public void workWhenConnectChange() {
        flightModeTV.setText("");
        gpsNumTV.setText("");
        hsTV.setText("");
        altitudeTV.setText("");
        batteryTV.setText("");
    }
}
