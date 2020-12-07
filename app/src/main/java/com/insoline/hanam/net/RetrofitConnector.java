package com.insoline.hanam.net;

import android.util.Log;

import com.inavi.mapsdk.geometry.LatLng;
import com.insoline.hanam.constant.AppConstant;
import com.insoline.hanam.dto.RequestLocation;
import com.insoline.hanam.net.json.AuthJson;
import com.insoline.hanam.net.json.CallJson;
import com.insoline.hanam.net.json.RequestJson;
import com.insoline.hanam.net.response.AuthResult;
import com.insoline.hanam.net.response.CarInfoResult;
import com.insoline.hanam.net.response.HistoryData;
import com.insoline.hanam.net.response.HistoryResult;
import com.insoline.hanam.net.response.RequestResult;
import com.insoline.hanam.net.response.VisitedLocationData;
import com.insoline.hanam.net.response.VisitedLocationResult;
import com.insoline.hanam.net.response.inaviapi.InaviApiAdm;
import com.insoline.hanam.net.response.inaviapi.InaviApiKeyword;
import com.insoline.hanam.net.response.inaviapi.InaviApiKeywordSuggestionResult;
import com.insoline.hanam.net.response.inaviapi.InaviApiPoi;
import com.insoline.hanam.net.response.inaviapi.InaviApiReverseGeocode;
import com.insoline.hanam.net.response.inaviapi.InaviApiRouteNormalResult;
import com.insoline.hanam.net.response.inaviapi.InaviApiSearchData;
import com.insoline.hanam.net.response.inaviapi.InaviApiSearchResult;
import com.insoline.hanam.net.retrofit.RetrofitInaviService;
import com.insoline.hanam.net.retrofit.RetrofitThinkWareService;
import com.insoline.hanam.net.retrofit.RetrofitThinkWareWebService;
import com.insoline.hanam.util.AES256Util;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitConnector
{
    private static final String INAVI_APP_KEY = "tgUajG3boKc4mCUz"; // S9AovEUrYhSLBZAg
    private static RetrofitInaviService inaviService;
    private static RetrofitThinkWareService thinkWareService;
    private static RetrofitThinkWareWebService thinkWareWebService;

    static
    {
        try
        {
            // 팅크웨어 API URL(TODO IP 변경시 또는 도메인으로 변경시 이 부분을 수정하시면 됩니다)
            String thinkwareBaseUrl = "https://hanam.insoline.net:8000";

            // 아이나비 지도 API URL
            String inaviBaseUrl = "https://api-maps.cloud.toast.com/";

            Retrofit inaviRetrofit = new Retrofit.Builder()
                    .baseUrl(inaviBaseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            Retrofit thinkWareRetrofit = new Retrofit.Builder()
                    .baseUrl(thinkwareBaseUrl)
                    .client(getUnsafeOkHttpClient().build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            Retrofit thinkWareWebRetrofit = new Retrofit.Builder()
                    .baseUrl(thinkwareBaseUrl)
                    .client(getUnsafeOkHttpClient().build())
                    .build();

            inaviService = inaviRetrofit.create(RetrofitInaviService.class);
            thinkWareService = thinkWareRetrofit.create(RetrofitThinkWareService.class);
            thinkWareWebService = thinkWareWebRetrofit.create(RetrofitThinkWareWebService.class);
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    public static OkHttpClient.Builder getUnsafeOkHttpClient()
    {
        try
        {
            final TrustManager[] trustAllCerts = new TrustManager[]
                    {
                            new X509TrustManager()
                            {
                                @Override
                                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
                                {

                                }

                                @Override
                                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
                                {

                                }

                                @Override
                                public X509Certificate[] getAcceptedIssuers()
                                {
                                    return new java.security.cert.X509Certificate[]{};
                                }
                            }

                    };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);

            return builder;
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }

        return null;
    }

    // ThinkWare API ///////////////////////////////////////////////////////////////////

    /** 전화번호 인증 관련 *******/

    public static void getVerificationCode(String mobileNum, HttpCallback callback)
    {
        try
        {
            String currentDt = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
            Call<AuthResult> taskCall = thinkWareService.getAuthCode(new AuthJson(currentDt, mobileNum, ""));
            taskCall.enqueue(new Callback<AuthResult>()
            {
                @Override
                public void onResponse(Call<AuthResult> call, Response<AuthResult> response)
                {
                    try
                    {
                        if(callback != null)
                        {
                            callback.onResult(response.body());
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }

                @Override
                public void onFailure(Call<AuthResult> call, Throwable t)
                {
                    try
                    {
                        if(callback != null)
                        {
                            callback.onResult(null);
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }
            });
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    public static void signUp(String mobileNum, String data, HttpCallback callback)
    {
        try
        {
            String currentDt = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
            Call<AuthResult> taskCall = thinkWareService.signUp(new AuthJson(currentDt, mobileNum, data));
            taskCall.enqueue(new Callback<AuthResult>()
            {
                @Override
                public void onResponse(Call<AuthResult> call, Response<AuthResult> response)
                {
                    try
                    {
                        if(callback != null)
                        {
                            callback.onResult(response.body());
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }

                @Override
                public void onFailure(Call<AuthResult> call, Throwable t)
                {
                    try
                    {
                        if(callback != null)
                        {
                            callback.onResult(null);
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }
            });
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    /** 버전 확인 ******/
    public static void versionCheck(String mobile, HttpCallback callback)
    {
        try
        {
            String currentDt = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
            Call<AuthResult> taskCall = thinkWareService.versionCheck(new AuthJson(currentDt, mobile, null));

            taskCall.enqueue(new Callback<AuthResult>()
            {
                @Override
                public void onResponse(Call<AuthResult> call, Response<AuthResult> response)
                {
                    try
                    {
                        AuthResult result = response.body();
                        if(result != null)
                        {
                            result.setVersion(AES256Util.decode(result.getVersion(), AES256Util.skey));
                        }

                        if(callback != null)
                        {
                            callback.onResult(result);
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }

                @Override
                public void onFailure(Call<AuthResult> call, Throwable t)
                {
                    try
                    {
                        if(callback != null)
                        {
                            callback.onResult(null);
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }
            });
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    /** 약관 *******/
    public static void getTerms(HttpCallback callback)
    {
        try
        {
            Call<ResponseBody> taskCall = thinkWareWebService.getTerms();
            taskCall.enqueue(new Callback<ResponseBody>()
            {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response)
                {
                    try
                    {
                        if(callback != null)
                        {
                            callback.onResult(response.body().string());
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t)
                {
                    try
                    {
                        if(callback != null)
                        {
                            callback.onResult(null);
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }
            });
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    /** 배차, 콜 관련 *******/

    public static void requestCall(String mobile, RequestLocation pos, RequestLocation dest, HttpCallback callback)
    {
        try
        {
            String currentDt = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
            Call<RequestResult> taskCall = thinkWareService.request(new RequestJson(currentDt, mobile, pos, dest));
            taskCall.enqueue(new Callback<RequestResult>()
            {
                @Override
                public void onResponse(Call<RequestResult> call, Response<RequestResult> response)
                {
                    try
                    {
                        RequestResult result = response.body();
                        if(result != null)
                        {
                            // 결과 문자열 디코딩
                            result.setCallID(AES256Util.decode(result.getCallID(), AES256Util.skey));
                            result.setCallDT(AES256Util.decode(result.getCallDT(), AES256Util.skey));
                        }

                        if(callback != null)
                        {
                            callback.onResult(result);
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }

                @Override
                public void onFailure(Call<RequestResult> call, Throwable t)
                {
                    try
                    {
                        if(callback != null)
                        {
                            callback.onResult(null);
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }
            });
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    public static void cancelCall(String mobile, String callId, String callDt, HttpCallback callback)
    {
        try
        {
            String currentDt = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
            Call<RequestResult> taskCall = thinkWareService.cancelCall(new CallJson(currentDt, mobile, callId, callDt));
            taskCall.enqueue(new Callback<RequestResult>()
            {
                @Override
                public void onResponse(Call<RequestResult> call, Response<RequestResult> response)
                {
                    try
                    {
                        if(callback != null)
                        {
                            callback.onResult(response.body());
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }

                @Override
                public void onFailure(Call<RequestResult> call, Throwable t)
                {
                    try
                    {
                        if(callback != null)
                        {
                            callback.onResult(null);
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }
            });
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    public static void callCheck(String mobile, String callId, String callDt, HttpCallback callback)
    {
        try
        {
            String currentDt = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
            Call<RequestResult> taskCall = thinkWareService.callCheck(new CallJson(currentDt, mobile, callId, callDt));
            taskCall.enqueue(new Callback<RequestResult>()
            {
                @Override
                public void onResponse(Call<RequestResult> call, Response<RequestResult> response)
                {
                    try
                    {
                        RequestResult result = response.body();
                        if(result != null)
                        {
                            // 결과 문자열 디코딩
                            result.setCallID(AES256Util.decode(result.getCallID(), AES256Util.skey));
                            result.setCallDT(AES256Util.decode(result.getCallDT(), AES256Util.skey));
                            result.setCallStatus(AES256Util.decode(result.getCallStatus(), AES256Util.skey));
                        }

                        if(callback != null)
                        {
                            callback.onResult(result);
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }

                @Override
                public void onFailure(Call<RequestResult> call, Throwable t)
                {
                    try
                    {
                        if(callback != null)
                        {
                            callback.onResult(null);
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }
            });
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    public static void getDriverInfo(String mobile, String callId, String callDt, HttpCallback callback)
    {
        try
        {
            String currentDt = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
            Call<CarInfoResult> taskCall = thinkWareService.getDriverInfo(new CallJson(currentDt, mobile, callId, callDt));
            taskCall.enqueue(new Callback<CarInfoResult>()
            {
                @Override
                public void onResponse(Call<CarInfoResult> call, Response<CarInfoResult> response)
                {
                    try
                    {
                        CarInfoResult result = response.body();
                        if(result != null)
                        {
                            // 결과 문자열 디코딩
                            result.setDriverName(AES256Util.decode(result.getDriverName(), AES256Util.skey));
                            result.setDriverPhoneNum(AES256Util.decode(result.getDriverPhoneNum(), AES256Util.skey));
                            result.setCarModel(AES256Util.decode(result.getCarModel(), AES256Util.skey));
                            result.setCarNum(AES256Util.decode(result.getCarNum(), AES256Util.skey));
                            result.setCarColor(AES256Util.decode(result.getCarColor(), AES256Util.skey));
                        }

                        if(callback != null)
                        {
                            callback.onResult(result);
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }

                @Override
                public void onFailure(Call<CarInfoResult> call, Throwable t)
                {
                    try
                    {
                        if(callback != null)
                        {
                            callback.onResult(null);
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }
            });
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    public static void getCarPosition(String mobile, String callId, String callDt, HttpCallback callback)
    {
        try
        {
            String currentDt = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
            Call<CarInfoResult> taskCall = thinkWareService.getCarPosition(new CallJson(currentDt, mobile, callId, callDt));
            taskCall.enqueue(new Callback<CarInfoResult>()
            {
                @Override
                public void onResponse(Call<CarInfoResult> call, Response<CarInfoResult> response)
                {
                    try
                    {
                        CarInfoResult result = response.body();
                        if(result != null)
                        {
                            // 결과 문자열 디코딩
                            result.setCarLat(AES256Util.decode(result.getCarLat(), AES256Util.skey));
                            result.setCarLon(AES256Util.decode(result.getCarLon(), AES256Util.skey));
                        }

                        if(callback != null)
                        {
                            callback.onResult(result);
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }

                @Override
                public void onFailure(Call<CarInfoResult> call, Throwable t)
                {
                    try
                    {
                        if(callback != null)
                        {
                            callback.onResult(null);
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }
            });
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    public static void getHistory(String mobile, HttpCallback callback)
    {
        try
        {
            String currentDt = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
            Call<HistoryResult> taskCall = thinkWareService.getHistory(new AuthJson(currentDt, mobile, null));

            taskCall.enqueue(new Callback<HistoryResult>()
            {
                @Override
                public void onResponse(Call<HistoryResult> call, Response<HistoryResult> response)
                {
                    try
                    {
                        HistoryResult result = response.body();
                        List<HistoryData> dataList = null;

                        if(result != null)
                        {
                            dataList = result.getData();
                            if(dataList != null)
                            {
                                for(HistoryData data : dataList)
                                {
                                    data.decode();
                                }
                            }
                        }

                        if(callback != null)
                        {
                            callback.onResult(dataList);
                        }
                    }
                    catch(Exception e)
                    {
                        //e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<HistoryResult> call, Throwable t)
                {
                    try
                    {
                        //t.printStackTrace();
                        //Log.d(AppConstant.LOG_TEMP_TAG, "fail! " + t.getMessage());
                        if(callback != null)
                        {
                            callback.onResult(null);
                        }
                    }
                    catch(Exception e)
                    {
                        //e.printStackTrace();
                    }
                }
            });
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    public static void removeHistory(String mobile, String data, HttpCallback callback)
    {
        try
        {
            String currentDt = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
            Call<AuthResult> taskCall = thinkWareService.removeHistory(new AuthJson(currentDt, mobile, data));

            taskCall.enqueue(new Callback<AuthResult>()
            {
                @Override
                public void onResponse(Call<AuthResult> call, Response<AuthResult> response)
                {
                    try
                    {
                        if(callback != null)
                        {
                            callback.onResult(response.body());
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }

                @Override
                public void onFailure(Call<AuthResult> call, Throwable t)
                {
                    try
                    {
                        if(callback != null)
                        {
                            callback.onResult(null);
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }
            });
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    public static void removeHistoryAll(String mobile, HttpCallback callback)
    {
        try
        {
            String currentDt = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
            Call<AuthResult> taskCall = thinkWareService.removeHistoryAll(new AuthJson(currentDt, mobile, null));

            taskCall.enqueue(new Callback<AuthResult>()
            {
                @Override
                public void onResponse(Call<AuthResult> call, Response<AuthResult> response)
                {
                    try
                    {
                        if(callback != null)
                        {
                            callback.onResult(response.body());
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }

                @Override
                public void onFailure(Call<AuthResult> call, Throwable t)
                {
                    try
                    {
                        if(callback != null)
                        {
                            callback.onResult(null);
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }
            });
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    public static void sendReview(String mobile, String callId, String callDt, int rate, HttpCallback callback)
    {
        try
        {
            String currentDt = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
            Call<AuthResult> taskCall = thinkWareService.sendReview(new CallJson(currentDt, mobile, callId, callDt, String.valueOf(rate)));

            taskCall.enqueue(new Callback<AuthResult>()
            {
                @Override
                public void onResponse(Call<AuthResult> call, Response<AuthResult> response)
                {
                    try
                    {
                        if(callback != null)
                        {
                            AuthResult result = response.body();
                            callback.onResult(result);
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }

                @Override
                public void onFailure(Call<AuthResult> call, Throwable t)
                {
                    try
                    {
                        if(callback != null)
                        {
                            callback.onResult(null);
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }
            });
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    public static void getFrequencyVisitedLocations(String mobile, HttpCallback callback)
    {
        try
        {
            String currentDt = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
            Call<VisitedLocationResult> taskCall = thinkWareService.getFrequencyVisitedLocations(new AuthJson(currentDt, mobile, null));

            taskCall.enqueue(new Callback<VisitedLocationResult>()
            {
                @Override
                public void onResponse(Call<VisitedLocationResult> call, Response<VisitedLocationResult> response)
                {
                    List<VisitedLocationData> dataList = null;

                    try
                    {
                        VisitedLocationResult result = response.body();
                        if(result != null)
                        {
                            dataList = result.getData();
                            if(dataList != null)
                            {
                                for(VisitedLocationData data : dataList)
                                {
                                    data.decode();
                                }

                                // 빈도 순으로 정렬
                                int size = dataList.size();
                                if(size > 1)
                                {
                                    Collections.sort(dataList, (o1, o2) ->
                                    {
                                        try
                                        {
                                            return o2.getHit() - o1.getHit();
                                        }
                                        catch(Exception e)
                                        {

                                        }

                                        return 0;
                                    });
                                }

                            }
                        }
                    }
                    catch(Exception e)
                    {
                        dataList = null;
                    }

                    if(callback != null)
                    {
                        callback.onResult(dataList);
                    }
                }

                @Override
                public void onFailure(Call<VisitedLocationResult> call, Throwable t)
                {
                    if(callback != null)
                    {
                        callback.onResult(null);
                    }
                }
            });
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // 콜센터 배차 요청 //////////////////////////////////////////////////////////////////////////////

    public static void sendPassengerLocation(String mobileNum, double lat, double lng, String positionName, HttpCallback callback)
    {
        try
        {
            String currentDt = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
            RequestLocation location = new RequestLocation();
            location.positionName = positionName;
            location.lat = lat;
            location.lng = lng;

            Call<AuthResult> taskCall = thinkWareService.sendPassengerLocation(new RequestJson(currentDt, mobileNum, location, null));

            taskCall.enqueue(new Callback<AuthResult>()
            {
                @Override
                public void onResponse(Call<AuthResult> call, Response<AuthResult> response)
                {
                    try
                    {
                        if(callback != null)
                        {
                            AuthResult result = response.body();
                            callback.onResult(result);
                        }
                    }
                    catch(Exception e)
                    {
                        //e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<AuthResult> call, Throwable t)
                {
                    try
                    {
                        if(callback != null)
                        {
                            callback.onResult(null);
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }
            });
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    /**
     * 가장 최근의 배차 요청 건의 정보를 준다.<br/>
     * (자동 배차 요청, 콜센터 통한 요청 상관없이)
     * @param mobileNum
     * @param callback
     */
    public static void recentAllocation(String mobileNum, HttpCallback callback)
    {
        try
        {
            String currentDt = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
            Call<RequestResult> taskCall = thinkWareService.recentAlloc(new AuthJson(currentDt, mobileNum, null));
            //Log.d(AppConstant.LOG_TEMP_TAG, "recent alloc");

            taskCall.enqueue(new Callback<RequestResult>()
            {
                @Override
                public void onResponse(Call<RequestResult> call, Response<RequestResult> response)
                {
                    try
                    {
                        RequestResult result = response.body();
                        if(result != null)
                        {
                            // 결과 문자열 디코딩
                            result.setCallID(AES256Util.decode(result.getCallID(), AES256Util.skey));
                            result.setCallDT(AES256Util.decode(result.getCallDT(), AES256Util.skey));
                            result.setCallStatus(AES256Util.decode(result.getCallStatus(), AES256Util.skey));
                        }

                        if(callback != null)
                        {
                            callback.onResult(result);
                        }
                    }
                    catch(Exception e)
                    {
                        //e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<RequestResult> call, Throwable t)
                {
                    try
                    {
                        if(callback != null)
                        {
                            callback.onResult(null);
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }
            });
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    // inavi map API ///////////////////////////////////////////////////////////////////

    public static void getReverseGeocode(double lat, double lng, HttpCallback callback)
    {
        try
        {
            Call<InaviApiReverseGeocode> taskCall = inaviService.getReverseGeocode(INAVI_APP_KEY, String.valueOf(lng), String.valueOf(lat), "1");
            taskCall.enqueue(new Callback<InaviApiReverseGeocode>()
            {
                @Override
                public void onResponse(Call<InaviApiReverseGeocode> call, Response<InaviApiReverseGeocode> response)
                {
                    try
                    {
                        if(callback != null)
                        {
                            callback.onResult(response.body());
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }

                @Override
                public void onFailure(Call<InaviApiReverseGeocode> call, Throwable t)
                {
                    try
                    {
                        if(callback != null)
                        {
                            callback.onResult(null);
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }
            });
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    public static void getRouteSearchNormalResult(LatLng latLng1, LatLng latLng2, HttpCallback callback)
    {
        try
        {
            Call<InaviApiRouteNormalResult> taskCall = inaviService.routeNormal(INAVI_APP_KEY,
                    latLng1.longitude, latLng1.latitude, latLng2.longitude, latLng2.latitude,
                    "real_traffic", "wgs84"
            );

            taskCall.enqueue(new Callback<InaviApiRouteNormalResult>()
            {
                @Override
                public void onResponse(Call<InaviApiRouteNormalResult> call, Response<InaviApiRouteNormalResult> response)
                {
                    try
                    {
                        if(callback != null)
                        {
                            callback.onResult(response.body());
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }

                @Override
                public void onFailure(Call<InaviApiRouteNormalResult> call, Throwable t)
                {
                    try
                    {
                        if(callback != null)
                        {
                            callback.onResult(null);
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }
            });
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    public static int getRouteSpendTime(InaviApiRouteNormalResult result)
    {
        if(result != null)
        {
            return result.route.data.spendTime;
        }

        return -1;
    }

    public static Call<InaviApiSearchResult> search(String keyword, HttpCallback callback)
    {
        try
        {
            if(keyword == null || keyword.trim().isEmpty())
            {
                callback.onResult(null);
            }

            Call<InaviApiSearchResult> taskCall = inaviService.searches(
                    INAVI_APP_KEY, keyword, "wgs84", "1"/*최상위 요소까지만 검색*/
            );

            taskCall.enqueue(new Callback<InaviApiSearchResult>()
            {
                @Override
                public void onResponse(Call<InaviApiSearchResult> call, Response<InaviApiSearchResult> response)
                {
                    try
                    {
                        if(call.isCanceled())
                        {
                            return;
                        }

                        List<RequestLocation> locationList = new ArrayList<>();
                        InaviApiSearchResult result = response.body();

                        if(result.header.isSuccessful)
                        {
                            InaviApiSearchData searchData = result.search;
                            if(searchData != null && searchData.result)
                            {
                                if(searchData.poiList != null && searchData.poiList.size() > 0)
                                {
                                    for(InaviApiPoi poi : searchData.poiList)
                                    {
                                        long poiId = poi.poiId;
                                        String name = poi.name1;
                                        double lat = Double.parseDouble(poi.dpy);
                                        double lng = Double.parseDouble(poi.dpx);
                                        String roadAddress = poi.roadName;
                                        String roadNumber = poi.roadNumber;
                                        String address = poi.address;
                                        String addressNumber = poi.addressNumber;

                                        RequestLocation location = new RequestLocation();
                                        if(name == null || name.trim().isEmpty())
                                        {
                                            // 장소 이름이 없는 경우
                                            // 주소의 일부를 대신 표시

                                            if(roadAddress != null && !roadAddress.trim().isEmpty() &&
                                                    roadNumber != null && !roadNumber.trim().isEmpty())
                                            {
                                                // 도로명 주소 사용
                                                String[] split = roadAddress.split(" ");
                                                int len = split.length;
                                                name = String.format("%s %s", split[len-1], roadNumber);

                                                location.positionName = name;
                                                location.address = (roadAddress + " " + roadNumber);
                                            }
                                            else
                                            {
                                                // 도로명 주소가 없으면, 구 주소를 사용
                                                String[] split = address.split(" ");
                                                int len = split.length;
                                                name = String.format("%s %s", split[len-1], addressNumber);

                                                location.positionName = name;
                                                location.address = (address + " " + addressNumber);
                                            }
                                        }
                                        else
                                        {
                                            location.positionName = name;

                                            if(roadAddress != null && !roadAddress.trim().isEmpty() &&
                                                    roadNumber != null && !roadNumber.trim().isEmpty())
                                            {
                                                // 도로명 주소가 있으면 도로명 주소로
                                                location.address = (roadAddress + " " + roadNumber);
                                                location.positionDetailName = location.address;
                                            }
                                            else
                                            {
                                                location.address = (address + " " + addressNumber);
                                                location.positionDetailName = location.address;
                                            }
                                        }

                                        location.poiId = poiId;
                                        location.lat = lat;
                                        location.lng = lng;

                                        locationList.add(location);
                                    }
                                }
                                else if(searchData.admList != null && searchData.admList.size() > 0)
                                {
                                    // 주소 검색의 경우
                                    for(InaviApiAdm adm : searchData.admList)
                                    {
                                        double lat = Double.parseDouble(adm.posY);
                                        double lng = Double.parseDouble(adm.posX);
                                        String roadAddress = adm.roadName;
                                        String roadNumber = adm.roadNumber;
                                        String address = adm.address;
                                        String addressNumber = adm.addressNumber;

                                        RequestLocation location = new RequestLocation();

                                        if(roadAddress != null && !roadAddress.trim().isEmpty() &&
                                                roadNumber != null && !roadNumber.trim().isEmpty())
                                        {
                                            // 도로명 주소 사용
                                            String[] split = roadAddress.split(" ");
                                            int len = split.length;
                                            String name = String.format("%s %s", split[len-1], roadNumber);

                                            location.positionName = name;
                                            location.address = (roadAddress + " " + roadNumber);
                                        }
                                        else
                                        {
                                            // 도로명 주소가 없으면, 구 주소를 사용
                                            String[] split = address.split(" ");
                                            int len = split.length;
                                            String name = String.format("%s %s", split[len-1], addressNumber);

                                            location.positionName = name;
                                            location.address = (address + " " + addressNumber);
                                        }

                                        location.lat = lat;
                                        location.lng = lng;

                                        locationList.add(location);
                                    }
                                }
                            }
                        }

                        if(callback != null)
                        {
                            if(locationList.size() <= 0)
                            {
                                locationList = null;
                            }

                            callback.onResult(locationList);
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }

                @Override
                public void onFailure(Call<InaviApiSearchResult> call, Throwable t)
                {
                    try
                    {
                        if(call.isCanceled())
                        {
                            return;
                        }

                        if(callback != null)
                        {
                            callback.onResult(null);
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }
            });

            return taskCall;
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }

        return null;
    }

    public static void getSuggestedKeywords(String query, HttpCallback callback)
    {
        try
        {
            if(query == null || query.trim().isEmpty())
            {
                callback.onResult(null);
            }

            Call<InaviApiKeywordSuggestionResult> taskCall = inaviService.proposers(INAVI_APP_KEY, query);
            taskCall.enqueue(new Callback<InaviApiKeywordSuggestionResult>()
            {
                @Override
                public void onResponse(Call<InaviApiKeywordSuggestionResult> call, Response<InaviApiKeywordSuggestionResult> response)
                {
                    try
                    {
                        InaviApiKeywordSuggestionResult result = response.body();
                        String keyword = null;
                        if(result != null && result.header.isSuccessful && result.proposer.result && result.proposer.count > 0)
                        {
                            // 빈도수로 정리(가장 빈도수가 높은 키워드가 맨 위)
                            Collections.sort(result.proposer.keywordList, keywordComparator);
                            keyword = result.proposer.keywordList.get(0).keyword;
                        }

                        if(callback != null)
                        {
                            callback.onResult(keyword);
                        }
                    }
                    catch(Exception e)
                    {
                        callback.onResult(null);
                    }
                }

                @Override
                public void onFailure(Call<InaviApiKeywordSuggestionResult> call, Throwable t)
                {
                    try
                    {
                        if(callback != null)
                        {
                            callback.onResult(null);
                        }
                    }
                    catch(Exception e)
                    {

                    }
                }
            });
        }
        catch(Exception e)
        {
            Log.d(AppConstant.LOG_DEBUG_TAG, e.getMessage());
        }
    }

    private static Comparator<InaviApiKeyword> keywordComparator = (o1, o2) ->
    {
        try
        {
            int f1 = o1.frequency;
            int f2 = o2.frequency;
            return f2 - f1; // DESC
        }
        catch(Exception e)
        {

        }

        return 0;
    };
}
