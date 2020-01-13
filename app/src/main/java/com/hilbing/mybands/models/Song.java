package com.hilbing.mybands.models;

public class Song {

    String mId;
    String mName;
    String mArtist;
    String mDuration;
    String mUrlYoutube;

    public Song () {}

    public Song(String mId, String mName, String mArtist, String mDuration, String mUrlYoutube) {
        this.mId = mId;
        this.mName = mName;
        this.mArtist = mArtist;
        this.mDuration = mDuration;
        this.mUrlYoutube = mUrlYoutube;
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

    public String getmDuration() {
        return mDuration;
    }

    public void setmDuration(String mDuration) {
        this.mDuration = mDuration;
    }

    public String getmUrlYoutube() {
        return mUrlYoutube;
    }

    public void setmUrlYoutube(String mUrlYoutube) {
        this.mUrlYoutube = mUrlYoutube;
    }
}
