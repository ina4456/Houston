package com.insoline.hanam.net.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VisitedLocationResult extends ListResult
{
    @SerializedName("data")
    private List<VisitedLocationData> data;

    public List<VisitedLocationData> getData()
    {
        return data;
    }

    public void setData(List<VisitedLocationData> data)
    {
        this.data = data;
    }
}
