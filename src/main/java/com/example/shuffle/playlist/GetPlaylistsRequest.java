package com.example.shuffle.playlist;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GetPlaylistsRequest {
    private String spotifyAccessToken;

    @JsonCreator
    public GetPlaylistsRequest(@JsonProperty("spotifyAccessToken") String spotifyAccessToken) {
        this.spotifyAccessToken = spotifyAccessToken;
    }

    public String getSpotifyAccessToken() {
        return spotifyAccessToken;
    }

    public void setSpotifyAccessToken(String spotifyAccessToken) {
        this.spotifyAccessToken = spotifyAccessToken;
    }
}
