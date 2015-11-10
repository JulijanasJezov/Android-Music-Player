package com.jj.mysimpleplayer;

public class Playlist {
    private int id;
    private String name;

    public Playlist(int playlistId, String playlistName) {
        id = playlistId;
        name = playlistName;
    }

    public int getId(){
        return id;
    }
    public String getName() {
        return name;
    }
}
