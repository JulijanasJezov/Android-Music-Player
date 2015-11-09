package com.jj.mysimpleplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.os.RemoteException;
import android.support.v7.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.MediaSessionCompat.Callback;
import android.util.Log;

import com.jj.mysimpleplayer.constants.Constants;

public class PlayerNotification {

    private MediaSessionManager sessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat mediaController;


    public void handleIntent( Intent intent ) {
        if( intent == null || intent.getAction() == null )
            return;

        String action = intent.getAction();

        if( action.equalsIgnoreCase( Constants.NOTIFICATION_PLAY ) ) {
            mediaController.getTransportControls().play();
        } else if( action.equalsIgnoreCase( Constants.NOTIFICATION_PAUSE ) ) {
            mediaController.getTransportControls().pause();
        } else if( action.equalsIgnoreCase( Constants.NOTIFICATION_PREV ) ) {
            mediaController.getTransportControls().skipToPrevious();
        } else if( action.equalsIgnoreCase(Constants.NOTIFICATION_NEXT ) ) {
            mediaController.getTransportControls().skipToNext();
        } else if( action.equalsIgnoreCase( Constants.NOTIFICATION_STOP ) ) {
            mediaController.getTransportControls().stop();
        }
    }

    private void buildNotification( NotificationCompat.Action action, Context service, Song song ) {
        Intent intent = new Intent( service.getApplicationContext(), PlaybackService.class );
        intent.setAction( Constants.NOTIFICATION_STOP );

        PendingIntent pendingIntent = PendingIntent.getService(service.getApplicationContext(), 1, intent, 0);

        Bitmap coverArt = song.getCoverArt() != null ? song.getCoverArt()
                : BitmapFactory.decodeResource(service.getResources(), R.drawable.default_art);


        Notification noti = new NotificationCompat.Builder(service)
                .setSmallIcon(R.drawable.default_art)
                .setContentTitle(song.getTitle())
                .setContentText(song.getArtist())
                .setLargeIcon(coverArt)
                .setStyle(new NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken()))
                .addAction(generateAction(android.R.drawable.ic_media_previous, "Previous", Constants.NOTIFICATION_PREV, service))
                .addAction( action )
                .addAction(generateAction(android.R.drawable.ic_media_next, "Next", Constants.NOTIFICATION_NEXT, service))
                .build();

        NotificationManager notificationManager = (NotificationManager) service.getSystemService( Context.NOTIFICATION_SERVICE);
        notificationManager.notify( 1, noti);
    }

    private NotificationCompat.Action generateAction( int icon, String title, String intentAction, Context service ) {
        Intent intent = new Intent( service.getApplicationContext(), PlaybackService.class );
        intent.setAction( intentAction );
        PendingIntent pendingIntent = PendingIntent.getService(service.getApplicationContext(), 1, intent, 0);
        return new NotificationCompat.Action.Builder( icon, title, pendingIntent ).build();

    }

    public void initSession(Context srv) {
        final Context service = srv;
        sessionManager = (MediaSessionManager) service.getSystemService(Context.MEDIA_SESSION_SERVICE);
        mediaSession = new MediaSessionCompat(service.getApplicationContext(), "media session", null, null);
        try {
            mediaController = new MediaControllerCompat(service.getApplicationContext(), mediaSession.getSessionToken());
        } catch (RemoteException ex) {
            Log.d("MediaControllerInit", "Error");
        }

        mediaSession.setActive(true);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mediaSession.setCallback(new Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                // buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", Constants.NOTIFICATION_PAUSE, service), service);
            }

            @Override
            public void onPause() {
                super.onPause();
                //buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", Constants.NOTIFICATION_PLAY, service), service);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                //buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", Constants.NOTIFICATION_PAUSE, service), service);
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                //buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", Constants.NOTIFICATION_PAUSE, service), service);
            }

            @Override
            public void onStop() {
                super.onStop();
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
            }

        });
    }

    public void showNotification(Context service, Song song) {
        buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", Constants.NOTIFICATION_PAUSE, service), service, song);
    }

    public void releaseSession() {
        mediaSession.release();
    }
}
