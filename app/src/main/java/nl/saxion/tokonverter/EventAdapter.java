package nl.saxion.tokonverter;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import nl.saxion.tokonverter.models.Event;

public class EventAdapter extends BaseAdapter {
    LayoutInflater mInflater;
    Context context;
    List<Event> events;

    public EventAdapter(Context c, List<Event> e) {
        events = e;
        context = c;
        mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public Event getItem(int position) {
        return events.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Event event = events.get(position);

        // Check if view can be recycled
        View v;
        if (convertView == null) v=mInflater.inflate(R.layout.event_list_item, null);
        else v = convertView;

        // Retrieve layout elements and fill them
        ImageView imageView = v.findViewById(R.id.eventImageViewEventListItem);
        event.setImage(imageView);

        TextView eventName = v.findViewById(R.id.eventNameTextViewEventListItem);
        eventName.setText(event.name);

        TextView eventDate = v.findViewById(R.id.eventDateTextViewEventListItem);
        eventDate.setText(event.date);

        return v;
    }


}
