package com.hilbing.mybands.models;

public class Instrument
{

    public String mId;
    public String mName;

    public Instrument(){}

    public Instrument(String mId, String mName)
    {
        this.mId = mId;
        this.mName = mName;
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
}
