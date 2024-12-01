package com.example.smartfarm.service;

import com.example.smartfarm.model.SensorData;
import org.springframework.web.client.RestTemplate;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DeviceService {
    private static final String BASE_URL = "http://192.168.4.2:8080"; // Địa chỉ IP và cổng của server Spring Boot
    private DeviceApi deviceApi;

    public DeviceService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        deviceApi = retrofit.create(DeviceApi.class);
    }

    public Call<String> controlLight(String state) {
        return deviceApi.controlLight(state);
    }

    public Call<String> controlFan(String state) {
        return deviceApi.controlFan(state);
    }

    public Call<String> controlDoor(String action) {
        return deviceApi.controlDoor(action);
    }

    public Call<String> feed() {
        return deviceApi.feed();
    }

    public Call<SensorData> getSensorData() {
        return deviceApi.getSensorData();
    }

    // Phương thức mới để lấy trạng thái thiết bị
    public Call<String> getDeviceStatus() {
        return deviceApi.getDeviceStatus();
    }

}
