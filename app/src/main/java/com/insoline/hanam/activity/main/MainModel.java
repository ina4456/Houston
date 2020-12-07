package com.insoline.hanam.activity.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.inavi.mapsdk.geometry.LatLng;
import com.insoline.hanam.constant.AppConstant;
import com.insoline.hanam.dto.RequestLocation;
import com.insoline.hanam.net.response.VisitedLocationData;
import com.insoline.hanam.net.response.inaviapi.InaviApiAdm;
import com.insoline.hanam.net.response.inaviapi.InaviApiReverseGeocode;
import com.insoline.hanam.util.CommonUtil;
import com.insoline.hanam.util.PermissionUtil;

import java.util.ArrayList;
import java.util.List;

public class MainModel
{
    private Context context;
    private LocationManager locationManager;

    private SharedPreferences pref;

    private RequestLocation requestLocation;                // 바로 호출시 역지오코딩을 이용해 가져온 위치 정보 or 검색으로 설정된 출발지
    private RequestLocation requestDestinationLocation;     // 검색으로 설정된 도착지

    public static final int CALL_STATUS_ALLOC       = 1;        // 배차완료
    public static final int CALL_STATUS_BOARD_ON    = 2;        // 승차
    public static final int CALL_STATUS_BOARD_OFF   = 3;        // 하차
    public static final int CALL_STATUS_CANCEL      = 4;        // 배차 요청 취소
    public static final int CALL_STATUS_CANCEL_BY_DRIVER = 5;   // 배차 요청 취소
    public static final int CALL_STATUS_ALLOC_WAIT  = 6;        // 배차 요청 중

    // 콜센터 전화번호
    public static String[] callCenterPhoneNums = {
            "031-791-4114"
    };

    private String mobileNum;
    private int callStatus;

    private String myCurrentPositionName;

    private boolean favoriteLocationClickFlag;
    private boolean requestUsingHistory;

    private List<VisitedLocationData> frequencyVisited;

    public MainModel(Context context)
    {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        pref = context.getSharedPreferences(AppConstant.COMMON_DATA, Context.MODE_PRIVATE);
        mobileNum = pref.getString(AppConstant.COMMON_DATA_MOBILE_NUMBER, null);
    }

    /**
     * 시간 지연 없이 바로 현재 위치를 잡아온다(약간의 오차가 있음).
     * @return  구글 Location 객체
     */
    public LatLng getLocationInstantly()
    {
        return getLocationInstantly(null);
    }

    public LatLng getLocationInstantly(LocationListener locationListener)
    {
        Location location = null;

        // 권한 체크 : 위치 측정
        if(PermissionUtil.checkPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) &&
                PermissionUtil.checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            boolean isGpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            List<Location> locationList = new ArrayList<>();

            if(isNetworkEnable == true) {
                Location locationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if(locationNetwork != null) {
                    locationList.add(locationNetwork);
                }
            }

            if(isGpsEnable == true) {
                Location locationGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(locationGps != null) {
                    locationList.add(locationGps);
                }
            }

            if(locationList.size() > 0) {
                for(Location l : locationList) {
                    if(l == null)
                        continue;

                    if(location == null || l.getAccuracy() < location.getAccuracy()) {
                        location = l;
                    }
                }
            }

            if(location == null && locationListener != null) {
                // 네트워크와 GSP 동시에 위치 측정(먼저 수신한 쪽을 우선한다)
                // 일단 한 번 위치가 잡히고 나면 보통 위 코드에서 위치 정보를 보내준다.
                userLocationListener = locationListener;
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, networkLocationListener, null);
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, gpsLocationListener, null);
            }
        }

        LatLng latLng = null;
        if(location != null) {
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
        }
        else {
            Log.d(AppConstant.LOG_DEBUG_TAG, "no location information!");
        }
        return latLng;
    }

    public void measureLocation() {
        if(PermissionUtil.checkPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) &&
                PermissionUtil.checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            boolean isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            boolean isGpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if(isNetworkEnable) {
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) { }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras){ }

                    @Override
                    public void onProviderEnabled(String provider){}

                    @Override
                    public void onProviderDisabled(String provider){ }
                }, null);
            }

            if(isGpsEnable){
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener(){
                    @Override
                    public void onLocationChanged(Location location){ }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras){}

                    @Override
                    public void onProviderEnabled(String provider){}

                    @Override
                    public void onProviderDisabled(String provider){ }
                }, null);
            }
        }
    }

    private LocationListener userLocationListener;

    private LocationListener networkLocationListener = new LocationListener()
    {
        @Override
        public void onLocationChanged(Location location){
            try{
                // 네트워크 쪽에서 먼저 잡히면 GPS 측정 종료
                locationManager.removeUpdates(gpsLocationListener);

                if(userLocationListener != null) {
                    userLocationListener.onLocationChanged(location);
                    userLocationListener = null;
                }
            }
            catch(Exception e){}
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras){ }

        @Override
        public void onProviderEnabled(String provider){}

        @Override
        public void onProviderDisabled(String provider){}
    };

    private LocationListener gpsLocationListener = new LocationListener()
    {
        @Override
        public void onLocationChanged(Location location) {
            try {
                // GPS 쪽에서 먼저 잡히면 네트워크 측정 종료
                locationManager.removeUpdates(networkLocationListener);

                if(userLocationListener != null) {
                    userLocationListener.onLocationChanged(location);
                    userLocationListener = null;
                }
            } catch(Exception e) { }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider)
        {

        }

        @Override
        public void onProviderDisabled(String provider)
        {

        }
    };

    public boolean isGpsAvailable()
    {
        try
        {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        catch(Exception e)
        {

        }

        return false;
    }

    public String getMobileNum()
    {
        return mobileNum;
    }

    public String getCallId()
    {
        return pref.getString(AppConstant.COMMON_DATA_CALL_ID, null);
    }

    public void setCallId(String callId)
    {
        SharedPreferences.Editor edit = pref.edit();
        edit.putString(AppConstant.COMMON_DATA_CALL_ID, callId);
        edit.commit();
    }

    public void removeCallId()
    {
        SharedPreferences.Editor edit = pref.edit();
        edit.remove(AppConstant.COMMON_DATA_CALL_ID);
        edit.commit();
    }

    public String getCallDt()
    {
        return pref.getString(AppConstant.COMMON_DATA_CALL_DT, null);
    }

    public void setCallDt(String callId)
    {
        SharedPreferences.Editor edit = pref.edit();
        edit.putString(AppConstant.COMMON_DATA_CALL_DT, callId);
        edit.commit();
    }

    public void removeCallDt()
    {
        SharedPreferences.Editor edit = pref.edit();
        edit.remove(AppConstant.COMMON_DATA_CALL_DT);
        edit.commit();
    }

    public void setCallDeparturePoint(double lat, double lng)
    {
        SharedPreferences.Editor edit = pref.edit();
        edit.putString(AppConstant.COMMON_DATA_CALL_DEPART_LAT, String.valueOf(lat));
        edit.putString(AppConstant.COMMON_DATA_CALL_DEPART_LNG, String.valueOf(lng));
        edit.commit();
    }

    public LatLng getCallDeparturePoint()
    {
        String lat = pref.getString(AppConstant.COMMON_DATA_CALL_DEPART_LAT, null);
        String lng = pref.getString(AppConstant.COMMON_DATA_CALL_DEPART_LNG, null);
        if(lat != null && lng != null)
        {
            return new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        }

        return null;
    }

    public void removeCallDeparturePoint()
    {
        SharedPreferences.Editor edit = pref.edit();
        edit.remove(AppConstant.COMMON_DATA_CALL_DEPART_LAT);
        edit.remove(AppConstant.COMMON_DATA_CALL_DEPART_LNG);
        edit.commit();
    }

    public void setCallDepartureName(String name)
    {
        SharedPreferences.Editor edit = pref.edit();
        edit.putString(AppConstant.COMMON_DATA_CALL_DEPART_NAME, name);
        edit.commit();
    }

    public String getCallDepartureName()
    {
        String name = pref.getString(AppConstant.COMMON_DATA_CALL_DEPART_NAME, null);
        return name;
    }

    public void removeCallDepartureName()
    {
        SharedPreferences.Editor edit = pref.edit();
        edit.remove(AppConstant.COMMON_DATA_CALL_DEPART_NAME);
        edit.commit();
    }

    public void setCallDepartureAddress(String address)
    {
        SharedPreferences.Editor edit = pref.edit();
        edit.putString(AppConstant.COMMON_DATA_CALL_DEPART_ADDR, address);
        edit.commit();
    }

    public String getCallDepartureAddress()
    {
        String address = pref.getString(AppConstant.COMMON_DATA_CALL_DEPART_ADDR, null);
        return address;
    }

    public void removeCallDepartureAddress()
    {
        SharedPreferences.Editor edit = pref.edit();
        edit.remove(AppConstant.COMMON_DATA_CALL_DEPART_ADDR);
        edit.commit();
    }

    public void setCallDestinationPoint(double lat, double lng)
    {
        SharedPreferences.Editor edit = pref.edit();
        edit.putString(AppConstant.COMMON_DATA_CALL_DEST_LAT, String.valueOf(lat));
        edit.putString(AppConstant.COMMON_DATA_CALL_DEST_LNG, String.valueOf(lng));
        edit.commit();
    }

    public LatLng getCallDestinationPoint()
    {
        String lat = pref.getString(AppConstant.COMMON_DATA_CALL_DEST_LAT, null);
        String lng = pref.getString(AppConstant.COMMON_DATA_CALL_DEST_LNG, null);
        if(lat != null && lng != null)
        {
            return new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        }

        return null;
    }

    public void removeCallDestinationPoint()
    {
        SharedPreferences.Editor edit = pref.edit();
        edit.remove(AppConstant.COMMON_DATA_CALL_DEST_LAT);
        edit.remove(AppConstant.COMMON_DATA_CALL_DEST_LNG);
        edit.commit();
    }

    public void setCallDestinationName(String name)
    {
        SharedPreferences.Editor edit = pref.edit();
        edit.putString(AppConstant.COMMON_DATA_CALL_DEST_NAME, name);
        edit.commit();
    }

    public String getCallDestinationName()
    {
        String name = pref.getString(AppConstant.COMMON_DATA_CALL_DEST_NAME, null);
        return name;
    }

    public void removeCallDestinationName()
    {
        SharedPreferences.Editor edit = pref.edit();
        edit.remove(AppConstant.COMMON_DATA_CALL_DEST_NAME);
        edit.commit();
    }

    public void setCallDestinationAddress(String address)
    {
        SharedPreferences.Editor edit = pref.edit();
        edit.putString(AppConstant.COMMON_DATA_CALL_DEST_ADDR, address);
        edit.commit();
    }

    public String getCallDestinationAddress()
    {
        String address = pref.getString(AppConstant.COMMON_DATA_CALL_DEST_ADDR, null);
        return address;
    }

    public void removeCallDestinationAddress()
    {
        SharedPreferences.Editor edit = pref.edit();
        edit.remove(AppConstant.COMMON_DATA_CALL_DEST_ADDR);
        edit.commit();
    }

    public void setCallDriverPhoneNumber(String phoneNumber)
    {
        SharedPreferences.Editor edit = pref.edit();
        edit.putString(AppConstant.COMMON_DATA_CALL_DRIVER_NUM, phoneNumber);
        edit.commit();
    }

    public String getCallDriverPhoneNumber()
    {
        return pref.getString(AppConstant.COMMON_DATA_CALL_DRIVER_NUM, null);
    }

    public void removeCallDriverPhoneNumber()
    {
        SharedPreferences.Editor edit = pref.edit();
        edit.remove(AppConstant.COMMON_DATA_CALL_DRIVER_NUM);
        edit.commit();
    }

    public void setCallDriverCarNumber(String carNumber)
    {
        SharedPreferences.Editor edit = pref.edit();
        edit.putString(AppConstant.COMMON_DATA_CALL_DRIVER_CAR_NUM, carNumber);
        edit.commit();
    }

    public String getCallDriverCarNumber()
    {
        return pref.getString(AppConstant.COMMON_DATA_CALL_DRIVER_CAR_NUM, null);
    }

    public void removeCallDriverCarNumber()
    {
        SharedPreferences.Editor edit = pref.edit();
        edit.remove(AppConstant.COMMON_DATA_CALL_DRIVER_CAR_NUM);
        edit.commit();
    }

    public void setCallSpendTime(int spendTime)
    {
        SharedPreferences.Editor edit = pref.edit();
        edit.putInt(AppConstant.COMMON_DATA_CALL_SPEND_TIME, spendTime);
        edit.commit();
    }

    public int getCallSpendTime()
    {
        int spendTime = pref.getInt(AppConstant.COMMON_DATA_CALL_SPEND_TIME, -1);
        return spendTime;
    }

    public void removeCallSpendTime()
    {
        SharedPreferences.Editor edit = pref.edit();
        edit.remove(AppConstant.COMMON_DATA_CALL_SPEND_TIME);
        edit.commit();
    }

    public void setCallCenterDtime(long timeInMills)
    {
        SharedPreferences.Editor edit = pref.edit();
        edit.putLong(AppConstant.COMMON_DATA_CALL_CENTER_DTIME, timeInMills);
        edit.commit();
    }

    public long getCallCenterDtime()
    {
        return pref.getLong(AppConstant.COMMON_DATA_CALL_CENTER_DTIME, 0);
    }

    public void removeCallCenterDtime()
    {
        SharedPreferences.Editor edit = pref.edit();
        edit.remove(AppConstant.COMMON_DATA_CALL_CENTER_DTIME);
        edit.commit();
    }

    public void setCallCenterCheckFlag()
    {
        SharedPreferences.Editor edit = pref.edit();
        edit.putBoolean(AppConstant.COMMON_DATA_CALL_CENTER_CHECK_FLAG, true);
        edit.commit();
    }

    public boolean getCallCenterCheckFlag()
    {
        boolean flag = pref.getBoolean(AppConstant.COMMON_DATA_CALL_CENTER_CHECK_FLAG, false);

        SharedPreferences.Editor edit = pref.edit();
        edit.remove(AppConstant.COMMON_DATA_CALL_CENTER_CHECK_FLAG);
        edit.commit();

        return flag;
    }

    public void removeCallInfo()
    {
        SharedPreferences.Editor edit = pref.edit();
        edit.remove(AppConstant.COMMON_DATA_CALL_ID);
        edit.remove(AppConstant.COMMON_DATA_CALL_DT);
        edit.remove(AppConstant.COMMON_DATA_CALL_DEPART_LAT);
        edit.remove(AppConstant.COMMON_DATA_CALL_DEPART_LNG);
        edit.remove(AppConstant.COMMON_DATA_CALL_DEPART_NAME);
        edit.remove(AppConstant.COMMON_DATA_CALL_DEPART_ADDR);
        edit.remove(AppConstant.COMMON_DATA_CALL_DEST_LAT);
        edit.remove(AppConstant.COMMON_DATA_CALL_DEST_LNG);
        edit.remove(AppConstant.COMMON_DATA_CALL_DEST_NAME);
        edit.remove(AppConstant.COMMON_DATA_CALL_DEST_ADDR);
        edit.remove(AppConstant.COMMON_DATA_CALL_DRIVER_NUM);
        edit.remove(AppConstant.COMMON_DATA_CALL_DRIVER_CAR_NUM);
        edit.remove(AppConstant.COMMON_DATA_CALL_SPEND_TIME);
        edit.commit();
    }

    public RequestLocation getRequestLocation()
    {
        return requestLocation;
    }

    public void setRequestLocation(RequestLocation requestLocation)
    {
        this.requestLocation = requestLocation;
    }

    /**
     * @param reverseGeocode
     * @param latLng            reverseGeocode에 adm 정보가 이 없는 경우 사용할 보조 좌표 정보
     */
    public void setRequestLocation(InaviApiReverseGeocode reverseGeocode, LatLng latLng)
    {
        if(reverseGeocode == null)
        {
            requestLocation = null;
            return;
        }

        requestLocation = makeRequestLocationFromReverseCode(reverseGeocode, latLng);
    }

    public RequestLocation getRequestDestinationLocation()
    {
        return requestDestinationLocation;
    }

    public void setRequestDestinationLocation(RequestLocation requestDestinationLocation)
    {
        this.requestDestinationLocation = requestDestinationLocation;
    }

    /**
     * @param reverseGeocode
     * @param latLng            역지오코딩에 사용된 좌표
     */
    public void setRequestDestinationLocation(InaviApiReverseGeocode reverseGeocode, LatLng latLng)
    {
        if(reverseGeocode == null)
        {
            requestDestinationLocation = null;
            return;
        }

        requestDestinationLocation = makeRequestLocationFromReverseCode(reverseGeocode, latLng);
    }

    private RequestLocation makeRequestLocationFromReverseCode(
            InaviApiReverseGeocode reverseGeocode, @NonNull LatLng latLng)
    {
        if(reverseGeocode != null &&
                reverseGeocode.location != null &&
                reverseGeocode.location.adm != null)
        {
            double lat = latLng.latitude;
            double lng = latLng.longitude;
            String positionName = null;
            String address = null;

            if(reverseGeocode.location.adm != null)
            {
                String buildingName = reverseGeocode.location.adm.buildingName;
                if(buildingName != null && !buildingName.trim().isEmpty())
                {
                    positionName = buildingName;
                }
                else
                {
                    positionName = CommonUtil.cutRoadAddress(
                            reverseGeocode.location.adm.roadName, reverseGeocode.location.adm.roadNumber);
                }

                address = String.format("%s %s", reverseGeocode.location.adm.roadName, reverseGeocode.location.adm.roadNumber);
                address = address.trim();
            }
            else
            {
                if(reverseGeocode.location.legalAddr != null)
                {
                    InaviApiAdm legalAddr = reverseGeocode.location.legalAddr;
                    positionName = String.format("%s %s %s", legalAddr.addressCategory2, legalAddr.addressCategory3, legalAddr.address);
                    address = String.format("%s %s", legalAddr.address, legalAddr.addressNumber);
                }
                else if(reverseGeocode.location.admAddress != null)
                {
                    InaviApiAdm admAddr = reverseGeocode.location.admAddress;
                    positionName = String.format("%s %s %s", admAddr.addressCategory2, admAddr.addressCategory3, admAddr.address);
                    address = String.format("%s %s", admAddr.address, admAddr.addressNumber);
                }

                if(address != null)
                {
                    address = address.trim();
                }
            }

            return new RequestLocation(lat, lng, address, positionName, address);
        }

        return null;
    }

    public int getCallStatus()
    {
        return callStatus;
    }

    public void setCallStatus(String callStatusStr) throws Exception
    {
        Log.d(AppConstant.LOG_DEBUG_TAG, "call status = " + callStatusStr);

        // 문자열을 숫자로
        if("배차중".equals(callStatusStr) || "접수".equals(callStatusStr) || "배차실패".equals(callStatusStr))
        {
            callStatus = CALL_STATUS_ALLOC_WAIT;
        }
        else if("배차완료".equals(callStatusStr))
        {
            callStatus = CALL_STATUS_ALLOC;
        }
        else if("승차".equals(callStatusStr))
        {
            callStatus = CALL_STATUS_BOARD_ON;
        }
        else if("하차".equals(callStatusStr))
        {
            callStatus = CALL_STATUS_BOARD_OFF;
        }
        else if("취소".equals(callStatusStr))
        {
            callStatus = CALL_STATUS_CANCEL;
        }
        else if("탑승실패".equals(callStatusStr))
        {
            callStatus = CALL_STATUS_CANCEL_BY_DRIVER;
        }
        else
        {
            // 통신오류 또는 비식별 문자열
            Log.d(AppConstant.LOG_DEBUG_TAG, "비식별 call status = " + callStatusStr);
            throw new Exception("network error or internal server error");
        }
    }

    public void setCallStatus(int callStatus)
    {
        this.callStatus = callStatus;
    }

    /**
     * 출발지, 도착지 모두 설정시, 출/도착지 모두 설정이 되었는지
     * @return
     */
    public boolean canCall()
    {
        return (requestLocation != null && requestDestinationLocation != null);
    }

    /**
     * 출/도착지 동일 여부 판단
     * @return
     */
    public boolean sameTwoPoints()
    {
        if(requestLocation != null && requestDestinationLocation != null)
        {
            final double MIN_DISTANCE = 10;

            //String positionName1 = requestLocation.positionName;
            //String positionName2 = requestDestinationLocation.positionName;
            String address1 = requestLocation.address;
            String address2 = requestDestinationLocation.address;
            long poiId1 = requestLocation.poiId;
            long poiId2 = requestDestinationLocation.poiId;
            //Log.d(AppConstant.LOG_TEMP_TAG, address1 + ", " + address2);

            // 주소 동일
            if(address1.equals(address2))
            {
                return true;
            }

            // 좌표 사이 동질성 판단

            double lat1 = requestLocation.lat;
            double lng1 = requestLocation.lng;
            LatLng latLng1 = new LatLng(lat1, lng1);

            double lat2 = requestDestinationLocation.lat;
            double lng2 = requestDestinationLocation.lng;
            LatLng latLng2 = new LatLng(lat2, lng2);

            double distance = CommonUtil.calculateDistanceBetweenPoints(latLng1, latLng2) * 1000; // meter
            //Log.d(AppConstant.LOG_TEMP_TAG, "distance = " + distance);
            if(distance <= MIN_DISTANCE)
            {
                // 너무 가까움. 출/도착지 동일로 판단.
                return true;
            }

            if(poiId1 > 0 && poiId2 > 0 && poiId1 == poiId2)
            {
                // poi id가 있는 경우(둘 다) 둘을 비교한다.
                // 리버스 지오코딩에는 poi id가 없으므로, 검색시 이 조건에 걸리는 경우는 거의 없을 듯.
                //Log.d(AppConstant.LOG_TEMP_TAG, "동일 poi id = " + poiId1);
                return true;
            }

            return false;
        }

        return true;
    }

    public String getMyCurrentPositionName()
    {
        return myCurrentPositionName;
    }

    public void setMyCurrentPositionName(String myCurrentPositionName)
    {
        this.myCurrentPositionName = myCurrentPositionName;
    }

    public RequestLocation getHomeInfo()
    {
        return CommonUtil.getFavoriteLocation(AppConstant.HOME, pref);
    }

    public RequestLocation getOfficeInfo()
    {
        return CommonUtil.getFavoriteLocation(AppConstant.OFFICE, pref);
    }

    public boolean isFavoriteLocationClickFlag()
    {
        return favoriteLocationClickFlag;
    }

    public void setFavoriteLocationClickFlag(boolean favoriteLocationClickFlag)
    {
        this.favoriteLocationClickFlag = favoriteLocationClickFlag;
    }

    public boolean isRequestUsingHistory()
    {
        return requestUsingHistory;
    }

    public void setRequestUsingHistory(boolean requestUsingHistory)
    {
        this.requestUsingHistory = requestUsingHistory;
    }

    public boolean readDirectlyCallTip()
    {
        return pref.getBoolean(AppConstant.COMMON_DATA_CALL_TIP_READ_FLAG, false);
    }

    public void setDirectlyCallTip()
    {
        SharedPreferences.Editor edit = pref.edit();
        edit.putBoolean(AppConstant.COMMON_DATA_CALL_TIP_READ_FLAG, true);
        edit.commit();
    }

    public List<VisitedLocationData> getFrequencyVisited()
    {
        return frequencyVisited;
    }

    public void setFrequencyVisited(List<VisitedLocationData> dataList)
    {
        filteringFrequencyVisitedLocationData(dataList);
    }

    public void setFrequencyVisited(Activity activity)
    {
        Intent intent = activity.getIntent();
        List<VisitedLocationData> dataList = intent.getParcelableArrayListExtra("frequencyVisited");
        filteringFrequencyVisitedLocationData(dataList);
    }

    private void filteringFrequencyVisitedLocationData(List<VisitedLocationData> dataList)
    {
        if(frequencyVisited == null)
        {
            frequencyVisited = new ArrayList<>();
        }
        else
        {
            frequencyVisited.clear();
        }

        if(dataList != null && dataList.size() > 0)
        {
            for(VisitedLocationData data : dataList)
            {
                String destName = data.getDestinationName();
                if(destName == null || destName.trim().isEmpty())
                {
                    continue;
                }

                frequencyVisited.add(data);
            }
        }
    }
}
