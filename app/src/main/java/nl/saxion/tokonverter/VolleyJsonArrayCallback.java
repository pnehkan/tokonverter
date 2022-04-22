package nl.saxion.tokonverter;

import org.json.JSONArray;
import org.json.JSONException;

public interface VolleyJsonArrayCallback {
    void onSuccess(JSONArray jsonArray) throws JSONException;
}
