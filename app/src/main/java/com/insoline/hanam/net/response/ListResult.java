package com.insoline.hanam.net.response;

import com.google.gson.annotations.SerializedName;

public class ListResult extends AuthResult
{
    @SerializedName("count")
    private String count;

    public String getCount()
    {
        return count;
    }

    public void setCount(String count)
    {
        this.count = count;
    }
}
