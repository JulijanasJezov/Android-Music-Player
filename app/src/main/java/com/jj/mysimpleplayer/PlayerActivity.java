package com.jj.mysimpleplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.IBinder;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jj.mysimpleplayer.interfaces.PlaybackServiceCallbacks;
import com.jj.mysimpleplayer.models.Song;
import com.jj.mysimpleplayer.utility.Helpers;
import com.jj.mysimpleplayer.constants.Constants;

public class PlayerActivity extends Activity implements MediaController.MediaPlayerControl, PlaybackServiceCallbacks {

    public PlaybackService playbackService;
    private Intent playbackIntent;
    private boolean playbackBound = false;
    private int songPosition;
    private SeekBar songSeekBar;
    private Handler seekHandler = new Handler();
    private TextView progressText;
    private TextView songDurationText;
    private Boolean isPlayRequested;
    private ImageButton playPauseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Get passed parameters
        Intent intent = getIntent();
        songPosition = intent.getIntExtra(Constants.SONG_POSITION, 0);
        isPlayRequested = intent.getBooleanExtra(Constants.PLAY_REQUEST, false);
        playPauseButton = (ImageButton)findViewById(R.id.play_pause);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(playbackIntent==null){
            playbackIntent = new Intent(this, PlaybackService.class);
        }

        bindService(playbackIntent, playbackServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /*
       Handles the service connection/disconnection
   */
    private ServiceConnection playbackServiceConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlaybackService.PlaybackBinder binder = (PlaybackService.PlaybackBinder)service;

            playbackService = binder.getService();

            playbackService.setCallbacks(PlayerActivity.this);

            if (playbackService.isPlaying()) {
                playPauseButton.setImageResource(R.drawable.ic_pause);
            } else {
                playPauseButton.setImageResource(R.drawable.ic_play);
            }

            playbackBound = true;

            if (isPlayRequested) {
                playbackService.setCurrentSong(songPosition);
                playbackService.playSong();
                playPauseButton.setImageResource(R.drawable.ic_pause);
                isPlayRequested = false;
            }

            initUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            playbackBound = false;
        }
    };

    @Override
    protected void onPause() {
        super.onPause();

        playbackBound = false;
        if (playbackService != null) {
            playbackService.removeCallbacks();
        }
        unbindService(playbackServiceConnection);
    }

    /*
       Handles play/pause button click
   */
    public void playPauseClick(View view) {
        if (playbackService.isPlaying()) {
            playPauseButton.setImageResource(R.drawable.ic_play);
            pause();
        }else{
            playPauseButton.setImageResource(R.drawable.ic_pause);;
            start();
        }
    }

    /*
       Set/unset shuffle feature on button click
   */
    public void onShuffleClick(View view) {
        if (playbackService.getShuffleStatus()) {
            playbackService.setShuffle(false);
        } else {
            playbackService.setShuffle(true);
        }

        updateShuffleButton();
    }

    /*
       Update shuffle button showing if it's on/off
   */
    private void updateShuffleButton() {
        ImageView shuffleButton = (ImageView) findViewById(R.id.shuffle);
        if (playbackService.getShuffleStatus()) {
            shuffleButton.getBackground().setColorFilter(getResources().getColor(R.color.light_orange), PorterDuff.Mode.SRC_ATOP);
        } else {
            shuffleButton.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        }
    }

    /*
       Next song button click
   */
    public void nextSongClick(View view) {
        nextSong();
    }

    /*
       Previous song button click
   */
    public void prevSongClick(View view) {
        prevSong();
    }

    /*
       Update UI on next song
   */
    @Override
    public void nextSong(){
        if (playbackService.nextSong()) {
            setSongDuration(Constants.UNSET_MAX_DURATION);
            playPauseButton.setImageResource(R.drawable.ic_pause);
        }
        initUI();
    }

    /*
       Update UI on previous song
   */
    public void prevSong(){
        if (playbackService.prevSong()) {
            setSongDuration(Constants.UNSET_MAX_DURATION);
            playPauseButton.setImageResource(R.drawable.ic_pause);
            initUI();
        }
    }

    /*
       Update UI when playlist ends
   */
    public void endPlaylist() {
        playPauseButton.setImageResource(R.drawable.ic_play);
    }

    /*
       Thread for maintaining the seek bar
   */
    Runnable maintainSeekBar = new Runnable() {
        @Override public void run() {
            updateSongSeekBar();
        }
    };

    /*
       Update the seek bar position depending on the song duration and current position
   */
    public void updateSongSeekBar() {
        if (playbackBound && playbackService.isPlayerStarted()) {
            if (songSeekBar.getMax() == Constants.UNSET_MAX_DURATION) {
                setSongDuration(getDuration());
            }

            int currentPos = getCurrentPosition();
            songSeekBar.setProgress(currentPos);
        }
        seekHandler.postDelayed(maintainSeekBar, 100);
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
        return playbackService.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return playbackService.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        playbackService.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return playbackService.isPlaying();
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

    /*
       Handle the seek bar user interaction clicks
   */
    private class seekBarListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            progressText.setText(Helpers.getFormattedTime(progress));
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {
            seekTo(seekBar.getProgress());
        }
    }

    /*
       Initialise/update the whole activity's UI
   */
    private void initUI() {
        Song song;

        song = playbackService.getSongLibrary().get(playbackService.getCurrentSong());

        TextView songTitleView = (TextView)findViewById(R.id.song_title);
        songTitleView.setText(song.getTitle());

        TextView songArtistView = (TextView)findViewById(R.id.song_artist);
        songArtistView.setText(song.getArtist());

        ImageView coverArtView = (ImageView)findViewById(R.id.cover_art);
        Bitmap coverArt = song.getCoverArt();
        if (coverArt == null) {
            coverArtView.setImageResource(R.drawable.default_art);
        } else {
            coverArtView.setImageBitmap(coverArt);
        }

        progressText = (TextView)findViewById(R.id.progress_text);
        songDurationText = (TextView)findViewById(R.id.song_duration_text);

        updateShuffleButton();

        songSeekBar = (SeekBar)findViewById(R.id.song_seek_bar);
        songSeekBar.setOnSeekBarChangeListener(new seekBarListener());
        updateSongSeekBar();
    }

    /*
       Sets the seek bar's duration of the passed value
   */
    private void setSongDuration(int dur) {
        songSeekBar.setMax(dur);
        songDurationText.setText(Helpers.getFormattedTime(dur));
    }
}
