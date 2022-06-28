package com.example.shuffle.controller;

import com.example.shuffle.playlist.*;
import com.example.shuffle.track.TrackSupport;
import com.google.gson.JsonArray;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.exceptions.detailed.UnauthorizedException;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.data.playlists.AddItemsToPlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.CreatePlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

@RestController
@ResponseBody
@CrossOrigin(origins = {"http://localhost:3000","https://spotify-true-shuffle-react.vercel.app", "www.notcloud.me"})
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No tracks found");
        }

        // Randomise order
        Collections.shuffle(allTracks);

        // Get user id
        GetCurrentUsersProfileRequest getCurrentUsersProfileRequest = spotifyApiService.getCurrentUsersProfile()
                .build();
        User user;
        try {
            user = getCurrentUsersProfileRequest.execute();
        } catch (UnauthorizedException e){
            LOG.info(e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            LOG.info(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        // Create new playlist
        // TODO Handle shuffleRequest.isMakeNewPlaylist
        final Playlist newPlaylist = createNewPlaylist(spotifyApiService, user.getId(), "True Shuffled Playlist");
        if (newPlaylist == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Creating new playlist failed");
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
            } catch (UnauthorizedException e){
                LOG.info(e.getMessage());
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                LOG.info(e.getMessage());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
        }

        return new PlaylistShuffleResponse("Success", newPlaylist.getExternalUrls().get("spotify"));
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
        } catch (UnauthorizedException e){
            LOG.info(e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            LOG.info(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping(value = "/get-playlist-info")
    public @ResponseBody GetPlaylistResponse getPlaylistInfo(@RequestBody GetPlaylistInfoRequest getPlaylistInfoRequest) {
        SpotifyApi spotifyApiService = new SpotifyApi.Builder()
                .setAccessToken(getPlaylistInfoRequest.getSpotifyAccessToken())
                .build();

        GetPlaylistRequest getPlaylistRequest = spotifyApiService
                .getPlaylist(getPlaylistInfoRequest.getPlaylistId())
                .build();
        try {
            Playlist playlist = getPlaylistRequest.execute();
            return new GetPlaylistResponse("Success", playlist);
        } catch (UnauthorizedException e){
            LOG.info(e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            LOG.info(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
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
