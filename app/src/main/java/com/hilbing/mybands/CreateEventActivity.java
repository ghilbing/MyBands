package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hilbing.mybands.fragments.EventMapFragment;
import com.hilbing.mybands.fragments.PlaylistsFragmentDialog;
import com.hilbing.mybands.interfaces.PlaylistClickListener;
import com.hilbing.mybands.models.Event;
import com.hilbing.mybands.models.Playlist;
import com.hilbing.mybands.utils.HttpDataHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateEventActivity extends AppCompatActivity  {
    public static final String TAG = CreateEventActivity.class.getCanonicalName();

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
    EditText playlistNameET;
    @BindView(R.id.create_event_playlist_id_ET)
    EditText playlistIdET;
    @BindView(R.id.create_event_create_BT)
    Button createEventBT;
    @BindView(R.id.google_maps_IV)
    ImageView googleMapsIV;

    private String currentBandIdPref;
    private String currentUserId;
    private List<Playlist> playlistsList = new ArrayList<>();
    private boolean savedInstanceStateDone;
    private ProgressDialog progressDialog;
    private double lat;
    private double lng;

    private PlaylistClickListener clickListener;

    private FragmentManager fragmentManager = getSupportFragmentManager();
    private EventMapFragment eventMapFragmentDialog = new EventMapFragment();
    private static final int ERROR_DIALOG_REQUEST = 9001;


    public PlaylistClickListener getClickListener() {
        return clickListener;
    }

    public void setClickListener(PlaylistClickListener clickListener){
        this.clickListener = clickListener;
    }

    private int mYear, mMonth, mDay, mHour, mMin, year, month, dayOfMonth, hourOfDay, min;
    private long dateInMillis, timeInMillis;
    private String addressLine;
    private double addressLat;
    private double addressLng;

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

        Intent googleMaps = getIntent();
        if(!TextUtils.isEmpty(googleMaps.getExtras().toString())){
            addressLine = googleMaps.getExtras().getString("addressLine");
            addressLat = googleMaps.getDoubleExtra("addressLat",0.0);
            addressLng = googleMaps.getDoubleExtra("addressLng", 0.0);
            playlistIdET.setText(addressLine);
            Log.i(TAG, googleMaps.getExtras().toString());
        }


        SharedPreferences preferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
        currentBandIdPref = preferences.getString("currentBandIdPref", "");

        eventsReference = FirebaseDatabase.getInstance().getReference().child("Events");
        eventsReference.keepSynced(true);
        playlistsReference = FirebaseDatabase.getInstance().getReference().child("Playlists");
        playlistsReference.keepSynced(true);

        progressDialog = new ProgressDialog(this);


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
                if(!savedInstanceStateDone) {
                    showPlaylists();
                }
            }
        });


        createEventBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createEvent();
            }
        });



        //Google maps




        googleMapsIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             //   if(isServiceOK()){
                    init();
              //  }

                /*String place = placeEventET.getText().toString();
                if (!TextUtils.isEmpty(place)) {

                    new ShowMap().execute(place);
                    // DialogFragment dialogFragment = EventMapFragment.newInstance();
                    // dialogFragment.show(getSupportFragmentManager(), getString(R.string.add_song_to_playlist));
                }*/
            }
        });

    }

    private void init(){
        Intent intent = new Intent(CreateEventActivity.this, MapActivity.class);
        startActivity(intent);
    }

    public boolean isServiceOK(){
        Log.d("", "isServiceOK: checking google services version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(CreateEventActivity.this);
        if(available == ConnectionResult.SUCCESS){
            //
            Log.d("", "isServicesOK: Google Play Services is Working");
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Log.d("", "isServiceOk: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(CreateEventActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "We can't make map request", Toast.LENGTH_LONG).show();
        }
        return false;


    }

    private class ShowMap extends AsyncTask<String, Void, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage(getResources().getString(R.string.please_wait));
            progressDialog.setCanceledOnTouchOutside(false);
           // progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String response;
            try{
                String address = strings[0];
                HttpDataHandler httpDataHandler = new HttpDataHandler();
                String url = String.format(getResources().getString(R.string.maps_googleapis), address);
                response = httpDataHandler.getHTTPData(url);
                return response;


            } catch (Exception e) {
                e.printStackTrace();
            }
           return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try{
                JSONObject jsonObject = new JSONObject(s);
                String latitude = ((JSONArray)jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry")
                        .getJSONObject("location").get("lat").toString();
                String longitude = ((JSONArray)jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry")
                        .getJSONObject("location").get("lng").toString();
                Log.d("GEOLOCATION . . . . . . . . . ", latitude + " " + longitude);

                lat = Double.valueOf(latitude);
                lng = Double.valueOf(longitude);
                Toast.makeText(CreateEventActivity.this, latitude + " " + longitude, Toast.LENGTH_LONG).show();

                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }



            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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


    private void createEvent() {

        String place = placeEventET.getText().toString();
        String name = nameEventET.getText().toString();
        String playlist = playlistNameET.getText().toString();
        String playlistId = playlistIdET.getText().toString();
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
            playlistNameET.setError(getResources().getString(R.string.select_a_playlist));
            selectPlaylistBT.requestFocus();
        }
        else {

            String id = eventsReference.push().getKey();

            Event event = new Event(id, type, name, date, time, place, playlist, playlistId, currentUserId, dateInMillis);
            eventsReference.child(currentBandIdPref).child(id).setValue(event);
            dateEventET.setText("");
            timeEventET.setText("");
            placeEventET.setText("");
            nameEventET.setText("");
            playlistNameET.setText("");
            playlistIdET.setText("");

        }


    }

    private String getDate(long time){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("EEE MMM dd hh:mm:ss yyyy", cal).toString();
        return date;
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
                dateEventET.setText((month+1) + "/" + dayOfMonth + "/" + year);
            }
        }, mYear, mMonth, mDay);
        datePickerDialog.show();

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
        String date = DateFormat.format("dd/MM/yyyy hh:mm", dateInMillis).toString();

    }



    @Override
    protected void onStart() {
        super.onStart();
        savedInstanceStateDone = false;
        SharedPreferences preferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
        currentBandIdPref = preferences.getString("currentBandIdPref", "");
        if(TextUtils.isEmpty(currentBandIdPref)){
            Toast.makeText(CreateEventActivity.this, getResources().getString(R.string.you_need_to_belong_to_a_band), Toast.LENGTH_LONG).show();
            sendUserToMainActivity();
        } else {

          //  showAlertDialog();
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
                        break;

                    case 1:
                        eventTypeSP.setSelection(1);
                        break;
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();


    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        savedInstanceStateDone = true;
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

    @Override
    protected void onResume() {
        super.onResume();
        savedInstanceStateDone = false;
    }

}
