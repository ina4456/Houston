package com.insoline.hanam.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.inavi.mapsdk.geometry.LatLng;
import com.insoline.hanam.R;
import com.insoline.hanam.activity.main.MainModel;
import com.insoline.hanam.constant.AppConstant;
import com.insoline.hanam.net.RetrofitConnector;
import com.insoline.hanam.net.response.CarInfoResult;
import com.insoline.hanam.net.response.RequestResult;
import com.insoline.hanam.net.response.inaviapi.InaviApiRouteNormalResult;
import com.insoline.hanam.util.NotificationUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CallCheckService extends Service
{
    private Context context;
    private MainModel mainModel;

    private static Set<Integer> messageSentSet;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private Future<?> timerTask = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        try
        {
            context = this;
            mainModel = new MainModel(context);

            messageSentSet = new HashSet<>();
        }
        catch(Exception e)
        {

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        try
        {
            int callStatus = intent.getIntExtra("callStatus", 0);
            mainModel.setCallStatus(callStatus);

            setCallCheckTask();
        }
        catch(Exception e)
        {

        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        try
        {
            //Log.d(AppConstant.LOG_TEMP_TAG, "onDestroy");
            messageSentSet.clear();

            if(timerTask != null)
            {
                timerTask.cancel(true);
                timerTask = null;
            }

            if(!scheduler.isShutdown())
            {
                scheduler.shutdown();
            }
        }
        catch(Exception e)
        {

        }
    }

    private Runnable callCheckRunnable = () ->
    {
        try
        {
            callCheck();
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    };

    private Runnable callCenterCheckRunnable = () ->
    {
        try
        {
            callCenterCheck();
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    };

    @SuppressWarnings("PointlessArithmeticExpression")
    public static long ONE_MIN_IN_MILLS = 1/*min*/ * 60/*sec*/ * 1000/*ms*/;

    private void setCallCheckTask()
    {
        String callId = mainModel.getCallId();
        if(callId != null && !callId.trim().isEmpty())
        {
            if(timerTask != null)
            {
                timerTask.cancel(true);
                timerTask = null;
            }

            int callStatus = mainModel.getCallStatus();
            int period = 0;

            if(callStatus == MainModel.CALL_STATUS_ALLOC_WAIT)
            {
                period = 2000;  // 2sec
            }
            else
            {
                period = 10000; // 10sec
            }

            timerTask = scheduler.scheduleAtFixedRate(callCheckRunnable, 0, period, TimeUnit.MILLISECONDS);
        }
        else
        {
            long now = System.currentTimeMillis();
            long callCenterDtime = mainModel.getCallCenterDtime();

            // 콜센터 버튼을 눌렀는지 확인
            if(callCenterDtime > 0)
            {
                if(now - callCenterDtime <= ONE_MIN_IN_MILLS)
                {
                    // 1분 이내. 콜센터에서 배차 접수를 했는지 확인(2초 주기)
                    timerTask = scheduler.scheduleAtFixedRate(callCenterCheckRunnable, 0, 2000, TimeUnit.MILLISECONDS);
                }
                else
                {
                    mainModel.removeCallCenterDtime();
                }
            }
        }
    }

    private void callCheck()
    {
        String callId = mainModel.getCallId();
        if(callId == null || callId.trim().isEmpty())
        {
            return;
        }

        RetrofitConnector.callCheck(mainModel.getMobileNum(), mainModel.getCallId(), mainModel.getCallDt(), resultObj ->
        {
            try
            {
                RequestResult result = (RequestResult) resultObj;
                if(result.isSuccessful())
                {
                    int callStatusOld = mainModel.getCallStatus();
                    String status = result.getCallStatus();
                    //Log.d(AppConstant.LOG_TEMP_TAG, "status = " + status + " in service");

                    try
                    {
                        mainModel.setCallStatus(status);
                    }
                    catch(Exception e)
                    {
                        return;
                    }

                    int callStatus = mainModel.getCallStatus();

                    if(callStatusOld == MainModel.CALL_STATUS_ALLOC_WAIT &&
                            callStatus == MainModel.CALL_STATUS_ALLOC)
                    {
                        // 주기가 바뀌므로 콜 체크 작업을 재설정한다.
                        setCallCheckTask();

                        // 배차 완료 메시지
                        sendAllocMessage();

                        // 기사님 차량 정보
                        getDriverInfo();
                    }
                    else
                    {
                        switch(callStatus)
                        {
                            case MainModel.CALL_STATUS_ALLOC:
                                // 기사님과의 거리 측정
                                measureDriverDistance();
                                break;

                            case MainModel.CALL_STATUS_BOARD_ON:
                                LatLng destination = mainModel.getCallDestinationPoint();
                                if(destination != null)
                                {
                                    // 내 위치와 목적지 사이 거리 측정
                                    measureDestinationDistance();
                                }
                                break;

                            case MainModel.CALL_STATUS_BOARD_OFF:
                            case MainModel.CALL_STATUS_CANCEL:
                            case MainModel.CALL_STATUS_CANCEL_BY_DRIVER:
                                // 서비스 종료

                                // 하차(도착) 또는 배차 취소. 콜 체크 중지
                                if(timerTask != null)
                                {
                                    timerTask.cancel(true);
                                    timerTask = null;
                                }

                                stopSelf();
                                break;
                        }
                    }
                }
            }
            catch(Exception e)
            {

            }
        });
    }

    private void callCenterCheck()
    {
        RetrofitConnector.recentAllocation(mainModel.getMobileNum(), resultObj ->
        {
            try
            {
                long now = System.currentTimeMillis();
                long callCenterDtime = mainModel.getCallCenterDtime();

                if(callCenterDtime <= 0)
                {
                    // 중간에 콜센터 체크가 종료됨
                    if(timerTask != null)
                    {
                        timerTask.cancel(true);
                        timerTask = null;
                    }
                    return;
                }

                RequestResult result = (RequestResult) resultObj;
                if(result != null && result.isSuccessful())
                {
                    String callId = result.getCallID();
                    String callStatus = result.getCallStatus();
                    mainModel.setCallStatus(callStatus);

                    int status = mainModel.getCallStatus();
                    if(callId != null && !callId.trim().isEmpty() &&
                            status != MainModel.CALL_STATUS_BOARD_OFF/*완료된 건*/ &&
                            status != MainModel.CALL_STATUS_CANCEL/*취소된 건, 아마도 기존 요청 건 중 호출 취소, 자동 취소 건*/ &&
                            status != MainModel.CALL_STATUS_CANCEL_BY_DRIVER)
                    {
                        mainModel.removeCallCenterDtime();

                        if(timerTask != null)
                        {
                            timerTask.cancel(true);
                            timerTask = null;
                        }

                        mainModel.setCallId(callId);
                        mainModel.setCallDt(result.getCallDT());
                        mainModel.setCallCenterCheckFlag();

                        // 콜체크로 변경
                        setCallCheckTask();
                        return;
                    }
                    else
                    {
                        mainModel.setCallStatus(0);
                    }
                }

                if(now - callCenterDtime > ONE_MIN_IN_MILLS)
                {
                    // 1분 초과. 콜센터 체크 종료
                    mainModel.removeCallCenterDtime();

                    if(timerTask != null)
                    {
                        timerTask.cancel(true);
                        timerTask = null;
                    }
                }
            }
            catch(Exception e)
            {

            }
        });
    }

    private void sendAllocMessage()
    {
        if(mainModel.getCallStatus() == MainModel.CALL_STATUS_ALLOC)
        {
            RetrofitConnector.getCarPosition(mainModel.getMobileNum(),
                    mainModel.getCallId(), mainModel.getCallDt(), resultObj ->
                    {
                        try
                        {
                            // 기사님 위치
                            CarInfoResult result = (CarInfoResult) resultObj;
                            double lat = Double.parseDouble(result.getCarLat());
                            double lng = Double.parseDouble(result.getCarLon());
                            LatLng driverLatLng = new LatLng(lat, lng);

                            // 출발지
                            LatLng departure = mainModel.getCallDeparturePoint();

                            // 기사님 위치와 출발지 사이의 소요시간 측정(아이나비)
                            RetrofitConnector.getRouteSearchNormalResult(departure, driverLatLng, resultObj1 ->
                            {
                                try
                                {
                                    InaviApiRouteNormalResult result1 = (InaviApiRouteNormalResult) resultObj1;
                                    int spendTime = RetrofitConnector.getRouteSpendTime(result1);

                                    if(spendTime >= 0)
                                    {
                                        int min = spendTime / 60;
                                        String title = String.format(getString(R.string.notification_text_type1), min);
                                        NotificationUtil.notify(context, 1, title);
                                    }
                                }
                                catch(Exception e)
                                {

                                }
                            });
                        }
                        catch(Exception e)
                        {

                        }
                    });
        }
    }

    private void measureDriverDistance()
    {
        if(messageSentSet.contains(2))
        {
            // 이미 알림을 보낸 적이 있음
            return;
        }

        if(mainModel.getCallStatus() == MainModel.CALL_STATUS_ALLOC)
        {
            RetrofitConnector.getCarPosition(mainModel.getMobileNum(),
                    mainModel.getCallId(), mainModel.getCallDt(), resultObj ->
                    {
                        try
                        {
                            // 기사님 위치
                            CarInfoResult result = (CarInfoResult) resultObj;
                            double lat = Double.parseDouble(result.getCarLat());
                            double lng = Double.parseDouble(result.getCarLon());
                            LatLng driverLatLng = new LatLng(lat, lng);

                            // 출발지
                            LatLng departure = mainModel.getCallDeparturePoint();

                            // 기사님 위치와 출발지 사이의 소요시간 측정(아이나비)
                            RetrofitConnector.getRouteSearchNormalResult(departure, driverLatLng, resultObj1 ->
                            {
                                try
                                {
                                    InaviApiRouteNormalResult result1 = (InaviApiRouteNormalResult) resultObj1;
                                    int spendTime = RetrofitConnector.getRouteSpendTime(result1);

                                    if(spendTime >= 0)
                                    {
                                        String carNumber = mainModel.getCallDriverCarNumber();
                                        int min = spendTime / 60;
                                        if(min <= 1)
                                        {
                                            messageSentSet.add(2);
                                            String title = String.format(getString(R.string.notification_text_type2), carNumber);
                                            NotificationUtil.notify(context, 2, title);
                                        }
                                    }
                                }
                                catch(Exception e)
                                {

                                }
                            });
                        }
                        catch(Exception e)
                        {

                        }
                    });
        }
    }

    private void measureDestinationDistance()
    {
        if(messageSentSet.contains(3))
        {
            // 이미 알림을 보낸 적이 있음
            return;
        }

        if(mainModel.getCallStatus() == MainModel.CALL_STATUS_BOARD_ON)
        {
            // 내 위치
            LatLng latLng = mainModel.getLocationInstantly();

            // 목적지
            LatLng destination = mainModel.getCallDestinationPoint();

            // 내 위치와 목적지 사이의 소요시간 측정(아이나비)
            RetrofitConnector.getRouteSearchNormalResult(destination, latLng, resultObj1 ->
            {
                try
                {
                    InaviApiRouteNormalResult result1 = (InaviApiRouteNormalResult) resultObj1;
                    int spendTime = RetrofitConnector.getRouteSpendTime(result1);

                    if(spendTime >= 0)
                    {
                        int min = spendTime / 60;
                        if(min <= 1)
                        {
                            messageSentSet.add(3);
                            String title = getString(R.string.notification_text_type3);
                            NotificationUtil.notify(context, 3, title);
                        }
                    }
                }
                catch(Exception e)
                {

                }
            });
        }
    }

    /**
     * 기사님 정보 저장(앱이 포어그라운드로 들어왔을 때를 위해)
     */
    private void getDriverInfo()
    {
        RetrofitConnector.getDriverInfo(mainModel.getMobileNum(),
                mainModel.getCallId(), mainModel.getCallDt(), resultObj ->
                {
                    try
                    {
                        // 기사님 정보
                        CarInfoResult result = (CarInfoResult) resultObj;
                        String carModel = result.getCarModel();
                        String carNumber = result.getCarNum();

                        String carNumberFull = null;
                        if(carModel != null && !carModel.trim().isEmpty())
                        {
                            carNumberFull = (carModel + " " + carNumber);
                        }
                        else
                        {
                            carNumberFull = carNumber;
                        }

                        String driverPhoneNum = result.getDriverPhoneNum();
                        mainModel.setCallDriverPhoneNumber(driverPhoneNum);
                        mainModel.setCallDriverCarNumber(carNumberFull);
                    }
                    catch(Exception e)
                    {

                    }
                });
    }
}