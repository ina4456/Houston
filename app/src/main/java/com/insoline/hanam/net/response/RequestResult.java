package com.insoline.hanam.net.response;

import com.google.gson.annotations.SerializedName;

public class RequestResult extends AuthResult
{
    @SerializedName("callID")
    private String callID;

    @SerializedName("callDT")
    private String callDT;

    @SerializedName("callStatus")
    private String callStatus;

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

    public String getCallStatus()
    {
        return callStatus;
    }

    public void setCallStatus(String callStatus)
    {
        this.callStatus = callStatus;
    }
}
