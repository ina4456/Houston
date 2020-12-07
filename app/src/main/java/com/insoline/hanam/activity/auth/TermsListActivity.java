package com.insoline.hanam.activity.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.insoline.hanam.R;
import com.insoline.hanam.activity.BaseActivity;
import com.insoline.hanam.activity.main.MainActivity;
import com.insoline.hanam.activity.main.MainModel;
import com.insoline.hanam.constant.AppConstant;
import com.insoline.hanam.net.RetrofitConnector;
import com.insoline.hanam.net.response.VisitedLocationData;
import com.insoline.hanam.util.CommonUtil;
import com.insoline.hanam.view.HoustonStyleDialog;
import com.insoline.hanam.view.HoustonStyleDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class TermsListActivity extends BaseActivity
{
    private Context context;
    private SharedPreferences pref;

    private View checkAllBtn;
    private View checkBtn1;
    private View checkBtn2;
    private View checkBtn3;

    private TextView bottomBtn;

    private HoustonStyleDialogBuilder dialogBuilder;

    private boolean fromIntro;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try
        {
            setContentView(R.layout.activity_terms_list);

            context = this;
            pref = getSharedPreferences(AppConstant.COMMON_DATA, MODE_PRIVATE);

            setStatusBarWhite();

            View backBtn = findViewById(R.id.btn_title_back);
            backBtn.setOnClickListener(v ->
            {
                try
                {
                    finish();
                }
                catch(Exception e)
                {
                    Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
                }
            });

            Intent intent = getIntent();
            fromIntro = intent.getBooleanExtra("fromIntro", true);

            View viewDetail1Btn = findViewById(R.id.btn_view_terms_detail1);
            View viewDetail2Btn = findViewById(R.id.btn_view_terms_detail2);
            View viewDetail3Btn = findViewById(R.id.btn_view_terms_detail3);
            viewDetail1Btn.setOnClickListener(clickListener);
            viewDetail2Btn.setOnClickListener(clickListener);
            viewDetail3Btn.setOnClickListener(clickListener);

            TextView text1 = findViewById(R.id.terms_text1);
            TextView text2 = findViewById(R.id.terms_text2);
            TextView text3 = findViewById(R.id.terms_text3);
            text1.setPaintFlags(text1.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            text2.setPaintFlags(text2.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            text3.setPaintFlags(text3.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            checkAllBtn = findViewById(R.id.btn_check_all);
            checkBtn1 = findViewById(R.id.btn_check1);
            checkBtn2 = findViewById(R.id.btn_check2);
            checkBtn3 = findViewById(R.id.btn_check3);

            checkAllBtn.setOnClickListener(v ->
            {
                try
                {
                    boolean selected = v.isSelected();
                    checkAllBtn.setSelected(!selected);
                    checkBtn1.setSelected(!selected);
                    checkBtn2.setSelected(!selected);
                    checkBtn3.setSelected(!selected);
                    bottomBtn.setEnabled(!selected);

                }
                catch(Exception e)
                {
                    Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
                }
            });

            checkBtn1.setOnClickListener(checkBtnClickListener);
            checkBtn2.setOnClickListener(checkBtnClickListener);
            checkBtn3.setOnClickListener(checkBtnClickListener);

            bottomBtn = findViewById(R.id.btn_bottom);
            bottomBtn.setEnabled(false);
            bottomBtn.setOnClickListener(v ->
            {
                try
                {
                    // 최초 설정들
                    preSet();

                    // 메인 화면으로 이동
                    goMain();
                }
                catch(Exception e)
                {
                    Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
                }
            });

            Typeface boldTypeface = getBoldTypeface();
            bottomBtn.setTypeface(boldTypeface);

            TextView titleText = findViewById(R.id.title_text);
            TextView agreeAllBtnText = findViewById(R.id.agree_all_btn_text);
            titleText.setTypeface(boldTypeface);
            agreeAllBtnText.setTypeface(boldTypeface);

            dialogBuilder = new HoustonStyleDialogBuilder(context);

            if(!fromIntro)
            {
                // 설정에서 들어온 경우
                checkBtn1.setVisibility(View.GONE);
                checkBtn2.setVisibility(View.GONE);
                checkBtn3.setVisibility(View.GONE);
                bottomBtn.setVisibility(View.GONE);
                View allAgreeLayout = findViewById(R.id.agree_all_btn_layout);
                allAgreeLayout.setVisibility(View.GONE);
            }
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    private void preSet()
    {
        SharedPreferences.Editor edit = pref.edit();

        // 모든 약관 동의
        edit.putBoolean(AppConstant.COMMON_DATA_AGREE_TERMS, true);

        // 첫 사용일 테니까 콜센터를 모두 선택한다.
        String[] callCenterList = MainModel.callCenterPhoneNums;
        int len = callCenterList.length;
        TreeSet<String> callCenterSet = new TreeSet<>();
        for(int i = 0; i < len; i++)
        {
            String phoneNum = callCenterList[i];
            callCenterSet.add(phoneNum);
        }

        String text = CommonUtil.getSelectedText(callCenterSet);
        edit.putString(AppConstant.COMMON_DATA_CALL_CENTER, text);

        edit.commit();
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
                Intent intent1 = new Intent(context, MainActivity.class);
                if(dataList != null && dataList.size() > 0)
                {
                    ArrayList<VisitedLocationData> list = new ArrayList<>();
                    list.addAll(dataList);

                    intent1.putParcelableArrayListExtra("frequencyVisited", list);
                }

                startActivity(intent1);
                finish();
            }
            catch(Exception e)
            {

            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        try
        {
            if(!fromIntro)
            {
                return;
            }

            // GPS on/off 확인
            MainModel mainModel = new MainModel(context);
            if(!mainModel.isGpsAvailable())
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
            }
        }
        catch(Exception e)
        {

        }
    }

    private View.OnClickListener checkBtnClickListener = v ->
    {
        try
        {
            boolean selected = v.isSelected();
            v.setSelected(!selected);

            if(selected)
            {
                checkAllBtn.setSelected(false);
                bottomBtn.setEnabled(false);
            }
            else
            {
                if(checkBtn1.isSelected() && checkBtn2.isSelected() && checkBtn3.isSelected())
                {
                    checkAllBtn.setSelected(true);
                    bottomBtn.setEnabled(true);
                }
            }
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    };

    private View.OnClickListener clickListener = v ->
    {
        try
        {
            // 이용 약관 상세 화면 보기
            int termsType = 0;

            switch(v.getId())
            {
                case R.id.btn_view_terms_detail1:
                    termsType = 1;
                    break;

                case R.id.btn_view_terms_detail2:
                    termsType = 2;
                    break;

                case R.id.btn_view_terms_detail3:
                    termsType = 3;
                    break;
            }

            Intent intent = new Intent(context, TermsDetailActivity.class);
            intent.putExtra("termsType", termsType);
            startActivity(intent);
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    };
}
