package com.jj.mysimpleplayer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.IBinder;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import java.util.ArrayList;

import com.jj.mysimpleplayer.PlaybackService.PlaybackBinder;
import com.jj.mysimpleplayer.adapters.DrawerAdapter;
import com.jj.mysimpleplayer.interfaces.PlaybackServiceCallbacks;
import com.jj.mysimpleplayer.models.Song;
import com.jj.mysimpleplayer.utility.Helpers;
import com.jj.mysimpleplayer.constants.Constants;

public class MainActivity extends AppCompatActivity implements PlaybackServiceCallbacks {
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private String activityTitle;
    private LinearLayout drawerLinearLayout;
    private ListView drawerList;
    private String[] sideNavItems;
    public static ArrayList<Song> songLibrary;
    public static ArrayList<Song> playlistSongs;

    public static PlaybackService playbackService;
    private Intent playbackIntent;

    public static boolean isPlaylistChosen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create side drawer navigation
        drawerLinearLayout = (LinearLayout)findViewById(R.id.side_nav);
        drawerList = (ListView)findViewById(R.id.side_nav_list);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

        sideNavItems = getResources().getStringArray(R.array.side_nav_items);

        DrawerAdapter drawerAdapter = new DrawerAdapter(this, sideNavItems);
        drawerList.setAdapter(drawerAdapter);
        setupSideNavToggle();

        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openFragment(position);
            }
        });

        // Get all the songs for the library
        songLibrary = Helpers.getSongLibrary(this);

        if (savedInstanceState == null) {
            openFragment(0); // if new activity open the first fragment view
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Set the activities title depending on the fragment opened
        Fragment fragment = getFragmentManager().findFragmentById(R.id.frame_container);
        assert getSupportActionBar() != null;
        if (fragment instanceof LibraryFragment) {
            activityTitle = sideNavItems[0];
        } else if (fragment instanceof PlaylistsFragment) {
            activityTitle = sideNavItems[1];
        }

        getSupportActionBar().setTitle(activityTitle);

        if(playbackIntent==null){
            playbackIntent = new Intent(this, PlaybackService.class);
            startService(playbackIntent);
        }

        bindService(playbackIntent, playbackServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /*
        Handles the service connection/disconnection
    */
    private ServiceConnection playbackServiceConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlaybackBinder binder = (PlaybackBinder)service;

            playbackService = binder.getService();
            playbackService.setCallbacks(MainActivity.this);
            playbackService.closeNotification();
            playbackService.setClosedAppFlag(false);

            if (playbackService.getSongLibrary() == null) {
                playbackService.setSongLibrary(songLibrary);
            }

            initMiniPlayerUI(false);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {}
    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (playbackService != null) {
            playbackService.removeCallbacks(); // Remove callbacks in the service when activity isn't visible
        }
        unbindService(playbackServiceConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stop the service when the app is closed and music isn't playing
        // Otherwise display notification
        if(!playbackService.isPlaying()) {
            stopService(playbackIntent);
            playbackService = null;
        } else {
            playbackService.showNotification();
            playbackService.setClosedAppFlag(true);
        }
    }

    /*
        Opens one of the fragments to display in the activity
    */
    private void openFragment(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new LibraryFragment();
                break;
            case 1:
                fragment = new PlaylistsFragment();
                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();

            drawerList.setItemChecked(position, true);
            drawerList.setSelection(position);
            activityTitle = sideNavItems[position];
            drawerLayout.closeDrawer(drawerLinearLayout);

            assert getSupportActionBar() != null;
            getSupportActionBar().setTitle(activityTitle);
        }
    }

    /*
        Set up side navigation toggle, handling onOpened/onClosed actions
    */
    private void setupSideNavToggle() {
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(getString(R.string.app_name));
                invalidateOptionsMenu();
            }

            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
                getSupportActionBar().setTitle(activityTitle);
            }
        };

        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.setDrawerListener(drawerToggle);
    }

    /*
        Initialise mini player displayed at the bottom, setting up the songs details
    */
    private void initMiniPlayerUI(boolean isNextSong) {
        Song song;

        song = playbackService.getSongLibrary().get(playbackService.getCurrentSong());

        TextView songTitle = (TextView)findViewById(R.id.current_song);
        songTitle.setText(song.getTitle());

        ImageButton playPauseButton = (ImageButton)findViewById(R.id.play_pause);
        if (playbackService.isPlaying() || isNextSong) {
            playPauseButton.setImageResource(R.drawable.ic_pause);
        } else {
            playPauseButton.setImageResource(R.drawable.ic_play);
        }
    }

    /*
        Handles the song click, finds the right song library and the song's position
    */
    public void onSongClick(View view) {
        Fragment f = getFragmentManager().findFragmentById(R.id.frame_container);

        if (f instanceof LibraryFragment) {
            isPlaylistChosen = false;
            playbackService.setSongLibrary(songLibrary);
        }else {
            isPlaylistChosen = true;
            playbackService.setSongLibrary(playlistSongs);
        }

        if (playbackService.isPlaying()) {
            playbackService.stopSong();
        }

        // Get song position from a song title view tag
        int songPos = Integer.parseInt(view.findViewById(R.id.song_title).getTag().toString());
        openPlayerIntent(songPos, true);
    }

    /*
        Opens a player activity with play request being false
    */
    public void openPlayer(View view) {
        openPlayerIntent(playbackService.getCurrentSong(), false);
    }

    /*
        Creates a new player intent and starts it
    */
    private void openPlayerIntent(int songPos, boolean playReq) {
        Intent playerActivityIntent = new Intent(this, PlayerActivity.class);
        playerActivityIntent.putExtra(Constants.SONG_POSITION, songPos);
        playerActivityIntent.putExtra(Constants.PLAY_REQUEST, playReq);
        startActivity(playerActivityIntent);
    }

    /*
        Handles the play/pause button click pausing/unpausing the song
    */
    public void playPauseClick(View view) {
        ImageButton playPauseButton = (ImageButton) view;
        if (playbackService.isPlaying()) {
            playPauseButton.setImageResource(R.drawable.ic_play);
            playbackService.pauseSong();
        } else {
            playPauseButton.setImageResource(R.drawable.ic_pause);
            playbackService.unpauseSong();
        }
    }

    /*
        Update UI when playlist ends
    */
    public void endPlaylist() {
        ImageButton playPauseButton = (ImageButton) findViewById(R.id.play_pause);
        playPauseButton.setImageResource(R.drawable.ic_play);
    }

    /*
        Update UI when next song comes on
    */
    @Override
    public void nextSong() {
        if (playbackService.nextSong()) {
            initMiniPlayerUI(true);
        }
    }
}
