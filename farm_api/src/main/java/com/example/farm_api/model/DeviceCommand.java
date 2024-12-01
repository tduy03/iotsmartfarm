// DeviceCommand.java
package com.example.farm_api.model;

public class DeviceCommand {
    private String state;  // for light and fan
    private String action; // for door

    public DeviceCommand() {}

    public DeviceCommand(String state, String action) {
        this.state = state;
        this.action = action;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}