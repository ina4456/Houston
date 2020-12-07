package com.insoline.hanam.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.insoline.hanam.constant.AppConstant;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtil
{
    public static final String[] requiredPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE
    };

    /**
     * 사용자에게 권한에 대한 설명이 필요한 경우 호출되는 콜백 리스너
     */
    public interface OnShouldShowRequestPermissionRationaleListener
    {
        void onShouldShowRequestPermissionRationale(String permission);
    }

    /**
     * 권한을 가지고 있는지 확인
     *
     * @param context
     * @param permission
     *
     * @return		해당 권한이 있다 = true
     * 				해당 권한이 없다 = false
     */
    public static boolean checkPermission(Context context, String permission)
    {
        try
        {
            if(Build.VERSION.SDK_INT < 23)
            {
                // 23 이하 버전에서는 권한 체크를 하지 않는다.
                // 마켓에서 설치 시 권한을 모두 획득한다.
                return true;
            }

            int permissionCheck = ContextCompat.checkSelfPermission(context, permission);

            if(permissionCheck == PackageManager.PERMISSION_DENIED)
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }

        return false;
    }

    /**
     * 권한을 확인하고 필요한 경우 사용자에게 해당 권한을 요청한다.
     *
     * @param context											컨텍스트
     * @param permissions										확인해야 하는 권한 목록
     * @param requestCode										권한 요청 후 호출될 콜백에서 식별한 요청 코드
     * @param onShouldShowRequestPermissionRationaleListener	사용자에게 권한에 대한 설명이 필요한 경우 호출되는 콜백 리스너
     *
     * @return	모든 권한을 가지고 있는 경우나 권한 체크가 필요 없는 경우 = true
     * 			권한 요청이 필요한 경우 = false
     */
    public static boolean checkAndRequestPermission(
            Context context, String[] permissions, int requestCode,
            OnShouldShowRequestPermissionRationaleListener onShouldShowRequestPermissionRationaleListener)
    {
        List<String> permissionNeeded;

        try
        {
            if(Build.VERSION.SDK_INT < 23)
            {
                // 23 이하 버전에서는 권한 체크를 하지 않는다.
                // 마켓에서 설치 시 권한을 모두 획득한다.
                return true;
            }

            permissionNeeded = new ArrayList<>();
            for(String permission : permissions)
            {
                int permissionCheck = ContextCompat.checkSelfPermission(context, permission);

                if(permissionCheck == PackageManager.PERMISSION_DENIED)
                {
                    // 해당 권한 없음. 권한 요청 목록에 추가
                    permissionNeeded.add(permission);
                }
            }

            int size = permissionNeeded.size();
            if(size <= 0)
            {
                // 모든 권한이 있음. 그대로 종료
                return true;
            }
            else
            {
                // 권한 획득이 필요한 권한이 있다.
                String[] requestPermissions = new String[size];
                for(int i = 0; i < size; i++)
                {
                    String permission = permissionNeeded.get(i);
                    if(ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission))
                    {
                        // 해당 권한에 대해서 사용자에게 설명하고 권한을 요청
                        if(onShouldShowRequestPermissionRationaleListener != null)
                        {
                            onShouldShowRequestPermissionRationaleListener.onShouldShowRequestPermissionRationale(permission);
                        }
                    }

                    // 필요한 권한 요청 목록 만들기
                    requestPermissions[i] = permission;
                }

                // 권한 요청
                ActivityCompat.requestPermissions(
                        (Activity) context, requestPermissions, requestCode);

                return false;
            }
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }

        return false;
    }
}
