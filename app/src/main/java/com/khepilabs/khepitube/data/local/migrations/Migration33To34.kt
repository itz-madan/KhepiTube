package com.khepilabs.khepitube.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Drops the retired online Music Brain graph while preserving playlists and downloaded media. */
class Migration33To34 : Migration(33, 34) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS music_graph_edge")
        database.execSQL("DROP TABLE IF EXISTS music_graph_playlist")
        database.execSQL("DROP TABLE IF EXISTS music_graph_album")
        database.execSQL("DROP TABLE IF EXISTS music_graph_artist")
        database.execSQL("DROP TABLE IF EXISTS music_graph_track")
    }
}
