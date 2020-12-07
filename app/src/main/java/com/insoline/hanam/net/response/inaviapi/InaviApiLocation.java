package com.insoline.hanam.net.response.inaviapi;

import com.google.gson.annotations.SerializedName;

public class InaviApiLocation
{
    @SerializedName("result")
    public boolean result;

    @SerializedName("hasAdmAddress")
    public boolean hasAdmAddress;

    @SerializedName("adm")
    public InaviApiAdm adm;

    @SerializedName("adm_address")
    public InaviApiAdm admAddress;

    @SerializedName("legal_address")
    public InaviApiAdm legalAddr;
}
