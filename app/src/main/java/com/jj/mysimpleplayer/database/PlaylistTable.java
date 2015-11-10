package com.jj.mysimpleplayer.database;


import android.provider.BaseColumns;

public final class PlaylistTable {
    public PlaylistTable() {}

    public static abstract class PlaylistTableEntry implements BaseColumns {
        public static final String TABLE_NAME = "Playlists";
        public static final String PLAYLIST_NAME = "name";
    }

    private static final String TEXT_TYPE = " TEXT";
    public static final String SQL_CREATE_PLAYLIST_TABLE =
            "CREATE TABLE IF NOT EXISTS " + PlaylistTableEntry.TABLE_NAME + " (" +
                    PlaylistTableEntry._ID + " INTEGER PRIMARY KEY," +
                    PlaylistTableEntry.PLAYLIST_NAME + TEXT_TYPE + " )";

    public static final String SQL_DELETE_PLAYLIST_TABLE =
            "DROP TABLE IF EXISTS " + PlaylistTableEntry.TABLE_NAME;
}
