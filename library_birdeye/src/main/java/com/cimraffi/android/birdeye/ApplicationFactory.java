package com.cimraffi.android.birdeye;

import android.app.Application;
import android.content.Context;
import androidx.multidex.MultiDex;

import com.cimraffi.android.birdeye.utils.CrashHandler;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.products.Aircraft;
import dji.sdk.products.HandHeld;
import dji.sdk.sdkmanager.DJISDKManager;

public class ApplicationFactory {
    public static final String TAG = ApplicationFactory.class.getName();

    private static ApplicationFactory app = null;
    public static Context c;
    public static Application a;
    public static boolean installMultiDex = true;

    public Context getBaseContext(){
        return c;
    }
    public Application getBaseApplication(){
        return a;
    }

    public static void notifyStatusChange(ConnectivityChangeEvent event) {
        EventBus.getDefault().post(event);
    }

    public static synchronized BaseProduct getProductInstance() {
        return DJISDKManager.getInstance().getProduct();
    }

    public static boolean isAircraftConnected() {
        return getProductInstance() != null && getProductInstance() instanceof Aircraft;
    }

    public static boolean isHandHeldConnected() {
        return getProductInstance() != null && getProductInstance() instanceof HandHeld;
    }

    public static synchronized Aircraft getAircraftInstance() {
        if (!isAircraftConnected()) {
            return null;
        }
        return (Aircraft) getProductInstance();
    }

    public static synchronized HandHeld getHandHeldInstance() {
        if (!isHandHeldConnected()) {
            return null;
        }
        return (HandHeld) getProductInstance();
    }

    public static boolean isRemoteControllerAvailable() {
        return isAircraftConnected() && (null != getAircraftInstance()
                .getRemoteController());
    }

    public ApplicationFactory(){
        if(installMultiDex){MultiDex.install(c);}
        com.secneo.sdk.Helper.install(a);
        CrashHandler.getInstance().init(c);
    }

    public static ApplicationFactory getInstance() {
        if (null == app){
            app = new ApplicationFactory();
            return app;
        }else{
            return ApplicationFactory.app;
        }
    }

    public enum ConnectivityChangeEvent {
        StatusChange,
        ProductConnected,
        ProductDisconnected,
        CameraConnect,
        CameraDisconnect,
    }

    public static synchronized Camera getCameraInstance() {

        if (getProductInstance() == null) return null;

        Camera camera = null;

        if (getProductInstance() instanceof Aircraft) {
            camera = ((Aircraft) getProductInstance()).getCamera();

        } else if (getProductInstance() instanceof HandHeld) {
            camera = ((HandHeld) getProductInstance()).getCamera();
        }

        return camera;
    }

    public static synchronized List<Gimbal> getGimbals() {

        if (getProductInstance() == null) return null;

        List<Gimbal> gimbals = ((Aircraft) getProductInstance()).getGimbals();

        return gimbals;
    }

    public static boolean isProductModuleAvailable() {
        return (null != getProductInstance());
    }

    public static boolean isCameraModuleAvailable() {
        return isProductModuleAvailable() &&
                (null != getProductInstance().getCamera());
    }

    public static boolean isPlaybackAvailable() {
        return isCameraModuleAvailable() &&
                (null != getProductInstance().getCamera().getPlaybackManager());
    }
}
