package com.jj.mysimpleplayer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.jj.mysimpleplayer.PlaybackService.PlaybackBinder;

public class MainActivity extends AppCompatActivity  {

    public final static String SONG_POSITION = "com.jj.mysimpleplayer.SONG_POS";

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;
    private ListView mDrawerList;
    private String[] sideNavItems;
    public static ArrayList<Song> songLibrary;

    public PlaybackService playbackService;
    private Intent playbackIntent;
    private boolean playbackBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerList = (ListView)findViewById(R.id.side_nav);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();

        sideNavItems = getResources().getStringArray(R.array.side_nav_items);
        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1, sideNavItems);
        mDrawerList.setAdapter(mAdapter);
        setupSideNavToggle();

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openFragment(position);
            }
        });

        songLibrary = new ArrayList<>();
        getSongLibrary();

        if (savedInstanceState == null) {
            openFragment(0);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(playbackIntent==null){
            playbackIntent = new Intent(this, PlaybackService.class);
            bindService(playbackIntent, playbackServiceConnection, Context.BIND_AUTO_CREATE);
            startService(playbackIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        bindService(playbackIntent, playbackServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection playbackServiceConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlaybackBinder binder = (PlaybackBinder)service;

            playbackService = binder.getService();

            playbackService.setSongLibrary(songLibrary);
            playbackBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            playbackBound = false;
        }
    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unbindService(playbackServiceConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(!playbackService.isPlaying()) {
            stopService(playbackIntent);
            playbackService = null;
        }
    }

    private void openFragment(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new LibraryFragment();
                break;
            case 1:
                fragment = new SettingsFragment();
                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();

            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            mActivityTitle = sideNavItems[position];
            mDrawerLayout.closeDrawer(mDrawerList);

            assert getSupportActionBar() != null;
            getSupportActionBar().setTitle(mActivityTitle);
        }
    }

    private void setupSideNavToggle() {
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(getString(R.string.app_name));
                invalidateOptionsMenu();
            }

            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
                getSupportActionBar().setTitle(mActivityTitle);
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    public void getSongLibrary() {
        ContentResolver musicResolver = getContentResolver();
        Uri imageUri = Uri.parse("content://media/external/audio/albumart");
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        Cursor musicCursor = musicResolver.query(musicUri, null, selection, null, null);

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
                        coverArt = MediaStore.Images.Media.getBitmap(getContentResolver(), albumArtUri);

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
    }

    public void playSong(View view) {
        // Get song position from a song title view tag
        int songPos = Integer.parseInt(view.findViewById(R.id.song_title).getTag().toString());
        playbackService.setCurrentSong(songPos);
        playbackService.playSong();

        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra(SONG_POSITION, songPos);
        startActivity(intent);
    }
}
