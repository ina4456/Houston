package com.insoline.hanam.activity.menu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.insoline.hanam.R;
import com.insoline.hanam.activity.BaseActivity;
import com.insoline.hanam.activity.auth.TermsListActivity;
import com.insoline.hanam.activity.main.MainModel;
import com.insoline.hanam.constant.AppConstant;
import com.insoline.hanam.dto.RequestLocation;
import com.insoline.hanam.util.CommonUtil;
import com.insoline.hanam.view.HoustonStyleDialog;
import com.insoline.hanam.view.HoustonStyleDialogBuilder;

import java.util.TreeSet;

public class MenuSettingActivity extends BaseActivity
{
    private Context context;
    private SharedPreferences pref;

    // 자주 가는 곳 ////////////////////////////

    private TextView homeAddressText;
    private View homeAddBtn;
    private View homeDeleteBtn;

    private TextView officeAddressText;
    private View officeAddBtn;
    private View officeDeleteBtn;

    // 환경설정 ////////////////////////////////

    private View callCenterSettingBtn;
    private TextView callCenterSelected;

    private TextView appVersionText;

    private View termsBtn;

    private RequestLocation homeInfo;
    private RequestLocation officeInfo;

    private HoustonStyleDialogBuilder dialogBuilder;
    private HoustonStyleDialog dialog;

    private boolean infoChanged;

    private static final int REQ_FAVORITE_LOCATION      = 1;
    private static final int REQ_CALL_CENTER            = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try
        {
            setContentView(R.layout.activity_menu_setting);

            context = this;
            pref = getSharedPreferences(AppConstant.COMMON_DATA, MODE_PRIVATE);

            setStatusBarWhite();

            initView();
            initData();
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    private void initView()
    {
        dialogBuilder = new HoustonStyleDialogBuilder(context);

        Typeface boldTypeface = getBoldTypeface();

        TextView titleText = findViewById(R.id.title_text);
        TextView favoritePlaceTitle = findViewById(R.id.favorite_place_title);
        TextView preferenceTitle = findViewById(R.id.preference_title);
        titleText.setTypeface(boldTypeface);
        favoritePlaceTitle.setTypeface(boldTypeface);
        preferenceTitle.setTypeface(boldTypeface);

        View actionExitBtn = findViewById(R.id.action_btn_exit);
        actionExitBtn.setOnClickListener(clickListener);

        homeAddressText = findViewById(R.id.home_address_text);
        homeAddBtn = findViewById(R.id.btn_add_home);
        homeDeleteBtn = findViewById(R.id.btn_delete_home);

        officeAddressText = findViewById(R.id.office_address_text);
        officeAddBtn = findViewById(R.id.btn_add_office);
        officeDeleteBtn = findViewById(R.id.btn_delete_office);

        callCenterSettingBtn = findViewById(R.id.btn_call_center_setting);
        callCenterSelected = findViewById(R.id.call_center_name_selected);
        appVersionText = findViewById(R.id.app_version_text);
        termsBtn = findViewById(R.id.btn_terms);

        homeAddBtn.setOnClickListener(clickListener);
        homeDeleteBtn.setOnClickListener(clickListener);
        officeAddBtn.setOnClickListener(clickListener);
        officeDeleteBtn.setOnClickListener(clickListener);
        callCenterSettingBtn.setOnClickListener(clickListener);
        termsBtn.setOnClickListener(clickListener);

        getCallCenterInfo();

        String appVer = CommonUtil.getVersionName(context);
        appVersionText.setText("v" + appVer);
    }

    private void initData()
    {
        getHomeInfo();
        getOfficeInfo();
    }

    private void getHomeInfo()
    {
        homeInfo = CommonUtil.getFavoriteLocation(AppConstant.HOME, pref);

        if(homeInfo != null)
        {
            homeAddressText.setText(homeInfo.positionName);
            homeAddressText.setVisibility(View.VISIBLE);
            homeDeleteBtn.setVisibility(View.VISIBLE);
            homeAddBtn.setVisibility(View.GONE);
        }
        else
        {
            homeAddressText.setVisibility(View.GONE);
            homeDeleteBtn.setVisibility(View.GONE);
            homeAddBtn.setVisibility(View.VISIBLE);
        }
    }

    private void getOfficeInfo()
    {
        officeInfo = CommonUtil.getFavoriteLocation(AppConstant.OFFICE, pref);

        if(officeInfo != null)
        {
            officeAddressText.setText(officeInfo.positionName);
            officeAddressText.setVisibility(View.VISIBLE);
            officeDeleteBtn.setVisibility(View.VISIBLE);
            officeAddBtn.setVisibility(View.GONE);
        }
        else
        {
            officeAddressText.setVisibility(View.GONE);
            officeDeleteBtn.setVisibility(View.GONE);
            officeAddBtn.setVisibility(View.VISIBLE);
        }
    }

    private void getCallCenterInfo()
    {
        String[] callCenterList = MainModel.callCenterPhoneNums;
        TreeSet<String> callCenterSet = CommonUtil.getSelectedCallCenter(pref);

        if(callCenterSet.size() <= 0)
        {
            callCenterSelected.setText(getString(R.string.menu_text_setting_need_regist_call_center));
        }
        else
        {
            if(callCenterList.length == callCenterSet.size())
            {
                // 콜센터 모두 이용
                callCenterSelected.setText(getString(R.string.menu_text_setting_use_all_call_centers));
            }
            else
            {
                // 콜센터 일부 이용
                callCenterSelected.setText(getString(R.string.menu_text_setting_use_call_centers_partially));
            }
        }
    }

    private View.OnClickListener clickListener = v ->
    {
        try
        {
            Intent intent = null;

            switch(v.getId())
            {
                case R.id.action_btn_exit:
                    finishTask();
                    break;

                case R.id.btn_add_home:
                    intent = new Intent(context, FavoriteLocationActivity.class);
                    intent.putExtra("type", AppConstant.HOME);
                    startActivityForResult(intent, REQ_FAVORITE_LOCATION);
                    break;

                case R.id.btn_delete_home:
                    if(dialog != null)
                    {
                        dialog.dismiss();
                    }

                    dialogBuilder.setTitle(getString(R.string.menu_text_delete_home))
                            .setNegativeButton(getString(R.string.app_text_cancel), () -> dialog = null)
                            .setPositiveButton(getString(R.string.app_text_delete), () ->
                            {
                                try
                                {
                                    dialog = null;
                                    CommonUtil.removeFavoriteLocation(AppConstant.HOME, pref);
                                    getHomeInfo();
                                    infoChanged = true;
                                }
                                catch(Exception e)
                                {

                                }
                            });

                    dialog = dialogBuilder.build();
                    dialog.show();
                    break;

                case R.id.btn_add_office:
                    intent = new Intent(context, FavoriteLocationActivity.class);
                    intent.putExtra("type", AppConstant.OFFICE);
                    startActivityForResult(intent, REQ_FAVORITE_LOCATION);
                    break;

                case R.id.btn_delete_office:
                    if(dialog != null)
                    {
                        dialog.dismiss();
                    }

                    dialogBuilder.setTitle(getString(R.string.menu_text_delete_office))
                            .setNegativeButton(getString(R.string.app_text_cancel), () -> dialog = null)
                            .setPositiveButton(getString(R.string.app_text_delete), () ->
                            {
                                try
                                {
                                    dialog = null;
                                    CommonUtil.removeFavoriteLocation(AppConstant.OFFICE, pref);
                                    getOfficeInfo();
                                    infoChanged = true;
                                }
                                catch(Exception e)
                                {

                                }
                            });

                    dialog = dialogBuilder.build();
                    dialog.show();
                    break;

                case R.id.btn_call_center_setting:
                    intent = new Intent(context, CallCenterActivity.class);
                    startActivityForResult(intent, REQ_CALL_CENTER);
                    break;

                case R.id.btn_terms:
                    intent = new Intent(context, TermsListActivity.class);
                    intent.putExtra("fromIntro", false);
                    startActivity(intent);
                    break;
            }
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    };

    @Override
    public void onBackPressed()
    {
        try
        {
            finishTask();
        }
        catch(Exception e)
        {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        try
        {
            if(resultCode == RESULT_OK)
            {
                switch(requestCode)
                {
                    case REQ_FAVORITE_LOCATION:
                        int type = data.getIntExtra("type", -1);
                        if(type > 0)
                        {
                            if(type == AppConstant.HOME)
                            {
                                getHomeInfo();
                            }
                            else
                            {
                                getOfficeInfo();
                            }

                            infoChanged = true;
                        }
                        break;

                    case REQ_CALL_CENTER:
                        getCallCenterInfo();
                        break;
                }
            }
        }
        catch(Exception e)
        {

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void finishTask()
    {
        Intent intent = getIntent();
        intent.putExtra("infoChanged", infoChanged);
        setResult(RESULT_OK, intent);
        finish();
    }
}
