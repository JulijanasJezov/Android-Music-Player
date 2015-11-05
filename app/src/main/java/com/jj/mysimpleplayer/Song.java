package com.jj.mysimpleplayer;

public class Song {
    private int id;
    private String title;
    private String artist;

    public Song(int songId, String songTitle, String songArtist) {
        id = songId;
        title = songTitle;
        artist = songArtist;
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
}
