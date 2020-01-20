package com.hilbing.mybands.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.cardview.widget.CardView;

public class Song implements Parcelable {

    String mId;
    String mName;
    String mArtist;
    String mYoutubeTitle;
    String mUrlYoutube;
    String mCurrentUser;

    public Song () {}

    public Song(String mId, String mName, String mArtist, String mUrlYoutube) {
        this.mId = mId;
        this.mName = mName;
        this.mArtist = mArtist;
        this.mUrlYoutube = mUrlYoutube;
    }

    public Song(String mId, String mName, String mArtist, String mYoutubeTitle, String mUrlYoutube, String mCurrentUser) {
        this.mId = mId;
        this.mName = mName;
        this.mArtist = mArtist;
        this.mYoutubeTitle = mYoutubeTitle;
        this.mUrlYoutube = mUrlYoutube;
        this.mCurrentUser = mCurrentUser;
    }

    protected Song(Parcel in) {
        mId = in.readString();
        mName = in.readString();
        mArtist = in.readString();
        mYoutubeTitle = in.readString();
        mUrlYoutube = in.readString();
        mCurrentUser = in.readString();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mId);
        parcel.writeString(mName);
        parcel.writeString(mArtist);
        parcel.writeString(mYoutubeTitle);
        parcel.writeString(mUrlYoutube);
        parcel.writeString(mCurrentUser);
    }
}
