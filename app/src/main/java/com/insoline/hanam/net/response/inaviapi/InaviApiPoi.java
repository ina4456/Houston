package com.insoline.hanam.net.response.inaviapi;

import com.google.gson.annotations.SerializedName;

public class InaviApiPoi
{
    @SerializedName("poiid")
    public long poiId;

    @SerializedName("dpx")
    public String dpx;

    @SerializedName("dpy")
    public String dpy;

    @SerializedName("name1")
    public String name1;

    @SerializedName("roadname")
    public String roadName;

    @SerializedName("roadjibun")
    public String roadNumber;

    @SerializedName("address")
    public String address;

    @SerializedName("jibun")
    public String addressNumber;
}
