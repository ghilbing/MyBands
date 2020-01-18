package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
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
import com.hilbing.mybands.models.Playlist;
import com.hilbing.mybands.models.Song;
import com.hilbing.mybands.models.UsersBands;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MyPlaylistsActivity extends AppCompatActivity {

    @BindView(R.id.my_playlists_toolbar)
    Toolbar toolbar;
    @BindView(R.id.my_playlists_scrollView_SV)
    ScrollView scrollViewSV;
    @BindView(R.id.my_playlists_message)
    TextView message;
    @BindView(R.id.my_playlists_add_BT)
    Button addPlaylistBT;
    @BindView(R.id.my_playlists_recyclerView_RV)
    RecyclerView recyclerView;

    private String currentUserId;
    private String currentBandIdPref;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference playlistReference;
    private DatabaseReference songsReference;
    private DatabaseReference usersReference;

    private FirebaseRecyclerAdapter recyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_playlists);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.my_playlists));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        progressDialog = new ProgressDialog(this);

        SharedPreferences preferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
        currentBandIdPref = preferences.getString("currentBandIdPref", "");

        if(TextUtils.isEmpty(currentBandIdPref)){
            message.setVisibility(View.VISIBLE);
            scrollViewSV.setVisibility(View.INVISIBLE);
        } else {
            message.setVisibility(View.INVISIBLE);
            scrollViewSV.setVisibility(View.VISIBLE);
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        playlistReference = FirebaseDatabase.getInstance().getReference().child("Playlists");
        playlistReference.keepSynced(true);
        songsReference = FirebaseDatabase.getInstance().getReference().child("Songs");
        songsReference.keepSynced(true);
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        usersReference.keepSynced(true);

        addPlaylistBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPlaylist();
            }
        });

        if(!TextUtils.isEmpty(currentBandIdPref)){
            displayMyPlaylists();
        }

    }

    private void displayMyPlaylists() {

        Query query = FirebaseDatabase.getInstance().getReference().child("Playlists").child(currentBandIdPref);

        FirebaseRecyclerOptions<Playlist> options = new FirebaseRecyclerOptions.Builder<Playlist>().setQuery(query,
                new SnapshotParser<Playlist>()
                {
                    @NonNull
                    @Override
                    public Playlist parseSnapshot(@NonNull DataSnapshot snapshot)
                    {
                        return new Playlist(
                                snapshot.child("mId").getValue().toString(),
                                snapshot.child("mPlaylistName").getValue().toString(),
                                snapshot.child("mCreator").getValue().toString());

                    }
                }).build();


        recyclerAdapter = new FirebaseRecyclerAdapter<Playlist, MyPlaylistsActivity.MyPlaylistsViewHolder>(options)
        {

            @NonNull
            @Override
            public MyPlaylistsActivity.MyPlaylistsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_playlists_layout, parent, false);
                return new MyPlaylistsActivity.MyPlaylistsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final MyPlaylistsActivity.MyPlaylistsViewHolder holder, int position, @NonNull final Playlist model)
            {
                final String playlistKey = getRef(position).getKey();

                holder.playlistNameTV.setText(model.getmPlaylistName());
                final String creator = model.getmCreator();
                usersReference.child(creator).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            final String userName = String.valueOf(dataSnapshot.child("mUserName").getValue());

                            holder.playlistCreatorTV.setText(userName);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });


            }
        };

        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.startListening();

    }

    public class MyPlaylistsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        @BindView(R.id.all_playlists_name_TV)
        TextView playlistNameTV;
        @BindView(R.id.all_playlists_creator_TV)
        TextView playlistCreatorTV;
        @BindView(R.id.all_songs_edit_IV)
        ImageView editSongIV;
        @BindView(R.id.all_songs_delete_IV)
        ImageView deleteSongIV;
        @BindView(R.id.all_songs_edition)
        LinearLayout editionLL;

        public MyPlaylistsViewHolder(@NonNull final View itemView)
        {
            super(itemView);
            mView = itemView;
            ButterKnife.bind(this, itemView);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    //get data


                    int itemClicked = getAdapterPosition();
                    //  showUpdateDialog(itemClicked, instrument);
                    return false;
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

        }
    }

    private void createPlaylist() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.create_playlist));
        builder.setMessage(getResources().getString(R.string.please_enter_a_playlist_name));
        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final String playlistName = input.getText().toString();
                if(TextUtils.isEmpty(playlistName)){
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.please_enter_a_playlist_name), Toast.LENGTH_LONG).show();
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
                                progressDialog.dismiss();
                            } else {
                                String message = task.getException().getMessage();
                                Toast.makeText(MyPlaylistsActivity.this, message, Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        }
                    });
                }

            }
        });

        builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();



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
        Intent mainIntent = new Intent(MyPlaylistsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }


}
