package com.insoline.hanam.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.insoline.hanam.activity.SplashActivity;
import com.insoline.hanam.constant.AppConstant;
import com.insoline.hanam.util.CommonUtil;

public class NotificationBroadcastReceiver extends BroadcastReceiver
{
    public static final String TAG = "com.insoline.hanam.receiver.NotificationBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        try
        {
            String className = "com.insoline.hanam.activity.main.MainActivity";
            boolean oldSdk = (Build.VERSION.SDK_INT < Build.VERSION_CODES.M);
            if(oldSdk)
            {
                if(CommonUtil.isActivityTopOld(context, className))
                {
                    // 메인이 실행 중이지만 백그라운드에 있음. 포어그라운드로 올린다.
                    CommonUtil.bringActivityToFront(context, className);
                }
                else
                {
                    // 앱이 실행 중이 아니다. 인트로부터 다시 시작.
                    Intent baseIntent = new Intent(context, SplashActivity.class);
                    baseIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(baseIntent);
                }
            }
            else
            {
                if(CommonUtil.isActivityTop(context, className))
                {
                    // 메인이 실행 중이지만 백그라운드에 있음. 포어그라운드로 올린다.
                    CommonUtil.bringActivityToFront(context, className);
                }
                else
                {
                    // 앱이 실행 중이 아니다. 인트로부터 다시 시작.
                    Intent baseIntent = new Intent(context, SplashActivity.class);
                    baseIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(baseIntent);
                }
            }
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }
}
