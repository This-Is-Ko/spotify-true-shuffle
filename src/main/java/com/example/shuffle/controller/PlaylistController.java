package com.example.shuffle.controller;

import com.example.shuffle.playlist.GetPlaylistsRequest;
import com.example.shuffle.playlist.GetPlaylistsResponse;
import com.example.shuffle.playlist.PlaylistShuffleResponse;
import com.example.shuffle.playlist.ShuffleRequest;
import com.google.gson.JsonArray;
import org.apache.hc.core5.http.ParseException;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
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

    private static final int MAX_TRACKS_IN_ONE_REQUEST = 100;

    @PostMapping(value = "/true-shuffle")
    public @ResponseBody PlaylistShuffleResponse shuffle(@RequestBody ShuffleRequest shuffleRequest) {
        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setAccessToken(shuffleRequest.getSpotifyAccessToken())
                .build();

        ArrayList<IPlaylistItem> allTracks = new ArrayList<>();
        try{
            // Get playlist tracks
            boolean isMoreTracks = true;
            GetPlaylistsItemsRequest getPlaylistsItemsRequest = spotifyApi
                    .getPlaylistsItems(shuffleRequest.getPlaylistId())
                    .offset(0)
                    .limit(50)
                    .build();
            while (isMoreTracks){
                final Paging<PlaylistTrack> playlistTrackPaging = getPlaylistsItemsRequest.execute();
                ArrayList<PlaylistTrack> currentTracks = new ArrayList<>(Arrays.asList(playlistTrackPaging.getItems()));
                // Store track information
                currentTracks.forEach(trackEntry -> {
                    allTracks.add(trackEntry.getTrack());
                });

                if (playlistTrackPaging.getNext() == null){
                    isMoreTracks = false;
                } else {
                    // Get next page of tracks
                    getPlaylistsItemsRequest = spotifyApi
                            .getPlaylistsItems(shuffleRequest.getPlaylistId())
                            .offset(playlistTrackPaging.getOffset() + 50)
                            .limit(50)
                            .build();
                }
            }
        } catch (ParseException | SpotifyWebApiException | IOException e) {
            System.out.println("Error: " + e.getMessage());
            return new PlaylistShuffleResponse("Error");
        }

        // Put into list? -> Randomise -> Create new playlist -> Add in groups of 100 tracks
        Collections.shuffle(allTracks);

        CreatePlaylistRequest createPlaylistRequest = spotifyApi.createPlaylist(shuffleRequest.getUserId(), "True Shuffled Playlist")
            .collaborative(false)
            .public_(false)
            .description("True Shuffle")
            .build();
        final Playlist newShuffledPlaylist;
        try {
            newShuffledPlaylist = createPlaylistRequest.execute();
        } catch (ParseException | SpotifyWebApiException | IOException e) {
            e.printStackTrace();
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
            addItemsToPlaylistRequest = spotifyApi
                    .addItemsToPlaylist(newShuffledPlaylist.getId(), trackUris)
                    .build();
            try {
                final SnapshotResult snapshotResult = addItemsToPlaylistRequest.execute();
                System.out.println("Snapshot ID: " + snapshotResult.getSnapshotId());
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                System.out.println("Error: " + e.getMessage());
                return new PlaylistShuffleResponse("Error");
            }
        }

        return new PlaylistShuffleResponse("Success");
    }

    @PostMapping(value = "/my-playlists")
    public @ResponseBody GetPlaylistsResponse getPlaylists(@RequestBody GetPlaylistsRequest getPlaylistsRequest) {
        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setAccessToken(getPlaylistsRequest.getSpotifyAccessToken())
                .build();
        GetListOfCurrentUsersPlaylistsRequest getListOfCurrentUsersPlaylistsRequest = spotifyApi
            .getListOfCurrentUsersPlaylists()
            .limit(50)
            .build();
        try {
            final Paging<PlaylistSimplified> playlistSimplifiedPaging = getListOfCurrentUsersPlaylistsRequest.execute();

            System.out.println("Response: " + playlistSimplifiedPaging);
            return new GetPlaylistsResponse("Success", playlistSimplifiedPaging.getItems());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            return new GetPlaylistsResponse("Error", null);
        }
    }
}
