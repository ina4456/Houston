package com.insoline.hanam.net.response.inaviapi;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class InaviApiSearchData
{
    @SerializedName("result")
    public boolean result;

    @SerializedName("poi")
    public List<InaviApiPoi> poiList;

    @SerializedName("adm")
    public List<InaviApiAdm> admList;
}
