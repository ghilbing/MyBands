package com.hilbing.mybands.models;

public class UsersInstruments
{

    String mUserId;
    String mUserName;
    String mUserInstrument;

    public UsersInstruments() {}

    public UsersInstruments(String mUserId, String mUserName, String mUserInstrument)
    {
        this.mUserId = mUserId;
        this.mUserName = mUserName;
        this.mUserInstrument = mUserInstrument;
    }


    public String getmUserId()
    {
        return mUserId;
    }

    public void setmUserId(String mUserId)
    {
        this.mUserId = mUserId;
    }

    public String getmUserName()
    {
        return mUserName;
    }

    public void setmUserName(String mUserName)
    {
        this.mUserName = mUserName;
    }

    public String getmUserInstrument()
    {
        return mUserInstrument;
    }

    public void setmUserInstrument(String mUserInstrument)
    {
        this.mUserInstrument = mUserInstrument;
    }
}
