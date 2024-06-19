package com.example.omrifit;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface PexelsService {
    @Headers("Authorization: qHQJiEBgaAxMv5g0gU9H1BS7JvKYON0PDAcAG1r4EoU47RHcRyMBcx2K")
    @GET("search")
    Call<PexelsResponse> searchPhotos(@Query("query") String query);
}
 class PexelsResponse {
    List<Photo> photos;

    static class Photo {
        Src src;

        static class Src {
            String original;
        }
    }
}

