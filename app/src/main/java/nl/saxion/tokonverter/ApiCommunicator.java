package nl.saxion.tokonverter;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class ApiCommunicator {
    String secret = "CoolBeans1994";
    String queryBaseUrl = "https://restest.sd42.nl/" + secret + "/events";
    RequestQueue queue;

    public ApiCommunicator(Context c) {
        queue = Volley.newRequestQueue(c);
    }


    public void postRequest(JSONObject jsonObject, final VolleyJsonObjectCallback callback) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, queryBaseUrl, jsonObject, response -> {
                    try {
                        callback.onSuccess(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    // Handle error
                    System.out.println("Error occurred while handling volley object request");
                });

        queue.add(jsonObjectRequest);
    }

    public void putRequest(Integer apiID, JSONObject jsonObject, final VolleyJsonObjectCallback callback) {
        String requestUrl = queryBaseUrl + "/" + apiID;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.PUT, requestUrl, jsonObject, response -> {
                    try {
                        callback.onSuccess(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    // Handle error
                    System.out.println("Error occurred while handling volley object request");
                });

        queue.add(jsonObjectRequest);
    }

    public void getRequest(final VolleyJsonArrayCallback callback) {

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, queryBaseUrl, null, response -> {
                    try {
                        callback.onSuccess(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    System.out.println("Error occurred while handling volley object request");
                });

        queue.add(jsonArrayRequest);
    }




}
