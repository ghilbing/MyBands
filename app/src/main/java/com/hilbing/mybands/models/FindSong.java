package com.hilbing.mybands.models;

public class FindSong {

    public String mArtist;
    public String mCurrentUser;
    public String mName;
    public String mUrlYoutube;

    public FindSong() {
    }

    public FindSong(String mArtist, String mCurrentUser, String mName, String mUrlYoutube) {
        this.mArtist = mArtist;
        this.mCurrentUser = mCurrentUser;
        this.mName = mName;
        this.mUrlYoutube = mUrlYoutube;
    }

    public String getmArtist() {
        return mArtist;
    }

    public void setmArtist(String mArtist) {
        this.mArtist = mArtist;
    }

    public String getmCurrentUser() {
        return mCurrentUser;
    }

    public void setmCurrentUser(String mCurrentUser) {
        this.mCurrentUser = mCurrentUser;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmUrlYoutube() {
        return mUrlYoutube;
    }

    public void setmUrlYoutube(String mUrlYoutube) {
        this.mUrlYoutube = mUrlYoutube;
    }
}
