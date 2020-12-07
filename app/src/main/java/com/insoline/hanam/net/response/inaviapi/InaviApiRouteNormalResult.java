package com.insoline.hanam.net.response.inaviapi;

import com.google.gson.annotations.SerializedName;

public class InaviApiRouteNormalResult extends InaviApiResult
{
    @SerializedName("route")
    public InaviApiRoute route;
}
