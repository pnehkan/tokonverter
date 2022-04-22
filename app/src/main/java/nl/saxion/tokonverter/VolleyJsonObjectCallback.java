package nl.saxion.tokonverter;

import org.json.JSONException;
import org.json.JSONObject;

public interface VolleyJsonObjectCallback {
    void onSuccess(JSONObject jsonObject) throws JSONException;
}
