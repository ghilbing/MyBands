package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.hilbing.mybands.models.FindSong;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MySongsDialogActivity extends AppCompatActivity {

    @BindView(R.id.my_songs_dialog_toolbar)
    Toolbar toolbar;
    @BindView(R.id.my_songs_dialog_searchView_SV)
    SearchView searchSV;
    @BindView(R.id.my_songs_dialog_RV)
    RecyclerView recyclerViewRV;
    @BindView(R.id.my_songs_dialog_band_name_TV)
    TextView bandNameTV;

    private String currentBandId;
    private String currentUserId;

    private FirebaseRecyclerAdapter recyclerAdapter;
    private DatabaseReference allSongsReference;
    private DatabaseReference bandsReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_songs_dialog);
        this.setFinishOnTouchOutside(false);

        ButterKnife.bind(this);

        SharedPreferences preferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
        currentBandId = preferences.getString("currentBandIdPref", "");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.my_songs));

        allSongsReference = FirebaseDatabase.getInstance().getReference().child("Songs").child(currentBandId);
        allSongsReference.keepSynced(true);
        bandsReference = FirebaseDatabase.getInstance().getReference().child("Bands");
        bandsReference.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        recyclerViewRV.setHasFixedSize(true);
        recyclerViewRV.setLayoutManager(new LinearLayoutManager(this));

        if(currentBandId != null) {

            showSongs();

            bandsReference.child(currentBandId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        bandNameTV.setText(dataSnapshot.child("mBandName").getValue().toString());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        searchSV.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchSongs(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchSongs(s);
                return false;
            }
        });


    }

    private void showSongs(){
        Query query = allSongsReference.orderByChild("mName");

        FirebaseRecyclerOptions<FindSong> options = new FirebaseRecyclerOptions.Builder<FindSong>().setQuery(query,
                new SnapshotParser<FindSong>()
                {
                    @NonNull
                    @Override
                    public FindSong parseSnapshot(@NonNull DataSnapshot snapshot)
                    {
                        return new FindSong(
                                snapshot.child("mArtist").getValue().toString(),
                                snapshot.child("mCurrentUser").getValue().toString(),
                                snapshot.child("mName").getValue().toString(),
                                snapshot.child("mUrlYoutube").getValue().toString());

                    }
                }).build();



        recyclerAdapter = new FirebaseRecyclerAdapter<FindSong, MySongsDialogActivity.FindSongViewHolder>(options)
        {

            @NonNull
            @Override
            public MySongsDialogActivity.FindSongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_songs_layout, parent, false);
                return new MySongsDialogActivity.FindSongViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final MySongsDialogActivity.FindSongViewHolder holder, int position, @NonNull final FindSong model)
            {
                final String songKey = getRef(position).getKey();

                holder.songNameTV.setText(model.getmName());
                holder.songArtistTV.setText(model.getmArtist());
                holder.youtubeLinkTV.setText(model.getmUrlYoutube());
                final String url = model.getmUrlYoutube();

                if(!url.equals(getResources().getString(R.string.no_link_from_youtube))){
                    holder.playSongIV.setVisibility(View.VISIBLE);
                    holder.playSongIV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            sendUserToYoutubeDialogActivity(url);
                        }
                    });
                }


                String user = model.getmCurrentUser();
                if(!user.equals(currentUserId)){
                    holder.editionLL.setVisibility(View.INVISIBLE);
                } else {
                    holder.editionLL.setVisibility(View.VISIBLE);
                }

                holder.deleteSongIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteSong(songKey);
                    }
                });

                holder.editSongIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendUserToUpdateSongActivity(songKey);
                    }
                });






                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //   sendUserToPersonActivity(songKey);
                        //  Toast.makeText(FindMusicianActivity.this, musicianKey, Toast.LENGTH_LONG).show();
                    }
                });
            }
        };

        recyclerViewRV.setAdapter(recyclerAdapter);
        recyclerAdapter.startListening();

    }




    private void searchSongs(String searchString)
    {

        Query query = allSongsReference.orderByChild("mName").startAt(searchString).endAt(searchString + "u\uf8ff");

        FirebaseRecyclerOptions<FindSong> options = new FirebaseRecyclerOptions.Builder<FindSong>().setQuery(query,
                new SnapshotParser<FindSong>()
                {
                    @NonNull
                    @Override
                    public FindSong parseSnapshot(@NonNull DataSnapshot snapshot)
                    {
                        return new FindSong(
                                snapshot.child("mArtist").getValue().toString(),
                                snapshot.child("mCurrentUser").getValue().toString(),
                                snapshot.child("mName").getValue().toString(),
                                snapshot.child("mUrlYoutube").getValue().toString());

                    }
                }).build();



        recyclerAdapter = new FirebaseRecyclerAdapter<FindSong, MySongsDialogActivity.FindSongViewHolder>(options)
        {

            @NonNull
            @Override
            public MySongsDialogActivity.FindSongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_songs_layout, parent, false);
                return new MySongsDialogActivity.FindSongViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final MySongsDialogActivity.FindSongViewHolder holder, int position, @NonNull final FindSong model)
            {
                final String songKey = getRef(position).getKey();

                holder.songNameTV.setText(model.getmName());
                holder.songArtistTV.setText(model.getmArtist());
                holder.youtubeLinkTV.setText(model.getmUrlYoutube());
                final String url = model.getmUrlYoutube();

                if(!url.equals(getResources().getString(R.string.no_link_from_youtube))){
                    holder.playSongIV.setVisibility(View.VISIBLE);
                    holder.playSongIV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            sendUserToYoutubeDialogActivity(url);
                        }
                    });
                }




                String user = model.getmCurrentUser();
                if(!user.equals(currentUserId)){
                    holder.editionLL.setVisibility(View.INVISIBLE);
                } else {
                    holder.editionLL.setVisibility(View.VISIBLE);
                }

                holder.deleteSongIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteSong(songKey);
                    }
                });




                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // sendUserToPersonActivity(songKey);
                        //  Toast.makeText(FindMusicianActivity.this, musicianKey, Toast.LENGTH_LONG).show();
                    }
                });
            }
        };

        recyclerViewRV.setAdapter(recyclerAdapter);
        recyclerAdapter.startListening();


    }

    private void sendUserToYoutubeDialogActivity(String url) {
        Intent youtubeDialogIntent = new Intent(MySongsDialogActivity.this, YoutubeDialogActivity.class);
        youtubeDialogIntent.putExtra("VIDEO_ID", url);
        startActivity(youtubeDialogIntent);

    }

    private void sendUserToUpdateSongActivity(String songKey) {
        Intent updateSongIntent = new Intent(MySongsDialogActivity.this, SongUpdateActivity.class);
        updateSongIntent.putExtra("SONG_ID", songKey);
        startActivity(updateSongIntent);

    }


    public class FindSongViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        @BindView(R.id.all_songs_name_TV)
        TextView songNameTV;
        @BindView(R.id.all_songs_artist_TV)
        TextView songArtistTV;
        @BindView(R.id.all_songs_current_user)
        TextView currentUserTV;
        @BindView(R.id.all_songs_youtube_link_TV)
        TextView youtubeLinkTV;
        @BindView(R.id.all_songs_play_IV)
        ImageView playSongIV;
        @BindView(R.id.all_songs_edit_IV)
        ImageView editSongIV;
        @BindView(R.id.all_songs_delete_IV)
        ImageView deleteSongIV;
        @BindView(R.id.all_songs_edition)
        LinearLayout editionLL;

        public FindSongViewHolder(@NonNull final View itemView)
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
                    Toast.makeText(MySongsDialogActivity.this, String.valueOf(itemClicked), Toast.LENGTH_LONG).show();

                    return false;
                }
            });

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

    private void deleteSong(String id) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Songs").child(currentBandId).child(id);
        databaseReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(MySongsDialogActivity.this, getResources().getString(R.string.song_deleted), Toast.LENGTH_LONG).show();
            }
        });



    }

    private void sendUserToMainActivity()
    {
        Intent mainIntent = new Intent(MySongsDialogActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }
}
