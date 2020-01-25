package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.hilbing.mybands.fragments.PlaylistsFragmentDialog;
import com.hilbing.mybands.interfaces.PlaylistClickListener;
import com.hilbing.mybands.models.Playlist;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UpdateEventActivity extends AppCompatActivity {


    @BindView(R.id.update_event_toolbar)
    Toolbar toolbar;
    @BindView(R.id.update_event_event_type_SP)
    Spinner eventTypeSP;
    @BindView(R.id.update_event_date_ET)
    EditText dateEventET;
    @BindView(R.id.update_event_date_picker_IV)
    ImageView datePickerIV;
    @BindView(R.id.update_event_time_ET)
    EditText timeEventET;
    @BindView(R.id.update_event_time_picker_IV)
    ImageView timePickerIV;
    @BindView(R.id.update_event_name_ET)
    EditText nameEventET;
    @BindView(R.id.update_event_place_ET)
    EditText placeEventET;
    @BindView(R.id.update_event_select_playlist_BT)
    Button selectPlaylistBT;
    @BindView(R.id.update_event_playlist_ET)
    EditText playlistNameET;
    @BindView(R.id.update_event_playlist_id_ET)
    EditText playlistIdET;
    @BindView(R.id.update_event_create_BT)
    Button updateEventBT;

    private String currentBandIdPref;
    private String currentUserId;
    private String eventId;
    private String idPlaylist;
    private String mDate;
    private String mEventType;
    private String mName;
    private String mPlace;
    private String mPlaylistName;
    private String mTime;
    private long timeS;
    private long mTimestamp;
    private boolean dateSelected = false;
    private boolean timeSelected = false;


    private List<Playlist> playlistsList = new ArrayList<>();

    private PlaylistClickListener clickListener;

    public PlaylistClickListener getClickListener() {
        return clickListener;
    }

    public void setClickListener(PlaylistClickListener clickListener){
        this.clickListener = clickListener;
    }

    private int mYear, mMonth, mDay, mHour, mMin, year, month, dayOfMonth, hourOfDay, min;
    private long dateInMillis, timeInMillis;

    private FirebaseAuth mAuth;
    private DatabaseReference eventsReference;
    private DatabaseReference playlistsReference;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_event);

        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();


        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.update_event));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        Intent intent = getIntent();
        eventId = intent.getStringExtra("eventKey");

        progressDialog = new ProgressDialog(this);


        SharedPreferences preferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
        currentBandIdPref = preferences.getString("currentBandIdPref", "");

        eventsReference = FirebaseDatabase.getInstance().getReference().child("Events");
        eventsReference.keepSynced(true);
        playlistsReference = FirebaseDatabase.getInstance().getReference().child("Playlists");
        playlistsReference.keepSynced(true);


        if (!TextUtils.isEmpty(currentBandIdPref)) {

            showDataFromFirebase(eventId);
        }



        datePickerIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dateSelected = true;
                setDate();
                if(dateSelected && !timeSelected){
                    Toast.makeText(UpdateEventActivity.this, getResources().getString(R.string.please_update_time_too), Toast.LENGTH_LONG).show();
                    timePickerIV.requestFocus();
                }

            }
        });

        timePickerIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dateSelected = true;
                setTime();
                if(!dateSelected && timeSelected){
                    Toast.makeText(UpdateEventActivity.this, getResources().getString(R.string.please_update_date_too), Toast.LENGTH_LONG).show();
                    datePickerIV.requestFocus();
                }
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

        updateEventBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateEvent();
            }
        });


    }



    private void showDataFromFirebase(String eventId) {

        eventsReference = FirebaseDatabase.getInstance().getReference().child("Events").child(currentBandIdPref).child(eventId);
        eventsReference.keepSynced(true);

        eventsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    idPlaylist = dataSnapshot.child("idPlaylist").getValue().toString();
                    currentUserId = dataSnapshot.child("mCurrentUser").getValue().toString();
                    mDate = dataSnapshot.child("mDate").getValue().toString();
                    mEventType = dataSnapshot.child("mEventType").getValue().toString();
                    mName = dataSnapshot.child("mName").getValue().toString();
                    mPlace = dataSnapshot.child("mPlace").getValue().toString();
                    mPlaylistName = dataSnapshot.child("mPlaylistName").getValue().toString();
                    mTime = dataSnapshot.child("mTime").getValue().toString();
                    mTimestamp = (long) dataSnapshot.child("mTimestamp").getValue();
                    Log.d("TIMESTAMP.......................", String.valueOf(mTimestamp));

                    dateEventET.setText(mDate);
                    timeEventET.setText(mTime);
                    placeEventET.setText(mPlace);
                    nameEventET.setText(mName);
                    eventTypeSP.setSelection(getIndexSpinner(eventTypeSP, mEventType));
                    playlistNameET.setText(mPlaylistName);
                    playlistIdET.setText(idPlaylist);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private int getIndexSpinner(Spinner spinner, String string) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(string)) {
                return i;
            }
        }

        return 0;
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
                        playlistsList.add(newPlaylist);
                    }

                    final PlaylistsFragmentDialog dialogFragment = PlaylistsFragmentDialog.newInstance(playlistsList, currentBandIdPref);
                    dialogFragment.setClickListener(new PlaylistClickListener() {
                        @Override
                        public void onPlaylistClick(String playlistId, String playlistName) {
                            playlistNameET.setText(playlistName);
                            playlistIdET.setText(playlistId);
                            dialogFragment.dismiss();
                            // Toast.makeText(getApplicationContext(), playlistId, Toast.LENGTH_SHORT).show();
                        }
                    });
                    dialogFragment.show(getSupportFragmentManager(), getString(R.string.add_song_to_playlist));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void updateEvent() {

        mName = nameEventET.getText().toString();
        mDate = dateEventET.getText().toString();
        mTime = timeEventET.getText().toString();
        mPlace = placeEventET.getText().toString();
        idPlaylist = playlistIdET.getText().toString();
        mEventType = eventTypeSP.getSelectedItem().toString();
        mPlaylistName = playlistNameET.getText().toString();

        if(dateInMillis == 0){
            timeS = mTimestamp;
        }
            else  {
                timeS = dateInMillis;
                Log.d("TIMESTAMP INSIDE IF FROM FIREBASE///////////", String.valueOf(timeS));
            }



        if (TextUtils.isEmpty(mName)) {
            nameEventET.setError(getResources().getString(R.string.enter_a_name_for_your_event));
            nameEventET.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(mDate)) {
            dateEventET.setError(getResources().getString(R.string.enter_event_date));
            datePickerIV.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(mTime)) {
            timeEventET.setError(getResources().getString(R.string.enter_event_time));
            timePickerIV.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(mPlace)){
            placeEventET.setError(getResources().getString(R.string.enter_a_place));
            placeEventET.requestFocus();

        }
        if (TextUtils.isEmpty(mPlaylistName)){
            playlistNameET.setError(getResources().getString(R.string.select_a_playlist));
            selectPlaylistBT.requestFocus();
        }

        else {

            progressDialog.setTitle(getResources().getString(R.string.updating_event));
            progressDialog.setMessage(getResources().getString(R.string.please_wait_while_we_are_updating_your_event));
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(true);

            HashMap eventMap = new HashMap();
            eventMap.put("idEvent", eventId);
            eventMap.put("idPlaylist", idPlaylist);
            eventMap.put("mCurrentUser", currentUserId);
            eventMap.put("mDate", mDate);
            eventMap.put("mEventType", mEventType);
            eventMap.put("mName", mName);
            eventMap.put("mPlace", mPlace);
            eventMap.put("mPlaylistName", mPlaylistName);
            eventMap.put("mTime", mTime);
            eventMap.put("mTimestamp", timeS);

            eventsReference.updateChildren(eventMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        //  sendUserToMainActivity();
                        Toast.makeText(UpdateEventActivity.this, getResources().getString(R.string.your_event_is_updated_succesfully), Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                        sendUserToMainActivity();
                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(UpdateEventActivity.this, getResources().getString(R.string.error_occurred) + ": " + message, Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                        sendUserToMainActivity();
                    }
                }
            });

        }
    }



    private void setDate() {
        final Calendar calendar = Calendar.getInstance();
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int yearDP, int  monthDP, int dayOfMonthDP) {
                year = yearDP;
                month = monthDP;
                dayOfMonth = dayOfMonthDP;
                dateEventET.setText(dayOfMonth + "/" + (month+1) + "/" + year);
            }
        }, mYear, mMonth, mDay);
        Log.d("YEAR VALUE >>>>>>>>>>>>>>>.", String.valueOf(year));
        datePickerDialog.show();

        Calendar calendarInMillis = new GregorianCalendar(year, month, dayOfMonth, hourOfDay, min);

        dateInMillis = calendarInMillis.getTimeInMillis();

    }

    private void setTime() {
        final Calendar calendar = Calendar.getInstance();
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMin = calendar.get(Calendar.MINUTE);




        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDayDP, int minDP) {
                hourOfDay = hourOfDayDP;
                min = minDP;
                timeEventET.setText(hourOfDay + ":" + min);
            }
        }, mHour, mMin, true);
        timePickerDialog.show();

        Calendar calendarInMillis = new GregorianCalendar(year, month, dayOfMonth, hourOfDay, min);

        dateInMillis = calendarInMillis.getTimeInMillis();


    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences preferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
        currentBandIdPref = preferences.getString("currentBandIdPref", "");
        if(TextUtils.isEmpty(currentBandIdPref)){
            Toast.makeText(UpdateEventActivity.this, getResources().getString(R.string.you_need_to_belong_to_a_band), Toast.LENGTH_LONG).show();
            sendUserToMainActivity();

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
        Intent mainIntent = new Intent(UpdateEventActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }




}
