package com.hilbing.mybands.models;

import androidx.cardview.widget.CardView;

public class Song {

    String mId;
    String mName;
    String mArtist;
    String mYoutubeTitle;
    String mUrlYoutube;
    String mCurrentUser;

    public Song () {}

    public Song(String mId, String mName, String mArtist, String mYoutubeTitle, String mUrlYoutube, String mCurrentUser) {
        this.mId = mId;
        this.mName = mName;
        this.mArtist = mArtist;
        this.mYoutubeTitle = mYoutubeTitle;
        this.mUrlYoutube = mUrlYoutube;
        this.mCurrentUser = mCurrentUser;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmArtist() {
        return mArtist;
    }

    public void setmArtist(String mArtist) {
        this.mArtist = mArtist;
    }

    public String getmUrlYoutube() {
        return mUrlYoutube;
    }

    public void setmUrlYoutube(String mUrlYoutube) {
        this.mUrlYoutube = mUrlYoutube;
    }

    public String getmCurrentUser() {
        return mCurrentUser;
    }

    public void setmCurrentUser(String mCurrentUser) {
        this.mCurrentUser = mCurrentUser;
    }

    public String getmYoutubeTitle() {
        return mYoutubeTitle;
    }

    public void setmYoutubeTitle(String mYoutubeTitle) {
        this.mYoutubeTitle = mYoutubeTitle;
    }
}
