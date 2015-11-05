package com.jj.mysimpleplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class SongAdapter extends BaseAdapter {
    private ArrayList<Song> songLibrary;
    private LayoutInflater songInflater;

    public SongAdapter(Context c, ArrayList<Song> songs){
        songLibrary = songs;
        songInflater = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return songLibrary.size();
    }

    @Override
    public Object getItem(int id) {
        return null;
    }

    @Override
    public long getItemId(int id) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LinearLayout songLay = (LinearLayout)songInflater.inflate
                (R.layout.song, parent, false);

        TextView songView = (TextView)songLay.findViewById(R.id.song_title);
        TextView artistView = (TextView)songLay.findViewById(R.id.song_artist);

        Song currSong = songLibrary.get(position);

        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());

        songLay.setTag(position);

        return songLay;
    }
}
