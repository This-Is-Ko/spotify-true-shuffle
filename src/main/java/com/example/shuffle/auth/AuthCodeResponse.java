package com.example.shuffle.auth;

public class AuthCodeResponse {

    private String loginUri;

    public AuthCodeResponse(String loginUri) {
        this.loginUri = loginUri;
    }

    public String getLoginUri() {
        return loginUri;
    }

    public void setLoginUri(String loginUri) {
        this.loginUri = loginUri;
    }
}
