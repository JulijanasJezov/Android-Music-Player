package com.jj.mysimpleplayer.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jj.mysimpleplayer.R;
import com.jj.mysimpleplayer.models.Song;

import java.util.ArrayList;

public class SongAdapter extends BaseAdapter {
    private ArrayList<Song> songLibrary;
    private LayoutInflater songInflater;
    private boolean isPlaylist;

    static class SongItem {
        TextView titleView;
        TextView artistView;
        ImageView coverArtView;
        LinearLayout songItem;
    }

    public SongAdapter(Context c, ArrayList<Song> songs, boolean playlist){
        songLibrary = songs;
        songInflater = LayoutInflater.from(c);
        isPlaylist = playlist;
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
        SongItem songItem;

        if (convertView == null) {
            convertView = songInflater.inflate(R.layout.song, parent, false);

            songItem = new SongItem();
            songItem.titleView = (TextView)convertView.findViewById(R.id.song_title);
            songItem.artistView = (TextView)convertView.findViewById(R.id.song_artist);
            songItem.coverArtView = (ImageView)convertView.findViewById(R.id.cover_art);
            songItem.songItem = (LinearLayout)convertView.findViewById(R.id.song_item);

            convertView.setTag(songItem);
        } else {
            songItem = (SongItem) convertView.getTag();
        }

        Song currentSong = songLibrary.get(position);

        if (currentSong != null) {
            songItem.titleView.setTag(position);
            songItem.titleView.setText(currentSong.getTitle());
            songItem.artistView.setText(currentSong.getArtist());

            Bitmap coverArt = currentSong.getCoverArt();
            if (coverArt == null) {
                songItem.coverArtView.setImageResource(R.drawable.default_art);
            } else {
                songItem.coverArtView.setImageBitmap(coverArt);
            }

            if (currentSong.isSelected() && isPlaylist) {
                songItem.songItem.setBackgroundColor(Color.GRAY);
            } else {
                songItem.songItem.setBackgroundColor(Color.WHITE);
            }

        }

        return convertView;
    }
}
