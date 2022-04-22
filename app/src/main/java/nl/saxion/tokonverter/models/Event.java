package nl.saxion.tokonverter.models;

import android.widget.ImageView;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import nl.saxion.tokonverter.EventDao;
import nl.saxion.tokonverter.JSONTranslator;
import nl.saxion.tokonverter.R;

@Entity
public class Event implements JSONTranslator {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public String date;
    public String imgUrl = null;
    public double currency;
    public Integer apiId = null;

    public void setImage(ImageView imageView) {
        if (imgUrl != null) {
            Picasso.get().load(imgUrl).placeholder(R.drawable.downloading).error(R.drawable.error).into(imageView);
        } else {
            imageView.setImageResource(R.drawable.party);
        }
    }

    @Override
    public JSONObject toJson(EventDao dao) throws JSONException, InterruptedException {
        JSONObject event = new JSONObject();
        if (apiId != null) {
            event.put("id", apiId);
        }

        event.put("name", name);
        event.put("date", date);
        event.put("imgUrl", imgUrl);
        event.put("currency", currency);

        StandsJsonArrayBuilder builder = new StandsJsonArrayBuilder(dao);
        Thread thread = new Thread(builder);
        thread.start();
        thread.join();
        JSONArray standsArray = builder.getStandsArray();

        event.put("stands", standsArray.toString());

        return event;
    }

    private class StandsJsonArrayBuilder implements Runnable {
        JSONArray standJsonArray;
        EventDao dao;

        public StandsJsonArrayBuilder(EventDao givenDao) {
            dao = givenDao;
        }

        @Override
        public void run() {
            standJsonArray = new JSONArray();
            List<Stand> stands = dao.getStandsListForEvent(id);
            for (Stand stand : stands) {
                JSONObject standJsonObject = null;
                try {
                    standJsonObject = stand.toJson(dao);
                } catch (JSONException | InterruptedException e) {
                    e.printStackTrace();
                }
                if (standJsonObject != null) {
                    standJsonArray.put(standJsonObject);
                }
            }
        }

        public JSONArray getStandsArray() {
            return standJsonArray;
        }
    }

    @Override
    public void fillFromJson(JSONObject jsonObject) throws JSONException {
        apiId = jsonObject.getInt("id");
        name = jsonObject.getString("name");
        if (jsonObject.has("date")){
            date = jsonObject.getString("date");
        }
        if (jsonObject.has("image")) {
            imgUrl = jsonObject.getString("image");
        }
        currency = jsonObject.getDouble("currency");
    }
}
