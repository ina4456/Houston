package com.insoline.hanam.dto;

import android.os.Parcel;
import android.os.Parcelable;

public class RequestLocation implements Parcelable
{
    public long poiId;
    public double lat = Double.NaN;
    public double lng = Double.NaN;
    public String address;
    public String positionName;
    public String positionDetailName;

    public RequestLocation()
    {

    }

    public RequestLocation(double lat, double lng, String address, String positionName, String positionDetailName)
    {
        this.lat = lat;
        this.lng = lng;
        this.address = address;
        this.positionName = positionName;
        this.positionDetailName = positionDetailName;
    }

    protected RequestLocation(Parcel in)
    {
        lat = in.readDouble();
        lng = in.readDouble();
        address = in.readString();
        positionName = in.readString();
        positionDetailName = in.readString();
        poiId = in.readLong();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeDouble(lat);
        dest.writeDouble(lng);
        dest.writeString(address);
        dest.writeString(positionName);
        dest.writeString(positionDetailName);
        dest.writeLong(poiId);
    }

    public static final Creator<RequestLocation> CREATOR = new Creator<RequestLocation>()
    {
        @Override
        public RequestLocation createFromParcel(Parcel in)
        {
            return new RequestLocation(in);
        }

        @Override
        public RequestLocation[] newArray(int size)
        {
            return new RequestLocation[size];
        }
    };

    public String getNameUsingAddres()
    {
        if(address == null)
        {
            return null;
        }

        String[] split = address.split(" ");
        int len = split.length;
        if(len >= 2)
        {
            // 서울시 강서구 신시내티로 238 ---> 신시내티로 238
            return String.format("%s %s", split[len-2], split[len-1]);
        }
        else
        {
            return address;
        }
    }

    public String getShortAddress()
    {
        if(address == null)
        {
            return null;
        }

        String[] split = address.split(" ");
        int len = split.length;
        if(len >= 3)
        {
            // 서울시 강서구 신시내티로 238 ---> 강서구 신시내티로 238
            return String.format("%s %s %s", split[len-3], split[len-2], split[len-1]);
        }
        else
        {
            return address;
        }
    }
}
