package com.hilbing.mybands.models;

public class PlaylistSongs {

    public String mIdPlaylist;
    public String mIdSong;
    public String mArtist;
    public String mYoutubeLink;

    public PlaylistSongs() {
    }

    public PlaylistSongs(String mIdPlaylist, String mIdSong, String mArtist, String mYoutubeLink) {
        this.mIdPlaylist = mIdPlaylist;
        this.mIdSong = mIdSong;
        this.mArtist = mArtist;
        this.mYoutubeLink = mYoutubeLink;
    }

    public String getmIdPlaylist() {
        return mIdPlaylist;
    }

    public void setmIdPlaylist(String mIdPlaylist) {
        this.mIdPlaylist = mIdPlaylist;
    }

    public String getmIdSong() {
        return mIdSong;
    }

    public void setmIdSong(String mIdSong) {
        this.mIdSong = mIdSong;
    }

    public String getmArtist() {
        return mArtist;
    }

    public void setmArtist(String mArtist) {
        this.mArtist = mArtist;
    }

    public String getmYoutubeLink() {
        return mYoutubeLink;
    }

    public void setmYoutubeLink(String mYoutubeLink) {
        this.mYoutubeLink = mYoutubeLink;
    }
}
