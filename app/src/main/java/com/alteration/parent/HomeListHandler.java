package com.alteration.parent;

class HomeListHandler {

    private String devID, devName;
    private int devNo;

    public HomeListHandler() {

    }

    HomeListHandler(String devId, String devName, int devNo) {
        this.setDevID(devId);
        this.setDevName(devName);
        this.setDevNo(devNo);
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

    public int getDevNo() {
        return devNo;
    }

    public void setDevNo(int devNo) {
        this.devNo = devNo;
    }
}
