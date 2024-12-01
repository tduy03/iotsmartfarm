package com.example.farm_api.controller;

import com.example.farm_api.model.SensorData;
import com.example.farm_api.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/device")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @PostMapping("/light")
    public String controlLight(@RequestParam String state) {
        return deviceService.controlLight(state);
    }

    @PostMapping("/fan")
    public String controlFan(@RequestParam String state) {
        return deviceService.controlFan(state);
    }

    @PostMapping("/servo/feed")
    public String feed() {
        return deviceService.feed();
    }

    @PostMapping("/servo/door")
    public String controlDoor(@RequestParam String action) {
        return deviceService.controlDoor(action);
    }

    @GetMapping("/sensor")
    public SensorData getSensorData() {
        return deviceService.getSensorData();
    }

    // Phương thức mới để lấy trạng thái thiết bị
    @GetMapping("/status")
    public String getDeviceStatus() {
        return deviceService.getDeviceStatus();
    }
}
