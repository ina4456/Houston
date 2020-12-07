package com.insoline.hanam.net.json;

import android.support.annotation.NonNull;

import com.insoline.hanam.dto.RequestLocation;
import com.insoline.hanam.util.AES256Util;

public class RequestJson extends AuthJson
{
    private String posName;
    private String posLat;
    private String posLon;
    private String posNameDetail;

    private String destLat;
    private String destLon;
    private String destination;

    public RequestJson(@NonNull String currentDT, @NonNull String mobile,
                       @NonNull RequestLocation pos, RequestLocation dest)
    {
        super(currentDT, mobile, null);

        try
        {
            posName = AES256Util.encode(pos.positionName, AES256Util.skey);
            posLat = AES256Util.encode(String.valueOf(pos.lat), AES256Util.skey);
            posLon = AES256Util.encode(String.valueOf(pos.lng), AES256Util.skey);

            if(pos.positionDetailName != null && !pos.positionDetailName.trim().isEmpty())
            {
                posNameDetail = AES256Util.encode(pos.positionDetailName, AES256Util.skey);
            }
            else
            {
                posNameDetail = AES256Util.encode(pos.address, AES256Util.skey);
            }

            if(dest != null)
            {
                destination = AES256Util.encode(dest.positionName, AES256Util.skey);
                destLat = AES256Util.encode(String.valueOf(dest.lat), AES256Util.skey);
                destLon = AES256Util.encode(String.valueOf(dest.lng), AES256Util.skey);
            }
        }
        catch(Exception e)
        {

        }
    }
}
