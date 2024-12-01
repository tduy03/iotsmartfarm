package com.example.smartfarm.service;

import com.example.smartfarm.model.SensorData;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface DeviceApi {

    @POST("/api/device/light")
    Call<String> controlLight(@Query("state") String state);

    @POST("/api/device/fan")
    Call<String> controlFan(@Query("state") String state);

    @POST("/api/device/servo/door")
    Call<String> controlDoor(@Query("action") String action);

    @POST("/api/device/servo/feed")
    Call<String> feed();

    @GET("/api/device/sensor")
    Call<SensorData> getSensorData();

    @GET("/api/device/status") // Thay đổi URL tùy theo định nghĩa của API
    Call<String> getDeviceStatus();



}
