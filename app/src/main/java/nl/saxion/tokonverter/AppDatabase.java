package nl.saxion.tokonverter;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import nl.saxion.tokonverter.EventDao;
import nl.saxion.tokonverter.models.Event;
import nl.saxion.tokonverter.models.Stand;
import nl.saxion.tokonverter.models.StandMenuItem;

@Database(entities = {Event.class, Stand.class, StandMenuItem.class}, version=6)
public abstract class AppDatabase extends RoomDatabase {
    public abstract EventDao eventDao();

    static AppDatabase instance;
    static synchronized public AppDatabase getInstance(Context context){
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

}
