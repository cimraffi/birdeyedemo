package com.cimraffi.android.birdeye.utils;


import androidx.annotation.Nullable;

import com.cimraffi.android.birdeye.ApplicationFactory;

import dji.common.product.Model;
import dji.sdk.accessory.AccessoryAggregation;
import dji.sdk.accessory.beacon.Beacon;
import dji.sdk.accessory.speaker.Speaker;
import dji.sdk.accessory.spotlight.Spotlight;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.flightcontroller.Simulator;
import dji.sdk.products.Aircraft;
import dji.sdk.products.HandHeld;
import dji.sdk.sdkmanager.DJISDKManager;

/**
 * Created by dji on 16/1/6.
 */
public class ModuleVerificationUtil {
    public static boolean isProductModuleAvailable() {
        return (null != ApplicationFactory.getProductInstance());
    }

    public static boolean isAircraft() {
        return ApplicationFactory.getProductInstance() instanceof Aircraft;
    }

    public static boolean isHandHeld() {
        return ApplicationFactory.getProductInstance() instanceof HandHeld;
    }

    public static boolean isCameraModuleAvailable() {
        return isProductModuleAvailable() && (null != ApplicationFactory.getProductInstance().getCamera());
    }

    public static boolean isPlaybackAvailable() {
        return isCameraModuleAvailable() && (null != ApplicationFactory.getProductInstance()
                .getCamera()
                .getPlaybackManager());
    }

    public static boolean isMediaManagerAvailable() {
        return isCameraModuleAvailable() && (null != ApplicationFactory.getProductInstance()
                .getCamera()
                .getMediaManager());
    }

    public static boolean isRemoteControllerAvailable() {
        return isProductModuleAvailable() && isAircraft() && (null != ApplicationFactory.getAircraftInstance()
                .getRemoteController());
    }

    public static boolean isFlightControllerAvailable() {
        return isProductModuleAvailable() && isAircraft() && (null != ApplicationFactory.getAircraftInstance()
                .getFlightController());
    }

    public static boolean isCompassAvailable() {
        return isFlightControllerAvailable() && isAircraft() && (null != ApplicationFactory.getAircraftInstance()
                .getFlightController()
                .getCompass());
    }

    //rtk判断
    public static boolean isRtkAvailable() {
        return isFlightControllerAvailable() && isAircraft()
                && (null != ApplicationFactory.getAircraftInstance()
                .getFlightController().getRTK());
    }

    //网络rtk
    public static boolean isNetRtkAvailable() {
        return isFlightControllerAvailable() && isAircraft() && (null != DJISDKManager.getInstance().getRTKNetworkServiceProvider());
    }

    //基站电池
    public static boolean isBaseStationAvailable() {
        return isFlightControllerAvailable() && isAircraft() && (null != ((Aircraft) ApplicationFactory.getProductInstance()).getBaseStation());
    }

    public static boolean isFlightLimitationAvailable() {
        return isFlightControllerAvailable() && isAircraft();
    }

    public static boolean isGimbalModuleAvailable() {
        return isProductModuleAvailable() && (null != ApplicationFactory.getProductInstance().getGimbal());
    }

    public static boolean isAirlinkAvailable() {
        return isProductModuleAvailable() && (null != ApplicationFactory.getProductInstance().getAirLink());
    }

    public static boolean isWiFiLinkAvailable() {
        return isAirlinkAvailable() && (null != ApplicationFactory.getProductInstance().getAirLink().getWiFiLink());
    }

    public static boolean isLightbridgeLinkAvailable() {
        return isAirlinkAvailable() && (null != ApplicationFactory.getProductInstance()
                .getAirLink()
                .getLightbridgeLink());
    }

    public static AccessoryAggregation getAccessoryAggregation() {
        Aircraft aircraft = (Aircraft) ApplicationFactory.getProductInstance();

        if (aircraft != null && null != aircraft.getAccessoryAggregation()) {
            return aircraft.getAccessoryAggregation();
        }
        return null;
    }

    public static Speaker getSpeaker() {
        Aircraft aircraft = (Aircraft) ApplicationFactory.getProductInstance();

        if (aircraft != null && null != aircraft.getAccessoryAggregation() && null != aircraft.getAccessoryAggregation().getSpeaker()) {
            return aircraft.getAccessoryAggregation().getSpeaker();
        }
        return null;
    }

    public static Beacon getBeacon() {
        Aircraft aircraft = (Aircraft) ApplicationFactory.getProductInstance();

        if (aircraft != null && null != aircraft.getAccessoryAggregation() && null != aircraft.getAccessoryAggregation().getBeacon()) {
            return aircraft.getAccessoryAggregation().getBeacon();
        }
        return null;
    }

    public static Spotlight getSpotlight() {
        Aircraft aircraft = (Aircraft) ApplicationFactory.getProductInstance();

        if (aircraft != null && null != aircraft.getAccessoryAggregation() && null != aircraft.getAccessoryAggregation().getSpotlight()) {
            return aircraft.getAccessoryAggregation().getSpotlight();
        }
        return null;
    }

    @Nullable
    public static Simulator getSimulator() {
        Aircraft aircraft = ApplicationFactory.getAircraftInstance();
        if (aircraft != null) {
            FlightController flightController = aircraft.getFlightController();
            if (flightController != null) {
                return flightController.getSimulator();
            }
        }
        return null;
    }

    @Nullable
    public static FlightController getFlightController() {
        Aircraft aircraft = ApplicationFactory.getAircraftInstance();
        if (aircraft != null) {
            return aircraft.getFlightController();
        }
        return null;
    }

    @Nullable
    public static boolean isMavic2Product() {
        BaseProduct baseProduct = ApplicationFactory.getProductInstance();
        if (baseProduct != null) {
            return baseProduct.getModel() == Model.MAVIC_2_PRO || baseProduct.getModel() == Model.MAVIC_2_ZOOM;
        }
        return false;
    }

}
