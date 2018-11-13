package com.fallendeductionist.evaluacion4;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    String API_BASE_URL = "http://10.0.2.2:8000";

    @GET("/solicitudes/")
    Call<List<Solicitude>> getSolicitudes();

    @FormUrlEncoded
    @POST("/solicitudes")
    Call< Solicitude> createSolicitude(@Field("title") String title,
                                   @Field("email") String email,
                                   @Field("navigation") String navigation);
    @Multipart
    @POST("/solicitudes")
    Call< Solicitude> createSolicitudeWithImage(
            @Part("title") RequestBody title,
            @Part("email") RequestBody email,
            @Part("navigation") RequestBody navigation,
            @Part MultipartBody.Part image);

}
