package com.insoline.hanam.net.response.inaviapi;

import com.google.gson.annotations.SerializedName;

public class InaviApiHeader
{
    @SerializedName("isSuccessful")
    public boolean isSuccessful;

    @SerializedName("resultCode")
    public int resultCode;

    @SerializedName("resultMessage")
    public String resultMessage;
}
