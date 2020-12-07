package com.insoline.hanam.activity.auth;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.insoline.hanam.R;
import com.insoline.hanam.activity.BaseActivity;
import com.insoline.hanam.constant.AppConstant;
import com.insoline.hanam.util.CommonUtil;

public class MobileNumberVerificationStep1Activity extends BaseActivity
{
    private Context context;

    private EditText editText;
    private View clearTextBtn;

    private TextView bottomBtn;

    private InputMethodManager inputMethodManager;

    private String mobileNum;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try
        {
            setContentView(R.layout.activity_mobile_number_verification);

            context = this;

            setStatusBarWhite();

            editText = findViewById(R.id.edit_text);
            clearTextBtn = findViewById(R.id.btn_clear_text);

            bottomBtn = findViewById(R.id.btn_bottom);

            clearTextBtn.setOnClickListener(clickListener);
            bottomBtn.setOnClickListener(clickListener);

            inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

            editText.requestFocus();
            inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_FORCED);

            Typeface boldTypeface = getBoldTypeface();
            bottomBtn.setTypeface(boldTypeface);

            TextView titleText = findViewById(R.id.title_text);
            titleText.setTypeface(boldTypeface);

            String mobileNumber = CommonUtil.getMobileNumber(context);
            if(mobileNumber != null)
            {
                mobileNumber = mobileNumber.replaceAll("\\+82", "0")
                        .replaceAll("-", "").trim();
                editText.setText(mobileNumber);
            }

            View editTextLayout = findViewById(R.id.edit_text_layout);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            {
                editTextLayout.setOutlineSpotShadowColor(Color.parseColor("#1a86a4"));
            }
        }
        catch(Exception e)
        {
            //e.printStackTrace();
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        try
        {
            inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    private View.OnClickListener clickListener = v ->
    {
        try
        {
            switch(v.getId())
            {
                case R.id.btn_clear_text:
                    editText.setText("");
                    break;

                case R.id.btn_bottom:
                    getVerificationCode();
                    break;
            }
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    };

    private void getVerificationCode()
    {
        mobileNum = editText.getText().toString().trim();

        if(!mobileNum.isEmpty())
        {
            mobileNum = mobileNum.replaceAll("\\+82", "0")
                    .replaceAll("-", "").trim();

            Intent intent = new Intent(context, MobileNumberVerificationStep2Activity.class);
            intent.putExtra("mobileNum", mobileNum);
            startActivityForResult(intent, 1);
        }
        else
        {
            Toast.makeText(context, getString(R.string.mobile_number_verification_toast1), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        try
        {

        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
