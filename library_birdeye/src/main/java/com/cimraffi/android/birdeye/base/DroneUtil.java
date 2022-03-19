package com.cimraffi.android.birdeye.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.exifinterface.media.ExifInterface;
import android.text.TextUtils;
import android.util.Log;

import com.adobe.xmp.XMPMeta;
import com.cimraffi.android.birdeye.ApplicationFactory;
import com.cimraffi.android.birdeye.utils.FileUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.product.Model;
import dji.sdk.base.BaseProduct;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;


public class DroneUtil {

    protected static final String TAG = DroneUtil.class.getSimpleName();

    private Context mContext;
    private static DroneUtil u = null;

    public static DroneUtil getInstance() {
        if (null == u) {
            u = new DroneUtil(ApplicationFactory.a);
            return u;
        } else {
            return DroneUtil.u;
        }
    }

    //无人机坐标
    protected double droneLocationLat = 181, droneLocationLng = 181;
    //无人机机头朝向
    protected float droneHeading = 0.0f;

    public ArrayList<String> logs = new ArrayList<>();

    public boolean isStopSendPhoto() {
        return stopSendPhoto;
    }

    public void setStopSendPhoto(boolean stopSendPhoto) {
        this.stopSendPhoto = stopSendPhoto;
    }

    private boolean stopSendPhoto = false;

    public String getTask_code() {
        return task_code;
    }

    public void setTask_code(String task_code) {
        this.task_code = task_code;
    }

    private String task_code;

    public DroneUtil(Context c) {
        this.mContext = c;
    }

    public void doFinish() {
        //Activity生命周期结束，做回收工作
        EventBus.getDefault().unregister(this);
    }

    private Callback_DroneUtil droneCallback = null;

    public void setDroneCallback(Callback_DroneUtil droneCallback) {
        this.droneCallback = droneCallback;
    }

    private void initCameraManager() {
        BaseProduct product = ApplicationFactory.getProductInstance();
        setCameraShootPhotoMode(new DroneCallback_setCameraShootPhotoMode() {
            @Override
            public void onFailure(String error) {

            }

            @Override
            public void onSuccess() {

            }
        });
    }


    //todo 与无人机的连接发生变化时需要做的工作
    public void workWhenConnectChange() {

    }
    /*
     * DJI设置结束
     * **************************************************
     * */

    private void setCameraShootPhotoMode(DroneCallback_setCameraShootPhotoMode c) {
        if (null != ApplicationFactory.getCameraInstance()) {
            ApplicationFactory.getCameraInstance().setMode(SettingsDefinitions.CameraMode.SHOOT_PHOTO, (DJIError mError) -> {
                if (mError != null) {
                    c.onFailure(mError.getDescription());
                } else {
                    c.onSuccess();
                }
            });
        } else {
            c.onFailure("相机未连接.");
        }
    }

    public interface DroneCallback_setCameraShootPhotoMode {
        void onFailure(String error);

        void onSuccess();
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

    public void configAirway() {
    }

    /**
     * 日志列表追加文本日志
     *
     * @param msg
     */
    protected void appendLog(String msg) {
        if (logs == null) {
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
        appendLog(string);
        Log.d("MapActivity", string);
    }

    private Bitmap getimage(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);//此时返回bm为空
        newOpts.inJustDecodeBounds = false;
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 2;//be=1表示不缩放
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        //return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
        return bitmap;
    }

    public void saveExif(String oldFilePath, String newFilePath) throws Exception {
        ExifInterface oldExif = new ExifInterface(oldFilePath);
        ExifInterface newExif = new ExifInterface(newFilePath);
        Class<ExifInterface> cls = ExifInterface.class;
        Field[] fields = cls.getFields();
        for (int i = 0; i < fields.length; i++) {
            String fieldName = fields[i].getName();
            if (!TextUtils.isEmpty(fieldName) && fieldName.startsWith("TAG")) {
                String fieldValue = fields[i].get(cls).toString();
                String attribute = oldExif.getAttribute(fieldValue);

                if (attribute != null) {
                    //if (!fieldValue.equals("GPSLatitude") && !fieldValue.equals("GPSLongitude")){
                    newExif.setAttribute(fieldValue, attribute);
                    //}
                }
            }
        }
        newExif.saveAttributes();
    }
}
