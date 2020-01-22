package com.hilbing.mybands.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hilbing.mybands.R;
import com.hilbing.mybands.models.Event;
import com.hilbing.mybands.models.Song;
import com.hilbing.mybands.widget.ListRemoteViewsFactory;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventAdapter extends ArrayAdapter<Event> {


    @BindView(R.id.widget_item_event_TV)
    TextView eventTV;



    private Context context;
    private List<Event> eventList;
    private String currentBandId;
    private String eventName;


    private DatabaseReference eventReference;

    public EventAdapter(Context context, List<Event> eventList, String currentBandId) {
        super(context, R.layout.item_event, eventList);
        this.context = context;
        this.eventList = eventList;
        this.currentBandId = currentBandId;


    }

    public EventAdapter(Context context, List<Event> eventList) {
        super(context, R.layout.item_event, eventList);
        this.context = context;
        this.eventList = eventList;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View listItem = inflater.inflate(R.layout.item_event, null, true);
        ButterKnife.bind(this, listItem);

        eventReference = FirebaseDatabase.getInstance().getReference().child("Events").child(currentBandId);
        eventReference.keepSynced(true);

        final Event event = eventList.get(position);
        eventName = event.getmName();

        eventTV.setText(eventName);

        return listItem;


    }

    @Override
    public int getCount() {
        return CollectionUtils.isEmpty(eventList) ? 0 :eventList.size();
    }


}
