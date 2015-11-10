package com.jj.mysimpleplayer;

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

import java.util.ArrayList;

public class PlaylistAdapter extends BaseAdapter {
    private ArrayList<Playlist> playlists;
    private LayoutInflater playlistInflater;

    static class PlaylistItem {
        TextView playlistNameView;
    }

    public PlaylistAdapter(Context c, ArrayList<Playlist> playlistsList) {
        playlists = playlistsList;
        playlistInflater = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return playlists.size();
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
        PlaylistItem playlistItem;

        if (convertView == null) {
            convertView = playlistInflater.inflate(R.layout.playlist, parent, false);

            playlistItem = new PlaylistItem();
            playlistItem.playlistNameView = (TextView) convertView.findViewById(R.id.playlist_name);

            convertView.setTag(playlistItem);
        } else {
            playlistItem = (PlaylistItem) convertView.getTag();
        }

        Playlist currentPlaylist = playlists.get(position);

        if (currentPlaylist != null) {
            playlistItem.playlistNameView.setTag(currentPlaylist.getId());
            playlistItem.playlistNameView.setText(currentPlaylist.getName());
        }

        return convertView;
    }
}