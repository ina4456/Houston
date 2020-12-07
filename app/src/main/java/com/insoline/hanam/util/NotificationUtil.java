package com.insoline.hanam.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.insoline.hanam.R;
import com.insoline.hanam.receiver.NotificationBroadcastReceiver;

public class NotificationUtil
{
    private static long[] vibratorPattern = new long[] {
            0/*바로 시작*/,
            /*패턴 시작*/250, 50, 250/*패턴 끝*/
    };

    private static NotificationManager notificationManager;

    private static void initNotificationManager(Context context)
    {
        if(notificationManager == null)
        {
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
    }

    private static NotificationChannel getNotificationChannel(String channelId, String channelName, int importance)
    {
        NotificationChannel channel = null;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            channel = notificationManager.getNotificationChannel(channelId);
            if(channel == null)
            {
                channel = new NotificationChannel(channelId, channelName, importance);
                channel.enableLights(true);
                channel.enableVibration(true);
                channel.setShowBadge(true);
                channel.setVibrationPattern(vibratorPattern);
                channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

                notificationManager.createNotificationChannel(channel);
            }
        }

        return channel;
    }

    public static void notify(Context context, int type, String title)
    {
        initNotificationManager(context);

        NotificationChannel channel = getNotificationChannel("houston", "taxi", NotificationManager.IMPORTANCE_DEFAULT);

        // 빌더 생성
        NotificationCompat.Builder builder = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            // 오레오(26) 이상. 채널 필요함
            builder = new NotificationCompat.Builder(context, channel.getId());
        }
        else
        {
            builder = new NotificationCompat.Builder(context);
            builder.setVibrate(vibratorPattern);
        }

        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setColor(Color.parseColor("#464646"))
                .setWhen(0)
                .setAutoCancel(true);

        Intent receiver = new Intent(NotificationBroadcastReceiver.TAG);
        receiver.setClass(context, NotificationBroadcastReceiver.class);

        PendingIntent content = PendingIntent.getBroadcast(context, type, receiver, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(content);

        Notification notification = builder.build();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            notificationManager.notify("houston", type, notification);
        }
        else
        {
            notificationManager.notify(type, notification);
        }
    }

    public static void cancelNotificationAll(Context context)
    {
        initNotificationManager(context);

        for(int i = 1; i <= 3; i++)
        {
            try
            {
                notificationManager.cancel("houston", i);
            }
            catch(Exception e)
            {

            }
        }
    }
}
