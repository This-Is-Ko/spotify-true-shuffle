package com.example.shuffle.playlist;

public class PlaylistShuffleResponse {
    private String status;
    private String playlistUrl;

    public PlaylistShuffleResponse(String status, String playlistUrl) {
        this.status = status;
        this.playlistUrl = playlistUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPlaylistUrl() {
        return playlistUrl;
    }

    public void setPlaylistUrl(String playlistUrl) {
        this.playlistUrl = playlistUrl;
    }
}
