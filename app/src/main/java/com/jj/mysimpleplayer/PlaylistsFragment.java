package com.jj.mysimpleplayer;

import android.app.Fragment;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;

import com.jj.mysimpleplayer.adapters.PlaylistAdapter;
import com.jj.mysimpleplayer.adapters.SongAdapter;
import com.jj.mysimpleplayer.constants.Constants;
import com.jj.mysimpleplayer.models.Playlist;
import com.jj.mysimpleplayer.models.Song;
import com.jj.mysimpleplayer.utility.Helpers;
import com.jj.mysimpleplayer.database.DatabaseHelper;
import com.jj.mysimpleplayer.database.PlaylistTable;
import com.jj.mysimpleplayer.database.SongsTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlaylistsFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private ArrayList<Playlist> playlists;
    private int currentPlaylistId;
    private View rootView;
    private SongAdapter playlistSongsAdapter;
    private PlaylistAdapter playlistAdapter;
    private ListView playlistSongsView;
    private int spinnerPos;
    private Spinner playlistsView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_playlists, container, false);

        dbHelper = new DatabaseHelper(rootView.getContext());

        MainActivity.playlistSongs = new ArrayList<>();

        currentPlaylistId = 0;

        ImageButton addNewPlaylist = (ImageButton) rootView.findViewById(R.id.add_playlist);
        addNewPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewPlaylistClicked(view);
            }
        });

        ImageButton removePlaylist = (ImageButton) rootView.findViewById(R.id.remove_playlist);
        removePlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removePlaylistClicked(view);
            }
        });

        if (savedInstanceState != null) {
            currentPlaylistId = savedInstanceState.getInt(Constants.PLAYLIST_ID);
            spinnerPos = savedInstanceState.getInt(Constants.SPINNER_POS);
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        playlists = Helpers.getPlaylists(dbHelper);
        playlistsView = (Spinner)rootView.findViewById(R.id.playlists_spinner);

        // Populate library list with songs
        playlistAdapter = new PlaylistAdapter(getActivity(), playlists);
        playlistsView.setAdapter(playlistAdapter);

        playlistSongsView = (ListView)rootView.findViewById(R.id.playlist_songs);

        playlistsView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> av, View view, int position, long id) {
                if (view != null) {
                    currentPlaylistId = Integer.parseInt(view.findViewById(R.id.playlist_name).getTag().toString());
                    spinnerPos = position;
                } else {
                    playlistsView.setSelection(spinnerPos);
                }

                loadPlaylist(rootView.getContext(), currentPlaylistId);
                playlistSongsAdapter = new SongAdapter(getActivity(), MainActivity.playlistSongs, false);
                playlistSongsView.setAdapter(playlistSongsAdapter);
            }

            public void onNothingSelected(AdapterView<?> av) {
                List<String> list = new ArrayList<String>();
                list.add("No Playlist found");
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_spinner_item, list);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                playlistsView.setAdapter(adapter);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt(Constants.PLAYLIST_ID, currentPlaylistId);
        savedInstanceState.putInt(Constants.SPINNER_POS, spinnerPos);
    }

    public void removePlaylistClicked(View view) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        db.delete(SongsTable.SongsTableEntry.TABLE_NAME, SongsTable.SongsTableEntry.PLAYLIST_ID + "=" + currentPlaylistId, null);
        db.delete(PlaylistTable.PlaylistTableEntry.TABLE_NAME, PlaylistTable.PlaylistTableEntry._ID + "=" + currentPlaylistId, null);

        int indexOfPlaylist = 0;

        for (Playlist pl : playlists) {
            if (pl.getId() == currentPlaylistId) {
                break;
            }

            indexOfPlaylist++;
        }

        playlists.remove(indexOfPlaylist);

        if(playlists.isEmpty()) {
            playlistSongsView.setAdapter(null);
        } else {
            playlistsView.setAdapter(null); // workaround for onItemSelected not triggered if position remains the same
        }

        playlistsView.setAdapter(playlistAdapter);
    }

    private void addNewPlaylistClicked(View view) {
        Intent addNewPlaylistIntent = new Intent(view.getContext(), NewPlaylistActivity.class);
        startActivity(addNewPlaylistIntent);
    }

    private void loadPlaylist(Context context, int playlistId) {
        ArrayList<Song> songsPlaylist = new ArrayList<>();
        Uri imageUri = Uri.parse("content://media/external/audio/albumart");
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                SongsTable.SongsTableEntry.SONG_ID,
                SongsTable.SongsTableEntry.SONG_ALBUM_ID,
                SongsTable.SongsTableEntry.SONG_TITLE,
                SongsTable.SongsTableEntry.SONG_ARTIST
        };

        String selection = SongsTable.SongsTableEntry.PLAYLIST_ID + " = " + playlistId;

        String sortOrder = SongsTable.SongsTableEntry.SONG_TITLE + " ASC";

        Cursor songsCursor = db.query(
                SongsTable.SongsTableEntry.TABLE_NAME,
                projection,
                selection,
                null,
                null,
                null,
                sortOrder
        );

        if(songsCursor != null && songsCursor.moveToFirst()){
            int songIdColumn = songsCursor.getColumnIndex
                    (SongsTable.SongsTableEntry.SONG_ID);
            int albumIdColumn = songsCursor.getColumnIndex
                    (SongsTable.SongsTableEntry.SONG_ALBUM_ID);
            int titleColumn = songsCursor.getColumnIndex
                    (SongsTable.SongsTableEntry.SONG_TITLE);
            int artistColumn = songsCursor.getColumnIndex
                    (SongsTable.SongsTableEntry.SONG_ARTIST);

            // Store loaded images
            HashMap<Integer, Bitmap> fetchedImages = new HashMap<>();

            do {
                int songId = songsCursor.getInt(songIdColumn);
                int albumId = songsCursor.getInt(albumIdColumn);
                String title = songsCursor.getString(titleColumn);
                String artist = songsCursor.getString(artistColumn);

                Uri albumArtUri = ContentUris.withAppendedId(imageUri, albumId);

                Bitmap coverArt = null;

                // Load image if it's already been loaded once
                coverArt = fetchedImages.get(albumId);

                if (coverArt == null) {
                    try {
                        coverArt = MediaStore.Images.Media.getBitmap(context.getContentResolver(), albumArtUri);

                        if (coverArt != null) {
                            fetchedImages.put(albumId, coverArt);
                        }
                    } catch (Exception ex) {
                    }
                }

                songsPlaylist.add(new Song(songId, albumId, title, artist, coverArt));
            }
            while (songsCursor.moveToNext());

            songsCursor.close();
        }

        MainActivity.playlistSongs = songsPlaylist;
        db.close();
    }
}
