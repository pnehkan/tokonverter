package nl.saxion.tokonverter.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import nl.saxion.tokonverter.ApiCommunicator;
import nl.saxion.tokonverter.AppDatabase;
import nl.saxion.tokonverter.EventAdapter;
import nl.saxion.tokonverter.EventDao;
import nl.saxion.tokonverter.R;
import nl.saxion.tokonverter.VolleyJsonArrayCallback;
import nl.saxion.tokonverter.models.Event;
import nl.saxion.tokonverter.models.Stand;
import nl.saxion.tokonverter.models.StandMenuItem;

public class SearchEventActivity extends AppCompatActivity {

    Button addEventManuallyButton;
    ApiCommunicator apiCommunicator;
    JSONArray foundEvents;
    EventDao dao;
    EditText searchEventEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_event);
        getSupportActionBar().setTitle("Add Event");

        // Get database access
        AppDatabase db = AppDatabase.getInstance(this);
        dao = db.eventDao();

        // Get the communicator for the API and the view elements
        apiCommunicator = new ApiCommunicator(this);

        // Get necessary view elements
        searchEventEditText = findViewById(R.id.searchEventEditTextSearchEventActivity);
        ProgressBar progressBar = findViewById(R.id.progressBarSearchEventsSearchEventActivity);

        // Get buttons
        ImageButton searchButton = findViewById(R.id.searchImageButtonSearchEventActivity);
        addEventManuallyButton = findViewById(R.id.addEventManuallyButtonSearchEventActivity);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                foundEvents = new JSONArray();
                String query = searchEventEditText.getText().toString().toLowerCase().trim();

                apiCommunicator.getRequest(new VolleyJsonArrayCallback() {
                    @Override
                    public void onSuccess(JSONArray jsonArray) throws JSONException {
                        progressBar.setVisibility(View.GONE);
                        List<Event> eventList = selectAndParseJSON(jsonArray, query);
                        if (eventList.size() > 0) {
                            displayResults(eventList);
                        } else {
                            Toast.makeText(SearchEventActivity.this, "No events were found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });


        addEventManuallyButton.setOnClickListener(v -> {
            Intent intent = new Intent(SearchEventActivity.this, EditEventManuallyActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private List<Event> selectAndParseJSON(JSONArray arr, String query) throws JSONException {
        Event event;
        List<Event> eventList = new ArrayList<>();
        JSONObject eventJSONObject = null;
        String name;

        if (arr != null) {
            for (int i=0; i < arr.length(); i++) {
                try {
                    eventJSONObject = arr.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (eventJSONObject != null) {
                    // See if the name contains the query
                    name = eventJSONObject.getString("name");
                    foundEvents.put(eventJSONObject);
                    if (name.toLowerCase().contains(query)) {
                        event = new Event();
                        event.fillFromJson(eventJSONObject);
                        eventList.add(event);
                    }
                }
            }
        }
        return eventList;
    }

    private void displayResults(List<Event> events) {
        ListView resultView = findViewById(R.id.eventSearchResultListViewSearchEventActivity);
        EventAdapter eventAdapter = new EventAdapter(this, events);
        resultView.setAdapter(eventAdapter);

        resultView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event event = eventAdapter.getItem(position);
                JSONObject eventJSON = null;
                try {
                    eventJSON = foundEvents.getJSONObject(position);
                    ResultListItemClickHandler resultListItemClickHandler = new ResultListItemClickHandler(event, eventJSON);
                    new Thread(resultListItemClickHandler).start();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private class ResultListItemClickHandler implements Runnable {
        Event event;
        JSONObject eventJSON;

        public ResultListItemClickHandler(Event givenEvent, JSONObject givenEventJSON) {
            event = givenEvent;
            eventJSON = givenEventJSON;
        }

        @Override
        public void run() {
            try {
                addEventToDatabase();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Intent viewEventIntent = new Intent(getApplicationContext(), ViewEventDetailsActivity.class);
            viewEventIntent.putExtra("nl.saxion.tokonverter.SHOW_EVENT_ID", event.id);
            startActivity(viewEventIntent);
            finish();
        }

        private void addEventToDatabase() throws JSONException {

            // Add the event to the database.
            event.id = dao.addEvent(event);

            JSONArray standJsonArray = new JSONArray(eventJSON.getString("stands"));
            if (standJsonArray.length() > 0) {
                JSONObject standJSONObject;

                // Now, loop over the stands for this event
                for (int i=0; i < standJsonArray.length(); i++) {
                    standJSONObject = standJsonArray.getJSONObject(i);
                    Stand stand = new Stand();
                    stand.fillFromJson(standJSONObject);
                    stand.eventId = event.id;
                    stand.id = dao.addStand(stand);

                    JSONArray standMenuItemsJsonArray = new JSONArray(standJSONObject.getString("menu_items"));
                    if (standMenuItemsJsonArray.length() > 0) {
                        JSONObject standMenuItemJSONObject;

                        // And loop over the menu items for the current stand
                        for (int j=0; j < standMenuItemsJsonArray.length(); j++) {
                            standMenuItemJSONObject = standMenuItemsJsonArray.getJSONObject(i);
                            StandMenuItem standMenuItem = new StandMenuItem();
                            standMenuItem.fillFromJson(standMenuItemJSONObject);
                            standMenuItem.standId = stand.id;
                            dao.addStandMenuItem(standMenuItem);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outstate) {
        super.onSaveInstanceState(outstate);
        outstate.putString("query", searchEventEditText.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        searchEventEditText.setText(savedInstanceState.get("query").toString());
    }

}
