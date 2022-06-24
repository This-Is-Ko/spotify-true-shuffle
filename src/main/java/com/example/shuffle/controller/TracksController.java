package com.example.shuffle.controller;

import com.example.shuffle.playlist.PlaylistSupport;
import com.example.shuffle.track.GetLikedTracksRequest;
import com.example.shuffle.track.GetLikedTracksResponse;
import com.example.shuffle.track.TrackSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.ArrayList;

@RestController
@ResponseBody
@RequestMapping("/track")
public class TracksController {

    Logger LOG = LoggerFactory.getLogger(TracksController.class);

    @PostMapping(value = "/liked-tracks")
    public @ResponseBody GetLikedTracksResponse getLikedTracks(@RequestBody GetLikedTracksRequest getLikedTracksRequest) {
        SpotifyApi spotifyApiService = new SpotifyApi.Builder()
                .setAccessToken(getLikedTracksRequest.getSpotifyAccessToken())
                .build();

        ArrayList<IPlaylistItem> allTracks = TrackSupport.getAllLikedTracks(spotifyApiService);
        if (allTracks == null){
            return new GetLikedTracksResponse("Error", null);
        }
        return new GetLikedTracksResponse("Success", allTracks);
    }
}
