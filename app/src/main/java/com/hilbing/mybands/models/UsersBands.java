package com.hilbing.mybands.models;

public class UsersBands {
    String mUserId;
    String mUserName;
    String mBandId;
    String mBandName;

    public UsersBands() {
    }

    public UsersBands(String mUserId, String mUserName, String mBandId, String mBandName) {
        this.mUserId = mUserId;
        this.mUserName = mUserName;
        this.mBandId = mBandId;
        this.mBandName = mBandName;
    }

    public String getmUserId() {
        return mUserId;
    }

    public void setmUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    public String getmUserName() {
        return mUserName;
    }

    public void setmUserName(String mUserName) {
        this.mUserName = mUserName;
    }

    public String getmBandId() {
        return mBandId;
    }

    public void setmBandId(String mBandId) {
        this.mBandId = mBandId;
    }

    public String getmBandName() {
        return mBandName;
    }

    public void setmBandName(String mBandName) {
        this.mBandName = mBandName;
    }

}
