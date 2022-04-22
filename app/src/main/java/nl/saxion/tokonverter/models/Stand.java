package nl.saxion.tokonverter.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import nl.saxion.tokonverter.EventDao;
import nl.saxion.tokonverter.JSONTranslator;

@Entity(foreignKeys = {@ForeignKey(
        entity = Event.class,
        parentColumns = "id",
        childColumns = "eventId",
        onDelete = ForeignKey.CASCADE
)})
public class Stand implements JSONTranslator {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public String baseImagePath;
    public String storedImageName;

    public long eventId;

    @Override
    public JSONObject toJson(EventDao dao) throws JSONException, InterruptedException {
        JSONObject stand = new JSONObject();
        stand.put("name", name);

        MenuItemsJsonArrayBuilder builder = new MenuItemsJsonArrayBuilder(dao);
        Thread thread = new Thread(builder);
        thread.start();
        thread.join();
        JSONArray menuItemsArray = builder.getMenuItemsJsonArray();

        stand.put("menu_items", menuItemsArray.toString());

        return stand;
    }

    private class MenuItemsJsonArrayBuilder implements Runnable {
        JSONArray menuItemsJsonArray;
        EventDao dao;

        public MenuItemsJsonArrayBuilder(EventDao givenDao) {
            dao = givenDao;
        }

        @Override
        public void run() {
            menuItemsJsonArray = new JSONArray();
            List<StandMenuItem> standMenuItems = dao.getMenuItemsListForStand(id);
            for (StandMenuItem standMenuItem : standMenuItems) {
                JSONObject standMenuItemJsonObject = null;
                try {
                    standMenuItemJsonObject = standMenuItem.toJson(dao);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (standMenuItemJsonObject != null) {
                    menuItemsJsonArray.put(standMenuItemJsonObject);
                }
            }
        }

        public JSONArray getMenuItemsJsonArray() {
            return menuItemsJsonArray;
        }
    }

    @Override
    public void fillFromJson(JSONObject jsonObject) throws JSONException {
        name = jsonObject.getString("name");
    }
}
