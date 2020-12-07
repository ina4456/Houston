package com.insoline.hanam.activity;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class BaseActivity extends FragmentActivity
{
    private Typeface basicTypeface;
    private Typeface boldTypeface;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID)
    {
        super.setContentView(layoutResID);
        try{
            AssetManager assetManager = getAssets();
            basicTypeface = CommonAssets.getTypeface(assetManager);
            boldTypeface = CommonAssets.getBoldTypeface(assetManager);

            ViewGroup root = findViewById(android.R.id.content);
            setGlobalFont(root);    // recursive call
        }
        catch(Exception e){}
    }

    private void setGlobalFont(ViewGroup root)
    {
        View child = null;
        for(int i = 0; i < root.getChildCount(); i++)
        {
            child = root.getChildAt(i);
            if(child instanceof TextView)
            {
                if(basicTypeface != null)
                {
                    ((TextView) child).setTypeface(basicTypeface);
                }
            }
            else if(child instanceof ViewGroup)
            {
                setGlobalFont((ViewGroup) child);
            }
            else
            {
                // ignore..
            }
        }
    }

    public Typeface getTypeface()
    {
        return basicTypeface;
    }

    public Typeface getBoldTypeface()
    {
        return boldTypeface;
    }

    protected void setStatusBarWhite()
    {
        setStatusBarColor(Color.WHITE);

        // 상태바 글자색 어둡게 처리
        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    protected void setStatusBarColor(int color)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            window.setStatusBarColor(color);
        }
    }
}
