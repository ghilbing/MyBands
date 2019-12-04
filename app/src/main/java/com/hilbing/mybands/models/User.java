package com.hilbing.mybands.models;

public class User {

    String mUserId;
    String mUserFullName;
    String mUserPhone;
    String mUserProfileImage;
    String mUserCountry;
    String mStatus;
    boolean mAvailable;

    public User(){}

    public User(String mUserId, String mUserFullName, String mUserPhone, String mUserProfileImage, String mUserCountry, String mStatus, boolean mAvailable) {
        this.mUserId = mUserId;
        this.mUserFullName = mUserFullName;
        this.mUserPhone = mUserPhone;
        this.mUserProfileImage = mUserProfileImage;
        this.mUserCountry = mUserCountry;
        this.mStatus = mStatus;
        this.mAvailable = mAvailable;
    }

    public String getmUserId() {
        return mUserId;
    }

    public void setmUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    public String getmUserFullName() {
        return mUserFullName;
    }

    public void setmUserFullName(String mUserFullName) {
        this.mUserFullName = mUserFullName;
    }

    public String getmUserPhone() {
        return mUserPhone;
    }

    public void setmUserPhone(String mUserPhone) {
        this.mUserPhone = mUserPhone;
    }

    public String getmUserProfileImage() {
        return mUserProfileImage;
    }

    public void setmUserProfileImage(String mUserProfileImage) {
        this.mUserProfileImage = mUserProfileImage;
    }

    public String getmUserCountry() {
        return mUserCountry;
    }

    public void setmUserCountry(String mUserCountry) {
        this.mUserCountry = mUserCountry;
    }

    public String getmStatus() {
        return mStatus;
    }

    public void setmStatus(String mStatus) {
        this.mStatus = mStatus;
    }

    public boolean ismAvailable() {
        return mAvailable;
    }

    public void setmAvailable(boolean mAvailable) {
        this.mAvailable = mAvailable;
    }
}
