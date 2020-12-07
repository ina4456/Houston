package com.insoline.hanam.net.retrofit;

import com.insoline.hanam.net.json.AuthJson;
import com.insoline.hanam.net.json.CallJson;
import com.insoline.hanam.net.json.RequestJson;
import com.insoline.hanam.net.response.AuthResult;
import com.insoline.hanam.net.response.CarInfoResult;
import com.insoline.hanam.net.response.HistoryResult;
import com.insoline.hanam.net.response.RequestResult;
import com.insoline.hanam.net.response.VisitedLocationResult;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RetrofitThinkWareService
{
    /**
     * 인증 번호 요청
     * @param authJson
     * @return
     */
    @POST("auth")
    Call<AuthResult> getAuthCode(@Body AuthJson authJson);

    /**
     * 인증 번호 확인(전화번호 등록)
     * @param authJson
     * @return
     */
    @POST("auth/set")
    Call<AuthResult> signUp(@Body AuthJson authJson);

    /**
     * 필수 업데이트 확인
     * @param authJson
     * @return
     */
    @POST("getinfo/version/android")
    Call<AuthResult> versionCheck(@Body AuthJson authJson);

    /**
     * 배차 요청
     * @param requestJson
     * @return
     */
    @POST("alloc/request")
    Call<RequestResult> request(@Body RequestJson requestJson);

    /**
     * 배차 취소(호출 취소)
     * @param callJson
     * @return
     */
    @POST("alloc/cancel")
    Call<RequestResult> cancelCall(@Body CallJson callJson);

    /**
     * 요청 상태 확인
     * @param callJson
     * @return
     */
    @POST("getinfo/callcheck")
    Call<RequestResult> callCheck(@Body CallJson callJson);

    /**
     * 배차된 기사님 정보
     * @param callJson
     * @return
     */
    @POST("getinfo/driver")
    Call<CarInfoResult> getDriverInfo(@Body CallJson callJson);

    /**
     * 배차된 차량 위치
     * @param callJson
     * @return
     */
    @POST("getinfo/carpos")
    Call<CarInfoResult> getCarPosition(@Body CallJson callJson);

    /**
     * 탑승이력
     * @param authJson
     * @return
     */
    @POST("getinfo/history")
    Call<HistoryResult> getHistory(@Body AuthJson authJson);

    /**
     * 탑승이력 삭제
     * @param authJson
     * @return
     */
    @POST("getinfo/history/remove")
    Call<AuthResult> removeHistory(@Body AuthJson authJson);

    /**
     * 탑승이력 전체 삭제
     * @param authJson
     * @return
     */
    @POST("getinfo/history/removeall")
    Call<AuthResult> removeHistoryAll(@Body AuthJson authJson);

    /**
     * 기사님 평가
     * @param callJson
     * @return
     */
    @POST("driver/review")
    Call<AuthResult> sendReview(@Body CallJson callJson);

    /**
     * 최빈 방문 목적지 목록(최대 10개)
     * @param authJson
     * @return
     */
    @POST("getinfo/freq/visited")
    Call<VisitedLocationResult> getFrequencyVisitedLocations(@Body AuthJson authJson);

    /**
     * 콜센터 연결 후 호출 요청 확인
     * @param authJson
     * @return
     */
    @POST("/getinfo/recent/alloc")
    Call<RequestResult> recentAlloc(@Body AuthJson authJson);

    /**
     * 콜센터에 사용자 위치 정보 전송
     * @param requestJson
     * @return
     */
    @POST("/set/passenger/location")
    Call<AuthResult> sendPassengerLocation(@Body RequestJson requestJson);
}
