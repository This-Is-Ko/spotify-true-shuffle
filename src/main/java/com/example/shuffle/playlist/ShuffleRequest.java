package com.example.shuffle.playlist;

public class ShuffleRequest {
    private boolean isUseLikedTracks;
    private String playlistId;
    private boolean isMakeNewPlaylist;
    private String spotifyAccessToken;

    public ShuffleRequest(boolean isUseLikedTracks, String playlistId, boolean isMakeNewPlaylist, String spotifyAT) {
        this.isUseLikedTracks = isUseLikedTracks;
        this.playlistId = playlistId;
        this.isMakeNewPlaylist = isMakeNewPlaylist;
        this.spotifyAccessToken = spotifyAT;
    }

    public String getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }

    public boolean isMakeNewPlaylist() {
        return isMakeNewPlaylist;
    }

    public void setMakeNewPlaylist(boolean makeNewPlaylist) {
        isMakeNewPlaylist = makeNewPlaylist;
    }

    public String getSpotifyAccessToken() {
        return spotifyAccessToken;
    }

    public void setSpotifyAccessToken(String spotifyAccessToken) {
        this.spotifyAccessToken = spotifyAccessToken;
    }

    public boolean isUseLikedTracks() {
        return isUseLikedTracks;
    }

    public void setUseLikedTracks(boolean useLikedTracks) {
        isUseLikedTracks = useLikedTracks;
    }
}
