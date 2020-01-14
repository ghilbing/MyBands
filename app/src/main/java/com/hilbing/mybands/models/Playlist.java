package com.hilbing.mybands.models;

public class Playlist {

    public String mId;
    public String mPlaylistName;

    public Playlist() {
    }

    public Playlist(String mId, String mPlaylistName) {
        this.mId = mId;
        this.mPlaylistName = mPlaylistName;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getmPlaylistName() {
        return mPlaylistName;
    }

    public void setmPlaylistName(String mPlaylistName) {
        this.mPlaylistName = mPlaylistName;
    }
}
