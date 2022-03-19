package com.cimraffi.android.birdeye.common;

import android.bluetooth.BluetoothDevice;

/**
 * 全局变量类
 */
public class Globle {
    public static BluetoothDevice connectDevice = null;

    public static String cookie = null;

//    public static int userId = -1;

    //飞控序列号
    public static String serialNumber;

    //火星坐标系
    public static boolean isGCJ02 = true;

    //当前处于国内还是国外
    public static boolean isInCN = true;

    //视频流推送开关
    public static boolean isPushVideo = false;

    //飞机实时位置推送开关
    public static boolean isPushLocate = false;
}
