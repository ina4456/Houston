package com.insoline.hanam.util;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.View;

import com.inavi.mapsdk.geometry.LatLng;
import com.insoline.hanam.activity.main.MainModel;
import com.insoline.hanam.constant.AppConstant;
import com.insoline.hanam.dto.RequestLocation;
import com.insoline.hanam.net.response.inaviapi.InaviApiAdm;
import com.insoline.hanam.net.response.inaviapi.InaviApiReverseGeocode;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class CommonUtil
{
    /**
     * 휴대장치의 전화번호를 가져온다(권한 필요)
     * @return
     */
    public static String getMobileNumber(Context context)
    {
        if(PermissionUtil.checkPermission(context, Manifest.permission.READ_PHONE_STATE))
        {
            TelephonyManager mgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return mgr.getLine1Number();
        }

        return null;
    }

    /**
     * DP ---> Pixel
     * @param dp
     * @return
     */
    public static float convertDpToPixel(float dp)
    {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

    /**
     * Pixel ---> DP
     * @param px
     * @return
     */
    public static float convertPixelsToDp(float px)
    {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return Math.round(dp);
    }

    /**
     * 뷰의 현재 모습을 비트맵에 그린다(지도 마커에 사용).
     * @param view          그려지는 뷰
     * @param bitmapConfig  그릴 때 비트맵 옵션
     * @return
     */
    public static Bitmap createMarkerBitmapUsingView(final View view, Bitmap.Config bitmapConfig)
    {
        Bitmap bitmap = null;

        try
        {
            if(view == null)
            {
                return null;
            }

            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int width = view.getMeasuredWidth();
            int height = view.getMeasuredHeight();
            view.layout(0, 0, width, height);

            // 비트맵을 만들고
            bitmap = Bitmap.createBitmap(width, height, bitmapConfig);

            // 캔버스에 뷰를 그린다
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.TRANSPARENT);
            view.draw(canvas);
        }
        catch(Exception e)
        {

        }

        return bitmap;
    }

    /**
     * 세 가지 상태에 대한 텍스트 색깔 selector를 만든다.
     * @param normalColor       일반 색상
     * @param pressedColor      눌렸을 때 색상
     * @param disabledColor     enabled==false일 때 색상
     * @return
     */
    public static ColorStateList getBtnTextColorStateList(String normalColor, String pressedColor, String disabledColor)
    {
        int[] pressedState = {android.R.attr.state_enabled, android.R.attr.state_pressed};
        int[] normalState = {android.R.attr.state_enabled, -android.R.attr.state_pressed};
        int[] disabledState = {-android.R.attr.state_enabled};

        ColorStateList colorStateList = new ColorStateList(
                new int[][] {pressedState, normalState, disabledState},
                new int[] {
                        Color.parseColor(pressedColor),
                        Color.parseColor(normalColor),
                        Color.parseColor(disabledColor)
                });

        return colorStateList;
    }

    /**
     * 주소를 간략하게 표현할 때 사용(주소 맨 뒤 두 단어만 사용)
     * @param roadAddress
     * @param roadNumber
     * @return
     */
    public static String cutRoadAddress(String roadAddress, String roadNumber)
    {
        String[] split = roadAddress.split(" ");
        String roadName = "";

        int start = split.length-2;
        if(start < 0)
        {
            start = 0;
        }

        for(int i = start; i < split.length; i++)
        {
            roadName += (split[i] + " ");
        }

        return String.format("%s %s", roadName.trim(), roadNumber);
    }

    public static String getReverseGeocodeString(InaviApiReverseGeocode result, boolean notUseBuildingName)
    {
        //Log.d(AppConstant.LOG_TEMP_TAG, "buildingName = " + buildingName);
        //Log.d(AppConstant.LOG_TEMP_TAG, "roadName = " + result.location.adm.roadName);
        //Log.d(AppConstant.LOG_TEMP_TAG, "roadNumber = " + result.location.adm.roadNumber);

        String text = null;

        if(result.location.adm != null)
        {
            String buildingName = result.location.adm.buildingName;

            if(buildingName != null && !buildingName.trim().isEmpty() && !notUseBuildingName)
            {
                text = buildingName;
            }
            else
            {
                text = CommonUtil.cutRoadAddress(result.location.adm.roadName, result.location.adm.roadNumber);
            }
        }
        else
        {
            if(result.location.legalAddr != null)
            {
                InaviApiAdm legalAddr = result.location.legalAddr;
                text = String.format("%s %s %s", legalAddr.addressCategory2, legalAddr.addressCategory3, legalAddr.address);
            }
            else if(result.location.admAddress != null)
            {
                InaviApiAdm admAddr = result.location.admAddress;
                text = String.format("%s %s %s", admAddr.addressCategory2, admAddr.addressCategory3, admAddr.address);
            }
        }

        return text;
    }


    public static String getTextFromAddress(InaviApiReverseGeocode result)
    {
//        String buildingName = result.location.adm.buildingName;
//        if(buildingName != null && !buildingName.trim().isEmpty())
//        {
//            text = buildingName;
//        }

        String address = result.location.adm.address;
        if(address == null || address.trim().isEmpty())
        {
            if(result.location.admAddress != null)
            {
                address = result.location.admAddress.address;
            }
            else if(result.location.legalAddr != null)
            {
                address = result.location.legalAddr.address;
            }
        }

        String text = "";
        if(address != null && !address.trim().isEmpty())
        {
            String[] arr = new String[2];
            String[] split = address.split(" ");
            int len = split.length;
            int start = len-1;
            for(int i = start; i >= 0; i--)
            {
                int j = (len-1) - i;
                if(j < 2)
                {
                    arr[j] = split[i];
                }
                else
                {
                    break;  // for(i)
                }
            }

            for(String str : arr)
            {
                if(str != null && !str.trim().isEmpty())
                {
                    text += str + " ";
                }
            }
        }

        return text.trim();
    }

    /**
     * 두 좌표 사이의 거리를 구한다.
     * @param latLng1
     * @param latLng2
     * @return              좌표 사이 거리(km)
     */
    public static double calculateDistanceBetweenPoints(LatLng latLng1, LatLng latLng2)
    {
        double theta = 0, distance = -1/*km*/;

        try
        {
            double lat1 = latLng1.latitude;
            double lat2 = latLng2.latitude;
            double lng1 = latLng1.longitude;
            double lng2 = latLng2.longitude;

            theta = lng1 - lng2;

            distance = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) +
                    Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
            distance = Math.acos(distance);
            distance = rad2deg(distance);
            distance = distance * 60 * 1.1515; // unit = mile
            distance = distance * 1.609344;
        }
        catch(Exception e)
        {
            distance = -1;
        }

        return distance;
    }

    // 주어진 도(degree) 값을 라디언으로 변환
    public static double deg2rad(double deg)
    {
        return (double) (deg * Math.PI / (double) 180d);
    }

    // 주어진 라디언(radian) 값을 도(degree) 값으로 변환
    public static double rad2deg(double rad)
    {
        return (double) (rad * (double) 180d / Math.PI);
    }

    ///// 자주 가는 곳 관련 //////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static RequestLocation getFavoriteLocation(int type, SharedPreferences pref)
    {
        if(type == AppConstant.HOME)
        {
            String name = pref.getString(AppConstant.COMMON_DATA_HOME_NAME, null);
            String address = pref.getString(AppConstant.COMMON_DATA_HOME_ADDRESS, null);
            String lat = pref.getString(AppConstant.COMMON_DATA_HOME_LAT, null);
            String lng = pref.getString(AppConstant.COMMON_DATA_HOME_LNG, null);
            return CommonUtil.makeFavoriteLocation(name, address, lat, lng);
        }
        else
        {
            String name = pref.getString(AppConstant.COMMON_DATA_OFFICE_NAME, null);
            String address = pref.getString(AppConstant.COMMON_DATA_OFFICE_ADDRESS, null);
            String lat = pref.getString(AppConstant.COMMON_DATA_OFFICE_LAT, null);
            String lng = pref.getString(AppConstant.COMMON_DATA_OFFICE_LNG, null);
            return CommonUtil.makeFavoriteLocation(name, address, lat, lng);
        }
    }

    public static RequestLocation makeFavoriteLocation(String name, String address, String lat, String lng)
    {
        if(name != null && !name.trim().isEmpty() && address != null && !address.trim().isEmpty() &&
                lat != null && !lat.trim().isEmpty() && lng != null && !lng.trim().isEmpty())
        {
            RequestLocation location = new RequestLocation();
            location.positionName = name;
            location.address = address;
            location.lat = Double.parseDouble(lat);
            location.lng = Double.parseDouble(lng);
            return location;
        }

        return null;
    }

    public static void addFavoriteLocation(int type, SharedPreferences pref, RequestLocation location)
    {
        SharedPreferences.Editor edit = pref.edit();

        if(type == AppConstant.HOME)
        {
            edit.putString(AppConstant.COMMON_DATA_HOME_NAME, location.positionName);
            edit.putString(AppConstant.COMMON_DATA_HOME_ADDRESS, location.address);
            edit.putString(AppConstant.COMMON_DATA_HOME_LAT, String.valueOf(location.lat));
            edit.putString(AppConstant.COMMON_DATA_HOME_LNG, String.valueOf(location.lng));
        }
        else
        {
            edit.putString(AppConstant.COMMON_DATA_OFFICE_NAME, location.positionName);
            edit.putString(AppConstant.COMMON_DATA_OFFICE_ADDRESS, location.address);
            edit.putString(AppConstant.COMMON_DATA_OFFICE_LAT, String.valueOf(location.lat));
            edit.putString(AppConstant.COMMON_DATA_OFFICE_LNG, String.valueOf(location.lng));
        }

        edit.commit();
    }

    public static void removeFavoriteLocation(int type, SharedPreferences pref)
    {
        SharedPreferences.Editor edit = pref.edit();

        if(type == AppConstant.HOME)
        {
            edit.remove(AppConstant.COMMON_DATA_HOME_NAME);
            edit.remove(AppConstant.COMMON_DATA_HOME_ADDRESS);
            edit.remove(AppConstant.COMMON_DATA_HOME_LAT);
            edit.remove(AppConstant.COMMON_DATA_HOME_LNG);
        }
        else
        {
            edit.remove(AppConstant.COMMON_DATA_OFFICE_NAME);
            edit.remove(AppConstant.COMMON_DATA_OFFICE_ADDRESS);
            edit.remove(AppConstant.COMMON_DATA_OFFICE_LAT);
            edit.remove(AppConstant.COMMON_DATA_OFFICE_LNG);
        }

        edit.commit();
    }

    ///// 마켓 관련 /////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 버전 문자열(예: 1.0.1)
     * @param context
     * @return
     */
    public static String getVersionName(Context context)
    {
        try
        {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        }
        catch(PackageManager.NameNotFoundException e)
        {
            return "?";
        }
    }

    /**
     * 숫자로 된 버전 코드(예: 101)
     * @param context
     * @return
     */
    public static long getVersionCode(Context context)
    {
        try
        {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            {
                return packageInfo.getLongVersionCode();
            }
            else
            {
                return packageInfo.versionCode;
            }
        }
        catch(PackageManager.NameNotFoundException e)
        {
            return -1;
        }
    }

    /**
     * 안드로이드 마켓으로 이동(구글 플레이 스토어 앱이나 해당 스토어 웹 화면으로 이동한다)
     * @param context
     */
    public static void goAndroidMarket(Context context)
    {
        try
        {
            String packageName = context.getPackageName();

            try
            {
                Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
                context.startActivity(marketIntent);
            }
            catch(ActivityNotFoundException e)
            {
                try
                {
                    Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
                    context.startActivity(marketIntent);
                }
                catch(Exception ex)
                {

                }
            }
        }
        catch(Exception e)
        {

        }
    }

    ///// 콜센터 관련 ///////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static TreeSet<String> getSelectedCallCenter(SharedPreferences pref)
    {
        String[] callCenterList = MainModel.callCenterPhoneNums;
        Set<String> listSet = new HashSet<>();
        for(String phoneNum : callCenterList)
        {
            listSet.add(phoneNum);
        }

        String selectedCallCenter = pref.getString(AppConstant.COMMON_DATA_CALL_CENTER, null);
        TreeSet<String> callCenterSet = new TreeSet<>();

        if(selectedCallCenter != null && !selectedCallCenter.trim().isEmpty())
        {
            String[] split = selectedCallCenter.split(",");
            for(String s : split)
            {
                if(s.trim().isEmpty())
                {
                    continue;
                }

                if(!listSet.contains(s))
                {
                    // 예전에 사용으로 설정되었지만 이제는 존재하지 않는 콜센터
                    continue;
                }

                callCenterSet.add(s);
            }
        }

        return callCenterSet;
    }

    public static String getSelectedText(TreeSet<String> stringSet)
    {
        if(stringSet.size() > 0)
        {
            String text = "";
            Iterator<String> iterator = stringSet.iterator();
            while(iterator.hasNext())
            {
                String s = iterator.next();
                text += (s + ",");
            }

            if(text.endsWith(","))
            {
                text = text.substring(0, text.length()-1);
            }

            return text;
        }

        return "";
    }

    public static void bringActivityToFront(Context context, String className)
    {
        try
        {
            // 액티비티 매니저
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

            // 실행 중인 태스크 목록
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                List<ActivityManager.AppTask> tasks = activityManager.getAppTasks();

                if(tasks == null || tasks.size() <= 0)
                {
                    return;
                }

                // 그렇지 않다면 루프를 돌면서 해당 액티비티를 찾는다
                int size = tasks.size();
                for(int i = 0; i < size; i++)
                {
                    ActivityManager.AppTask task = tasks.get(i);

                    // 태스크의 최상위 액티비티 클래스 이름을 가져온다
                    ActivityManager.RecentTaskInfo taskInfo = task.getTaskInfo();
                    ComponentName baseActivity = taskInfo.baseActivity;

                    String activityClassName = null;
                    if(baseActivity != null)
                    {
                        activityClassName = baseActivity.getClassName();
                    }

                    if(activityClassName != null && activityClassName.equals(className))
                    {
                        // 이름이 같다면 해당 액티비티를 맨 앞으로 올리고 루프 종료
                        activityManager.moveTaskToFront(taskInfo.id, ActivityManager.MOVE_TASK_WITH_HOME);
                        break;
                    }
                }
            }
            else
            {
                List<ActivityManager.RunningTaskInfo> recentTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

                if(recentTasks == null || recentTasks.size() <= 0)
                {
                    return;
                }

                // 루프를 돌면서 해당 액티비티를 찾는다
                int size = recentTasks.size();
                for(int i = 0; i < size; i++)
                {
                    ActivityManager.RunningTaskInfo task = recentTasks.get(i);

                    // 태스크의 최상위 액티비티 클래스 이름을 가져온다
                    ComponentName baseActivity = task.baseActivity;

                    String activityClassName = null;
                    if(baseActivity != null)
                    {
                        activityClassName = baseActivity.getClassName();
                    }

                    if(activityClassName != null && activityClassName.equals(className))
                    {
                        // 이름이 같다면 해당 액티비티를 맨 앞으로 올리고 루프 종료
                        activityManager.moveTaskToFront(task.id, ActivityManager.MOVE_TASK_WITH_HOME);
                        break;
                    }
                }
            }
        }
        catch(Exception e)
        {

        }
    }

    public static boolean isActivityTop(Context context, String className)
    {
        try
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.AppTask> appTasks = activityManager.getAppTasks();

                return isActivityTop(className, appTasks);
            }
        }
        catch(Exception e)
        {

        }

        return false;
    }

    public static boolean isActivityTopOld(Context context, String className)
    {
        try
        {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> recentTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

            return isActivityTopOld(className, recentTasks);
        }
        catch(Exception e)
        {

        }

        return false;
    }

    private static boolean isActivityTop(String className, List<ActivityManager.AppTask> appTasks)
    {
        try
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                if(appTasks == null || appTasks.size() <= 0)
                    return false;

                // 상위 액티비티의 이름을 가져온다
                ActivityManager.RecentTaskInfo taskInfo = appTasks.get(0).getTaskInfo();
                ComponentName topActivity = taskInfo.baseActivity;
                String topActivityClassName = null;
                if(topActivity != null)
                {
                    topActivityClassName = topActivity.getClassName();
                }

                //Log.d(AppConstant.LOG_TEMP_TAG, "topActivityClassName = " + topActivityClassName);

                // 최상위 액티비티 == className
                if(className.equals(topActivityClassName))
                {
                    return true;
                }
            }
        }
        catch(Exception e)
        {

        }

        return false;
    }

    private static boolean isActivityTopOld(String className, List<ActivityManager.RunningTaskInfo> recentTasks)
    {
        try
        {
            if(recentTasks == null || recentTasks.size() <= 0)
                return false;

            // 상위 액티비티의 이름을 가져온다
            ComponentName topActivity = recentTasks.get(0).baseActivity;
            String topActivityClassName = null;
            if(topActivity != null)
            {
                topActivityClassName = topActivity.getClassName();
            }

            // 최상위 액티비티 == className
            if(className.equals(topActivityClassName))
            {
                return true;
            }
        }
        catch(Exception e)
        {

        }

        return false;
    }
}
