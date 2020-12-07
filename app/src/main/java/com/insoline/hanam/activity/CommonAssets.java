package com.insoline.hanam.activity;

import android.content.res.AssetManager;
import android.graphics.Typeface;

public class CommonAssets
{
    public static Typeface basicTypeface;
    public static Typeface boldTypeface;

    public static Typeface getTypeface(AssetManager assetManager)
    {
        if(basicTypeface == null)
        {
            try
            {
                basicTypeface = Typeface.createFromAsset(assetManager, "inavi_rixgo_m.ttf");
            }
            catch(Exception e)
            {

            }
        }

        return basicTypeface;
    }

    public static Typeface getBoldTypeface(AssetManager assetManager)
    {
        if(boldTypeface == null)
        {
            try
            {
                boldTypeface = Typeface.createFromAsset(assetManager, "inavi_rixgo_B.ttf");
            }
            catch(Exception e)
            {

            }
        }

        return boldTypeface;
    }
}
