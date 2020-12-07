package com.insoline.hanam.net.response;

import com.google.gson.annotations.SerializedName;
import com.insoline.hanam.util.AES256Util;

public class AuthResult
{
    public AuthResult()
    {

    }

    public AuthResult(boolean isSuccessful)
    {
        this.isSuccessful = isSuccessful;
    }

    @SerializedName("currentDT")
    private String currentDT;

    @SerializedName("isSuccessful")
    private boolean isSuccessful;

    @SerializedName("msg")
    private String msg;

    @SerializedName("isForceful")
    private boolean isForceful;

    @SerializedName("version")
    private String version;

    public String getCurrentDT()
    {
        return currentDT;
    }

    public void setCurrentDT(String currentDT)
    {
        this.currentDT = currentDT;
    }

    public boolean isSuccessful()
    {
        return isSuccessful;
    }

    public void setSuccessful(boolean successful)
    {
        isSuccessful = successful;
    }

    public String getMsg()
    {
        try
        {
            return AES256Util.decode(msg, AES256Util.skey);
        }
        catch(Exception e)
        {

        }

        return msg;
    }

    public void setMsg(String msg)
    {
        this.msg = msg;
    }

    public boolean isForceful()
    {
        return isForceful;
    }

    public void setForceful(boolean forceful)
    {
        isForceful = forceful;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }
}
