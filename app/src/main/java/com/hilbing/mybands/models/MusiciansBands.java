package com.hilbing.mybands.models;

public class MusiciansBands {

    public String bandName;
    public String bandProfileImage;

    public MusiciansBands() {
    }

    public MusiciansBands(String bandName, String bandProfileImage) {
        this.bandName = bandName;
        this.bandProfileImage = bandProfileImage;
    }

    public String getBandName() {
        return bandName;
    }

    public void setBandName(String bandName) {
        this.bandName = bandName;
    }

    public String getBandProfileImage() {
        return bandProfileImage;
    }

    public void setBandProfileImage(String bandProfileImage) {
        this.bandProfileImage = bandProfileImage;
    }
}
