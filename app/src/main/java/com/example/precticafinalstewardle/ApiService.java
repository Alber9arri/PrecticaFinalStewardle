package com.example.precticafinalstewardle;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("drivers.json")
    Call<String> getData();
}
