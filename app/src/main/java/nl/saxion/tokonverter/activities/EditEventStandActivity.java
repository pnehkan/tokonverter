package nl.saxion.tokonverter.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import nl.saxion.tokonverter.AppDatabase;
import nl.saxion.tokonverter.EventDao;
import nl.saxion.tokonverter.R;
import nl.saxion.tokonverter.models.Event;
import nl.saxion.tokonverter.models.Stand;
import nl.saxion.tokonverter.InternalStorageManager;
import nl.saxion.tokonverter.models.StandMenuItem;

public class EditEventStandActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    Intent receivedIntent;
    long eventID;
    long standID;
    Event event;
    Stand stand;
    EventDao dao;
    StandMenuItem standMenuItem;
    LinearLayout mContainerView;
    ImageView standImageView;
    EditText standNameEditText;
    Bitmap imageBitmap;
    InternalStorageManager internalStorageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event_stand);

        // See if an Intent was given
        receivedIntent = getIntent();
        if (receivedIntent == null) finish();

        // See if the Intent has extras
        eventID = receivedIntent.getLongExtra("nl.saxion.tokonverter.EDIT_STAND_EVENT_ID", -1);
        if (eventID < 0) finish();

        // Get db instance and dao
        AppDatabase db = AppDatabase.getInstance(this);
        dao =  db.eventDao();
        internalStorageManager = new InternalStorageManager();

        // Set the title on the action bar
        dao.getEvent(eventID).observe(EditEventStandActivity.this, new Observer<Event>() {
            @Override
            public void onChanged(Event _event) {
                if (_event == null) finish();
                event = _event;
                getSupportActionBar().setTitle(event.name + "stand editor");
            }
        });

        // Get necessary view elements
        standNameEditText = findViewById(R.id.standNameEditTextViewEditEventStandActivity);
        standImageView = findViewById(R.id.foodStandImageViewEditEventStandActivity);
        mContainerView = findViewById(R.id.standMenuItemsOverviewEditLayoutEditEventStandActivity);

        // Get buttons
        Button takeImageButton = findViewById(R.id.takeImageButtonEditEventStandActivity);
        Button saveButton = findViewById(R.id.saveButtonEditEventStandActivity);
        Button addRowButton = findViewById(R.id.addRowButtonEditEventStandActivity);
        Button deleteStandButton = findViewById(R.id.deleteStandButtonEditEventStandActivity);

        // See if we are editing a stand or creating a new one.
        standID = receivedIntent.getLongExtra("nl.saxion.tokonverter.EDIT_STAND_STAND_ID", -1);
        if (standID >= 0) {
            dao.getStand(standID).observe(EditEventStandActivity.this, new Observer<Stand>() {
                @Override
                public void onChanged(Stand _stand) {
                    if (_stand == null) {
                        finish();
                        return;
                    }
                    stand = _stand;
                    standNameEditText.setText(stand.name);
                    if (stand.storedImageName != null) {
                        Bitmap imageBitmap = internalStorageManager.loadBitmapFromStorage(stand.baseImagePath, stand.storedImageName);
                        standImageView.setImageBitmap(imageBitmap);
                    } else {
                        standImageView.setImageResource(R.drawable.foodtruck);
                    }

                    dao.getMenuItemsLiveDataForStand(stand.id).observe(EditEventStandActivity.this, new Observer<List<StandMenuItem>>() {
                        @Override
                        public void onChanged(List<StandMenuItem> standMenuItems) {
                            mContainerView.removeAllViews();

                            for (StandMenuItem standMenuItem: standMenuItems) {
                                if (standMenuItem == null) continue;
                                inflateEditRow(standMenuItem);
                            }
                        }
                    });

                }
            });
        } else {
            deleteStandButton.setVisibility(View.GONE);
            stand = new Stand();
            standImageView.setImageResource(R.drawable.foodtruck);
            inflateEditRow(null); inflateEditRow(null);
        }

        addRowButton.setOnClickListener(v -> inflateEditRow(null));

        takeImageButton.setOnClickListener(v -> dispatchTakePictureIntent());

        deleteStandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(() -> dao.deleteStand(stand)).start();
                finish();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean allFieldsValid = validateOnSubmit();

                if (allFieldsValid) {
                    // First, set the general information of the stand.
                    String standName = standNameEditText.getText().toString();

                    // Save the bitmap to internal storage and get the "parent" path
                    String saveName = standName + (new Random().nextInt()) + ".png";
                    if (imageBitmap != null) {
                        stand.baseImagePath = internalStorageManager.saveToInternalStorage(EditEventStandActivity.this, imageBitmap, saveName);
                        stand.storedImageName = saveName;
                    }

                    stand.name = standName;
                    stand.eventId = eventID;

                    // Add the stand to the DB and wait for the ID to be returned.
                    Thread thread = new Thread(() -> {
                        if (stand.id <= 0) {
                            stand.id = dao.addStand(stand);
                        } else {
                            dao.updateStand(stand);
                        }
                        addOrUpdateMenuItems();
                    });
                    thread.start();
                    finish();
                }
            }
        });
    }

    private boolean validateOnSubmit() {
        boolean valid = true;
        String err = "This field is required";
        if (standNameEditText.length() == 0) {
            standNameEditText.setError(err);
            valid = false;
        }
        return valid;
    }

    private void addOrUpdateMenuItems() {
        // Dive into the menu.
        for (int i=0; i<mContainerView.getChildCount(); i++) {
            View row = mContainerView.getChildAt(i);

            EditText nameEditText = row.findViewById(R.id.editMenuItemEditTextEditableRowView);
            String menuItemName = nameEditText.getText().toString();
            TextView menuItemIdTextView = row.findViewById(R.id.secretMenuItemIDTextViewEditableRowView);
            String menuItemIDString = menuItemIdTextView.getText().toString();
            EditText costEditText = row.findViewById(R.id.tokenCostEditTextNumberDecimalEditableRowView);

            // Only add if the fieldname was NOT left empty
            if (!menuItemName.matches("")){

                if (!menuItemIDString.equals("")) {
                    long menuItemID = Long.parseLong(menuItemIDString);

                    standMenuItem = dao.getStandMenuItemObject(menuItemID);
                    standMenuItem.name = nameEditText.getText().toString();
                    standMenuItem.tokenCost = Double.parseDouble(costEditText.getText().toString());
                    new Thread(() -> dao.updateStandMenuItem(standMenuItem)).start();
                } else {
                    standMenuItem = new StandMenuItem();
                    standMenuItem.name = nameEditText.getText().toString();
                    standMenuItem.tokenCost = Double.parseDouble(costEditText.getText().toString());
                    standMenuItem.standId = stand.id;

                    new Thread(() -> dao.addStandMenuItem(standMenuItem)).start();
                }
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Error: Camera unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imageBitmap = (Bitmap) data.getExtras().get("data");
            standImageView.setImageBitmap(imageBitmap);
        }
    }

    private void inflateEditRow(StandMenuItem standMenuItem) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.editable_row_view, null);

        if (standMenuItem != null) {
            EditText menuItemName = rowView.findViewById(R.id.editMenuItemEditTextEditableRowView);
            EditText menuItemCost = rowView.findViewById(R.id.tokenCostEditTextNumberDecimalEditableRowView);
            TextView secretIDView = rowView.findViewById(R.id.secretMenuItemIDTextViewEditableRowView);
            menuItemName.setText(standMenuItem.name);
            menuItemCost.setText(String.format("%.2f", standMenuItem.tokenCost));
            secretIDView.setText("" + standMenuItem.id);
        }

        ImageButton deleteButton = rowView.findViewById(R.id.deleteRowButtonEditableRowView);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView menuItemIdTextView = rowView.findViewById(R.id.secretMenuItemIDTextViewEditableRowView);
                String menuItemIDString = menuItemIdTextView.getText().toString();

                // Remove from the database if the ID was set
                if (!menuItemIDString.equals("")){
                    long menuItemID = Long.parseLong(menuItemIDString);
                    dao.getStandMenuItemLiveData(menuItemID).observe(EditEventStandActivity.this, new Observer<StandMenuItem>() {
                        @Override
                        public void onChanged(StandMenuItem standMenuItem) {
                            if (standMenuItem == null) return;
                            new Thread(() -> dao.deleteStandMenuItem(standMenuItem)).start();
                        }
                    });
                }

                // Remove the editable row by calling the getParent on button
                mContainerView.removeView((View) v.getParent());
            }
        });

        // Finally, add the view to the containerview to that we can see it.
        mContainerView.addView(rowView);
    }



    @Override
    protected void onSaveInstanceState(@NonNull Bundle outstate) {
        super.onSaveInstanceState(outstate);

        outstate.putString("standName", standNameEditText.getText().toString());

        ArrayList<String> itemNames = new ArrayList<>();
        ArrayList<String> itemIDs = new ArrayList<>();
        ArrayList<String> itemCosts = new ArrayList<>();

        for (int i=0; i<mContainerView.getChildCount(); i++) {
            View row = mContainerView.getChildAt(i);

            EditText nameEditText = row.findViewById(R.id.editMenuItemEditTextEditableRowView);
            TextView menuItemIdTextView = row.findViewById(R.id.secretMenuItemIDTextViewEditableRowView);
            EditText costEditText = row.findViewById(R.id.tokenCostEditTextNumberDecimalEditableRowView);

            itemNames.add(nameEditText.getText().toString());
            itemIDs.add(menuItemIdTextView.getText().toString());
            itemCosts.add(costEditText.getText().toString());
        }

        outstate.putStringArrayList("itemNames", itemNames);
        outstate.putStringArrayList("itemIDs", itemIDs);
        outstate.putStringArrayList("itemCosts", itemCosts);

    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        standNameEditText.setText(savedInstanceState.get("standName").toString());

        ArrayList<String> itemNames = savedInstanceState.getStringArrayList("itemNames");
        ArrayList<String> itemIDs = savedInstanceState.getStringArrayList("itemIDs");
        ArrayList<String> itemCosts = savedInstanceState.getStringArrayList("itemCosts");


        for (int i=0; i<mContainerView.getChildCount(); i++) {
            View row = mContainerView.getChildAt(i);

            EditText nameEditText = row.findViewById(R.id.editMenuItemEditTextEditableRowView);
            TextView menuItemIdTextView = row.findViewById(R.id.secretMenuItemIDTextViewEditableRowView);
            EditText costEditText = row.findViewById(R.id.tokenCostEditTextNumberDecimalEditableRowView);

            nameEditText.setText(itemNames.get(i));
            menuItemIdTextView.setText(itemIDs.get(i));
            costEditText.setText(itemCosts.get(i));
        }
    }
}