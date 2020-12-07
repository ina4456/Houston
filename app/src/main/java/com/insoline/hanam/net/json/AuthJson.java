package com.insoline.hanam.net.json;

import android.support.annotation.NonNull;

import com.insoline.hanam.util.AES256Util;

public class AuthJson
{
    private String currentDT;
    private String authCode;
    private String mobile;
    private String data;

    public AuthJson(@NonNull String currentDT, @NonNull String mobile, String data)
    {
        try
        {
            String authCode = AES256Util.encode(currentDT, AES256Util.skey);
            String encodedMobileNum = AES256Util.encode(mobile.trim(), AES256Util.skey);

            String encodedData = null;
            if(data != null && !data.trim().isEmpty())
            {
                encodedData = AES256Util.encode(data.trim(), AES256Util.skey);
            }

            this.currentDT = currentDT;
            this.authCode = authCode;
            this.mobile = encodedMobileNum;
            this.data = encodedData;

            //Log.d(AppConstant.LOG_TEMP_TAG, "currentDT = " + this.currentDT);
            //Log.d(AppConstant.LOG_TEMP_TAG, "authCode = " + this.authCode);
            //Log.d(AppConstant.LOG_TEMP_TAG, "mobile = " + this.mobile);
            //Log.d(AppConstant.LOG_TEMP_TAG, "data = " + this.data);
        }
        catch(Exception e)
        {

        }
    }

    public String getCurrentDT()
    {
        return currentDT;
    }

    public void setCurrentDT(String currentDT)
    {
        this.currentDT = currentDT;
    }

    public String getAuthCode()
    {
        return authCode;
    }

    public void setAuthCode(String authCode)
    {
        this.authCode = authCode;
    }

    public String getMobile()
    {
        return mobile;
    }

    public void setMobile(String mobile)
    {
        this.mobile = mobile;
    }

    public String getData()
    {
        return data;
    }

    public void setData(String data)
    {
        this.data = data;
    }
}
