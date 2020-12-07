package com.insoline.hanam.net.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HistoryResult extends ListResult
{
    @SerializedName("data")
    private List<HistoryData> data;

    public List<HistoryData> getData()
    {
        return data;
    }

    public void setData(List<HistoryData> data)
    {
        this.data = data;
    }
}
