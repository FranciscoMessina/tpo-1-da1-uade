package com.da_grupo9.ronda.data.remote;

import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("auth/login/password") Call<ResponseBody> login(@Body Map<String, String> body);
    @POST("auth/otp/request") Call<ResponseBody> requestOtp(@Body Map<String, String> body);
    @POST("auth/otp/resend") Call<ResponseBody> resendOtp(@Body Map<String, String> body);
    @POST("auth/otp/verify") Call<ResponseBody> verifyOtp(@Body Map<String, String> body);
    @POST("auth/logout") Call<ResponseBody> logout();
}
