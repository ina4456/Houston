package com.insoline.hanam.activity.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import com.inavi.mapsdk.geometry.LatLng;
import com.inavi.mapsdk.maps.InaviMapSdk;
import com.inavi.mapsdk.maps.InvMapFragment;
import com.insoline.hanam.R;
import com.insoline.hanam.activity.BaseActivity;
import com.insoline.hanam.constant.AppConstant;
import com.insoline.hanam.dto.RequestLocation;
import com.insoline.hanam.net.RetrofitConnector;
import com.insoline.hanam.net.response.AuthResult;
import com.insoline.hanam.net.response.inaviapi.InaviApiReverseGeocode;
import com.insoline.hanam.service.CallCheckService;
import com.insoline.hanam.util.CommonUtil;
import com.insoline.hanam.util.PermissionUtil;
import com.insoline.hanam.view.HoustonStyleDialog;
import com.insoline.hanam.view.HoustonStyleDialogBuilder;

public class MainActivity extends BaseActivity
{
    private Context context;
    private SharedPreferences pref;

    private InvMapFragment invMapFragment;
    private MainPresenter presenter;
    private HoustonStyleDialogBuilder dialogBuilder;
    private MainModel mainModel;

    private boolean versionCheckOk;

    private boolean getDepartureAddress;
    private boolean getDestinationAddress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try
        {
            setContentView(R.layout.activity_main);

            initSystemData();

            // 프레젠터 생성
            initPresenter();

            // 아이나비 맵 설정
            initInaviMap();

            // 기타 뷰 설정
            initView();
        }
        catch(Exception e)
        {
            //e.printStackTrace();
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    private void initSystemData()
    {
        context = this;
        pref = getSharedPreferences(AppConstant.COMMON_DATA, MODE_PRIVATE);
        mainModel = new MainModel(context);
    }

    private void initPresenter()
    {
        boolean fullStatusBarStyleEnable = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
        presenter = new MainPresenter(this, fullStatusBarStyleEnable);
        presenter.measureLocationOneTime(); // 위치 교정을 위해 위치를 한 번 측정한다.
    }

    private void initInaviMap()
    {
        InaviMapSdk.getInstance(this).setAuthFailureCallback((errCode, msg) ->
        {
            try
            {
                Log.d(AppConstant.LOG_DEBUG_TAG, "inavi map init error! errorCode = " + errCode);
            }
            catch(Exception e)
            {
                Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
            }
        });

        invMapFragment = (InvMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        invMapFragment.getMapAsync(inaviMap ->
        {
            try
            {
                presenter.setInaviMap(inaviMap, invMapFragment.getMapView());
                presenter.initLocation(false);
            }
            catch(Exception e)
            {
                Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
            }
        });
    }

    private void initView()
    {
        dialogBuilder = new HoustonStyleDialogBuilder(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        try
        {
            try
            {
                // 백그라운드 체크 서비스 종료(기존에 존재하고 있던 서비스)
                Intent intent = new Intent(context, CallCheckService.class);
                stopService(intent);
            }
            catch(Exception e)
            {

            }

            if(checkLocationPermission())
            {
                if(!versionCheckOk/*필수 업데이트 체크(1회)*/)
                {
                    checkVersion();
                }
                else
                {
                    if(checkLocationInfoAvailable()/*GPS on/off 확인*/)
                    {
                        // 프리젠터 resume 작업
                        presenter.onResume();
                    }
                }
            }
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        try
        {
            // 프리젠터 pause 작업
            presenter.onPause();

            String callId = presenter.getCallId();
            long callCenterDtime = presenter.getCallCenterDtime();

            if(isFinishing())
            {
                // 앱 종료시. 콜센터 체크는 백그라운드로 들어갈 때만 한다.
                callCenterDtime = 0;
                presenter.removeCallCenterDtime();
            }

            if((callId != null && !callId.trim().isEmpty()) || callCenterDtime > 0)
            {
                // 백그라운드 체크 서비스 실행
                Intent intent = new Intent(context, CallCheckService.class);
                intent.putExtra("callStatus", presenter.getCallStatus());
                startService(intent);
            }
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        try
        {
            presenter.onDestroy();
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    @Override
    public void onBackPressed()
    {
        try
        {
            if(presenter.canIQuit())
            {
                // quit app
                super.onBackPressed();
            }
            else
            {
                // 프리젠터가 백버튼 이벤트 처리
                presenter.onBackPressed();
            }
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        try
        {
            switch(requestCode)
            {
                case AppConstant.REQ_CODE_MENU_HISTORY:
                    if(resultCode == RESULT_OK)
                    {
                        // 선택된 경로로 바로 호출

                        RequestLocation departure = data.getParcelableExtra("departure");
                        RequestLocation destination = data.getParcelableExtra("destination");

                        if(departure == null)
                        {
                            boolean changeHistory = data.getBooleanExtra("changeHistory", false);
                            if(changeHistory)
                            {
                                presenter.refreshFrequencyVisitedLocations(null);
                            }
                        }
                        else
                        {
                            // 주소가 필요함
                            getDepartureAddress = false;
                            getDestinationAddress = (destination == null);
                            mainModel.setRequestLocation(null);
                            mainModel.setRequestDestinationLocation(null);

                            RetrofitConnector.getReverseGeocode(departure.lat, departure.lng, resultObj ->
                            {
                                try
                                {
                                    getDepartureAddress = true;
                                    InaviApiReverseGeocode result = (InaviApiReverseGeocode) resultObj;
                                    mainModel.setRequestLocation(result, new LatLng(departure.lat, departure.lng));
                                    RequestLocation location = mainModel.getRequestLocation();
                                    departure.address = location.address;
                                    mainModel.setRequestLocation(departure);

                                    requestCall();
                                }
                                catch(Exception e)
                                {

                                }
                            });

                            if(destination != null)
                            {
                                RetrofitConnector.getReverseGeocode(destination.lat, destination.lng, resultObj ->
                                {
                                    try
                                    {
                                        getDestinationAddress = true;
                                        InaviApiReverseGeocode result = (InaviApiReverseGeocode) resultObj;
                                        mainModel.setRequestDestinationLocation(result, new LatLng(destination.lat, destination.lng));
                                        RequestLocation location = mainModel.getRequestDestinationLocation();
                                        destination.address = location.address;
                                        mainModel.setRequestDestinationLocation(destination);

                                        requestCall();
                                    }
                                    catch(Exception e)
                                    {

                                    }
                                });
                            }
                        }
                    }
                    break;

                case AppConstant.REQ_CODE_MENU_SETTING:
                    break;
            }
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void requestCall()
    {
        if(getDepartureAddress && getDestinationAddress)
        {
            presenter.requestUsingHistory(mainModel.getRequestLocation(), mainModel.getRequestDestinationLocation());
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 필수 업데이트 확인
     */
    private void checkVersion()
    {
        String mobileNum = pref.getString(AppConstant.COMMON_DATA_MOBILE_NUMBER, null);
        RetrofitConnector.versionCheck(mobileNum, resultObj ->
        {
            try
            {
                AuthResult result = (AuthResult) resultObj;
                if(result != null)
                {
                    boolean successful = result.isSuccessful();
                    boolean forceful = result.isForceful();
                    String versionName = result.getVersion();
                    boolean validVersion = false;

                    if(versionName == null || versionName.trim().isEmpty())
                    {
                        // 버전 정보 없음. 성공으로 간주
                        successful = true;
                        forceful = false;
                    }
                    else
                    {
                        long versionCode = CommonUtil.getVersionCode(context);
                        long versionCodeCompared = Long.parseLong(versionName.replaceAll("\\.", ""));

                        if(versionCodeCompared <= versionCode)
                        {
                            validVersion = true;
                        }
                    }

                    if(successful && !validVersion)
                    {
                        HoustonStyleDialog.OnNegativeButtonListener onNegativeButtonListener = null;
                        String negativeBtnText = getString(R.string.app_text_cancel);

                        if(forceful)
                        {
                            onNegativeButtonListener = onNegativeButtonListenerForceful;
                            negativeBtnText = getString(R.string.app_text_finish_app);
                        }
                        // else... 강제 업데이트가 아닐 때는 별도 작업 필요 없음

                        // 필수 업데이트 있음
                        dialogBuilder.setCancelable(false)
                                .setBoldTitle(getString(R.string.main_text_update_alarm))
                                .setContent(getString(R.string.main_text_update_alarm_content))
                                .setNegativeButton(negativeBtnText, onNegativeButtonListener)
                                .setPositiveButton(getString(R.string.app_text_update), () ->
                                {
                                    try
                                    {
                                        // 업데이트 화면으로 이동
                                        CommonUtil.goAndroidMarket(context);
                                    }
                                    catch(Exception e)
                                    {

                                    }
                                });

                        HoustonStyleDialog dialog = dialogBuilder.build();
                        dialog.show();
                    }
                    else    // successful=false면 버전 체크 생략
                    {
                        versionCheckOk = true;

                        if(checkLocationInfoAvailable()/*GPS on/off 확인*/)
                        {
                            // 프리젠터 resume 작업
                            presenter.onResume();
                        }
                    }
                }
            }
            catch(Exception e)
            {

            }
        });
    }

    private HoustonStyleDialog.OnNegativeButtonListener onNegativeButtonListenerForceful = () ->
    {
        try
        {
            // 앱 종료
            finish();
        }
        catch(Exception e)
        {

        }
    };

    /**
     * 위치 정보 On/Off 확인
     * @return
     */
    private boolean checkLocationInfoAvailable()
    {
        if(!presenter.isGpsAvailable())
        {
            String title = String.format(getString(R.string.main_text_gps_on_off_check), getString(R.string.app_name));
            dialogBuilder.setCancelable(false).setTitle(title)
                    .setNegativeButton(getString(R.string.app_text_finish_app), () ->
                    {
                        try
                        {
                            // 앱 종료
                            finish();
                        }
                        catch(Exception e)
                        {

                        }
                    })
                    .setPositiveButton(getString(R.string.app_text_turn_on_gps), () ->
                    {
                        try
                        {
                            // 위치 서비스 설정 페이지 열기
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                        }
                        catch(Exception e)
                        {

                        }
                    });

            HoustonStyleDialog dialog = dialogBuilder.build();
            dialog.show();

            return false;
        }

        return true;
    }

    /**
     * 위치 권한 확인(앱 작동에 필수)
     * @return
     */
    private boolean checkLocationPermission()
    {
        if(!PermissionUtil.checkPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                !PermissionUtil.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION))
        {
            // 위치 권한이 없다. 앱 종료
            finish();
            return false;
        }

        return true;
    }
}
