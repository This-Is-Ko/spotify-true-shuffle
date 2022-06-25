package com.example.shuffle.playlist;

import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistsItemsRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class PlaylistSupport {

    static Logger LOG = LoggerFactory.getLogger(PlaylistSupport.class);

    public static ArrayList<IPlaylistItem> getAllTracksFromPlaylist(SpotifyApi spotifyApiService, ShuffleRequest shuffleRequest){
        ArrayList<IPlaylistItem> allTracks = new ArrayList<>();
        try{
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
        } catch (ParseException | SpotifyWebApiException | IOException e) {
            LOG.info(e.getMessage());
            return null;
        }
        LOG.debug("Retrieved tracks from playlist");
        return allTracks;
    }
}
