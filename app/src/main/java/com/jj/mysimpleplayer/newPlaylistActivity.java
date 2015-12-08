package com.jj.mysimpleplayer;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.jj.mysimpleplayer.adapters.SongAdapter;
import com.jj.mysimpleplayer.models.Playlist;
import com.jj.mysimpleplayer.models.Song;
import com.jj.mysimpleplayer.utility.Helpers;
import com.jj.mysimpleplayer.database.DatabaseHelper;
import com.jj.mysimpleplayer.database.PlaylistTable;
import com.jj.mysimpleplayer.database.SongsTable;

import java.util.ArrayList;

public class NewPlaylistActivity extends AppCompatActivity {

    private ArrayList<Song> playlist = new ArrayList<>();
    private ArrayList<Song> songLibrary;
    private ArrayList<Playlist> existingPlaylists;
    private SongAdapter songAdapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_playlist);

        // Load the list of available songs
        ListView playlistView = (ListView)findViewById(R.id.add_to_playlist_list);
        songLibrary = MainActivity.songLibrary;

        // Recreate the selected list of songs
        for (Song s : songLibrary) {
            if (s.isSelected()) playlist.add(s);
        }

        // Populate library list with songs
        songAdapter = new SongAdapter(this, songLibrary, true);
        playlistView.setAdapter(songAdapter);

        dbHelper = new DatabaseHelper(getApplicationContext());

        existingPlaylists = Helpers.getPlaylists(dbHelper);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_new_playlist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create_new:
                createNewPlaylist();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
        Handle on song click, highlighting the selected song
    */
    public void onSongClick(View view) {
        int songPos = Integer.parseInt(view.findViewById(R.id.song_title).getTag().toString());
        Song song = MainActivity.songLibrary.get(songPos);

        if (playlist.contains(song)) {
            int index = playlist.indexOf(song);
            playlist.remove(index);
            song.setSelected(false);
        } else {
            playlist.add(song);
            song.setSelected(true);
        }

        songAdapter.notifyDataSetChanged();

    }

    /*
        Creates a new playlist of chosen songs and saves it to the database
    */
    private void createNewPlaylist() {
        String toastText = null;
        EditText playlistNameText = (EditText)findViewById(R.id.playlist_name);
        String playlistName = playlistNameText.getText().toString();
        if (playlistName.isEmpty() || playlist.isEmpty())  {
            toastText = "Name is required!";
            Toast toast = Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        // check if playlist name is unique
        for (Playlist pl : existingPlaylists) {
            if (pl.getName().toLowerCase().equals(playlistName.toLowerCase())) {
                toastText = "Playlist " + playlistName + " already exists!";
                Toast toast = Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues playlistValues = new ContentValues();
        playlistValues.put(PlaylistTable.PlaylistTableEntry.PLAYLIST_NAME, playlistName);

        long newPlaylistId;
        newPlaylistId = db.insert(
                PlaylistTable.PlaylistTableEntry.TABLE_NAME,
                null,
                playlistValues);

        for (Song song  : playlist) {
            ContentValues songValues = new ContentValues();
            songValues.put(SongsTable.SongsTableEntry.SONG_ID, song.getId());
            songValues.put(SongsTable.SongsTableEntry.PLAYLIST_ID, newPlaylistId);
            songValues.put(SongsTable.SongsTableEntry.SONG_ALBUM_ID, song.getAlbumId());
            songValues.put(SongsTable.SongsTableEntry.SONG_TITLE, song.getTitle());
            songValues.put(SongsTable.SongsTableEntry.SONG_ARTIST, song.getArtist());

            db.insert(
                    SongsTable.SongsTableEntry.TABLE_NAME,
                    null,
                    songValues);
        }

        // Remove the highlighted list items
        for (Song song : playlist) {
            int index = songLibrary.indexOf(song);
            songLibrary.get(index).setSelected(false);
        }

        finish();
    }
}
