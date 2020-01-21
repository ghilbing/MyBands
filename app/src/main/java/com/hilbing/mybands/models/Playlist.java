package com.hilbing.mybands.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Playlist implements Parcelable {

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

    protected Playlist(Parcel in) {
        mId = in.readString();
        mPlaylistName = in.readString();
        mCreator = in.readString();
    }

    public static final Creator<Playlist> CREATOR = new Creator<Playlist>() {
        @Override
        public Playlist createFromParcel(Parcel in) {
            return new Playlist(in);
        }

        @Override
        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mId);
        parcel.writeString(mPlaylistName);
        parcel.writeString(mCreator);
    }
}
