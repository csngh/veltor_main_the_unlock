package com.alteration.child;

public class ChildDeviceListHandler {
    private String devID, devName;

    public ChildDeviceListHandler(String devName, String devID) {
        this.setDevID(devID);
        this.setDevName(devName);
    }

    public String getDevID() {
        return devID;
    }

    public void setDevID(String devID) {
        this.devID = devID;
    }

    public String getDevName() {
        return devName;
    }

    public void setDevName(String devName) {
        this.devName = devName;
    }
}
