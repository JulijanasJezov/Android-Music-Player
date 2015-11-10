package com.jj.mysimpleplayer.database;

import android.provider.BaseColumns;

public final class SongsTable {
    public SongsTable() {}

    public static abstract class SongsTableEntry implements BaseColumns {
        public static final String TABLE_NAME = "Songs";
        public static final String SONG_ID = "songId";
        public static final String PLAYLIST_ID = "playlistId";
        public static final String SONG_ALBUM_ID = "albumId";
        public static final String SONG_TITLE = "title";
        public static final String SONG_ARTIST = "artist";
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INT";
    private static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_SONGS_TABLE =
            "CREATE TABLE IF NOT EXISTS " + SongsTableEntry.TABLE_NAME + " (" +
                    SongsTableEntry._ID + " INTEGER PRIMARY KEY," +
                    SongsTableEntry.PLAYLIST_ID + INT_TYPE + COMMA_SEP +
                    SongsTableEntry.SONG_ID + INT_TYPE + COMMA_SEP +
                    SongsTableEntry.SONG_ALBUM_ID + INT_TYPE + COMMA_SEP +
                    SongsTableEntry.SONG_TITLE + TEXT_TYPE + COMMA_SEP +
                    SongsTableEntry.SONG_ARTIST + TEXT_TYPE + " )";

    public static final String SQL_DELETE_SONGS_TABLE =
            "DROP TABLE IF EXISTS " + SongsTableEntry.TABLE_NAME;
}
