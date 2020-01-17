package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SongUpdateActivity extends AppCompatActivity {

    @BindView(R.id.update_song_song_name_ET)
    EditText songNameET;
    @BindView(R.id.update_song_artist_band_ET)
    EditText songArtistBandET;
    @BindView(R.id.update_song_youtube_title_ET)
    EditText songYoutubeTitleET;
    @BindView(R.id.update_song_youtube_link_ET)
    EditText songYoutubeLinkET;
    @BindView(R.id.update_song_update_song_BT)
    Button updateSongBT;
    @BindView(R.id.update_song_toolbar)
    Toolbar toolbar;
    @BindView(R.id.update_song_layout_RL)
    RelativeLayout layout;
    @BindView(R.id.update_song_search_song_youtube_BT)
    Button searchSongBT;

    private DatabaseReference songsReference;
    private FirebaseAuth mAuth;

    private String currentUserId;
    private String currentBandIdPref;
    private String songId;
    private String videoURL;
    private String songTitleYoutube;
    private ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_update);

        if(savedInstanceState != null){
            songNameET.setText(savedInstanceState.getString("Song"));
            songArtistBandET.setText(savedInstanceState.getString("Artist"));
        }
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.update_song));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        progressDialog = new ProgressDialog(this);

        SharedPreferences preferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
        currentBandIdPref = preferences.getString("currentBandIdPref", "");

        songsReference = FirebaseDatabase.getInstance().getReference().child("Songs").child(currentBandIdPref);
        songsReference.keepSynced(true);

        Intent intent = getIntent();
        songId = intent.getStringExtra("SONG_ID");
        videoURL = intent.getStringExtra("YouTubeURL");


        if(!TextUtils.isEmpty(videoURL)){
            songYoutubeLinkET.setText(videoURL);
        } else {
            songYoutubeLinkET.setText(getResources().getString(R.string.no_link_from_youtube));
        }

        if(!TextUtils.isEmpty(songId)) {

            songsReference.child(songId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String songName = dataSnapshot.child("mName").getValue().toString();
                        String songArtist = dataSnapshot.child("mArtist").getValue().toString();
                        String songYoutubeTitle = dataSnapshot.child("mYoutubeTitle").getValue().toString();
                        String songYoutubeLink = dataSnapshot.child("mUrlYoutube").getValue().toString();
                        String currentUser = dataSnapshot.child("mCurrentUser").getValue().toString();

                        songNameET.setText(songName);
                        songArtistBandET.setText(songArtist);
                        songYoutubeTitleET.setText(songYoutubeTitle);
                        songYoutubeLinkET.setText(songYoutubeLink);

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


        searchSongBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToSearchSongActivity();
            }
        });


    }

    private void sendUserToSearchSongActivity() {
        Intent searchSongIntent = new Intent(SongUpdateActivity.this, SearchYoutubeUpdateActivity.class);
        searchSongIntent.putExtra("SONG_ID", songId);
        startActivity(searchSongIntent);
        finish();

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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        videoURL = getIntent().getStringExtra("YouTubeURL");
        songTitleYoutube = getIntent().getStringExtra("YouTubeTitle");

        if(!TextUtils.isEmpty(videoURL) && !TextUtils.isEmpty(songTitleYoutube)){
            songYoutubeLinkET.setText(videoURL);
            songYoutubeTitleET.setText(songTitleYoutube);
        } else {
            songYoutubeLinkET.setText(getResources().getString(R.string.no_link_from_youtube));
            songYoutubeTitleET.setText(getResources().getString(R.string.no_title_from_youtube));
        }
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

        outState.putString("Song", songNameET.getText().toString());
        outState.putString("Artist", songArtistBandET.getText().toString());
        outState.getString("SongId", songId);
        Log.d("SAVEINSTANCESTATE", outState.getString("Song") + " " + outState.getString("Artist"));
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        songNameET.setText(savedInstanceState.getString("Song"));
        songArtistBandET.setText(savedInstanceState.getString("Artist"));
        songId = savedInstanceState.getString("SongId");
    }

    private void sendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SongUpdateActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }
}
