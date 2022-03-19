package com.cimraffi.android.birdeye.utils;
import com.cimraffi.android.birdeye.ApplicationFactory;

import dji.sdk.sdkmanager.DJISDKManager;

public class DJIUtil {

    /**
     * 获取大疆飞机型号
     */
    public static String getDJIModel() {
        String djimodel = "";
        try {
            djimodel = ApplicationFactory.getProductInstance().getModel().getDisplayName();
            return djimodel;
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }

    }

    /**
     * 获取大疆SDK的版本
     */
    public static String getDJIVersion() {
        String version = "";
        try {
            version = DJISDKManager.getInstance().getSDKVersion();
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }
}
