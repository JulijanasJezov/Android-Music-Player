package com.jj.mysimpleplayer;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;

public class NewPlaylistActivity extends AppCompatActivity {

    private ArrayList<Integer> playlist;
    SongAdapter songAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_playlist);

        ListView playlistView = (ListView)findViewById(R.id.add_to_playlist_list);
        playlist = new ArrayList<>();

        // Populate library list with songs
        songAdapter = new SongAdapter(this, MainActivity.songLibrary);
        playlistView.setAdapter(songAdapter);


    }

    public void onSongClick(View view) {
        int songPos = Integer.parseInt(view.findViewById(R.id.song_title).getTag().toString());
        Song song = MainActivity.songLibrary.get(songPos);
        int songId = song.getId();

        if (playlist.contains(songId)) {
            int index = playlist.indexOf(songId);
            playlist.remove(index);
            song.setSelected(false);
        } else {
            playlist.add(songId);
            song.setSelected(true);
        }

        songAdapter.notifyDataSetChanged();

    }

}
