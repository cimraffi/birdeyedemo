package com.cimraffi.android.birdeye.base;

import dji.common.error.DJIError;

public interface Callback_DroneUtil {
    default void ShowDownloadProgressDialog(String info) {};

    default void SetDownloadProgressDialogMsg(String info, double p){};

    default void HideDownloadProgressDialog() {};
}
