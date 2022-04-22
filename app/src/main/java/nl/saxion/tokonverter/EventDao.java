package nl.saxion.tokonverter;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import nl.saxion.tokonverter.models.Event;
import nl.saxion.tokonverter.models.Stand;
import nl.saxion.tokonverter.models.StandMenuItem;

@Dao
public interface EventDao {

    // ### EVENT QUERIES ###
    @Query("SELECT * FROM event")
    LiveData<List<Event>> getAllEvents();

    @Query("SELECT * FROM event WHERE id=:id")
    LiveData<Event> getEvent(long id);

    @Insert
    long addEvent(Event event);

    @Update
    void updateEvent(Event event);

    @Delete
    void deleteEvent(Event event);


    // ## STAND QUERIES ###
    class StandWithItemCount extends Stand {
        public Integer numItems;
    }

    @Query("SELECT stand.*, count(standmenuitem.id) as numItems " +
            "FROM stand " +
            "LEFT JOIN standmenuitem on stand.id=standmenuitem.standId " +
            "WHERE stand.eventId=:eventId " +
            "GROUP BY stand.id")
    LiveData<List<StandWithItemCount>> getStandsWithItemCountForEvent(long eventId);

    @Query("SELECT * FROM stand WHERE id=:id")
    LiveData<Stand> getStand(long id);

    @Query("SELECT * FROM stand WHERE eventId=:eventId")
    List<Stand> getStandsListForEvent(long eventId);

    @Insert
    long addStand(Stand stand);

    @Update
    void updateStand(Stand stand);

    @Delete
    void deleteStand(Stand stand);


    // ### STAND MENU ITEM QUERIES ###
    @Query("SELECT * FROM standmenuitem WHERE standId=:standId")
    LiveData<List<StandMenuItem>> getMenuItemsLiveDataForStand(long standId);

    @Query("SELECT * FROM standmenuitem WHERE standId=:standId")
    List<StandMenuItem> getMenuItemsListForStand(long standId);

    @Query("SELECT * FROM standmenuitem WHERE id=:menuItemId")
    LiveData<StandMenuItem> getStandMenuItemLiveData(long menuItemId);

    @Query("SELECT * FROM standmenuitem WHERE id=:menuItemId")
    StandMenuItem getStandMenuItemObject(long menuItemId);

    @Insert
    void addStandMenuItem(StandMenuItem standMenuItem);

    @Update
    void updateStandMenuItem(StandMenuItem standMenuItem);

    @Delete
    void deleteStandMenuItem(StandMenuItem standMenuItem);
}
