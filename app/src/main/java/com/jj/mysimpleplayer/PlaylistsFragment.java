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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.jj.mysimpleplayer.constants.Constants;
import com.jj.mysimpleplayer.database.DatabaseHelper;
import com.jj.mysimpleplayer.database.PlaylistTable;
import com.jj.mysimpleplayer.database.SongsTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class PlaylistsFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private ArrayList<Playlist> playlists;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_playlists, container, false);

        dbHelper = new DatabaseHelper(rootView.getContext());

        playlists = new ArrayList<>();
        getPlaylists();

        ListView playlistsView = (ListView)rootView.findViewById(R.id.playlists_list);

        // Populate library list with songs
        PlaylistAdapter playlistAdapter = new PlaylistAdapter(getActivity(), playlists);
        playlistsView.setAdapter(playlistAdapter);

        LinearLayout addNewPlaylist = (LinearLayout) rootView.findViewById(R.id.add_new_playlist_layout);
        addNewPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewPlaylistClicked(view);
            }
        });

        playlistsView.setClickable(true);
        playlistsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View view, int position, long id) {
                int playlistId = Integer.parseInt(view.findViewById(R.id.playlist_name).getTag().toString());
                loadPlaylist(rootView.getContext(), playlistId);
            }
        });

        return rootView;
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
                    } catch (Exception ex) { }
                }

                songsPlaylist.add(new Song(songId, albumId, title, artist, null));
            }
            while (songsCursor.moveToNext());

            songsCursor.close();
        }
    }

    private void getPlaylists() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                PlaylistTable.PlaylistTableEntry._ID,
                PlaylistTable.PlaylistTableEntry.PLAYLIST_NAME,
        };

        String sortOrder = PlaylistTable.PlaylistTableEntry.PLAYLIST_NAME + " ASC";

        Cursor playlistsCursor = db.query(
                PlaylistTable.PlaylistTableEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        if(playlistsCursor != null && playlistsCursor.moveToFirst()){
            int nameColumn = playlistsCursor.getColumnIndex
                    (PlaylistTable.PlaylistTableEntry.PLAYLIST_NAME);
            int idColumn = playlistsCursor.getColumnIndex
                    (PlaylistTable.PlaylistTableEntry._ID);

            do {
                int thisId = playlistsCursor.getInt(idColumn);
                String name = playlistsCursor.getString(nameColumn);

                playlists.add(new Playlist(thisId, name));
            }
            while (playlistsCursor.moveToNext());

            playlistsCursor.close();
        }
    }
}
