package com.insoline.hanam.activity.auth;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.insoline.hanam.R;
import com.insoline.hanam.activity.BaseActivity;
import com.insoline.hanam.constant.AppConstant;
import com.insoline.hanam.net.RetrofitConnector;

public class TermsDetailActivity extends BaseActivity
{
    private View backBtn;
    private TextView titleText;
    private WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try
        {
            setContentView(R.layout.activity_terms_detail);

            backBtn = findViewById(R.id.btn_title_back);
            titleText = findViewById(R.id.title_text);

            titleText.setTypeface(getBoldTypeface());
            backBtn.setOnClickListener(v ->
            {
                try
                {
                    finish();
                }
                catch(Exception e)
                {

                }
            });

            String title = null;
            Intent intent = getIntent();
            int termsType = intent.getIntExtra("termsType", 0);
            switch(termsType)
            {
                case 1: title = getString(R.string.terms_agree_terms1); break;
                case 2: title = getString(R.string.terms_agree_terms2); break;
                case 3: title = getString(R.string.terms_agree_terms3); break;
            }
            titleText.setText(title);

            webView = findViewById(R.id.web_view);

            webView.setHorizontalScrollBarEnabled(false);
            webView.setVerticalScrollBarEnabled(true);
            webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
            webView.setBackgroundColor(Color.parseColor("#f3f3f3"));

            WebSettings settings = webView.getSettings();
            settings.setBuiltInZoomControls(false);                          // 줌 컨트롤 비허용
            settings.setSupportZoom(true);                                   // 기본 줌 조작
            settings.setAllowFileAccess(false);
            settings.setAllowFileAccessFromFileURLs(false);
            settings.setJavaScriptEnabled(false);                            // 자바스크립트 비허용
            settings.setDomStorageEnabled(true);                             // HTML5 DOM storage 허용
            settings.setJavaScriptCanOpenWindowsAutomatically(false);        // window.open() 비허용
            settings.setLoadWithOverviewMode(false);
            settings.setUseWideViewPort(false);

            RetrofitConnector.getTerms(resultObj ->
            {
                try
                {
                    String html = (String) resultObj;
                    webView.loadDataWithBaseURL("", html, "text/html", "UTF-8", null);
                }
                catch(Exception e)
                {

                }
            });
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }
}
