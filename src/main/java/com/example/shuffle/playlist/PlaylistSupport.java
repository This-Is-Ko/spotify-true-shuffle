package com.example.shuffle.playlist;

import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistsItemsRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class PlaylistSupport {

    static Logger LOG = LoggerFactory.getLogger(PlaylistSupport.class);

    public static ArrayList<IPlaylistItem> getAllTracksFromPlaylist(SpotifyApi spotifyApiService, ShuffleRequest shuffleRequest) throws ParseException, SpotifyWebApiException, IOException {
        ArrayList<IPlaylistItem> allTracks = new ArrayList<>();
        // Get playlist tracks
        boolean isMoreTracks = true;
        GetPlaylistsItemsRequest getPlaylistsItemsRequest = spotifyApiService
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
                getPlaylistsItemsRequest = spotifyApiService
                        .getPlaylistsItems(shuffleRequest.getPlaylistId())
                        .offset(playlistTrackPaging.getOffset() + 50)
                        .limit(50)
                        .build();
            }
        }
        if (allTracks.size() > 0){
            LOG.debug("Retrieved tracks from playlist");
        }
        return allTracks;
    }

    public static ArrayList<PlaylistSimplified> getAllUserPlaylists(SpotifyApi spotifyApiService) throws ParseException, SpotifyWebApiException, IOException {
        ArrayList<PlaylistSimplified> allPlaylists = new ArrayList<>();
        // Get playlists
        boolean isMorePlaylists = true;
        GetListOfCurrentUsersPlaylistsRequest getListOfCurrentUsersPlaylistsRequest = spotifyApiService
                .getListOfCurrentUsersPlaylists()
                .limit(50)
                .build();
        while (isMorePlaylists){
            final Paging<PlaylistSimplified> playlistSimplifiedPaging = getListOfCurrentUsersPlaylistsRequest.execute();
            ArrayList<PlaylistSimplified> currentPlaylists = new ArrayList<>(Arrays.asList(playlistSimplifiedPaging.getItems()));
            allPlaylists.addAll(currentPlaylists);

            if (playlistSimplifiedPaging.getNext() == null){
                isMorePlaylists = false;
            } else {
                // Get next page of playlists
                getListOfCurrentUsersPlaylistsRequest = spotifyApiService
                        .getListOfCurrentUsersPlaylists()
                        .offset(playlistSimplifiedPaging.getOffset() + 50)
                        .limit(50)
                        .build();
            }
        }
        LOG.debug("Retrieved all playlists");
        return allPlaylists;
    }

    public static Playlist getPlaylistInfo(SpotifyApi spotifyApiService, String playlistId) throws ParseException, SpotifyWebApiException, IOException {
        GetPlaylistRequest getPlaylistRequest = spotifyApiService
                .getPlaylist(playlistId)
                .build();
        return getPlaylistRequest.execute();
    }
}
