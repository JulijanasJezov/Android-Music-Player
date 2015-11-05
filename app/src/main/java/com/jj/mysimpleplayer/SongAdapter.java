package com.jj.mysimpleplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class SongAdapter extends BaseAdapter {
    private ArrayList<Song> songLibrary;
    private LayoutInflater songInflater;

    static class ViewHolderItem {
        TextView songView;
        TextView artistView;
    }

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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;

        if (convertView == null) {
            convertView = songInflater.inflate(R.layout.song, parent, false);

            viewHolder = new ViewHolderItem();
            viewHolder.songView = (TextView)convertView.findViewById(R.id.song_title);
            viewHolder.artistView = (TextView)convertView.findViewById(R.id.song_artist);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        Song currentSong = songLibrary.get(position);

        if (currentSong != null) {
            viewHolder.songView.setText(currentSong.getTitle());
            viewHolder.artistView.setText(currentSong.getArtist());
        }

        return convertView;
    }
}
