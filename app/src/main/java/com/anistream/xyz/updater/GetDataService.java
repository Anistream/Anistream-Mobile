package com.anistream.xyz.updater;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface GetDataService {

    @GET("anistream.json")
    Call<UpdatePojo> getCurrentVersion();

    @Streaming
    @GET
    Call<ResponseBody> downloadFileByUrl(@Url String fileUrl);
}