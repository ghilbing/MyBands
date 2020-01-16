package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hilbing.mybands.models.BandMembers;
import com.hilbing.mybands.models.FindMusician;
import com.hilbing.mybands.models.FindSong;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MySongsActivity extends AppCompatActivity {

    @BindView(R.id.my_songs_toolbar)
    Toolbar toolbar;
    @BindView(R.id.my_songs_searchView_SV)
    SearchView searchSV;
    @BindView(R.id.my_songs_RV)
    RecyclerView recyclerViewRV;
    @BindView(R.id.my_songs_band_name_TV)
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
        setContentView(R.layout.activity_my_songs);

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



        recyclerAdapter = new FirebaseRecyclerAdapter<FindSong, MySongsActivity.FindSongViewHolder>(options)
        {

            @NonNull
            @Override
            public MySongsActivity.FindSongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_songs_layout, parent, false);
                return new MySongsActivity.FindSongViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final MySongsActivity.FindSongViewHolder holder, int position, @NonNull final FindSong model)
            {
                final String songKey = getRef(position).getKey();

                holder.songNameTV.setText(model.getmName());
                holder.songArtistTV.setText(model.getmArtist());
                holder.youtubeLinkTV.setText(model.getmUrlYoutube());
                String url = model.getmUrlYoutube();

                if(url != getResources().getString(R.string.no_link_from_youtube)){
                    holder.playSongIV.setVisibility(View.VISIBLE);
                }


                String user = model.getmCurrentUser();
                if(user != currentUserId){
                    holder.editionLL.setVisibility(View.INVISIBLE);
                } else if (user == currentUserId){
                    holder.editionLL.setVisibility(View.VISIBLE);
                }




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



        recyclerAdapter = new FirebaseRecyclerAdapter<FindSong, MySongsActivity.FindSongViewHolder>(options)
        {

            @NonNull
            @Override
            public MySongsActivity.FindSongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_songs_layout, parent, false);
                return new MySongsActivity.FindSongViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final MySongsActivity.FindSongViewHolder holder, int position, @NonNull final FindSong model)
            {
                final String songKey = getRef(position).getKey();

                holder.songNameTV.setText(model.getmName());
                holder.songArtistTV.setText(model.getmArtist());
                holder.youtubeLinkTV.setText(model.getmUrlYoutube());
                String url = model.getmUrlYoutube();

                if(url != getResources().getString(R.string.no_link_from_youtube)){
                    holder.playSongIV.setVisibility(View.VISIBLE);
                }


                String user = model.getmCurrentUser();
                if(user != currentUserId){
                    holder.editionLL.setVisibility(View.INVISIBLE);
                } else if (user == currentUserId){
                    holder.editionLL.setVisibility(View.VISIBLE);
                }




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

    private void sendUserToPersonActivity(String musicianKey)
    {
        Intent personIntent = new Intent(MySongsActivity.this, PersonActivity.class);
        personIntent.putExtra("selectedUser", musicianKey);
        personIntent.putExtra("currentBandId", currentBandId);
        startActivity(personIntent);
        finish();
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

    private void sendUserToMainActivity()
    {
        Intent mainIntent = new Intent(MySongsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }
}
