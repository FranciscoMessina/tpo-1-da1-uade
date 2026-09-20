package com.da_grupo9.ronda.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {PublicacionEntity.class}, version = 3, exportSchema = false)
public abstract class RondaDatabase extends RoomDatabase {
    public abstract PublicacionDao publicacionDao();
}
