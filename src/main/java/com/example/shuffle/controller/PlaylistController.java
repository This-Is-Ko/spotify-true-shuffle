package com.example.shuffle.controller;

import com.example.shuffle.playlist.*;
import com.example.shuffle.track.TrackSupport;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
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
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.data.follow.legacy.UnfollowPlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.AddItemsToPlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.CreatePlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.RemoveItemsFromPlaylistRequest;
import se.michaelthelin.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

@RestController
@ResponseBody
@CrossOrigin(origins = {"http://localhost:3000","https://spotify-true-shuffle-react.vercel.app", "https://www.notcloud.me"})
@RequestMapping("/playlist")
public class PlaylistController {

    Logger LOG = LoggerFactory.getLogger(PlaylistController.class);

    private static final int MAX_TRACKS_IN_ONE_REQUEST = 100;
    private static final String SHUFFLED_PLAYLIST_PREFIX = "[Shuffled] ";
    private static final String LIKED_TRACKS_PLAYLIST_ID = "likedTracks";

    @PostMapping(value = "/shuffle")
    public @ResponseBody PlaylistShuffleResponse shuffle(@RequestBody ShuffleRequest shuffleRequest) {
        SpotifyApi spotifyApiService = new SpotifyApi.Builder()
                .setAccessToken(shuffleRequest.getSpotifyAccessToken())
                .build();

        ArrayList<IPlaylistItem> allTracks;
        try {
            if (shuffleRequest.getPlaylistId().equals(LIKED_TRACKS_PLAYLIST_ID)){
                allTracks = TrackSupport.getAllLikedTracks(spotifyApiService);
            } else {
                allTracks = PlaylistSupport.getAllTracksFromPlaylist(spotifyApiService, shuffleRequest);
            }
        } catch (UnauthorizedException e){
            LOG.info(e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            LOG.info(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        if (allTracks.size() == 0){
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

        final Playlist newPlaylist;
        try{
            String shuffledPlaylistName;
            if (shuffleRequest.getPlaylistId().equals(LIKED_TRACKS_PLAYLIST_ID)){
                shuffledPlaylistName = SHUFFLED_PLAYLIST_PREFIX + "Liked Tracks";
            } else {
                // Get selected playlist info
                Playlist playlistInfo = PlaylistSupport.getPlaylistInfo(spotifyApiService, shuffleRequest.getPlaylistId());
                shuffledPlaylistName = SHUFFLED_PLAYLIST_PREFIX + playlistInfo.getName();
            }

            // Search for existing shuffled playlist
            ArrayList<PlaylistSimplified> allUserPlaylists = PlaylistSupport.getAllUserPlaylists(spotifyApiService);
            boolean existingShuffledPlaylist = false;
            PlaylistSimplified existingPlaylist = null;
            for (int i=0; i < allUserPlaylists.size(); i++){
                if (allUserPlaylists.get(i).getName().equals(shuffledPlaylistName)){
                    existingPlaylist = allUserPlaylists.get(i);
                    existingShuffledPlaylist = true;
                    break;
                }
            }

            // Unfollow/delete existing playlist
            if (existingShuffledPlaylist){
                UnfollowPlaylistRequest unfollowPlaylistRequest = spotifyApiService
                        .unfollowPlaylist(user.getId(), existingPlaylist.getId())
                        .build();
                String string = unfollowPlaylistRequest.execute();
                if (!string.equals("null")){
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deleting existing playlist failed");
                }
            }
            // Create new playlist
            newPlaylist = createNewPlaylist(spotifyApiService, user.getId(), shuffledPlaylistName);
            if (newPlaylist == null){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Creating new playlist failed");
            }
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            LOG.info(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
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
        try {
            // Get all playlists
            ArrayList<PlaylistSimplified> playlists = PlaylistSupport.getAllUserPlaylists(spotifyApiService);
            ArrayList<PlaylistSimplified> filteredPlaylists = new ArrayList<>();

            // Get user name
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

            // Add liked tracks option as a playlist
            PlaylistSimplified likedTracks = new PlaylistSimplified.Builder()
                    .setName("Liked Tracks")
                    .setOwner(user)
                    .setId(LIKED_TRACKS_PLAYLIST_ID)
                    .setImages(new Image.Builder().setUrl("https://4197r62cmrjs32n9dndpi2o1-wpengine.netdna-ssl.com/wp-content/uploads/2020/07/square-placeholder.jpg").build())
                    .build();
            // Add in likedTracks first
            filteredPlaylists.add(likedTracks);

            // Remove shuffled playlists by name
            for (PlaylistSimplified playlist : playlists) {
                if (!playlist.getName().startsWith(SHUFFLED_PLAYLIST_PREFIX)) {
                    filteredPlaylists.add(playlist);
                }
            }
            return new GetPlaylistsResponse("Success", filteredPlaylists);
        } catch (UnauthorizedException e){
            LOG.info(e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            LOG.info(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping(value = "/get-playlist-info")
    public @ResponseBody GetPlaylistResponse getPlaylistInfoEndpoint(@RequestBody GetPlaylistInfoRequest getPlaylistInfoRequest) {
        SpotifyApi spotifyApiService = new SpotifyApi.Builder()
                .setAccessToken(getPlaylistInfoRequest.getSpotifyAccessToken())
                .build();

        // Processing for liked tracks
        Playlist likedTracksPlaylist;
        if (getPlaylistInfoRequest.getPlaylistId().equals(LIKED_TRACKS_PLAYLIST_ID)) {
            try {
                likedTracksPlaylist = new Playlist.Builder().setId(LIKED_TRACKS_PLAYLIST_ID)
                        .setTracks(new Paging.Builder<PlaylistTrack>().setTotal(TrackSupport.getNumOfLikedTracks(spotifyApiService)).build()).build();
                return new GetPlaylistResponse("Success", likedTracksPlaylist);
            }
            catch (UnauthorizedException e){
                LOG.info(e.getMessage());
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                LOG.info(e.getMessage());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
        }

        // Other playlist processing
        try {
            return new GetPlaylistResponse("Success", PlaylistSupport.getPlaylistInfo(spotifyApiService, getPlaylistInfoRequest.getPlaylistId()));
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
