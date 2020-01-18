package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hilbing.mybands.adapters.SongAdapter;
import com.hilbing.mybands.models.FindSong;
import com.hilbing.mybands.models.Playlist;
import com.hilbing.mybands.models.Song;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlaylistActivity extends AppCompatActivity {

    @BindView(R.id.playlist_entername_ET)
    EditText namePlaylistET;
    @BindView(R.id.playlist_create_BT)
    Button createPlaylistBT;
    @BindView(R.id.playlist_add_song_BT)
    Button addSongBT;
    @BindView(R.id.playlist_songs_RV)
    RecyclerView recyclerView;
    @BindView(R.id.playlist_toolbar)
    Toolbar toolbar;
    @BindView(R.id.playlist_scrollView_SV)
    ScrollView scrollViewSV;
    @BindView(R.id.playlist_message)
    TextView message;

    private String currentUserId;
    private String currentBandIdPref;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference playlistReference;
    private DatabaseReference songsReference;

    private List<Song> songsList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();



        progressDialog = new ProgressDialog(this);

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle(R.string.create_playlist);

        SharedPreferences preferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
        currentBandIdPref = preferences.getString("currentBandIdPref", "");

        if(TextUtils.isEmpty(currentBandIdPref)){
            message.setVisibility(View.VISIBLE);
            scrollViewSV.setVisibility(View.INVISIBLE);
        } else {
            message.setVisibility(View.INVISIBLE);
            scrollViewSV.setVisibility(View.VISIBLE);
        }

        playlistReference = FirebaseDatabase.getInstance().getReference().child("Playlists");
        playlistReference.keepSynced(true);
        songsReference = FirebaseDatabase.getInstance().getReference().child("Songs");
        songsReference.keepSynced(true);

        createPlaylistBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePlaylist();
            }
        });

        addSongBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSongsDialog(currentBandIdPref);

            }
        });



    }

    private void showSongsDialog(String currentBandIdPref) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.songs_playlist_dialog, null);
        dialogBuilder.setView(dialogView);

        final ListView songsLV = dialogView.findViewById(R.id.playlist_dialog_songs_LV);

        songsReference.child(currentBandIdPref).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                songsList.clear();

                for (DataSnapshot songSnapshot : dataSnapshot.getChildren()) {
                    Song song = songSnapshot.getValue(Song.class);
                    songsList.add(song);
                }


                SongAdapter adapter = new SongAdapter(getApplicationContext(), songsList);
                songsLV.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        dialogBuilder.setTitle(getResources().getString(R.string.adding_song));
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

    }

    private void sendUserToMySongsDialogActivity() {
        Intent dialogIntent = new Intent(PlaylistActivity.this, MySongsDialogActivity.class);
        startActivity(dialogIntent);

    }


    private void savePlaylist() {
        final String playlistName = namePlaylistET.getText().toString();


        if (TextUtils.isEmpty(playlistName))
        {
            namePlaylistET.setError(getResources().getString(R.string.enter_a_name_for_a_new_playlist));
            namePlaylistET.requestFocus();
            return;
        }
        else
        {

            progressDialog.setTitle(getResources().getString(R.string.creating_playlist));
            progressDialog.setMessage(getResources().getString(R.string.please_wait_while_we_are_creating_your_new_playlist));
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(true);

            String id = playlistReference.push().getKey();

            final Playlist playlist = new Playlist(id, playlistName, currentUserId);
            playlistReference.child(currentBandIdPref).child(id).setValue(playlist).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        getSupportActionBar().setTitle(playlistName);
                        addSongBT.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);
                        createPlaylistBT.setVisibility(View.INVISIBLE);
                        progressDialog.dismiss();
                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(PlaylistActivity.this, message, Toast.LENGTH_LONG).show();
                        addSongBT.setVisibility(View.INVISIBLE);
                        recyclerView.setVisibility(View.INVISIBLE);
                        createPlaylistBT.setVisibility(View.VISIBLE);
                        progressDialog.dismiss();
                    }
                }
            });

        }
    }
}
