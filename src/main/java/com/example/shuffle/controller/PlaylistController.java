package com.example.shuffle.controller;

import com.example.shuffle.playlist.*;
import com.example.shuffle.track.TrackSupport;
import com.google.gson.JsonArray;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.data.library.GetUsersSavedTracksRequest;
import se.michaelthelin.spotify.requests.data.playlists.AddItemsToPlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.CreatePlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistsItemsRequest;

import java.io.IOException;
import java.util.*;

@RestController
@ResponseBody
@RequestMapping("/playlist")
public class PlaylistController {

    Logger LOG = LoggerFactory.getLogger(PlaylistController.class);

    private static final int MAX_TRACKS_IN_ONE_REQUEST = 100;

    @PostMapping(value = "/shuffle")
    public @ResponseBody PlaylistShuffleResponse shuffle(@RequestBody ShuffleRequest shuffleRequest) {
        SpotifyApi spotifyApiService = new SpotifyApi.Builder()
                .setAccessToken(shuffleRequest.getSpotifyAccessToken())
                .build();

        ArrayList<IPlaylistItem> allTracks;
        if (shuffleRequest.isUseLikedTracks()){
            allTracks = TrackSupport.getAllLikedTracks(spotifyApiService);
        } else {
            allTracks = PlaylistSupport.getAllTracksFromPlaylist(spotifyApiService, shuffleRequest);
        }

        if (allTracks == null){
            return new PlaylistShuffleResponse("Error");
        }

        // Randomise order
        Collections.shuffle(allTracks);

        // Create new playlist
        // TODO Handle shuffleRequest.isMakeNewPlaylist
        final Playlist newPlaylist = createNewPlaylist(spotifyApiService, shuffleRequest.getUserId(), "True Shuffled Playlist");
        if (newPlaylist == null){
            return new PlaylistShuffleResponse("Error");
        }

        boolean isAddedAllTracks = false;
        int numOfCalls = 0;
        AddItemsToPlaylistRequest addItemsToPlaylistRequest;
        while (!isAddedAllTracks){
            JsonArray trackUris = new JsonArray();
            // Add 100 tracks at a time
            for (int i=0; i<MAX_TRACKS_IN_ONE_REQUEST; i++){
                int index = numOfCalls * MAX_TRACKS_IN_ONE_REQUEST + i;
                if (index < allTracks.size()){
                    trackUris.add(allTracks.get(index).getUri());
                } else {
                    isAddedAllTracks = true;
                    break;
                }
            }

            numOfCalls++;
            addItemsToPlaylistRequest = spotifyApiService
                    .addItemsToPlaylist(newPlaylist.getId(), trackUris)
                    .build();
            try {
                final SnapshotResult snapshotResult = addItemsToPlaylistRequest.execute();
                LOG.info("Snapshot ID: " + snapshotResult.getSnapshotId());
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                LOG.info(e.getMessage());
                return new PlaylistShuffleResponse("Error");
            }
        }

        return new PlaylistShuffleResponse("Success");
    }

    @PostMapping(value = "/my-playlists")
    public @ResponseBody GetPlaylistsResponse getPlaylists(@RequestBody GetPlaylistsRequest getPlaylistsRequest) {
        SpotifyApi spotifyApiService = new SpotifyApi.Builder()
            .setAccessToken(getPlaylistsRequest.getSpotifyAccessToken())
            .build();
        GetListOfCurrentUsersPlaylistsRequest getListOfCurrentUsersPlaylistsRequest = spotifyApiService
            .getListOfCurrentUsersPlaylists()
            .limit(50)
            .build();
        try {
            final Paging<PlaylistSimplified> playlistSimplifiedPaging = getListOfCurrentUsersPlaylistsRequest.execute();
            return new GetPlaylistsResponse("Success", playlistSimplifiedPaging.getItems());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            LOG.info(e.getMessage());
            return new GetPlaylistsResponse("Error", null);
        }
    }

    private Playlist createNewPlaylist(SpotifyApi spotifyApiService, String userId, String playlistName){
        CreatePlaylistRequest createPlaylistRequest = spotifyApiService.createPlaylist(userId, playlistName)
                .collaborative(false)
                .public_(false)
                .description("True Shuffle")
                .build();
        try {
            return createPlaylistRequest.execute();
        } catch (ParseException | SpotifyWebApiException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
