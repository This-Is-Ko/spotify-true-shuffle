package com.example.shuffle.track;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GetLikedTracksRequest {
    private String spotifyAccessToken;

    @JsonCreator
    public GetLikedTracksRequest(@JsonProperty("spotifyAccessToken") String spotifyAccessToken) {
        this.spotifyAccessToken = spotifyAccessToken;
    }

    public String getSpotifyAccessToken() {
        return spotifyAccessToken;
    }

    public void setSpotifyAccessToken(String spotifyAccessToken) {
        this.spotifyAccessToken = spotifyAccessToken;
    }
}
