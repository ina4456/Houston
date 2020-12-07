package com.insoline.hanam.net.response.inaviapi;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class InaviApiPath
{
    @SerializedName("coords")
    public List<InaviApiCoord> coords;

    @SerializedName("speed")
    public int speed;

    @SerializedName("distance")
    public int distance;

    @SerializedName("spend_time")
    public int spendTime;    // sec
}
