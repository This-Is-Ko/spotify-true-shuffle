package com.example.shuffle.playlist;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GetPlaylistInfoRequest {
    private String spotifyAccessToken;
    private String playlistId;

    @JsonCreator
    public GetPlaylistInfoRequest(@JsonProperty("spotifyAccessToken") String spotifyAccessToken, String playlistId) {
        this.spotifyAccessToken = spotifyAccessToken;
        this.playlistId = playlistId;
    }

    public String getSpotifyAccessToken() {
        return spotifyAccessToken;
    }

    public void setSpotifyAccessToken(String spotifyAccessToken) {
        this.spotifyAccessToken = spotifyAccessToken;
    }

    public String getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }
}
