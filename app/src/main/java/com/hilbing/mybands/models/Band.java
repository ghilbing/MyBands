package com.hilbing.mybands.models;

public class Band
{

    public String mBandId;
    public String mCreatorId;
    public String mBandName;
    public String mBandImage;
    public String mBandStory;
    public String mCountry;
    public boolean mAvailable;

    public Band()
    {
    }

    public Band(String mBandId, String mCreatorId, String mBandName, String mBandImage, String mBandStory, String mCountry, boolean mAvailable)
    {
        this.mBandId = mBandId;
        this.mCreatorId = mCreatorId;
        this.mBandName = mBandName;
        this.mBandImage = mBandImage;
        this.mBandStory = mBandStory;
        this.mCountry = mCountry;
        this.mAvailable = mAvailable;
    }

    public Band(String mBandId, String mBandName, String mBandImage) {
        this.mBandId = mBandId;
        this.mBandName = mBandName;
        this.mBandImage = mBandImage;
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

    public String getmBandImage() {
        return mBandImage;
    }

    public void setmBandImage(String mBandImage) {
        this.mBandImage = mBandImage;
    }

    public String getmBandStory() {
        return mBandStory;
    }

    public void setmBandStory(String mBandStory) {
        this.mBandStory = mBandStory;
    }

    public String getmCountry() {
        return mCountry;
    }

    public void setmCountry(String mCountry) {
        this.mCountry = mCountry;
    }

    public boolean ismAvailable() {
        return mAvailable;
    }

    public void setmAvailable(boolean mAvailable) {
        this.mAvailable = mAvailable;
    }

    public String getmCreatorId() {
        return mCreatorId;
    }

    public void setmCreatorId(String mCreatorId) {
        this.mCreatorId = mCreatorId;
    }
}
