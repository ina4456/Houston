package com.insoline.hanam.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;
import com.insoline.hanam.R;
import com.insoline.hanam.activity.auth.MobileNumberVerificationStep1Activity;
import com.insoline.hanam.activity.auth.PermissionCheckActivity;
import com.insoline.hanam.activity.auth.TermsListActivity;
import com.insoline.hanam.activity.main.MainActivity;
import com.insoline.hanam.constant.AppConstant;
import com.insoline.hanam.net.RetrofitConnector;
import com.insoline.hanam.net.response.RequestResult;
import com.insoline.hanam.net.response.VisitedLocationData;
import com.insoline.hanam.util.PermissionUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE;

public class SplashActivity extends BaseActivity {
    private Context context;
    private SharedPreferences pref;

    private String callStatus;
    private List<VisitedLocationData> dataList;

    private boolean callCheckComplete;
    private boolean frequencyVisitedCheckComplete;

    AppUpdateManager appUpdateManager;
    Task<AppUpdateInfo> appUpdateInfoTask;
    int UPDATE_REQUEST_CODE = 100;


    //루팅체크//
    public static final String ROOT_PATH = Environment. getExternalStorageDirectory() + "";
    public static final String ROOTING_PATH_1 = "/system/bin/su";
    public static final String ROOTING_PATH_2 = "/system/xbin/su";
    public static final String ROOTING_PATH_3 = "/system/app/SuperUser.apk";
    public static final String ROOTING_PATH_4 = "/data/data/com.noshufou.android.su";
    public String[] RootFilesPath = new String[]{ ROOT_PATH + ROOTING_PATH_1 , ROOT_PATH + ROOTING_PATH_2 , ROOT_PATH + ROOTING_PATH_3 , ROOT_PATH + ROOTING_PATH_4 };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 두꺼운 글씨체 설정
        Typeface boldTypeface = getBoldTypeface();
        TextView appNameText = findViewById(R.id.app_name);
        TextView appTypeText = findViewById(R.id.app_type);
        appNameText.setTypeface(boldTypeface);
        appTypeText.setTypeface(boldTypeface);

        context = this;
        pref = getSharedPreferences(AppConstant.COMMON_DATA, MODE_PRIVATE);


        checkRooting();

        //업데이트 알림
        appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
        appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();//Returns an intent object that you use to check for an update.


        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                //업데이트 요청을 너무 자주 보낼 경우 사용자가 귀찮아할 수도 있으므로 업데이트 요청 횟수를 염두에 두어야 합니다. 즉, 앱의 기능에 중요한 변경인 경우에만 인앱 업데이트를 요청하도록 제한해야 합니다.
                requestUpdate(appUpdateInfo);
            } else {
                //업데이트는 각 AppUpdateInfo 인스턴스를 사용하여 한 번만 시작할 수 있습니다. 실패한 경우 업데이트를 다시 시도하려면 새 AppUpdateInfo를 요청하고 업데이트가 사용 가능하고 허용되는지 다시 확인해야 합니다.
                Log.d("업데이트 가능 버전 없음", "x");
            }
        });


        new Handler().postDelayed(() ->
        {
            try
            {
                Class<? extends FragmentActivity> targetActivity = null;

                // 권한 확인 후 권한이 없으면 권한 설정 페이지로
                int permissionCount = PermissionUtil.requiredPermissions.length;
                for(int i = 0; i < permissionCount; i++)
                {
                    String permission = PermissionUtil.requiredPermissions[i];
                    if(!PermissionUtil.checkPermission(context, permission)/*필수 권한 없음*/)
                    {
                        // 필수 권한 화면으로 이동
                        Intent intent = new Intent(context, PermissionCheckActivity.class);
                        startActivity(intent);
                        finish();
                        return;
                    }
                }

                // 전화 번호 인증 확인
                String mobileNumber = pref.getString(AppConstant.COMMON_DATA_MOBILE_NUMBER, null);
                if(mobileNumber == null || mobileNumber.trim().isEmpty())
                {
                    // 인증되지 않음. 전화번호 인증 화면으로 이동
                    targetActivity = MobileNumberVerificationStep1Activity.class;
                }
                else
                {
                    // 휴대폰 인증됨
                    boolean agreeTerms = pref.getBoolean(AppConstant.COMMON_DATA_AGREE_TERMS, false);
                    if(!agreeTerms)
                    {
                        // 이용 약관 동의 없으면 이용 약관 동의로
                        targetActivity = TermsListActivity.class;
                    }
                    else
                    {
                        // 빈도수 정보
                        RetrofitConnector.getFrequencyVisitedLocations(mobileNumber, resultObj ->
                        {
                            frequencyVisitedCheckComplete = true;

                            try
                            {
                                //noinspection unchecked
                                dataList = (List<VisitedLocationData>) resultObj;

                                goMain();
                            }
                            catch(Exception e)
                            {

                            }
                        });

                        // call id가 있는지 확인
                        String callId = pref.getString(AppConstant.COMMON_DATA_CALL_ID, null);
                        String callDt = pref.getString(AppConstant.COMMON_DATA_CALL_DT, null);

                        if(callId != null && !callId.trim().isEmpty())
                        {
                            // 콜 체크 후 이동
                            RetrofitConnector.callCheck(mobileNumber, callId, callDt, resultObj ->
                            {
                                try
                                {
                                    RequestResult result = (RequestResult) resultObj;
                                    callStatus = result.getCallStatus();
                                    callCheckComplete = true;

                                    goMain();
                                }
                                catch(Exception e)
                                {

                                }
                            });
                            return;
                        }
                        else
                        {
                            callCheckComplete = true;
                            goMain();
                            return;
                        }
                    }
                }

                if(targetActivity != null)
                {
                    Intent intent = new Intent(context, targetActivity);
                    startActivity(intent);
                    finish();
                }
            }
            catch(Exception e)
            {
                Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
            }

        }, 1500);
    }


    @Override
    protected void onResume() {
        super.onResume();
        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(
                        appUpdateInfo -> {
                            if (appUpdateInfo.updateAvailability()
                                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                                // 인 앱 업데이트가 이미 실행중이었다면 계속해서 진행하도록
                                try {
                                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, SplashActivity.this, UPDATE_REQUEST_CODE);
                                } catch (IntentSender.SendIntentException e) {
                                    e.printStackTrace();
                                }
                            }else{
                                Log.d("업데이트 가능 버전 없음", "x");
                            }
                        });
    }


    public void requestUpdate(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult( // 'getAppUpdateInfo()' 에 의해 리턴된 인텐트
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    this,   // 현재 업데이트 요청을 만든 액티비티, 여기선 MainActivity.
                    UPDATE_REQUEST_CODE); // onActivityResult 에서 사용될 REQUEST_CODE.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == UPDATE_REQUEST_CODE) {
            Toast myToast = Toast.makeText(this.getApplicationContext(), "MY_REQUEST_CODE", Toast.LENGTH_SHORT);
            myToast.show();

            // 업데이트가 성공적으로 끝나지 않은 경우
            if (resultCode != RESULT_OK) {
                Toast.makeText(this.getApplicationContext(), "업데이트 실패", Toast.LENGTH_SHORT).show();
                Log.d("SplashAct", "Update flow failed! Result code: " + resultCode);
                Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();  // 업데이트가 취소되거나 실패하면 업데이트를 다시 요청할 수 있다.,
                appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        // 업데이트를 다시 요청한다.
                        requestUpdate(appUpdateInfo);
                    }
                });
            }else{
                Toast.makeText(this.getApplicationContext(), "업데이트 성공", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void goMain()
    {
        if(callCheckComplete && frequencyVisitedCheckComplete)
        {
            Intent intent = new Intent(context, MainActivity.class);

            if(callStatus != null && !callStatus.trim().isEmpty())
            {
                intent.putExtra("callStatus", callStatus);
            }

            if(dataList != null && dataList.size() > 0)
            {
                ArrayList<VisitedLocationData> list = new ArrayList<>();
                list.addAll(dataList);

                intent.putParcelableArrayListExtra("frequencyVisited", list);
            }

            startActivity(intent);
            finish();
        }
    }





    public void checkRooting(){
        boolean isRootingFlag = false;
        try { Runtime.getRuntime().exec("su");
            isRootingFlag = true;
        } catch ( Exception e) { // Exception 나면 루팅 false;
            isRootingFlag = false;
        }

        if(!isRootingFlag){
            File[] rootingFiles = new File[RootFilesPath.length];
            for(int i=0 ; i < RootFilesPath.length; i++){
                rootingFiles[i] = new File(RootFilesPath[i]);
            }

            for(File f : rootingFiles){
                if(f != null && f.exists() && f.isFile()){
                    isRootingFlag = true;
                    break;
                }else{
                    isRootingFlag = false;
                }
            }
        }
        Log.d("test", "isRootingFlag = " + isRootingFlag);
    }





}
