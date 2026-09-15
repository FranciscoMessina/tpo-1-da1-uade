package com.da_grupo9.ronda.di;

import android.content.Context;
import androidx.room.Room;

import com.da_grupo9.ronda.data.local.PublicacionDao;
import com.da_grupo9.ronda.data.local.RondaDatabase;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public final class DatabaseModule {

    private DatabaseModule() {
    }

    @Provides
    @Singleton
    static RondaDatabase provideRondaDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(
                context,
                RondaDatabase.class,
                "ronda_database"
        ).fallbackToDestructiveMigration().build();
    }

    @Provides
    @Singleton
    static PublicacionDao providePublicacionDao(RondaDatabase database) {
        return database.publicacionDao();
    }
}
