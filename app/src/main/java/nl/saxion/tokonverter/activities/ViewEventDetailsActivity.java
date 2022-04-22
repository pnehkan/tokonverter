package nl.saxion.tokonverter.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import nl.saxion.tokonverter.ApiCommunicator;
import nl.saxion.tokonverter.AppDatabase;
import nl.saxion.tokonverter.EventDao;
import nl.saxion.tokonverter.R;
import nl.saxion.tokonverter.InternalStorageManager;
import nl.saxion.tokonverter.VolleyJsonObjectCallback;
import nl.saxion.tokonverter.models.Event;
import nl.saxion.tokonverter.models.StandMenuItem;

public class ViewEventDetailsActivity extends AppCompatActivity {

    Event event;
    EventDao dao;
    LayoutInflater inflater;
    InternalStorageManager internalStorageManager;
    ApiCommunicator apiCommunicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event_details);

        // Get database access
        AppDatabase db = AppDatabase.getInstance(this);
        dao = db.eventDao();

        // Get API and storage access
        internalStorageManager = new InternalStorageManager();
        apiCommunicator = new ApiCommunicator(this);

        // Finish if we did not receive an intent
        Intent receivedIntent;
        receivedIntent = getIntent();
        if (receivedIntent == null) finish();

        // Finish if we do not know the event id
        long eventID = receivedIntent.getLongExtra("nl.saxion.tokonverter.SHOW_EVENT_ID", -1);
        if (eventID < 0) finish();

        // Get the event details, inflate the layout and set actionbar title
        dao.getEvent(eventID).observe(this, new Observer<Event>() {
            @Override
            public void onChanged(Event _event) {
                if (_event == null) {
                    finish();
                }
                event = _event;
                inflateStandLayout();

                getSupportActionBar().setTitle(event.name);
            }
        });

        // Get button for adding a new stand
        FloatingActionButton floatingActionButton = findViewById(R.id.addNewStandFloatingActionButtonViewEventDetailsActivity);
        floatingActionButton.setOnClickListener(v -> {
            Intent startIntent = new Intent(ViewEventDetailsActivity.this, EditEventStandActivity.class);
            startIntent.putExtra("nl.saxion.tokonverter.EDIT_STAND_EVENT_ID", event.id);
            startActivity(startIntent);
        });
    }

    public void inflateStandLayout() {
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout mContainerView = findViewById(R.id.standOverviewLinearLayoutViewEventDetailsActivity);

        // Get the stands with item count for the event
        dao.getStandsWithItemCountForEvent(event.id).observe(ViewEventDetailsActivity.this, new Observer<List<EventDao.StandWithItemCount>>() {
            @Override
            public void onChanged(List<EventDao.StandWithItemCount> standsWithItemCounts) {

                // Remove all views so that we are starting fresh
                mContainerView.removeAllViews();

                // For each stand in the event, set up a row
                for (EventDao.StandWithItemCount stand: standsWithItemCounts) {
                    // Retrieve the view to be inflated
                    View row = inflater.inflate(R.layout.stand_info_header_and_content, null);

                    // Get the view elements
                    TextView standHeaderTextView = row.findViewById(R.id.standNameHeaderTextViewStandInfoHeaderAndContent);
                    TextView numMenuItems = row.findViewById(R.id.numberOfMenuItemsForStandStandInfoHeaderAndContent);
                    ImageView standHeaderImageView = row.findViewById(R.id.standHeaderImageViewStandInfoHeaderAndContent);

                    // Set the values of each view element
                    standHeaderTextView.setText(stand.name);

                    if (stand.numItems == 1) numMenuItems.setText(stand.numItems + " item");
                    else numMenuItems.setText(stand.numItems + " items");

                    if (stand.storedImageName != null) {
                        Bitmap bitmapImage = internalStorageManager.loadBitmapFromStorage(stand.baseImagePath, stand.storedImageName);
                        if (bitmapImage != null) standHeaderImageView.setImageBitmap(bitmapImage);
                        else standHeaderImageView.setImageResource(R.drawable.foodtruck);
                    } else {
                        standHeaderImageView.setImageResource(R.drawable.foodtruck);
                    }

                    // Set an onclicklistener on the entire row, so that it can be edited
                    //      when clicked. This saves an extra pencil icon which might be confusing if there are a lot.
                    row.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent startIntent = new Intent(ViewEventDetailsActivity.this, EditEventStandActivity.class);
                            startIntent.putExtra("nl.saxion.tokonverter.EDIT_STAND_EVENT_ID", event.id);
                            startIntent.putExtra("nl.saxion.tokonverter.EDIT_STAND_STAND_ID", stand.id);
                            startActivity(startIntent);
                        }
                    });

                    // Finally, inflate the menu of this stand
                    inflateStandMenuLayout(stand.id, row);

                    // And add the view to the containerview.
                    mContainerView.addView(row);
                }
            }
        });
    }

    private void inflateStandMenuLayout(long id, View standRow) {
        LinearLayout mContainerView = standRow.findViewById(R.id.menuStandItemsLinearLayout);

        dao.getMenuItemsLiveDataForStand(id).observe(ViewEventDetailsActivity.this, new Observer<List<StandMenuItem>>() {
            @Override
            public void onChanged(List<StandMenuItem> standMenuItems) {
                mContainerView.removeAllViews();

                for (StandMenuItem standMenuItem : standMenuItems) {
                    View menuItemRow = inflater.inflate(R.layout.stand_menu_item, null);

                    TextView menuNameTextView = menuItemRow.findViewById(R.id.nameTextViewStandMenuItem);
                    TextView menuTokenCostTextView = menuItemRow.findViewById(R.id.tokenCostTextViewStandMenuItem);
                    TextView menuEuroCostTextView = menuItemRow.findViewById(R.id.euroCostTextView);

                    menuNameTextView.setText(standMenuItem.name);
                    menuTokenCostTextView.setText(String.format("%.2f", standMenuItem.tokenCost));

                    Double euroCost = standMenuItem.tokenCost * event.currency;
                    menuEuroCostTextView.setText(String.format("%.2f", euroCost));

                    mContainerView.addView(menuItemRow);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Finish if the event was deleted in the returning activity
        if (resultCode == 2) {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar.
        getMenuInflater().inflate(R.menu.event_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.editEventMenuButton:
                Intent intent = new Intent(this, EditEventManuallyActivity.class);

                intent.putExtra("nl.saxion.tokonverter.EDIT_EVENT_ID", event.id);
                startActivityForResult(intent, 1);

                return(true);

            case R.id.uploadToAPIMenuButton:

                JSONObject eventJsonObject;

                try {
                    eventJsonObject = event.toJson(dao);
                } catch (JSONException | InterruptedException e) {
                    e.printStackTrace();
                    return false;
                }
                if (eventJsonObject != null) {
                    if (event.apiId == null) {
                        apiCommunicator.postRequest(eventJsonObject, new VolleyJsonObjectCallback() {
                            @Override
                            public void onSuccess(JSONObject response) throws JSONException {
                                event.apiId = (Integer) response.get("id");

                                //TODO : fix the below attempt to update the event, it does not work.
                                new Thread(() -> dao.updateEvent(event));

                                Toast.makeText(ViewEventDetailsActivity.this, "Successfully added to API", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        apiCommunicator.putRequest(event.apiId, eventJsonObject, new VolleyJsonObjectCallback() {
                            @Override
                            public void onSuccess(JSONObject jsonObject) {
                                Toast.makeText(ViewEventDetailsActivity.this, "Successfully updated in API", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                return(true);
        }
        return (super.onOptionsItemSelected(item));
    }


}