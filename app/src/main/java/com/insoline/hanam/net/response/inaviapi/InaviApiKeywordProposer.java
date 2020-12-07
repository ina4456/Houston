package com.insoline.hanam.net.response.inaviapi;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class InaviApiKeywordProposer
{
    @SerializedName("result")
    public boolean result;

    @SerializedName("count")
    public int count;

    @SerializedName("keyword")
    public List<InaviApiKeyword> keywordList;
}
