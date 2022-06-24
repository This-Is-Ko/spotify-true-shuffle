package com.example.shuffle.playlist;

public class PlaylistShuffleResponse {
    private String status;

    public PlaylistShuffleResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
