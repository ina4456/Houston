package com.insoline.hanam.net.response.inaviapi;

import com.google.gson.annotations.SerializedName;

public class InaviApiAdm
{
    @SerializedName("bldname")
    public String buildingName;

    @SerializedName("postcode")
    public String postCode;

    @SerializedName("posx")
    public String posX;

    @SerializedName("posy")
    public String posY;

    @SerializedName("roadname")
    public String roadName;

    @SerializedName("roadjibun")
    public String roadNumber;

    @SerializedName("address")
    public String address;

    @SerializedName("jibun")
    public String addressNumber;

    @SerializedName("address_category1")
    public String addressCategory1;

    @SerializedName("address_category2")
    public String addressCategory2;

    @SerializedName("address_category3")
    public String addressCategory3;

    @SerializedName("address_category4")
    public String addressCategory4;
}
