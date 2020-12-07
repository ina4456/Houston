package com.insoline.hanam.net.response.inaviapi;

import com.google.gson.annotations.SerializedName;

public class InaviApiSearchResult extends InaviApiResult
{
    @SerializedName("search")
    public InaviApiSearchData search;
}
