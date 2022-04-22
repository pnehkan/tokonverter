package nl.saxion.tokonverter.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import nl.saxion.tokonverter.EventAdapter;
import nl.saxion.tokonverter.EventDao;
import nl.saxion.tokonverter.AppDatabase;
import nl.saxion.tokonverter.R;
import nl.saxion.tokonverter.models.Event;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppDatabase db = AppDatabase.getInstance(this);
        EventDao dao = db.eventDao();

        ListView eventListView = findViewById(R.id.eventOverviewListViewMainActivity);

        dao.getAllEvents().observe(this, new Observer<List<Event>>() {
            @Override
            public void onChanged(List<Event> events) {
                EventAdapter eventAdapter = new EventAdapter(MainActivity.this, events);
                eventListView.setAdapter(eventAdapter);
            }
        });

        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ViewEventDetailsActivity.class);
                Event event = (Event) eventListView.getAdapter().getItem(position);
                intent.putExtra("nl.saxion.tokonverter.SHOW_EVENT_ID", event.id);
                startActivity(intent);
            }
        });


        addButton = findViewById(R.id.addEventFloatingActionButtonMainActivity);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchEventActivity.class);
            startActivity(intent);
        });
    }

}