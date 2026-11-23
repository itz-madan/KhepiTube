package com.khepilabs.khepitube.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Drops the retired online Music Home feed caches. */
class Migration34To35 : Migration(34, 35) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS music_home_chips_cache")
        database.execSQL("DROP TABLE IF EXISTS music_home_cache")
    }
}
