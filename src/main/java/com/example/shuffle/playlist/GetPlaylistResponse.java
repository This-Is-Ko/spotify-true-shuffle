package com.example.shuffle.playlist;

import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;

public class GetPlaylistResponse {
    private String status;
    private Playlist playlist;

    public GetPlaylistResponse(String status, Playlist playlist) {
        this.status = status;
        this.playlist = playlist;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }
}
