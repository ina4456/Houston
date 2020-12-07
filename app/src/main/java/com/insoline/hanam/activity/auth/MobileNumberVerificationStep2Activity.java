package com.insoline.hanam.activity.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.insoline.hanam.R;
import com.insoline.hanam.activity.BaseActivity;
import com.insoline.hanam.activity.main.MainActivity;
import com.insoline.hanam.constant.AppConstant;
import com.insoline.hanam.net.RetrofitConnector;
import com.insoline.hanam.net.response.AuthResult;
import com.insoline.hanam.view.HoustonStyleDialog;
import com.insoline.hanam.view.HoustonStyleDialogBuilder;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MobileNumberVerificationStep2Activity extends BaseActivity
{
    private Context context;
    private SharedPreferences pref;

    private EditText editText;
    private TextView remainTimeText;
    private View retryCodeBtn;

    private TextView bottomBtn;

    private InputMethodManager inputMethodManager;

    private String mobileNum;
    private int remainTime;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private Future<?> timerTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try
        {
            setContentView(R.layout.activity_mobile_number_verification_step2);

            context = this;
            pref = getSharedPreferences(AppConstant.COMMON_DATA, MODE_PRIVATE);

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
        setStatusBarWhite();

        editText = findViewById(R.id.edit_text);
        remainTimeText = findViewById(R.id.remain_time_text);
        retryCodeBtn = findViewById(R.id.btn_retry);

        bottomBtn = findViewById(R.id.btn_bottom);
        bottomBtn.setEnabled(false);

        retryCodeBtn.setOnClickListener(clickListener);
        bottomBtn.setOnClickListener(clickListener);

        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        editText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                try
                {
                    int len = editText.getText().toString().length();
                    bottomBtn.setEnabled((len > 0));
                }
                catch(Exception e)
                {

                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });

        editText.requestFocus();
        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_FORCED);

        Typeface boldTypeface = getBoldTypeface();
        bottomBtn.setTypeface(boldTypeface);

        TextView titleText = findViewById(R.id.title_text);
        titleText.setTypeface(boldTypeface);

        View editTextLayout = findViewById(R.id.edit_text_layout);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        {
            editTextLayout.setOutlineSpotShadowColor(Color.parseColor("#1a86a4"));
        }
    }

    private void initData()
    {
        Intent intent = getIntent();
        mobileNum = intent.getStringExtra("mobileNum");
        getVerificationCode();
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

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        try
        {
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
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    @Override
    public void onBackPressed()
    {
        try
        {
            setResult(RESULT_CANCELED);
            finish();
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
                case R.id.btn_retry:
                    getVerificationCode();
                    break;

                case R.id.btn_bottom:
                    // 인증 확인
                    if(remainTime > 0)
                    {
                        String code = editText.getText().toString();
                        RetrofitConnector.signUp(mobileNum, code, resultObj ->
                        {
                            try
                            {
                                AuthResult result = (AuthResult) resultObj;
                                if(result.isSuccessful())
                                {
                                    // 인증 성공
                                    onVerificationSuccess();
                                }
                                else
                                {
                                    // 인증 번호 불일치
                                    onVerificationFailed();
                                }
                            }
                            catch(Exception e)
                            {

                            }
                        });
                    }
                    break;
            }
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    };

    private void onVerificationSuccess()
    {
        if(timerTask != null)
        {
            timerTask.cancel(true);
            timerTask = null;
        }

        // 인증 성공
        SharedPreferences.Editor edit = pref.edit();
        edit.putString(AppConstant.COMMON_DATA_MOBILE_NUMBER, mobileNum);
        edit.commit();

        boolean agreeTerms = pref.getBoolean(AppConstant.COMMON_DATA_AGREE_TERMS, false);
        if(agreeTerms)
        {
            // 약관 모두 확인. 메인 액티비티로 보낸다.
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else
        {
            // 약관 페이지로 이동
            Intent intent = new Intent(context, TermsListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private void onVerificationFailed()
    {

        String message = getString(R.string.mobile_number_verification_failed);
        HoustonStyleDialogBuilder builder = new HoustonStyleDialogBuilder(this);
        builder.setTitle(message).setAutoCancelable(true);
        HoustonStyleDialog dialog = builder.build();
        dialog.show();
    }

    private void getVerificationCode()
    {
        if(!mobileNum.isEmpty())
        {
            RetrofitConnector.getVerificationCode(mobileNum, resultObj ->
            {
                try
                {
                    AuthResult result = (AuthResult) resultObj;
                    if(result.isSuccessful())
                    {
                        intTimerTask();
                    }
                }
                catch(Exception e)
                {
                    //e.printStackTrace();
                }
            });
        }
    }

    private void intTimerTask()
    {
        if(timerTask != null)
        {
            timerTask.cancel(true);
        }

        editText.setText("");

        remainTime = 300;   // sec

        bottomBtn.setEnabled(false);
        bottomBtn.setText(getString(R.string.app_text_ok));

        remainTimeText.setText("05:00");

        timerTask = scheduler.scheduleAtFixedRate(() -> setRemainTimeLayout(),
                1000, 1000, TimeUnit.MILLISECONDS);
    }

    private void setRemainTimeLayout()
    {
        runOnUiThread(() ->
        {
            try
            {
                --remainTime;

                if(remainTime <= 0)
                {
                    if(timerTask != null)
                    {
                        timerTask.cancel(true);
                        timerTask = null;
                    }

                    remainTime = 0;
                    bottomBtn.setEnabled(false);
                }

                String text = String.format(Locale.getDefault(), "%02d:%02d", remainTime/60, remainTime%60);
                remainTimeText.setText(text);
            }
            catch(Exception e)
            {

            }
        });
    }
}
