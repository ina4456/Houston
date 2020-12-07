package com.insoline.hanam.net.response.inaviapi;

import com.google.gson.annotations.SerializedName;

public class InaviApiKeywordSuggestionResult extends InaviApiResult
{
    @SerializedName("proposer")
    public InaviApiKeywordProposer proposer;
}
