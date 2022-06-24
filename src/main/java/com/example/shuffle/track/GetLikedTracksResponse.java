package com.example.shuffle.track;

import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.ArrayList;

public class GetLikedTracksResponse {
    private String status;
    private ArrayList<IPlaylistItem> allTracks;

    public GetLikedTracksResponse(String status, ArrayList<IPlaylistItem> allTracks) {
        this.status = status;
        this.allTracks = allTracks;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<IPlaylistItem> getAllTracks() {
        return allTracks;
    }

    public void setAllTracks(ArrayList<IPlaylistItem> allTracks) {
        this.allTracks = allTracks;
    }
}
