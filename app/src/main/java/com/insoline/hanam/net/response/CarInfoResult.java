package com.insoline.hanam.net.response;

import com.google.gson.annotations.SerializedName;

public class CarInfoResult extends AuthResult
{
    @SerializedName("carNum")
    private String carNum;

    @SerializedName("driverName")
    private String driverName;

    @SerializedName("carColor")
    private String carColor;

    @SerializedName("carModel")
    private String carModel;

    @SerializedName("driverTelNum")
    private String driverPhoneNum;

    @SerializedName("carLat")
    private String carLat;

    @SerializedName("carLon")
    private String carLon;

    public String getCarNum()
    {
        return carNum;
    }

    public void setCarNum(String carNum)
    {
        this.carNum = carNum;
    }

    public String getDriverName()
    {
        return driverName;
    }

    public void setDriverName(String driverName)
    {
        this.driverName = driverName;
    }

    public String getCarColor()
    {
        return carColor;
    }

    public void setCarColor(String carColor)
    {
        this.carColor = carColor;
    }

    public String getCarModel()
    {
        return carModel;
    }

    public void setCarModel(String carModel)
    {
        this.carModel = carModel;
    }

    public String getCarLat()
    {
        return carLat;
    }

    public void setCarLat(String carLat)
    {
        this.carLat = carLat;
    }

    public String getCarLon()
    {
        return carLon;
    }

    public void setCarLon(String carLon)
    {
        this.carLon = carLon;
    }

    public String getDriverPhoneNum()
    {
        return driverPhoneNum;
    }

    public void setDriverPhoneNum(String driverPhoneNum)
    {
        this.driverPhoneNum = driverPhoneNum;
    }
}
