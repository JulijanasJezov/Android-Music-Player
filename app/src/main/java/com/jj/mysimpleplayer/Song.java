package com.jj.mysimpleplayer;

import android.graphics.Bitmap;

public class Song {
    private int id;
    private int albumId;
    private String title;
    private String artist;
    private Bitmap coverArt;

    public Song(int songId, int albId, String songTitle, String songArtist, Bitmap songCoverArt) {
        id = songId;
        albumId = albId;
        title = songTitle;
        artist = songArtist;
        coverArt = songCoverArt;
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

}
