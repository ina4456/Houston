package com.insoline.hanam.activity.menu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.insoline.hanam.R;
import com.insoline.hanam.activity.BaseActivity;
import com.insoline.hanam.activity.main.MainModel;
import com.insoline.hanam.constant.AppConstant;
import com.insoline.hanam.util.CommonUtil;

import java.util.TreeSet;

public class CallCenterActivity extends BaseActivity
{
    private SharedPreferences pref;
    private LayoutInflater inflater;

    private View actionBackBtn;
    private View actionExitBtn;
    private View useAllCheckBtn;
    private LinearLayout callCenterContainer;

    private TreeSet<String> callCenterSet;
    private String selectedCallCenters;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try
        {
            setContentView(R.layout.activity_call_center);

            pref = getSharedPreferences(AppConstant.COMMON_DATA, MODE_PRIVATE);
            inflater = LayoutInflater.from(this);

            actionBackBtn = findViewById(R.id.btn_title_back);
            actionExitBtn = findViewById(R.id.btn_title_close);
            useAllCheckBtn = findViewById(R.id.btn_check_all);
            callCenterContainer = findViewById(R.id.call_center_container);

            actionBackBtn.setOnClickListener(clickListener);
            actionExitBtn.setOnClickListener(clickListener);
            useAllCheckBtn.setOnClickListener(clickListener);

            selectedCallCenters = pref.getString(AppConstant.COMMON_DATA_CALL_CENTER, "");
            callCenterSet = CommonUtil.getSelectedCallCenter(pref);

            Intent intent = getIntent();
            boolean fromMain = intent.getBooleanExtra("fromMain", false);
            actionBackBtn.setVisibility(fromMain ? View.GONE : View.VISIBLE);
            actionExitBtn.setVisibility(fromMain ? View.VISIBLE : View.GONE);

            addCallCenters();
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    private void addCallCenters()
    {
        Typeface typeface = getTypeface();
        String[] callCenterList = MainModel.callCenterPhoneNums;

        boolean checkAll = true;
        int len = callCenterList.length;
        for(int i = 0; i < len; i++)
        {
            String name = getString(R.string.app_text_call_center) + " " + (i+1);
            String phoneNum = callCenterList[i];

            View view = inflater.inflate(R.layout.layout_call_center_btn, null);
            TextView callCenterName = view.findViewById(R.id.call_center_name_text);
            View checkBtn = view.findViewById(R.id.btn_check);
            checkBtn.setOnClickListener(checkBtnClickListener);

            callCenterName.setText(name);
            callCenterName.setTypeface(typeface);
            checkBtn.setSelected(callCenterSet.contains(phoneNum));

            if(!checkBtn.isSelected())
            {
                checkAll = false;
            }

            view.setTag(phoneNum);

            callCenterContainer.addView(view);
        }

        useAllCheckBtn.setSelected(checkAll);
    }

    @Override
    public void onBackPressed()
    {
        try
        {
            finishTask();
        }
        catch(Exception e)
        {

        }
    }

    private View.OnClickListener clickListener = v ->
    {
        try
        {
            switch(v.getId())
            {
                case R.id.btn_title_back:
                case R.id.btn_title_close:
                    finishTask();
                    break;

                case R.id.btn_check_all:
                    boolean checked = useAllCheckBtn.isSelected();
                    useAllCheckBtn.setSelected(!checked);
                    onClickCheckAll(!checked);
                    break;
            }
        }
        catch(Exception e)
        {

        }
    };

    private View.OnClickListener checkBtnClickListener = v ->
    {
        try
        {
            v.setSelected(!v.isSelected());

            boolean allCheck = true;
            int count = callCenterContainer.getChildCount();
            for(int i = 0; i < count; i++)
            {
                View view = callCenterContainer.getChildAt(i);
                View checkBtn = view.findViewById(R.id.btn_check);
                if(!checkBtn.isSelected())
                {
                    allCheck = false;
                }
            }

            useAllCheckBtn.setSelected(allCheck);
        }
        catch(Exception e)
        {

        }
    };

    private void onClickCheckAll(boolean selected)
    {
        int count = callCenterContainer.getChildCount();
        for(int i = 0; i < count; i++)
        {
            View view = callCenterContainer.getChildAt(i);
            View checkBtn = view.findViewById(R.id.btn_check);
            checkBtn.setSelected(selected);
        }
    }

    private void finishTask()
    {
        // 선택된 콜 센터 정보 갱신

        callCenterSet.clear();

        int count = callCenterContainer.getChildCount();
        for(int i = 0; i < count; i++)
        {
            View view = callCenterContainer.getChildAt(i);
            View checkBtn = view.findViewById(R.id.btn_check);
            if(checkBtn.isSelected())
            {
                String phoneNum = (String) view.getTag();
                callCenterSet.add(phoneNum);
            }
        }

        String text = CommonUtil.getSelectedText(callCenterSet);
        if(!selectedCallCenters.equals(text))
        {
            // 변경 사항이 있는 경우만 처리
            SharedPreferences.Editor edit = pref.edit();
            edit.remove(AppConstant.COMMON_DATA_CALL_CENTER);

            if(!text.trim().isEmpty())
            {
                edit.putString(AppConstant.COMMON_DATA_CALL_CENTER, text);
            }

            edit.commit();

            setResult(RESULT_OK);
        }

        finish();
    }
}
