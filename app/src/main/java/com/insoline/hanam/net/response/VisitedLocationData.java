package com.insoline.hanam.net.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.insoline.hanam.util.AES256Util;

public class VisitedLocationData implements Parcelable
{
    @SerializedName("id")
    private String id;

    @SerializedName("hitNum")
    private String hitNum;

    @SerializedName("destinationName")
    private String destinationName;

    @SerializedName("destinationLat")
    private String destinationLat;

    @SerializedName("destinationLon")
    private String destinationLng;

    public VisitedLocationData()
    {

    }

    protected VisitedLocationData(Parcel in)
    {
        id = in.readString();
        hitNum = in.readString();
        destinationName = in.readString();
        destinationLat = in.readString();
        destinationLng = in.readString();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(id);
        dest.writeString(hitNum);
        dest.writeString(destinationName);
        dest.writeString(destinationLat);
        dest.writeString(destinationLng);
    }

    public static final Creator<VisitedLocationData> CREATOR = new Creator<VisitedLocationData>()
    {
        @Override
        public VisitedLocationData createFromParcel(Parcel in)
        {
            return new VisitedLocationData(in);
        }

        @Override
        public VisitedLocationData[] newArray(int size)
        {
            return new VisitedLocationData[size];
        }
    };

    public void decode() throws Exception
    {
        id = AES256Util.decode(id, AES256Util.skey);
        hitNum = AES256Util.decode(hitNum, AES256Util.skey);
        destinationName = AES256Util.decode(destinationName, AES256Util.skey);
        destinationLat = AES256Util.decode(destinationLat, AES256Util.skey);
        destinationLng = AES256Util.decode(destinationLng, AES256Util.skey);
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getHitNum()
    {
        return hitNum;
    }

    public void setHitNum(String hitNum)
    {
        this.hitNum = hitNum;
    }

    public String getDestinationName()
    {
        return destinationName;
    }

    public void setDestinationName(String destinationName)
    {
        this.destinationName = destinationName;
    }

    public String getDestinationLat()
    {
        return destinationLat;
    }

    public void setDestinationLat(String destinationLat)
    {
        this.destinationLat = destinationLat;
    }

    public String getDestinationLng()
    {
        return destinationLng;
    }

    public void setDestinationLng(String destinationLng)
    {
        this.destinationLng = destinationLng;
    }

    public int getHit()
    {
        if(hitNum == null || hitNum.trim().isEmpty())
        {
            return 0;
        }

        try
        {
            return Integer.parseInt(hitNum);
        }
        catch(Exception e)
        {

        }

        return 0;
    }
}
