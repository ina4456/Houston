package com.insoline.hanam.activity.auth;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import com.insoline.hanam.R;
import com.insoline.hanam.activity.BaseActivity;
import com.insoline.hanam.activity.main.MainActivity;
import com.insoline.hanam.constant.AppConstant;
import com.insoline.hanam.net.RetrofitConnector;
import com.insoline.hanam.net.response.VisitedLocationData;
import com.insoline.hanam.util.PermissionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PermissionCheckActivity extends BaseActivity
{
    private Context context;
    private SharedPreferences pref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try
        {
            setContentView(R.layout.activity_permission_check);

            context = this;
            pref = getSharedPreferences(AppConstant.COMMON_DATA, MODE_PRIVATE);

            setStatusBarWhite();

            Typeface boldTypeface = getBoldTypeface();

            TextView titleText = findViewById(R.id.title_text);
            String text = String.format(Locale.getDefault(),
                    getString(R.string.permission_check_title), getString(R.string.app_name));
            titleText.setText(text);
            titleText.setTypeface(boldTypeface);

            TextView bottomBtn = findViewById(R.id.btn_bottom);
            bottomBtn.setOnClickListener(v ->
            {
                try
                {
                    // 필수 권한 요청
                    PermissionUtil.checkAndRequestPermission(context, PermissionUtil.requiredPermissions, 1, rationaleListener);
                }
                catch(Exception e)
                {
                    Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
                }
            });

            bottomBtn.setTypeface(boldTypeface);

            TextView subTitleText1 = findViewById(R.id.sub_title_text1);
            TextView subTitleText2 = findViewById(R.id.sub_title_text2);
            TextView subTitleText3 = findViewById(R.id.sub_title_text3);
            subTitleText1.setTypeface(boldTypeface);
            subTitleText2.setTypeface(boldTypeface);
            subTitleText3.setTypeface(boldTypeface);
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    private AlertDialog dialog1;
    private AlertDialog dialog2;

    private PermissionUtil.OnShouldShowRequestPermissionRationaleListener rationaleListener =
            permission ->
            {
                try
                {
                    String title = null;
                    String message = null;

                    if(permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION))
                    {
                        if(dialog1 == null)
                        {
                            title = getString(R.string.permission_check_sub_title1);
                            message = getString(R.string.permission_check_rationale_text1);

                            AlertDialog.Builder builder = new AlertDialog.Builder(context);

                            builder.setMessage(String.format(Locale.getDefault(), "[%s] %s", title, message))
                                    .setCancelable(true)
                                    .setPositiveButton(getString(R.string.app_text_ok), (dialog, which) -> dialog1 = null);

                            dialog1 = builder.create();
                            dialog1.show();
                        }
                    }
                    else if(permission.equals(Manifest.permission.READ_PHONE_STATE) ||
                            permission.equals(Manifest.permission.READ_CONTACTS))
                    {
                        if(dialog2 == null)
                        {
                            title = getString(R.string.permission_check_sub_title2);
                            message = getString(R.string.permission_check_rationale_text2);

                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setMessage(String.format(Locale.getDefault(), "[%s] %s", title, message))
                                    .setCancelable(true)
                                    .setPositiveButton(getString(R.string.app_text_ok), (dialog, which) -> dialog2 = null);

                            dialog2 = builder.create();
                            dialog2.show();
                        }
                    }
                    else
                    {
                        return;
                    }
                }
                catch(Exception e)
                {
                    Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
                }
            };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        try
        {
            int grantedCount = 0;
            int count = permissions.length;
            if(count == 0)
            {
                return;
            }

            for(int i = 0; i < count; i++)
            {
                if(grantResults[i] == PackageManager.PERMISSION_DENIED)
                {
                    // 권한 거부
                }
                else
                {
                    grantedCount++;
                }
            }

            if(grantedCount == count)
            {
                if(dialog1 != null)
                {
                    dialog1.hide();
                    dialog1 = null;
                }

                if(dialog2 != null)
                {
                    dialog2.hide();
                    dialog2 = null;
                }

                String mobileNumber = pref.getString(AppConstant.COMMON_DATA_MOBILE_NUMBER, null);
                boolean agreeTerms = pref.getBoolean(AppConstant.COMMON_DATA_AGREE_TERMS, false);

                if(mobileNumber == null || mobileNumber.trim().isEmpty())
                {
                    // 휴대폰 인증 화면으로 이동
                    Intent intent = new Intent(context, MobileNumberVerificationStep1Activity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                else if(!agreeTerms)
                {
                    // 약관 동의 화면으로 이동
                    Intent intent = new Intent(context, TermsListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                else
                {
                    // 메인 액티비티로 연결
                    goMain();
                }
            }
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    private void goMain()
    {
        // 빈도수 정보 확인 후 메인 화면 시작
        String mobileNumber = pref.getString(AppConstant.COMMON_DATA_MOBILE_NUMBER, null);
        RetrofitConnector.getFrequencyVisitedLocations(mobileNumber, resultObj ->
        {
            List<VisitedLocationData> dataList = null;

            try
            {
                //noinspection unchecked
                dataList = (List<VisitedLocationData>) resultObj;
            }
            catch(Exception e)
            {

            }

            try
            {
                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);

                if(dataList != null && dataList.size() > 0)
                {
                    ArrayList<VisitedLocationData> list = new ArrayList<>();
                    list.addAll(dataList);

                    intent.putParcelableArrayListExtra("frequencyVisited", list);
                }

                startActivity(intent);
            }
            catch(Exception e)
            {

            }
        });
    }
}
