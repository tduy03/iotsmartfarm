package com.example.farm_api.service;

import com.example.farm_api.model.SensorData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DeviceService {

    @Autowired
    private RestTemplate restTemplate;

    private final String baseUrl = "http://192.168.4.1"; // Địa chỉ IP của ESP8266

    // Biến để lưu trữ trạng thái thiết bị
    private boolean isLightOn = false;
    private boolean isFanOn = false;
    private boolean isDoorOpen = false;
    private boolean isFeeding = false;

    public String controlLight(String state) {
        String url = baseUrl + "/light?state=" + state;
        String response = restTemplate.postForObject(url, null, String.class);
        // Cập nhật trạng thái
        isLightOn = state.equals("on");
        return response;
    }

    public String controlFan(String state) {
        String url = baseUrl + "/fan?state=" + state;
        String response = restTemplate.postForObject(url, null, String.class);
        // Cập nhật trạng thái
        isFanOn = state.equals("on");
        return response;
    }

    public String feed() {
        String url = baseUrl + "/servo/feed";
        String response = restTemplate.postForObject(url, null, String.class);
        // Cập nhật trạng thái
        isFeeding = true; // Giả định rằng việc cho ăn đang diễn ra
        return response;
    }

    public String controlDoor(String action) {
        String url = baseUrl + "/servo/door?action=" + action;
        String response = restTemplate.postForObject(url, null, String.class);
        // Cập nhật trạng thái
        isDoorOpen = action.equals("open");
        return response;
    }

    public SensorData getSensorData() {
        String url = baseUrl + "/sensor";
        return restTemplate.getForObject(url, SensorData.class);
    }

    // Phương thức để lấy trạng thái thiết bị
    public String getDeviceStatus() {
        return String.format("Light: %s, Fan: %s, Door: %s, Feeding: %s",
                isLightOn ? "ON" : "OFF",
                isFanOn ? "ON" : "OFF",
                isDoorOpen ? "OPEN" : "CLOSED",
                isFeeding ? "IN PROGRESS" : "NOT IN PROGRESS");
    }
}
