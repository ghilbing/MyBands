package com.hilbing.mybands.models;

public class FindMusician
{
    public String mUserProfileImage;
    public String mUserName;
    public String mUserStatus;

    public FindMusician()
    {
    }

    public FindMusician(String mUserProfileImage, String mUserName, String mUserStatus) {
        this.mUserProfileImage = mUserProfileImage;
        this.mUserName = mUserName;
        this.mUserStatus = mUserStatus;
    }

    public String getmUserProfileImage() {
        return mUserProfileImage;
    }

    public void setmUserProfileImage(String mUserProfileImage) {
        this.mUserProfileImage = mUserProfileImage;
    }

    public String getmUserName() {
        return mUserName;
    }

    public void setmUserName(String mUserName) {
        this.mUserName = mUserName;
    }

    public String getmUserStatus() {
        return mUserStatus;
    }

    public void setmUserStatus(String mUserStatus) {
        this.mUserStatus = mUserStatus;
    }
}
