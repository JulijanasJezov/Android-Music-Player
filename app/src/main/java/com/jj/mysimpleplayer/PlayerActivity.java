package com.jj.mysimpleplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController;

public class PlayerActivity extends Activity implements MediaController.MediaPlayerControl {

    public PlaybackService playbackService;
    private Intent playbackIntent;
    private boolean playbackBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(playbackIntent==null){
            playbackIntent = new Intent(this, PlaybackService.class);
            bindService(playbackIntent, playbackServiceConnection, Context.BIND_AUTO_CREATE);
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
            PlaybackService.PlaybackBinder binder = (PlaybackService.PlaybackBinder)service;

            playbackService = binder.getService();

            playbackBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            playbackBound = false;
        }
    };

    @Override
    protected void onPause() {
        super.onPause();

        unbindService(playbackServiceConnection);
    }

    public void playPause(View view) {
        if (playbackService.isPlaying()) {
            pause();
        }else{
            start();
        }
    }

    @Override
    public void start() {
        playbackService.unpauseSong();
    }

    @Override
    public void pause() {
        playbackService.pauseSong();
    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public void seekTo(int pos) {

    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return false;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
