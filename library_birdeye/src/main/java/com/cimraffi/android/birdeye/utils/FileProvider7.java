package com.cimraffi.android.birdeye.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import androidx.core.content.FileProvider;

import java.io.File;

/**
 * 对于面向 Android 7.0 的应用，Android 框架执行的 StrictMode API 政策禁止在您的应用外部公开 file:// URI。
 * 如果一项包含文件 URI 的 intent 离开你的应用，则应用出现故障，并出现 FileUriExposedException 异常。
 * 要在应用间共享文件，需发送一项 content:// URI，并授予 URI 临时访问权限。
 * 进行此授权的最简单方式是使用 FileProvider 类。
 *
 * https://developer.android.com/about/versions/nougat/android-7.0-changes
 * Created by ZhangGuanjun on 2018/9/24.
 */
public class FileProvider7 {

    public static Uri getUriForFile(Context context, File file) {
        Uri fileUri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            fileUri = getUriForFile24(context, file);
        } else {
            fileUri = Uri.fromFile(file);
        }
        return fileUri;
    }

    /**
     * 清单文件中provider节点下的authorities，需和context.getPackageName() + ".android7.fileprovider"保持一致
     */
    public static Uri getUriForFile24(Context context, File file) {
        Uri fileUri = FileProvider.getUriForFile(context,
                /*BuildConfig.APPLICATION_ID +*/ ".android7.fileprovider",
                file);
        return fileUri;
    }

    public static void setIntentDataAndType(Context context,
                                            Intent intent,
                                            String type,
                                            File file,
                                            boolean writeAble) {
        if (Build.VERSION.SDK_INT >= 24) {
            intent.setDataAndType(getUriForFile(context, file), type);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (writeAble) {
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        } else {
            intent.setDataAndType(Uri.fromFile(file), type);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
    }
}