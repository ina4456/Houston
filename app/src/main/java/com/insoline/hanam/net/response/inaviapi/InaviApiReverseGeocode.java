package com.insoline.hanam.net.response.inaviapi;

import com.google.gson.annotations.SerializedName;

public class InaviApiReverseGeocode extends InaviApiResult
{
    @SerializedName("location")
    public InaviApiLocation location;
}
