package com.jj.mysimpleplayer.utility;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.jj.mysimpleplayer.models.Playlist;
import com.jj.mysimpleplayer.models.Song;
import com.jj.mysimpleplayer.database.DatabaseHelper;
import com.jj.mysimpleplayer.database.PlaylistTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class Helpers {

    public static String getFormattedTime(int progress) {
        int seconds = 1000;
        int minutes = seconds * 60;
        int hours = minutes * 60;

        int elapsedHours = progress / hours;
        progress = progress % hours;

        int elapsedMinutes = progress / minutes;
        progress = progress % minutes;

        int elapsedSeconds = progress / seconds;

        return elapsedHours != 0 ? String.format("%d:%d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds)
                : String.format("%d:%02d", elapsedMinutes, elapsedSeconds);
    }

    public static ArrayList<Song> getSongLibrary(Context context) {
        ContentResolver musicResolver = context.getContentResolver();
        Uri imageUri = Uri.parse("content://media/external/audio/albumart");
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        Cursor musicCursor = musicResolver.query(musicUri, null, selection, null, null);

        ArrayList<Song> songLibrary = new ArrayList<>();
        if(musicCursor != null && musicCursor.moveToFirst()){
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int albumIdColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Albums.ALBUM_ID);

            // Store loaded images
            HashMap<Integer, Bitmap> fetchedImages = new HashMap<>();

            do {
                int thisId = musicCursor.getInt(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                int albumId = musicCursor.getInt(albumIdColumn);

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

                songLibrary.add(new Song(thisId, albumId, thisTitle, thisArtist, coverArt));
            }
            while (musicCursor.moveToNext());

            Collections.sort(songLibrary, new Comparator<Song>() {
                public int compare(Song a, Song b) {
                    return a.getTitle().compareTo(b.getTitle());
                }
            });

            musicCursor.close();
        }

        return songLibrary;
    }

    public static ArrayList<Playlist> getPlaylists(DatabaseHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        ArrayList<Playlist> playlists = new ArrayList<>();

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

        return playlists;
    }
}
