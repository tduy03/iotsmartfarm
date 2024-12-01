package com.example.farm_api.model;

public class SensorData {
    private float temperature;
    private float humidity;

    public SensorData(float temperature, float humidity) {
        this.temperature = temperature;
        this.humidity = humidity;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getHumidity() {
        return humidity;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }
}
