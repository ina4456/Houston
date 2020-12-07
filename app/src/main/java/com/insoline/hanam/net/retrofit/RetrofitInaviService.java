package com.insoline.hanam.net.retrofit;

import com.insoline.hanam.net.response.inaviapi.InaviApiKeywordSuggestionResult;
import com.insoline.hanam.net.response.inaviapi.InaviApiReverseGeocode;
import com.insoline.hanam.net.response.inaviapi.InaviApiRouteNormalResult;
import com.insoline.hanam.net.response.inaviapi.InaviApiSearchResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RetrofitInaviService
{
    @GET("maps/v3.0/appkeys/{appkey}/addresses")
    Call<InaviApiReverseGeocode> getReverseGeocode(
            @Path("appkey") String appKey,
            @Query("posX") String posX, @Query("posY") String posY,
            @Query("coordtype") String coordtype
    );

    @GET("maps/v3.0/appkeys/{appkey}/route-normal")
    Call<InaviApiRouteNormalResult> routeNormal(
            @Path("appkey") String appKey,
            @Query("startX") double startX, @Query("startY") double startY,
            @Query("endX") double endX, @Query("endY") double endY,
            @Query("option") String option, @Query("coordType") String coordType
    );

    @GET("maps/v3.0/appkeys/{appkey}/searches")
    Call<InaviApiSearchResult> searches(
            @Path("appkey") String appKey,
            @Query("query") String query,
            @Query("coordType") String coordType,
            @Query("depth") String depth
    );

    @GET("maps/v3.0/appkeys/{appkey}/proposers")
    Call<InaviApiKeywordSuggestionResult> proposers(
            @Path("appkey") String appKey,
            @Query("query") String query
    );
}
