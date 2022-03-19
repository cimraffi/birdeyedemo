package com.cimraffi.android.birdeye.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.BitmapFactory;
import androidx.core.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;
import com.cimraffi.android.birdeye.R;

/**
 * 向系统发送通知
 * Created by HuangZc on 2017/11/16
 */
public class NotificationBuild {

    public static Notification getNotification(
            Context sContext, PendingIntent pendingIntent, String title, String content, int sBitmap) {
        return getNotification(sContext, null, pendingIntent, null, title, content, sBitmap);
    }

    /**
     * 版本兼容 Notification
     *
     * @param sContext
     * @param contentView
     * @param pendingIntent
     * @param title
     * @param content
     * @return
     */
    public static Notification getNotification(
            Context sContext, RemoteViews contentView,
            PendingIntent pendingIntent, String ticker, String title, String content, int sBitmap) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(sContext);
        if (!TextUtils.isEmpty(ticker)) {
            builder.setTicker(ticker);
        }
        if (!TextUtils.isEmpty(title)) {
            builder.setContentTitle(title);
        }
        if (!TextUtils.isEmpty(content)) {
            builder.setContentText(content);
        }
        if (contentView != null) {
            builder.setContent(contentView);
        }

        builder.setContentIntent(pendingIntent);
        builder.setLargeIcon(BitmapFactory.decodeResource(sContext.getResources(), sBitmap));
        builder.setSmallIcon(R.mipmap.logo);//6.0以上系统显示。

        Notification notification = builder.build();
        notification.icon = R.mipmap.logo;//6.0以下系统显示。
        return notification;
    }
}
