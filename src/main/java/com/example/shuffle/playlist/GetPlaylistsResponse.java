package com.example.shuffle.playlist;

import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;

import java.util.ArrayList;

public class GetPlaylistsResponse {
    private String status;
    private ArrayList<PlaylistSimplified> allPlaylists;

    public GetPlaylistsResponse(String status, ArrayList<PlaylistSimplified> allPlaylists) {
        this.status = status;
        this.allPlaylists = allPlaylists;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<PlaylistSimplified> getAllPlaylists() {
        return allPlaylists;
    }

    public void setAllPlaylists(ArrayList<PlaylistSimplified> allPlaylists) {
        this.allPlaylists = allPlaylists;
    }
}
