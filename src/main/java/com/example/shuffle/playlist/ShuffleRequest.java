package com.example.shuffle.playlist;

public class ShuffleRequest {
    private String playlistId;
    private String userId;
    private boolean isMakeNewPlaylist;
    private String spotifyAccessToken;

    public ShuffleRequest(String playlistId, String userId, boolean isMakeNewPlaylist, String spotifyAT) {
        this.playlistId = playlistId;
        this.userId = userId;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSpotifyAccessToken() {
        return spotifyAccessToken;
    }

    public void setSpotifyAccessToken(String spotifyAccessToken) {
        this.spotifyAccessToken = spotifyAccessToken;
    }
}
