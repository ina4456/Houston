package com.insoline.hanam.net.json;

import android.support.annotation.NonNull;

import com.insoline.hanam.util.AES256Util;

public class CallJson extends AuthJson
{
    private String callID;
    private String callDT;

    public CallJson(@NonNull String currentDT, @NonNull String mobile,
                    @NonNull String callID, @NonNull String callDT)
    {
        super(currentDT, mobile, null);

        try
        {
            this.callID = AES256Util.encode(callID, AES256Util.skey);
            this.callDT = AES256Util.encode(callDT, AES256Util.skey);
        }
        catch(Exception e)
        {

        }
    }

    public CallJson(@NonNull String currentDT, @NonNull String mobile,
                    @NonNull String callID, @NonNull String callDT, String data)
    {
        super(currentDT, mobile, null);

        try
        {
            this.callID = AES256Util.encode(callID, AES256Util.skey);
            this.callDT = AES256Util.encode(callDT, AES256Util.skey);
            setData(AES256Util.encode(data, AES256Util.skey));
        }
        catch(Exception e)
        {

        }
    }

    public String getCallID()
    {
        return callID;
    }

    public void setCallID(String callID)
    {
        this.callID = callID;
    }

    public String getCallDT()
    {
        return callDT;
    }

    public void setCallDT(String callDT)
    {
        this.callDT = callDT;
    }
}
