package com.example.shuffle.auth;

public class AuthCodeRedirectResponse {
    private String status;
    private String spotifyAccessToken;
    private String spotifyRefreshToken;
    private Integer expiry;

    public AuthCodeRedirectResponse(String status, String spotifyAccessToken, String spotifyRefreshToken, Integer expiry) {
        this.status = status;
        this.spotifyAccessToken = spotifyAccessToken;
        this.spotifyRefreshToken = spotifyRefreshToken;
        this.expiry = expiry;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSpotifyAccessToken() {
        return spotifyAccessToken;
    }

    public void setSpotifyAccessToken(String spotifyAccessToken) {
        this.spotifyAccessToken = spotifyAccessToken;
    }

    public Integer getExpiry() {
        return expiry;
    }

    public void setExpiry(Integer expiry) {
        this.expiry = expiry;
    }

    public String getSpotifyRefreshToken() {
        return spotifyRefreshToken;
    }

    public void setSpotifyRefreshToken(String spotifyRefreshToken) {
        this.spotifyRefreshToken = spotifyRefreshToken;
    }
}
