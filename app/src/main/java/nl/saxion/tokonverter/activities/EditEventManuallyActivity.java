package nl.saxion.tokonverter.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import nl.saxion.tokonverter.EventDao;
import nl.saxion.tokonverter.AppDatabase;
import nl.saxion.tokonverter.R;
import nl.saxion.tokonverter.models.Event;


public class EditEventManuallyActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    Button saveButton;
    TextView datePickerTextView;
    EditText eventNameEditText, eventImageURL, eventCurrency;
    Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manually_edit_event);

        AppDatabase db = AppDatabase.getInstance(this);
        EventDao dao = db.eventDao();

        // Get View elements
        Button deleteButton = findViewById(R.id.deleteButtonEditEventManuallyActivity);
        eventNameEditText = findViewById(R.id.eventNameEditTextEditEventManuallyActivity);
        eventImageURL = findViewById(R.id.imageURLEditTextViewEditEventManuallyActivity);
        eventCurrency = findViewById(R.id.currencyEditTextEditEventManuallyActivity);
        datePickerTextView = findViewById(R.id.eventDateTextViewForDatePickerEditEventManuallyActivity);
        datePickerTextView.setOnClickListener(v -> showDatePickerDialog());


        // See if intent is set
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        // See if we are adding a new event or editing one.
        long id = intent.getLongExtra("nl.saxion.tokonverter.EDIT_EVENT_ID", -1);
        if (id >= 0) {
            dao.getEvent(id).observe(EditEventManuallyActivity.this, new Observer<Event>() {
                @Override
                public void onChanged(Event _event) {
                    if (_event == null) {
                        finish();
                        return;
                    }
                    event = _event;
                    eventNameEditText.setText(event.name);
                    datePickerTextView.setText(event.date);
                    eventImageURL.setText(event.imgUrl);
                    eventCurrency.setText(Double.toString(event.currency));
                    getSupportActionBar().setTitle("Editing " + event.name);
                }
            });
        } else {
            deleteButton.setVisibility(View.GONE);
            getSupportActionBar().setTitle("Add Event Manually");
            event = new Event();
        }

        deleteButton.setOnClickListener(v -> {
            new Thread(() -> dao.deleteEvent(event)).start();
            setResult(2);
            finish();
        });

        saveButton = findViewById(R.id.saveEventButtonEditEventManuallyActivity);
        saveButton.setOnClickListener(v -> {
            boolean allFieldsValid = validateOnSubmit();

            if (allFieldsValid) {
                event.name = eventNameEditText.getText().toString();
                event.date = datePickerTextView.getText().toString();
                event.currency = Double.parseDouble(eventCurrency.getText().toString());

                String url = eventImageURL.getText().toString();
                if (url.toLowerCase().endsWith(".jpg") || url.toLowerCase().endsWith(".jpeg")) {
                    event.imgUrl = url;
                } else {
                    Toast.makeText(this, "Only JPG allowed. Using standard image.", Toast.LENGTH_LONG).show();
                }

                if (id < 0){
                    new Thread(() -> dao.addEvent(event)).start();
                } else {
                    new Thread(() -> dao.updateEvent(event)).start();
                }
                finish();
            }
        });
    }

    private boolean validateOnSubmit() {
        boolean valid = true;
        String err = "This field is required";
        if (eventNameEditText.length() == 0) {
            eventNameEditText.setError(err);
            valid = false;
        }
        if (eventCurrency.length() == 0) {
            eventCurrency.setError(err);
            valid = false;
        }
        return valid;
    }

    private void showDatePickerDialog() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, this,
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String date = dayOfMonth + "-" + month + "-" + year;
        datePickerTextView.setText(date);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outstate) {
        super.onSaveInstanceState(outstate);

        outstate.putString("date", datePickerTextView.getText().toString());
        outstate.putString("eventName", eventNameEditText.getText().toString());
        outstate.putString("eventImageUrl", eventImageURL.getText().toString());
        outstate.putString("eventCurrency", eventCurrency.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        datePickerTextView.setText(savedInstanceState.get("date").toString());
        eventNameEditText.setText(savedInstanceState.get("eventName").toString());
        eventImageURL.setText(savedInstanceState.get("eventImageUrl").toString());
        eventCurrency.setText(savedInstanceState.get("eventCurrency").toString());
    }
}