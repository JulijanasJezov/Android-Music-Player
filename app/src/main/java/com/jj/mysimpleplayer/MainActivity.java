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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


import java.util.ArrayList;

import com.jj.mysimpleplayer.PlaybackService.PlaybackBinder;

public class MainActivity extends AppCompatActivity implements PlaybackServiceCallbacks {

    public final static String SONG_POSITION = "com.jj.mysimpleplayer.SONG_POS";
    public final static String PLAY_REQUEST = "com.jj.mysimpleplayer.PLAY_REQ";

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

        songLibrary = Helpers.getSongLibrary(this);

        if (savedInstanceState == null) {
            openFragment(0);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(playbackIntent==null){
            playbackIntent = new Intent(this, PlaybackService.class);
            startService(playbackIntent);
        }

        bindService(playbackIntent, playbackServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection playbackServiceConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlaybackBinder binder = (PlaybackBinder)service;

            playbackService = binder.getService();
            playbackService.setCallbacks(MainActivity.this);
            playbackService.setSongLibrary(songLibrary);

            initMiniPlayerUI(false);

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

        playbackService.removeCallbacks();
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

    private void initMiniPlayerUI(boolean isNextSong) {
        Song song = songLibrary.get(playbackService.getCurrentSong());
        TextView songTitle = (TextView)findViewById(R.id.current_song);
        songTitle.setText(song.getTitle());

        Button playPauseButton = (Button)findViewById(R.id.play_pause);
        playPauseButton.setText(playbackService.isPlaying() || isNextSong ? "Pause" : "Play");

    }

    public void playSong(View view) {
        // Get song position from a song title view tag
        int songPos = Integer.parseInt(view.findViewById(R.id.song_title).getTag().toString());
        openPlayerIntent(songPos, true);
    }

    public void openPlayer(View view) {
        openPlayerIntent(playbackService.getCurrentSong(), false);
    }

    private void openPlayerIntent(int songPos, boolean playReq) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra(SONG_POSITION, songPos);
        intent.putExtra(PLAY_REQUEST, playReq);
        startActivity(intent);
    }

    public void playPauseClick(View view) {
        Button playPauseButton = (Button) view;
        if (playbackService.isPlaying()) {
            playPauseButton.setText("Play");
            playbackService.pauseSong();
        } else {
            playPauseButton.setText("Pause");
            playbackService.unpauseSong();
        }
    }

    @Override
    public void nextSong() {
        if (playbackService.nextSong()) {
            initMiniPlayerUI(true);
        }
    }
}
