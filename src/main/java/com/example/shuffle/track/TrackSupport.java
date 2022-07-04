package com.example.shuffle.track;

import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.SavedTrack;
import se.michaelthelin.spotify.requests.data.library.GetUsersSavedTracksRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class TrackSupport {

    static Logger LOG = LoggerFactory.getLogger(TrackSupport.class);

    public static ArrayList<IPlaylistItem> getAllLikedTracks(SpotifyApi spotifyApiService) throws ParseException, SpotifyWebApiException, IOException {
        ArrayList<IPlaylistItem> allTracks = new ArrayList<>();
        GetUsersSavedTracksRequest getUsersSavedTracksRequest = spotifyApiService.getUsersSavedTracks()
                .limit(50)
                .offset(0)
                .build();
        boolean isMoreTracks = true;
        while (isMoreTracks) {
            final Paging<SavedTrack> savedTrackPaging = getUsersSavedTracksRequest.execute();
            ArrayList<SavedTrack> currentTracks = new ArrayList<>(Arrays.asList(savedTrackPaging.getItems()));
            // Store track information
            currentTracks.forEach(trackEntry -> {
                allTracks.add(trackEntry.getTrack());
            });

            if (savedTrackPaging.getNext() == null){
                isMoreTracks = false;
            } else {
                // Get next page of tracks
                getUsersSavedTracksRequest = spotifyApiService.getUsersSavedTracks()
                        .limit(50)
                        .offset(savedTrackPaging.getOffset() + 50)
                        .build();
            }
        }
        if (allTracks.size() > 0){
            LOG.debug("Retrieved tracks from liked tracks");
        }
        return allTracks;
    }

    public static int getNumOfLikedTracks(SpotifyApi spotifyApiService) throws ParseException, SpotifyWebApiException, IOException {
        int total = 0;
        GetUsersSavedTracksRequest getUsersSavedTracksRequest = spotifyApiService.getUsersSavedTracks()
                .limit(50)
                .offset(0)
                .build();
        boolean isMoreTracks = true;
        while (isMoreTracks) {
            final Paging<SavedTrack> savedTrackPaging = getUsersSavedTracksRequest.execute();
            total += savedTrackPaging.getItems().length;

            if (savedTrackPaging.getNext() == null){
                isMoreTracks = false;
            } else {
                // Get next page of tracks
                getUsersSavedTracksRequest = spotifyApiService.getUsersSavedTracks()
                        .limit(50)
                        .offset(savedTrackPaging.getOffset() + 50)
                        .build();
            }
        }
        return total;
    }
}
