package com.example.shuffle.controller;

import com.example.shuffle.track.GetLikedTracksRequest;
import com.example.shuffle.track.GetLikedTracksResponse;
import com.example.shuffle.track.TrackSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;

import java.util.ArrayList;

@RestController
@ResponseBody
@CrossOrigin(origins = {"http://localhost:3000","https://spotify-true-shuffle-react.vercel.app", "www.notcloud.me"})
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No tracks found");
        }
        return new GetLikedTracksResponse("Success", allTracks);
    }
}
