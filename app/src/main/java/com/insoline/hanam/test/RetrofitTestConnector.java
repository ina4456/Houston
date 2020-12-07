package com.insoline.hanam.test;

import android.util.Log;

import com.inavi.mapsdk.geometry.LatLng;
import com.insoline.hanam.activity.main.MainModel;
import com.insoline.hanam.constant.AppConstant;
import com.insoline.hanam.dto.RequestLocation;
import com.insoline.hanam.net.HttpCallback;
import com.insoline.hanam.net.response.AuthResult;
import com.insoline.hanam.net.response.CarInfoResult;
import com.insoline.hanam.net.response.HistoryData;
import com.insoline.hanam.net.response.RequestResult;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class RetrofitTestConnector
{
    public static int callStatusForTest = MainModel.CALL_STATUS_ALLOC_WAIT;
    public static int count = 0;

    public static void initTest(int callStatus)
    {
        count = 0;
        callStatusForTest = callStatus;
    }

    public static void requestCall(String mobile, RequestLocation pos, RequestLocation dest, HttpCallback callback)
    {
        try
        {
            RequestResult result = new RequestResult();
            result.setCallID("abcdefg200731");
            result.setCallDT("200731");
            result.setSuccessful(true);
            callback.onResult(result);
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    public static void cancelCall(String mobile, String callId, String callDt, HttpCallback callback)
    {
        try
        {
            RequestResult result = new RequestResult();
            result.setSuccessful(true);
            callback.onResult(result);
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    public static void callCheck(String mobile, String callId, String callDt, HttpCallback callback)
    {
        try
        {
            //Log.d(AppConstant.LOG_TEMP_TAG, "[callCheck] callStatus = " + callStatusForTest + ", (" + count +")");

            if(callStatusForTest == 0)
            {
                callStatusForTest = MainModel.CALL_STATUS_ALLOC_WAIT;
            }
            else if(callStatusForTest == MainModel.CALL_STATUS_ALLOC_WAIT)
            {
                if(count == 5)
                {
                    callStatusForTest = MainModel.CALL_STATUS_ALLOC;
                    count = 0;
                }
                else
                {
                    count++;
                }
            }
            else if(callStatusForTest == MainModel.CALL_STATUS_ALLOC)
            {
                if(count == 5)
                {
                    callStatusForTest = MainModel.CALL_STATUS_BOARD_ON;
                    count = 0;
                }
                else
                {
                    count++;
                }
            }
            else if(callStatusForTest == MainModel.CALL_STATUS_BOARD_ON)
            {
                if(count == 5)
                {
                    callStatusForTest = MainModel.CALL_STATUS_BOARD_OFF;
                    count = 0;
                }
                else
                {
                    count++;
                }
            }

            String status = null;
            if(callStatusForTest == MainModel.CALL_STATUS_ALLOC_WAIT)
            {
                status = "배차중";
            }
            else if(callStatusForTest == MainModel.CALL_STATUS_ALLOC)
            {
                status = "배차완료";
            }
            else if(callStatusForTest == MainModel.CALL_STATUS_BOARD_ON)
            {
                status = "승차";
            }
            else if(callStatusForTest == MainModel.CALL_STATUS_BOARD_OFF)
            {
                status = "하차";
            }
            else if(callStatusForTest == MainModel.CALL_STATUS_CANCEL)
            {
                status = "취소";
            }

            if(status != null)
            {
                RequestResult result = new RequestResult();
                result.setSuccessful(true);
                result.setCallStatus(status);
                callback.onResult(result);
            }
            else
            {
                callback.onResult(null);
            }
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    public static void getDriverInfo(String mobile, String callId, String callDt, HttpCallback callback)
    {
        try
        {
            CarInfoResult result = new CarInfoResult();
            result.setSuccessful(true);
            result.setDriverName("홍길동");
            result.setCarNum("서울 12가 1234");
            result.setCarModel("소나타");
            result.setDriverPhoneNum("010-2737-4765");
            callback.onResult(result);
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    public static void getCarPosition(String mobile, String callId, String callDt, HttpCallback callback)
    {
        try
        {
            LatLng[] positions = {
                    new LatLng(37.540024, 126.735693),
                    new LatLng(37.540024, 126.736208),
                    new LatLng(37.539964, 126.737056),
                    new LatLng(37.539887, 126.737850),
                    new LatLng(37.539810, 126.738783),
                    new LatLng(37.540678, 126.739169),
                    new LatLng(37.541325, 126.738536),
                    new LatLng(37.541044, 126.737603),
                    new LatLng(37.541299, 126.737067),
                    new LatLng(37.541129, 126.736595)

            };

            LatLng latLng = positions[count];

            CarInfoResult result = new CarInfoResult();
            result.setSuccessful(true);
            result.setCarLat(String.valueOf(latLng.latitude));
            result.setCarLon(String.valueOf(latLng.longitude));
            callback.onResult(result);
            //Log.d(AppConstant.LOG_TEMP_TAG, "    car pos = " + result.getCarLat() + "," + result.getCarLon());
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    public static void getHistory(String mobile, HttpCallback callback)
    {
        try
        {
            List<HistoryData> dataList = new ArrayList<>();

            HistoryData data1 = new HistoryData();
            data1.setId("1");
            data1.setCarId("12가 3456");
            data1.setCarTypeName("소나타");
            data1.setDriverName("김팅크");
            data1.setDepartureName("삼환하이펙스");
            data1.setDepartureLat("37.4016952");
            data1.setDepartureLng("127.108565");
            data1.setDestinationName("강남역");
            data1.setDestinationLat("37.4972162");
            data1.setDestinationLng("127.025317");
            data1.setDriverTelNumber("010-2737-4765");
            data1.setGetInTime("2020-08-15T12:32:10.00Z");
            data1.setGetOutTime("2020-08-15T13:50:21.00Z");

            HistoryData data2 = new HistoryData();
            data2.setId("2");
            data2.setCarId("12가 3456");
            data2.setCarTypeName("소나타");
            data2.setDriverName("김팅크");
            data2.setDepartureName("계양구청");
            data2.setDepartureLat("37.5378002");
            data2.setDepartureLng("126.735563");
            data2.setDriverTelNumber("010-2737-4765");
            data2.setGetInTime("2020-08-16T18:00:01.00Z");
            data2.setGetOutTime("2020-08-16T19:15:31.00Z");

            HistoryData data3 = new HistoryData();
            data3.setId("3");
            data3.setCarId("12가 3456");
            data3.setCarTypeName("소나타");
            data3.setDriverName("김팅크");
            data3.setDepartureName("삼환하이펙스");
            data3.setDepartureLat("37.4016952");
            data3.setDepartureLng("127.108565");
            data3.setDestinationName("강남역");
            data3.setDestinationLat("37.4972162");
            data3.setDestinationLng("127.025317");
            data3.setDriverTelNumber("010-2737-4765");
            data3.setGetInTime("2020-08-17T10:03:13.00Z");
            data3.setGetOutTime("2020-08-17T11:05:01.00Z");

            dataList.add(data1);
            dataList.add(data2);
            dataList.add(data3);

            callback.onResult(dataList);
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    public static void sendReview(String mobile, int rate, HttpCallback callback)
    {
        AuthResult result = new AuthResult();
        result.setSuccessful(true);
        callback.onResult(result);
    }

    public static void sendPassengerLocation(String mobileNum, double lat, double lng, String positionName, HttpCallback callback)
    {
        try
        {
            //Log.d(AppConstant.LOG_TEMP_TAG, "[TEST] 콜센터에 위치 정보 전송 : " + lat + "," + lng + " (" + positionName + ")");
            AuthResult result = new AuthResult();
            result.setSuccessful(true);
            callback.onResult(result);
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    public static void recentAllocation(String mobileNum, HttpCallback callback)
    {
        try
        {
            RequestResult result = new RequestResult();
            result.setSuccessful(true);
            result.setCallID("16");
            result.setCallDT("200903");
            result.setCallStatus("접수");
            callback.onResult(result);
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }
}
