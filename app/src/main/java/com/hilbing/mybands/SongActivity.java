package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
import com.hilbing.mybands.adapters.SongAdapter;
import com.hilbing.mybands.models.Song;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SongActivity extends AppCompatActivity {

    @BindView(R.id.song_song_name_ET)
    EditText songNameET;
    @BindView(R.id.song_artist_band_ET)
    EditText songArtistBandET;
    @BindView(R.id.song_duration_ET)
    EditText songDurationET;
    @BindView(R.id.song_youtube_link_ET)
    EditText songYoutubeLinkET;
    @BindView(R.id.song_add_song_BT)
    Button addSongBT;
    @BindView(R.id.songs_LV)
    ListView songsLV;
    @BindView(R.id.song_toolbar)
    Toolbar toolbar;
    @BindView(R.id.song_search_song_youtube_BT)
    Button searchSongBT;

    private List<Song> songsList = new ArrayList<>();

    private DatabaseReference databaseSongs;

    private String currentBandIdPref;
    private String currentUser;
    private String videoURL;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        if(savedInstanceState != null){
            songNameET.setText(savedInstanceState.getString("Song"));
            songArtistBandET.setText(savedInstanceState.getString("Artist"));
        }

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid();

        videoURL = getIntent().getStringExtra("YouTubeURL");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        actionBar.setTitle(R.string.add_song);

        SharedPreferences preferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
        currentBandIdPref = preferences.getString("currentBandIdPref", "");

        databaseSongs = FirebaseDatabase.getInstance().getReference("Songs");
        databaseSongs.keepSynced(true);


        songYoutubeLinkET.setText(R.string.no_data_available);

        if(!TextUtils.isEmpty(videoURL)){
            songYoutubeLinkET.setText(videoURL);
        } else {
            songYoutubeLinkET.setText(getResources().getString(R.string.no_link_from_youtube));
        }

        searchSongBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToSearchSongActivity();
            }
        });

        addSongBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSong();
            }
        });

        songsLV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Song song = songsList.get(i);
                showUpdateDialog(song.getmId(), song.getmName(), song.getmArtist(), song.getmDuration(), song.getmUrlYoutube());
                return false;
            }
        });

    }

    private void sendUserToSearchSongActivity() {
        Intent searchSongIntent = new Intent(SongActivity.this, SearchYoutubeActivity.class);
        startActivity(searchSongIntent);

    }

    private void addSong() {

        String name = songNameET.getText().toString();
        String artist = songArtistBandET.getText().toString();
        String duration = songDurationET.getText().toString();
        String youtubeLink = songYoutubeLinkET.getText().toString();

        String id = databaseSongs.push().getKey();

        Song song = new Song(id, name, artist, duration, youtubeLink, currentUser);
        databaseSongs.child(currentBandIdPref).child(id).setValue(song);
        Toast.makeText(SongActivity.this, getResources().getString(R.string.song_added), Toast.LENGTH_LONG).show();

    }

    private void showUpdateDialog(final String id, String name, String artist, String duration, String youtubeLink){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.update_dialog_song, null);
        dialogBuilder.setView(dialogView);

        final EditText nameET = dialogView.findViewById(R.id.newSongNameET);
        final EditText artistET = dialogView.findViewById(R.id.newArtistNameET);
        final EditText durationET = dialogView.findViewById(R.id.newDurationET);
        final EditText youtubeET = dialogView.findViewById(R.id.newYoutubeLinkET);
        final Button updateBT = dialogView.findViewById(R.id.update_BT);
        final Button deleteBT = dialogView.findViewById(R.id.delete_BT);

        nameET.setText(name);
        artistET.setText(artist);
        durationET.setText(duration);
        youtubeET.setText(youtubeLink);

        dialogBuilder.setTitle(getResources().getString(R.string.updating_song));
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        updateBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName = nameET.getText().toString();
                String newArtist = artistET.getText().toString();
                String newDuration = durationET.getText().toString();
                String newYoutube = youtubeET.getText().toString();

                updateSong(id, newName, newArtist, newDuration, newYoutube, currentUser);

                alertDialog.dismiss();

            }
        });

        deleteBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteSong(id);
                alertDialog.dismiss();
            }
        });

    }

    private void deleteSong(String id) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Songs").child(currentBandIdPref).child(id);
        databaseReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(SongActivity.this, getResources().getString(R.string.song_deleted), Toast.LENGTH_LONG).show();
            }
        });



    }


    private boolean updateSong(String id, String name, String artist, String duration, String youtube, String currentUser){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Songs").child(currentBandIdPref).child(id);
        Song song = new Song(id, name, artist, duration, youtube, currentUser);
        databaseReference.setValue(song);
        Toast.makeText(SongActivity.this, getResources().getString(R.string.song_added_successfully), Toast.LENGTH_LONG).show();
        return true;

    }


    @Override
    public void onStart() {
        super.onStart();
        databaseSongs.child(currentBandIdPref).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                songsList.clear();

                for (DataSnapshot songSnapshot : dataSnapshot.getChildren()) {
                    Song song = songSnapshot.getValue(Song.class);
                    songsList.add(song);
                }


                SongAdapter adapter = new SongAdapter(SongActivity.this, songsList);
                songsLV.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

        outState.putString("Song", songNameET.getText().toString());
        outState.putString("Artist", songArtistBandET.getText().toString());
        Log.d("SAVEINSTANCESTATE", outState.getString("Song") + " " + outState.getString("Artist"));
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        songNameET.setText(savedInstanceState.getString("Song"));
        songArtistBandET.setText(savedInstanceState.getString("Artist"));
    }

    private void sendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SongActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);

    }
}
