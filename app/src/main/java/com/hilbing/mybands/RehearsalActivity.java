package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.hilbing.mybands.models.Event;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RehearsalActivity extends AppCompatActivity {

    @BindView(R.id.rehearsal_toolbar)
    Toolbar toolbar;
    @BindView(R.id.rehearsal_events_RV)
    RecyclerView recyclerView;

    private String currentBandIdPref;
    private DatabaseReference eventsReference;
    private FirebaseRecyclerAdapter recyclerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rehearsal);

        ButterKnife.bind(this);

        SharedPreferences preferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
        currentBandIdPref = preferences.getString("currentBandIdPref", "");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.rehearsals));

        eventsReference = FirebaseDatabase.getInstance().getReference().child("Events");


        showEvents();
    }

    private void showEvents() {


        Query query = eventsReference.child(currentBandIdPref).orderByChild("mTimestamp").startAt(System.currentTimeMillis());


        Log.d("T O D A Y", String.valueOf(System.currentTimeMillis()));

        if(!TextUtils.isEmpty(query.toString())) {
            Log.d("QUERY TO STRING SHOW EVENTS INTO REHEARSALS", query.toString());

            FirebaseRecyclerOptions<Event> options = new FirebaseRecyclerOptions.Builder<Event>().setQuery(query,
                    new SnapshotParser<Event>() {
                        @NonNull
                        @Override
                        public Event parseSnapshot(@NonNull DataSnapshot snapshot) {
                            return new Event(
                                    snapshot.child("idEvent").getValue().toString(),
                                    snapshot.child("mEventType").getValue().toString(),
                                    snapshot.child("mName").getValue().toString(),
                                    snapshot.child("mDate").getValue().toString(),
                                    snapshot.child("mTime").getValue().toString(),
                                    snapshot.child("mPlace").getValue().toString(),
                                    snapshot.child("mPlaylistName").getValue().toString(),
                                    snapshot.child("idPlaylist").getValue().toString(),
                                    snapshot.child("mCurrentUser").getValue().toString(),
                                    (Long) snapshot.child("mTimestamp").getValue());

                        }
                    }).build();


            recyclerAdapter = new FirebaseRecyclerAdapter<Event, RehearsalActivity.EventViewHolder>(options) {

                @NonNull
                @Override
                public RehearsalActivity.EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_events_layout, parent, false);
                    return new RehearsalActivity.EventViewHolder(view);
                }

                @Override
                protected void onBindViewHolder(@NonNull final RehearsalActivity.EventViewHolder holder, int position, @NonNull final Event model) {
                    final String eventKey = getRef(position).getKey();

                    holder.eventTypeTV.setText(model.getmEventType());
                    holder.eventNameTV.setText(model.getmName());
                    holder.eventPlaceTV.setText(model.getmPlace());
                    holder.eventDateTV.setText(model.getmDate());
                    holder.eventTimeTV.setText(model.getmTime());
                    holder.eventPlaylistTV.setText(model.getmPlaylistName());

                    holder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //   sendUserToPersonActivity(songKey);
                            //  Toast.makeText(FindMusicianActivity.this, musicianKey, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            };

            recyclerView.setAdapter(recyclerAdapter);
            recyclerAdapter.startListening();
            recyclerAdapter.notifyDataSetChanged();
        }
        else
        {
            Toast.makeText(this, getString(R.string.no_data_available), Toast.LENGTH_SHORT).show();
        }

    }

    public class EventViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        @BindView(R.id.all_events_type_TV)
        TextView eventTypeTV;
        @BindView(R.id.all_events_name_TV)
        TextView eventNameTV;
        @BindView(R.id.all_events_place_TV)
        TextView eventPlaceTV;
        @BindView(R.id.all_events_date_TV)
        TextView eventDateTV;
        @BindView(R.id.all_events_time_TV)
        TextView eventTimeTV;
        @BindView(R.id.all_events_playlist_TV)
        TextView eventPlaylistTV;

        public EventViewHolder(@NonNull final View itemView)
        {
            super(itemView);
            mView = itemView;
            ButterKnife.bind(this, itemView);

            itemView.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View view)
                {
                    int itemClicked = getAdapterPosition();
                    Toast.makeText(RehearsalActivity.this, String.valueOf(itemClicked), Toast.LENGTH_LONG).show();

                    return false;
                }
            });

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(null != recyclerAdapter)
        {
            recyclerAdapter.stopListening();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(null != recyclerAdapter)
        {
            recyclerAdapter.startListening();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {

        int id = item.getItemId();

        if(id == android.R.id.home)
        {
            sendUserToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendUserToMainActivity()
    {
        Intent mainIntent = new Intent(RehearsalActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }
}
