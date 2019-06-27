package com.alteration.parent;

class ShareListHandler {

    private String devName;

    public ShareListHandler() {

    }

    public ShareListHandler(String devName) {
        this.setDevName(devName);
    }

    public String getDevName() {
        return devName;
    }

    public void setDevName(String devName) {
        this.devName = devName;
    }
}
