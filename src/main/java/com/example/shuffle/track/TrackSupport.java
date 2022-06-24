package com.example.shuffle.track;

import com.example.shuffle.controller.TracksController;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.SavedTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.library.GetUsersSavedTracksRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class TrackSupport {

    static Logger LOG = LoggerFactory.getLogger(TrackSupport.class);

    public static ArrayList<IPlaylistItem> getAllLikedTracks(SpotifyApi spotifyApiService){
        ArrayList<IPlaylistItem> allTracks = new ArrayList<>();
        GetUsersSavedTracksRequest getUsersSavedTracksRequest = spotifyApiService.getUsersSavedTracks()
                .limit(50)
                .offset(0)
                .build();
        boolean isMoreTracks = true;
        try {
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
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            LOG.info(e.getMessage());
            return null;
        }
        return allTracks;
    }
}
