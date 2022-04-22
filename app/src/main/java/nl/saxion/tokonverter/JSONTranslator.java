package nl.saxion.tokonverter;

import org.json.JSONException;
import org.json.JSONObject;

public interface JSONTranslator {
    JSONObject toJson(EventDao dao) throws JSONException, InterruptedException;
    void fillFromJson(JSONObject jsonObject) throws JSONException;
}
