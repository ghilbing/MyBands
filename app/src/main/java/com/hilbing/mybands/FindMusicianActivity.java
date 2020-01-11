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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hilbing.mybands.models.FindMusician;
import com.hilbing.mybands.models.UsersInstruments;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class FindMusicianActivity extends AppCompatActivity {

    @BindView(R.id.find_musician_appbar_layout)
    Toolbar toolbar;
    @BindView(R.id.find_musician_SV)
    SearchView searchMusicianSV;
    @BindView(R.id.search_musician_RV)
    RecyclerView recyclerViewRV;
    @BindView(R.id.search_band_name_TV)
    TextView bandNameTV;

    private String currentBandId;

    private FirebaseRecyclerAdapter recyclerAdapter;
    private DatabaseReference allMusiciansReference;
    private DatabaseReference bandsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_musician);

        ButterKnife.bind(this);



        Intent intent = getIntent();
        if(intent != null) {
            currentBandId = intent.getStringExtra("currentBandId");
        }



        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.find_musicians));

        allMusiciansReference = FirebaseDatabase.getInstance().getReference().child("Users");
        bandsReference = FirebaseDatabase.getInstance().getReference().child("Bands");

        recyclerViewRV.setHasFixedSize(true);
        recyclerViewRV.setLayoutManager(new LinearLayoutManager(this));

        if(currentBandId != null) {

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

        searchMusicianSV.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchMusicians(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchMusicians(s);
                return false;
            }
        });


    }

    private void searchMusicians(String searchString)
    {

            Query query = allMusiciansReference.orderByChild("mUserName").startAt(searchString).endAt(searchString + "u\uf8ff");

            FirebaseRecyclerOptions<FindMusician> options = new FirebaseRecyclerOptions.Builder<FindMusician>().setQuery(query,
                    new SnapshotParser<FindMusician>()
                    {
                        @NonNull
                        @Override
                        public FindMusician parseSnapshot(@NonNull DataSnapshot snapshot)
                        {
                            return new FindMusician(
                                    snapshot.child("mUserProfileImage").getValue().toString(),
                                    snapshot.child("mUserName").getValue().toString(),
                                    snapshot.child("mUserStatus").getValue().toString());

                        }
                    }).build();


            recyclerAdapter = new FirebaseRecyclerAdapter<FindMusician, FindMusicianViewHolder>(options)
            {

                @NonNull
                @Override
                public FindMusicianViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_musicians_layout, parent, false);
                    return new FindMusicianViewHolder(view);
                }

                @Override
                protected void onBindViewHolder(@NonNull FindMusicianViewHolder holder, int position, @NonNull final FindMusician model)
                {
                    final String musicianKey = getRef(position).getKey();

                    holder.fullNameTV.setText(model.getmUserName());
                    Picasso.get().load(model.getmUserProfileImage()).placeholder(R.drawable.profile).into(holder.profileCIV);
                    holder.statusTV.setText(model.getmUserStatus());

                    holder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            sendUserToPersonActivity(musicianKey);
                          //  Toast.makeText(FindMusicianActivity.this, musicianKey, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            };

            recyclerViewRV.setAdapter(recyclerAdapter);
            recyclerAdapter.startListening();

    }


    public class FindMusicianViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        @BindView(R.id.all_musicians_full_name_TV)
        TextView fullNameTV;
        @BindView(R.id.all_musicians_image_CIV)
        CircleImageView profileCIV;
        @BindView(R.id.all_musicians_status_TV)
        TextView statusTV;

        public FindMusicianViewHolder(@NonNull final View itemView)
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
        Intent personIntent = new Intent(FindMusicianActivity.this, PersonActivity.class);
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
        Intent mainIntent = new Intent(FindMusicianActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }
}
