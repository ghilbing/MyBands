package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

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
import com.hilbing.mybands.models.MusiciansBands;
import com.hilbing.mybands.models.UsersBands;
import com.hilbing.mybands.models.UsersInstruments;
import com.squareup.picasso.Picasso;

import java.io.CharArrayWriter;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MusiciansActivity extends AppCompatActivity {

    @BindView(R.id.musicians_RV)
    RecyclerView musiciansRV;
    @BindView(R.id.musicians_toolbar)
    Toolbar toolbar;

    private FirebaseRecyclerAdapter recyclerAdapter;
    private DatabaseReference usersBandsReference;
    private DatabaseReference usersDataReference;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private String currentBandId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musicians);

        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        usersBandsReference = FirebaseDatabase.getInstance().getReference().child("BandsMusicians").child(currentUserId);
        usersDataReference = FirebaseDatabase.getInstance().getReference().child("Users");

        SharedPreferences preferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
        currentBandId = preferences.getString("currentBandIdPref", "");

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        actionBar.setTitle(R.string.band_members);

        musiciansRV.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        musiciansRV.setLayoutManager(linearLayoutManager);

        displayMusicians();

    }

    private void displayMusicians()
    {

        Query query = FirebaseDatabase.getInstance().getReference().child("BandsMusicians").child(currentBandId);

        FirebaseRecyclerOptions<BandMembers> options = new FirebaseRecyclerOptions.Builder<BandMembers>().setQuery(query,
                new SnapshotParser<BandMembers>()
                {
                    @NonNull
                    @Override
                    public BandMembers parseSnapshot(@NonNull DataSnapshot snapshot)
                    {
                        return new BandMembers(snapshot.getKey());

                    }
                }).build();


        recyclerAdapter = new FirebaseRecyclerAdapter<BandMembers, MusicianViewHolder>(options)

        {

            @NonNull
            @Override
            public MusicianViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_musicians_band_layout, parent, false);
                return new MusicianViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final MusicianViewHolder holder, int position, @NonNull final BandMembers model)
            {
                final String musicianKey = getRef(position).getKey();
             //   holder.dateTV.setText(model.getmDate());
                usersDataReference.child(musicianKey).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            final String userName = String.valueOf(dataSnapshot.child("mUserName").getValue());
                            String profileImage = String.valueOf(dataSnapshot.child("mUserProfileImage").getValue());
                            String status = String.valueOf(dataSnapshot.child("mUserStatus").getValue());

                            holder.fullNameTV.setText(userName);
                            Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(holder.profileCIV);
                            holder.statusTV.setText(status);

                            holder.mView.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    CharSequence options[] = new CharSequence[]
                                            {
                                                    userName + " " + getResources().getString(R.string.instruments),
                                                    getResources().getString(R.string.send_message)
                                            };
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MusiciansActivity.this);
                                    builder.setTitle(getResources().getString(R.string.select_option));
                                    builder.setItems(options, new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i)
                                        {
                                            if(i == 0)
                                            {
                                                Intent instrumentIntent = new Intent(MusiciansActivity.this, AddInstrumentActivity.class);
                                                instrumentIntent.putExtra("mUserId", musicianKey);
                                                startActivity(instrumentIntent);
                                            }
                                            if(i == 1)
                                            {
                                                if(!currentUserId.equals(musicianKey))
                                                {
                                                Intent messageIntent = new Intent(MusiciansActivity.this, MessagesActivity.class);
                                                messageIntent.putExtra("mUserId", musicianKey);
                                                messageIntent.putExtra("mUserName", userName);
                                                messageIntent.putExtra("mBandId", currentBandId);
                                                startActivity(messageIntent);
                                                }
                                                else {
                                                    Toast.makeText(MusiciansActivity.this, getResources().getString(R.string.you_cannot_send_messages_to_yourself), Toast.LENGTH_LONG).show();
                                                }

                                            }
                                        }
                                    });
                                    builder.show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });


            }
        };

        musiciansRV.setAdapter(recyclerAdapter);
        recyclerAdapter.startListening();


    }


    public class MusicianViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        @BindView(R.id.all_musicians_band_full_name_TV)
        TextView fullNameTV;
        @BindView(R.id.all_musicians_band_image_CIV)
        CircleImageView profileCIV;
        @BindView(R.id.all_musicians_band_status_TV)
        TextView statusTV;
        @BindView(R.id.all_musicians_band_date_TV)
        TextView dateTV;

        public MusicianViewHolder(@NonNull final View itemView)
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
    protected void onStart() {
        super.onStart();
        recyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        recyclerAdapter.stopListening();
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
        Intent mainIntent = new Intent(MusiciansActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }
}
