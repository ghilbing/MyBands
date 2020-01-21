package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hilbing.mybands.fragments.PlaylistsFragmentDialog;
import com.hilbing.mybands.fragments.SongsFragmentDialog;
import com.hilbing.mybands.models.Event;
import com.hilbing.mybands.models.Playlist;
import com.hilbing.mybands.models.Song;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateEventActivity extends AppCompatActivity {

    @BindView(R.id.create_event_toolbar)
    Toolbar toolbar;
    @BindView(R.id.create_event_event_type_SP)
    Spinner eventTypeSP;
    @BindView(R.id.create_event_date_ET)
    EditText dateEventET;
    @BindView(R.id.create_event_date_picker_IV)
    ImageView datePickerIV;
    @BindView(R.id.create_event_time_ET)
    EditText timeEventET;
    @BindView(R.id.create_event_time_picker_IV)
    ImageView timePickerIV;
    @BindView(R.id.create_event_name_ET)
    EditText nameEventET;
    @BindView(R.id.create_event_place_ET)
    EditText placeEventET;
    @BindView(R.id.create_event_select_playlist_BT)
    Button selectPlaylistBT;
    @BindView(R.id.create_event_playlist_ET)
    EditText playlistET;
    @BindView(R.id.create_event_create_BT)
    Button createEventBT;

    private String currentBandIdPref;
    private String currentUserId;
    private List<Playlist> playlistsList = new ArrayList<>();

    private int mYear, mMonth, mDay, mHour, mMin;

    private FirebaseAuth mAuth;
    private DatabaseReference eventsReference;
    private DatabaseReference playlistsReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.create_event));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        eventsReference = FirebaseDatabase.getInstance().getReference().child("Events");
        eventsReference.keepSynced(true);
        playlistsReference = FirebaseDatabase.getInstance().getReference().child("Playlists");
        playlistsReference.keepSynced(true);


        datePickerIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDate();

            }
        });

        timePickerIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTime();
            }
        });

        timeEventET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String spinner = eventTypeSP.getSelectedItem().toString();
                nameEventET.setText(spinner + " " + dateEventET.getText().toString() + " at: " + timeEventET.getText().toString());
            }
        });

        selectPlaylistBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPlaylists();
            }
        });




        createEventBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createEvent();
            }
        });

    }

    private void showPlaylists() {


        playlistsReference.child(currentBandIdPref).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    playlistsList.clear();

                    for (DataSnapshot songSnapshot : dataSnapshot.getChildren()) {
                        Playlist playlist = songSnapshot.getValue(Playlist.class);
                        String mId = playlist.getmId();
                        String mCreator = playlist.getmCreator();
                        String mName = playlist.getmPlaylistName();
                        Playlist newPlaylist = new Playlist(mId, mName, mCreator);
                        playlistsList.add(playlist);
                    }

                    DialogFragment dialogFragment = PlaylistsFragmentDialog.newInstance(playlistsList, currentBandIdPref);
                    dialogFragment.show(getSupportFragmentManager(), getString(R.string.add_song_to_playlist));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void createEvent() {

        String place = placeEventET.getText().toString();
        String name = nameEventET.getText().toString();
        String playlist = playlistET.getText().toString();
        String time = timeEventET.getText().toString();
        String date = dateEventET.getText().toString();
        String type = eventTypeSP.getSelectedItem().toString();

        if(TextUtils.isEmpty(date)){
            dateEventET.setError(getResources().getString(R.string.enter_event_date));
            datePickerIV.requestFocus();
        }

        if(TextUtils.isEmpty(time)){
            timeEventET.setError(getResources().getString(R.string.enter_event_time));
            timePickerIV.requestFocus();

        }

        if (TextUtils.isEmpty(place))
        {
            placeEventET.setError(getResources().getString(R.string.enter_a_place));
            placeEventET.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(name)){
            nameEventET.setError(getResources().getString(R.string.enter_a_name_for_your_event));
            nameEventET.requestFocus();
        }

        if(TextUtils.isEmpty(playlist)){
            playlistET.setError(getResources().getString(R.string.select_a_playlist));
            playlistET.requestFocus();
        }
        else {

            String id = eventsReference.push().getKey();

            Event event = new Event(id, type, name, date, time, place, playlist, currentUserId);
            eventsReference.child(currentBandIdPref).child(id).setValue(event);
            dateEventET.setText("");
            timeEventET.setText("");
            placeEventET.setText("");
            nameEventET.setText("");
            playlistET.setText("");

        }


    }

    private void setTime() {
        final Calendar calendar = Calendar.getInstance();
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMin = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int min) {
                timeEventET.setText(hourOfDay + ":" + min);
            }
        }, mHour, mMin, true);
        timePickerDialog.show();


    }

    private void setDate() {
        final Calendar calendar = Calendar.getInstance();
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                dateEventET.setText(dayOfMonth + "/" + (month+1) + "/" + year);
            }
        }, mYear, mMonth, mDay);
        datePickerDialog.show();

    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences preferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
        currentBandIdPref = preferences.getString("currentBandIdPref", "");
        if(TextUtils.isEmpty(currentBandIdPref)){
            Toast.makeText(CreateEventActivity.this, getResources().getString(R.string.you_need_to_belong_to_a_band), Toast.LENGTH_LONG).show();
            sendUserToMainActivity();
        } else {
            showAlertDialog();
        }

    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.select_event));
        String[] events = {getResources().getString(R.string.rehearsal), getResources().getString(R.string.concert)};
        Log.d("CREATE EVENTS ACTIVITY", events[0] + events[1]);
        builder.setItems(events, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i){
                    case 0:
                        eventTypeSP.setSelection(0);

                    case 1:
                        eventTypeSP.setSelection(1);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();


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
        Intent mainIntent = new Intent(CreateEventActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }
}
