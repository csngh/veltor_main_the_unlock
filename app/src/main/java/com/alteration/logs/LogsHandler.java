package com.alteration.logs;

class LogsHandler {

    private String nickName;

    LogsHandler(String nickName) {
        this.setNickName(nickName);
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
