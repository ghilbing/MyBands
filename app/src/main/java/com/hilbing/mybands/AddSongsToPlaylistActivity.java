package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.hilbing.mybands.fragments.SongsFragmentDialog;
import com.hilbing.mybands.models.Playlist;
import com.hilbing.mybands.models.Song;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddSongsToPlaylistActivity extends AppCompatActivity {

    @BindView(R.id.my_songs_playlists_toolbar)
    Toolbar toolbar;
    @BindView(R.id.my_songs_playlists_scrollView_SV)
    ScrollView scrollViewSV;
    @BindView(R.id.my_songs_playlists_message)
    TextView message;
    @BindView(R.id.my_songs_playlists_add_BT)
    Button addSongBT;
    @BindView(R.id.my_songs_playlists_recyclerView_RV)
    RecyclerView recyclerView;

    private String currentUserId;
    private String currentBandIdPref;
    private ProgressDialog progressDialog;

    private FragmentManager fragmentManager = getSupportFragmentManager();
    private SongsFragmentDialog songsFragmentDialog = new SongsFragmentDialog();

    private String currentPlaylistId;
    private String currentPlaylistName;
    private List<Song> songsList = new ArrayList<>();

    private FirebaseAuth mAuth;
    private DatabaseReference playlistReference;
    private DatabaseReference songsReference;
    private boolean savedInstanceStateDone;

    private FirebaseRecyclerAdapter recyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_songs_to_playlist);

        ButterKnife.bind(this);

        Intent intent = getIntent();

        currentPlaylistId = intent.getStringExtra("PLAYLIST_ID");
        currentPlaylistName = intent.getStringExtra("PLAYLIST_NAME");

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(currentPlaylistName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        progressDialog = new ProgressDialog(this);

        SharedPreferences preferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
        currentBandIdPref = preferences.getString("currentBandIdPref", "");

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        playlistReference = FirebaseDatabase.getInstance().getReference().child("Playlists");
        playlistReference.keepSynced(true);
        songsReference = FirebaseDatabase.getInstance().getReference().child("Songs");
        songsReference.keepSynced(true);

        addSongBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!savedInstanceStateDone) {
                    showSongs();
                }

            }
        });

        if (!TextUtils.isEmpty(currentBandIdPref)) {
            displayMySongsFromPlaylist();
        }


    }

    private void showSongs() {


        songsReference.child(currentBandIdPref).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    songsList.clear();

                    for (DataSnapshot songSnapshot : dataSnapshot.getChildren()) {
                        Song song = songSnapshot.getValue(Song.class);
                        String mId = song.getmId();
                        String mArtist = song.getmArtist();
                        String mName = song.getmName();
                        String mUrlYoutube = song.getmUrlYoutube();
                        Song newSong = new Song(mId, mName, mArtist, mUrlYoutube);
                        songsList.add(newSong);
                    }

                    DialogFragment dialogFragment = SongsFragmentDialog.newInstance(songsList, currentBandIdPref, currentPlaylistId);
                    dialogFragment.show(getSupportFragmentManager(), getString(R.string.add_song_to_playlist));

                } else {
                    Toast.makeText(AddSongsToPlaylistActivity.this, getResources().getString(R.string.you_need_to_add_songs), Toast.LENGTH_LONG).show();
                    sendUserToSongsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendUserToSongsActivity() {
        Intent songIntent = new Intent(AddSongsToPlaylistActivity.this, SongActivity.class);
        startActivity(songIntent);
    }

    private void displayMySongsFromPlaylist() {

        Query query = FirebaseDatabase.getInstance().getReference().child("PlaylistsSongs").child(currentBandIdPref).child(currentPlaylistId);
        if (!TextUtils.isEmpty(query.toString())) {

            FirebaseRecyclerOptions<Song> options = new FirebaseRecyclerOptions.Builder<Song>().setQuery(query,
                    new SnapshotParser<Song>() {
                        @NonNull
                        @Override
                        public Song parseSnapshot(@NonNull DataSnapshot snapshot) {
                            return new Song(
                                    snapshot.child("mId").getValue().toString(),
                                    snapshot.child("mName").getValue().toString(),
                                    snapshot.child("mArtist").getValue().toString(),
                                    snapshot.child("mUrlYoutube").getValue().toString());

                        }
                    }).build();


            recyclerAdapter = new FirebaseRecyclerAdapter<Song, AddSongsToPlaylistActivity.MySongsViewHolder>(options) {

                @NonNull
                @Override
                public AddSongsToPlaylistActivity.MySongsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_songs_playlist_layout, parent, false);
                    return new AddSongsToPlaylistActivity.MySongsViewHolder(view);
                }

                @Override
                protected void onBindViewHolder(@NonNull final AddSongsToPlaylistActivity.MySongsViewHolder holder, int position, @NonNull final Song model) {
                    final String songKey = getRef(position).getKey();

                    holder.songNameTV.setText(model.getmName());
                    holder.songArtistTV.setText(model.getmArtist());
                    holder.youtubeLinkTV.setText(model.getmUrlYoutube());

                    if (!model.getmUrlYoutube().equals(getResources().getString(R.string.no_link_from_youtube))) {
                        holder.playSongIV.setVisibility(View.VISIBLE);
                        holder.playSongIV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                sendUserToYoutubeDialogActivity(model.getmUrlYoutube());
                            }
                        });
                    }

                    holder.deleteSongIV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            deleteSong(songKey);
                        }
                    });


                }
            };
        }

        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.startListening();

    }

    private void deleteSong(String id) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("PlaylistsSongs").child(currentBandIdPref).child(currentPlaylistId).child(id);
        databaseReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(AddSongsToPlaylistActivity.this, getResources().getString(R.string.song_deleted), Toast.LENGTH_LONG).show();
            }
        });

    }

    private void sendUserToYoutubeDialogActivity(String url) {
        Intent youtubeDialogIntent = new Intent(AddSongsToPlaylistActivity.this, YoutubeDialogActivity.class);
        youtubeDialogIntent.putExtra("VIDEO_ID", url);
        startActivity(youtubeDialogIntent);

    }


    public class MySongsViewHolder extends RecyclerView.ViewHolder {
        View mView;
        @BindView(R.id.all_songs_playlist_name_TV)
        TextView songNameTV;
        @BindView(R.id.all_songs_playlist_artist_TV)
        TextView songArtistTV;
        @BindView(R.id.all_songs_playlist_youtube_link_TV)
        TextView youtubeLinkTV;
        @BindView(R.id.all_songs_playlist_play_IV)
        ImageView playSongIV;
        @BindView(R.id.all_songs_playlist_delete_IV)
        ImageView deleteSongIV;

        public MySongsViewHolder(@NonNull final View itemView) {
            super(itemView);
            mView = itemView;
            ButterKnife.bind(this, itemView);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int itemClicked = getAdapterPosition();
                    Toast.makeText(AddSongsToPlaylistActivity.this, String.valueOf(itemClicked), Toast.LENGTH_LONG).show();

                    return false;
                }
            });

        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            sendUserToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        savedInstanceStateDone = false;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        savedInstanceStateDone = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        savedInstanceStateDone = false;
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(AddSongsToPlaylistActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);

    }


}
