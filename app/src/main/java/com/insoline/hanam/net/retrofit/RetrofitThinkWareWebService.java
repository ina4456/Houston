package com.insoline.hanam.net.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface RetrofitThinkWareWebService
{
    /**
     * 약관 내용
     * @return
     */
    @GET("getinfo/terms")
    Call<ResponseBody> getTerms();
}
