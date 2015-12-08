package com.jj.mysimpleplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.jj.mysimpleplayer.constants.Constants;
import com.jj.mysimpleplayer.interfaces.PlaybackServiceCallbacks;
import com.jj.mysimpleplayer.models.Song;

import java.util.ArrayList;
import java.util.Random;

public class PlaybackService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener {

    private final IBinder playbackBinder = new PlaybackBinder();
    private PlaybackServiceCallbacks playbackServiceCallbacks;

    private MediaPlayer player;
    private ArrayList<Song> songLibrary;
    private int songPosition;
    private boolean isPlayerStarted = false;
    private boolean isAppClosed = false;

    private MediaSessionCompat mediaSession;
    private MediaControllerCompat mediaController;
    private NotificationManager notificationManager;
    private boolean isDeleteReceiverRegistered = false;
    private boolean isNotificationShown = false;
    private Handler notificationHandler = new Handler();
    private boolean shuffle = false;


    @Override
    public IBinder onBind(Intent intent) {
        return playbackBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mediaSession.release();
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        songPosition = 0;
        player = new MediaPlayer();

        setupMusicPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mediaSession == null) initSession();

        handleIntent(intent);
        checkNotificationStatus();
        return super.onStartCommand(intent, flags, startId);
    }

    /*
        Set up the music player
    */
    public void setupMusicPlayer () {
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
    }

    /*
        Set currently playing list
    */
    public void setSongLibrary(ArrayList<Song> songs) {
        songLibrary = songs;
    }

    /*
        Get currently playing list
    */
    public ArrayList<Song> getSongLibrary() {
        return songLibrary;
    }

    /*
        Set the position of the song to play
    */
    public void setCurrentSong(int songPos) {
        songPosition = songPos;
    }

    /*
        Get the position of the song that is playing
    */
    public int getCurrentSong() {
        return songPosition;
    }

    /*
        Works out the song's Id from the Song's object, gets an URI and plays the song
    */
    public void playSong() {
        player.reset();

        int currentSongId = songLibrary.get(songPosition).getId();

        Uri songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentSongId);

        try {
            player.setDataSource(getApplicationContext(), songUri);
        }
        catch (Exception ex) {
            Log.e("setDataSource", "Cannot set the song" + ex.getMessage());
        }

        player.prepareAsync();
    }

    /*
        Stop the player playing
    */
    public void stopSong() {
        player.stop();
        isPlayerStarted = false;
    }

    /*
        Pause the player
    */
    public void pauseSong(){
        player.pause();
    }

    /*
        Unpause the player
    */
    public void unpauseSong() {
        if (!isPlayerStarted) {
            playSong();
        } else {
            player.seekTo(getCurrentPosition());
            player.start();
        }

    }

    /*
        Seek to time position of the song
    */
    public void seekTo(int position) {
        player.seekTo(position);
    }

    /*
        Changes the song to the next one and plays it
    */
    public boolean nextSong() {
        if (shuffle) {
            Random rand = new Random();
            int currentSongPosition = songPosition;
            while (songPosition == currentSongPosition) {
                songPosition = rand.nextInt(songLibrary.size());
            }
        } else {
            songPosition++;
        }

        isPlayerStarted = false;

        if(songPosition >= songLibrary.size()) {
            songPosition--;
        }

        playSong();

        return true;
    }

    /*
        Changes the song to the previous one and plays it
    */
    public boolean prevSong() {
        songPosition--;
        isPlayerStarted = false;

        if(songPosition < 0) {
            songPosition++;
        }

        playSong();

        return true;
    }

    /*
        Get the status if player is playing
    */
    public boolean isPlaying() {
        return player.isPlaying();
    }

    /*
        When the song ends start playing the next one if it exists
    */
    @Override
    public void onCompletion(MediaPlayer mp) {
        boolean playlistEnded = songPosition == songLibrary.size() - 1;
        if (playlistEnded) {
            seekTo(0);
            pauseSong();
        }

        if (playbackServiceCallbacks != null) {
            if (playlistEnded) {
                playbackServiceCallbacks.endPlaylist();
            } else {
                playbackServiceCallbacks.nextSong();
            }
        } else {
            if (!playlistEnded) {
                nextSong();
            }
            showNotification();
        }
    }

    /*
        Get the current time position of the song set
    */
    public int getCurrentPosition(){
        return player.getCurrentPosition();
    }

    /*
        Get the full time duration of the song set
    */
    public int getDuration() {
        return player.getDuration();
    }

    /*
        Get the status if player is started
    */
    public boolean isPlayerStarted() {
        return isPlayerStarted;
    }

    /*
        Start the player playing
    */
    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        isPlayerStarted = true;
    }

    /*
        Close notification when the task is removed
    */
    @Override
    public void onTaskRemoved(Intent rootIntent){
        super.onTaskRemoved(rootIntent);
        closeNotification();
    }

    /*
        Stop and remove everything connected to the server
    */
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (isDeleteReceiverRegistered) {
            unregisterReceiver(deleteNotificationReceiver);
        }

        notificationHandler.removeCallbacks(maintainNotificationStatus);
        player.reset();
        player.release();
    }

    /*
        Get this service class
    */
    public class PlaybackBinder extends Binder {
        PlaybackService getService() {
            return PlaybackService.this;
        }
    }

    /*
        Set callbacks to update the UI e.g. on song change
    */
    public void setCallbacks(PlaybackServiceCallbacks callbacks) {
        playbackServiceCallbacks = callbacks;
    }

    /*
        Remove callbacks
    */
    public void removeCallbacks() {
        playbackServiceCallbacks = null;
    }

    /*
        Set the application closed status
    */
    public void setClosedAppFlag(boolean isClosed) {
        isAppClosed = isClosed;
    }

    /*
        Set shuffle feature to off/on
    */
    public void setShuffle(boolean shuff) {
        shuffle = shuff;
    }

    /*
        Get shuffle status if off/on
    */
    public boolean getShuffleStatus() {
        return shuffle;
    }

    // Notification

    /*
        Set the notification's controls
    */
    public void handleIntent( Intent intent ) {
        if( intent == null || intent.getAction() == null )
            return;

        String action = intent.getAction();

        switch (action) {
            case Constants.NOTIFICATION_PLAY:
                mediaController.getTransportControls().play();
                break;
            case Constants.NOTIFICATION_PAUSE:
                mediaController.getTransportControls().pause();
                break;
            case Constants.NOTIFICATION_PREV:
                mediaController.getTransportControls().skipToPrevious();
                break;
            case Constants.NOTIFICATION_NEXT:
                mediaController.getTransportControls().skipToNext();
                break;
            default:
                break;
        }
    }

    /*
        Build and display the notification displaying the right controls
    */
    private void buildNotification( NotificationCompat.Action action) {
        Intent deletedIntent = new Intent(Constants.NOTIFICATION_DELETED);
        PendingIntent deletedPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, deletedIntent, 0);
        registerReceiver(deleteNotificationReceiver, new IntentFilter(Constants.NOTIFICATION_DELETED));
        isDeleteReceiverRegistered = true;

        Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);

        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                mainActivityIntent, 0);

        boolean isCloseable = action.title == "Pause";

        Song currentSong = songLibrary.get(songPosition);

        Bitmap coverArt = currentSong.getCoverArt() != null ? currentSong.getCoverArt()
                : BitmapFactory.decodeResource(getResources(), R.drawable.default_art);

        int[] lockScreenActions = {0, 1, 2};

        Notification mediaStyleNotification = new NotificationCompat.Builder(this)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setDeleteIntent(deletedPendingIntent)
                .setContentIntent(mainActivityPendingIntent)
                .setSmallIcon(R.drawable.default_art)
                .setContentTitle(currentSong.getTitle())
                .setContentText(currentSong.getArtist())
                .setLargeIcon(coverArt)
                .setStyle(new NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(lockScreenActions)
                        .setMediaSession(mediaSession.getSessionToken()))
                .addAction(generateAction(android.R.drawable.ic_media_previous, "Previous", Constants.NOTIFICATION_PREV))
                .addAction(action)
                .addAction(generateAction(android.R.drawable.ic_media_next, "Next", Constants.NOTIFICATION_NEXT))
                .setOngoing(isCloseable)
                .build();

        notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE);
        mediaStyleNotification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(1, mediaStyleNotification);
        isNotificationShown = true;
    }

    /*
        Generates notification's action e.g. previous, next song etc.
    */
    private NotificationCompat.Action generateAction(int icon, String title, String intentAction ) {
        Intent intent = new Intent( getApplicationContext(), PlaybackService.class );
        intent.setAction( intentAction );
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);

        return new NotificationCompat.Action.Builder( icon, title, pendingIntent ).build();
    }

    /*
        Initialise media session setting all the controls for notification
    */
    public void initSession() {
        mediaSession = new MediaSessionCompat(getApplicationContext(), "media session", null, null);
        try {
            mediaController = new MediaControllerCompat(getApplicationContext(), mediaSession.getSessionToken());
        } catch (RemoteException ex) {
            Log.e("Media controller", "Cannot create MediaControllerCompat " + ex.getMessage());
        }

        mediaSession.setActive(true);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                unpauseSong();
                buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", Constants.NOTIFICATION_PAUSE));
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseSong();
                buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", Constants.NOTIFICATION_PLAY));
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                nextSong();
                buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", Constants.NOTIFICATION_PAUSE));
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                prevSong();
                buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", Constants.NOTIFICATION_PAUSE));
            }
        });
    }

    /*
        Display notification
    */
    public void showNotification() {
        buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", Constants.NOTIFICATION_PAUSE));
    }

    /*
        Remove notification from the notifications bar
    */
    public void closeNotification() {
        if (notificationManager != null) {
            notificationManager.cancel(1);
            isNotificationShown = false;
        }
    }

    /*
        Catches when notification has been swiped off and stops the service if application is closed
    */
    private final BroadcastReceiver deleteNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isNotificationShown = false;
            if (isAppClosed) {
                Intent playbackService = new Intent(getApplicationContext(), PlaybackService.class);
                stopService(playbackService);
            }
        }
    };

    /*
        Thread running to show/close notification when needed
    */
    Runnable maintainNotificationStatus = new Runnable() {
        @Override public void run() {
            checkNotificationStatus();
        }
    };

    /*
        Method running in the thread to display or remove notification
    */
    public void checkNotificationStatus() {
        if (player.isPlaying() && playbackServiceCallbacks == null &&  !isNotificationShown) {
            showNotification();
        } else if (playbackServiceCallbacks != null && isNotificationShown) {
            closeNotification();
        }
        notificationHandler.postDelayed(maintainNotificationStatus, 1000);
    }
}
