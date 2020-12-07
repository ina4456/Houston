package com.insoline.hanam.net.response.inaviapi;

import com.google.gson.annotations.SerializedName;

public class InaviApiKeyword
{
    @SerializedName("keyword")
    public String keyword;

    @SerializedName("frequency")
    public int frequency;
}
