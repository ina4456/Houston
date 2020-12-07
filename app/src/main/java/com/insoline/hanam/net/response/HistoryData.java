package com.insoline.hanam.net.response;

import com.google.gson.annotations.SerializedName;
import com.insoline.hanam.util.AES256Util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class HistoryData
{
    @SerializedName("id")
    private String id;

    @SerializedName("carID")
    private String carId;

    @SerializedName("cusPosName")
    private String departureName;

    @SerializedName("cusLat")
    private String departureLat;

    @SerializedName("cusLon")
    private String departureLng;

    @SerializedName("destinationName")
    private String destinationName;

    @SerializedName("destinationLat")
    private String destinationLat;

    @SerializedName("destinationLon")
    private String destinationLng;

    @SerializedName("driverTelNum")
    private String driverTelNumber;

    @SerializedName("carModel")
    private String carTypeName;

    @SerializedName("driverName")
    private String driverName;

    @SerializedName("getInTime")
    private String getInTime;

    @SerializedName("getOutTime")
    private String getOutTime;

    public void decode() throws Exception
    {
        id = AES256Util.decode(id, AES256Util.skey);
        carId = AES256Util.decode(carId, AES256Util.skey);
        departureName = AES256Util.decode(departureName, AES256Util.skey);
        departureLat = AES256Util.decode(departureLat, AES256Util.skey);
        departureLng = AES256Util.decode(departureLng, AES256Util.skey);
        destinationName = AES256Util.decode(destinationName, AES256Util.skey);
        destinationLat = AES256Util.decode(destinationLat, AES256Util.skey);
        destinationLng = AES256Util.decode(destinationLng, AES256Util.skey);
        driverTelNumber = AES256Util.decode(driverTelNumber, AES256Util.skey);
        carTypeName = AES256Util.decode(carTypeName, AES256Util.skey);
        driverName = AES256Util.decode(driverName, AES256Util.skey);
        getInTime = AES256Util.decode(getInTime, AES256Util.skey);
        getOutTime = AES256Util.decode(getOutTime, AES256Util.skey);
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getCarId()
    {
        return carId;
    }

    public void setCarId(String carId)
    {
        this.carId = carId;
    }

    public String getDepartureName()
    {
        return departureName;
    }

    public void setDepartureName(String departureName)
    {
        this.departureName = departureName;
    }

    public String getDepartureLat()
    {
        return departureLat;
    }

    public void setDepartureLat(String departureLat)
    {
        this.departureLat = departureLat;
    }

    public String getDepartureLng()
    {
        return departureLng;
    }

    public void setDepartureLng(String departureLng)
    {
        this.departureLng = departureLng;
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

    public String getDriverTelNumber()
    {
        return driverTelNumber;
    }

    public void setDriverTelNumber(String driverTelNumber)
    {
        this.driverTelNumber = driverTelNumber;
    }

    public String getCarTypeName()
    {
        return carTypeName;
    }

    public void setCarTypeName(String carTypeName)
    {
        this.carTypeName = carTypeName;
    }

    public String getDriverName()
    {
        return driverName;
    }

    public void setDriverName(String driverName)
    {
        this.driverName = driverName;
    }

    public long getGetInTime()
    {
        try
        {
            // 예: 2020-08-17T14:59:31.41Z
            //Log.d(AppConstant.LOG_TEMP_TAG, "getInTime = " + getInTime);
            if(getInTime == null || getInTime.trim().isEmpty())
            {
                return -1;
            }

            TimeZone timeZone = TimeZone.getTimeZone("+09:00");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            simpleDateFormat.setTimeZone(timeZone);

            Date date = simpleDateFormat.parse(getInTime);
            return date.getTime();
        }
        catch(Exception e)
        {

        }

        return -1;
    }

    public void setGetInTime(String getInTime)
    {
        this.getInTime = getInTime;
    }

    public long getGetOutTime()
    {
        try
        {
            // 예: 2020-08-17 14:59:31
            //Log.d(AppConstant.LOG_TEMP_TAG, "getOutTime = " + getOutTime);
            if(getOutTime == null || getOutTime.trim().isEmpty())
            {
                return -1;
            }

            TimeZone timeZone = TimeZone.getTimeZone("+09:00");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            simpleDateFormat.setTimeZone(timeZone);

            Date date = simpleDateFormat.parse(getOutTime);
            return date.getTime();
        }
        catch(Exception e)
        {

        }

        return -1;
    }

    public void setGetOutTime(String getOutTime)
    {
        this.getOutTime = getOutTime;
    }
}
