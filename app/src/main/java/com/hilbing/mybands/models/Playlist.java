package com.hilbing.mybands.models;

public class Playlist {

    public String mId;
    public String mPlaylistName;
    public String mCreator;

    public Playlist() {
    }

    public Playlist(String mId, String mPlaylistName, String mCreator) {
        this.mId = mId;
        this.mPlaylistName = mPlaylistName;
        this.mCreator = mCreator;
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

    public String getmCreator() {
        return mCreator;
    }

    public void setmCreator(String mCreator) {
        this.mCreator = mCreator;
    }
}
