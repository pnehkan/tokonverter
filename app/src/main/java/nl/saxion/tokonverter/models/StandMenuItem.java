package nl.saxion.tokonverter.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

import nl.saxion.tokonverter.EventDao;
import nl.saxion.tokonverter.JSONTranslator;

@Entity(foreignKeys = {@ForeignKey(
        entity = Stand.class,
        parentColumns = "id",
        childColumns = "standId",
        onDelete = ForeignKey.CASCADE
)})
public class StandMenuItem implements JSONTranslator {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public double tokenCost;

    public long standId;

    @Override
    public JSONObject toJson(EventDao dao) throws JSONException {
        JSONObject menuItem = new JSONObject();
        menuItem.put("name", name);
        menuItem.put("token_cost", tokenCost);

        return menuItem;
    }

    @Override
    public void fillFromJson(JSONObject jsonObject) throws JSONException {
        name = jsonObject.getString("name");
        tokenCost = jsonObject.getDouble("token_cost");
    }
}
