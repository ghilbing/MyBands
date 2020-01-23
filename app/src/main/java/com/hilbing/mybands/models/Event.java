package com.hilbing.mybands.models;

public class Event {
    public String idEvent;
    public String mEventType;
    public String mName;
    public String mDate;
    public String mTime;
    public String mPlace;
    public String idPlaylist;
    public String mPlaylistName;
    public String mCurrentUser;
    public long mTimestamp;

    public Event() {
    }

    public Event(String idEvent, String mEventType, String mName, String mDate, String mTime, String mPlace, String mPlaylistName, String idPlaylist, String mCurrentUser, long mTimestamp) {
        this.idEvent = idEvent;
        this.mEventType = mEventType;
        this.mName = mName;
        this.mDate = mDate;
        this.mTime = mTime;
        this.mPlace = mPlace;
        this.mPlaylistName = mPlaylistName;
        this.idPlaylist = idPlaylist;
        this.mCurrentUser = mCurrentUser;
        this.mTimestamp = mTimestamp;
    }

    public String getIdEvent() {
        return idEvent;
    }

    public void setIdEvent(String idEvent) {
        this.idEvent = idEvent;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmDate() {
        return mDate;
    }

    public void setmDate(String mDate) {
        this.mDate = mDate;
    }

    public String getmTime() {
        return mTime;
    }

    public void setmTime(String mTime) {
        this.mTime = mTime;
    }

    public String getmPlace() {
        return mPlace;
    }

    public void setmPlace(String mPlace) {
        this.mPlace = mPlace;
    }

    public String getIdPlaylist() {
        return idPlaylist;
    }

    public void setIdPlaylist(String idPlaylist) {
        this.idPlaylist = idPlaylist;
    }

    public String getmCurrentUser() {
        return mCurrentUser;
    }

    public void setmCurrentUser(String mCurrentUser) {
        this.mCurrentUser = mCurrentUser;
    }

    public String getmEventType() {
        return mEventType;
    }

    public void setmEventType(String mEventType) {
        this.mEventType = mEventType;
    }

    public String getmPlaylistName() {
        return mPlaylistName;
    }

    public void setmPlaylistName(String mPlaylistName) {
        this.mPlaylistName = mPlaylistName;
    }

    public long getmTimestamp() {
        return mTimestamp;
    }

    public void setmTimestamp(long mTimestamp) {
        this.mTimestamp = mTimestamp;
    }
}
