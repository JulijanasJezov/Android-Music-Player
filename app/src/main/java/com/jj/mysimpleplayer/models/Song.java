package com.jj.mysimpleplayer.models;

import android.graphics.Bitmap;

public class Song {
    private int id;
    private int albumId;
    private String title;
    private String artist;
    private Bitmap coverArt;
    private boolean isSelected; // used for highlighting the list items in new playlist

    public Song(int songId, int albId, String songTitle, String songArtist, Bitmap songCoverArt) {
        id = songId;
        albumId = albId;
        title = songTitle;
        artist = songArtist;
        coverArt = songCoverArt;
        isSelected = false;
    }

    public int getId(){
        return id;
    }

    public int getAlbumId() {
        return albumId;
    }

    public String getTitle(){
        return title;
    }

    public String getArtist(){
        return artist;
    }

    public Bitmap getCoverArt() {
        return coverArt;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

}
