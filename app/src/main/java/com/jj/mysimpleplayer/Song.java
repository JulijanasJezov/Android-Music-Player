package com.jj.mysimpleplayer;

import android.graphics.Bitmap;

public class Song {
    private int id;
    private String title;
    private String artist;
    private Bitmap coverArt;

    public Song(int songId, String songTitle, String songArtist, Bitmap songCoverArt) {
        id = songId;
        title = songTitle;
        artist = songArtist;
        coverArt = songCoverArt;
    }

    public int getId(){
        return id;
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

}
