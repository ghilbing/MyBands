package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.hilbing.mybands.models.Band;
import com.hilbing.mybands.models.MusiciansBands;
import com.hilbing.mybands.models.UsersBands;
import com.hilbing.mybands.models.UsersInstruments;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MyBandsActivity extends AppCompatActivity {

    @BindView(R.id.my_bands_toolbar)
    Toolbar toolbar;
    @BindView(R.id.my_bands_added_RV)
    RecyclerView bandsAddedRV;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter recyclerAdapter;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private DatabaseReference bandsMusicianRef;
    private DatabaseReference bandsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bands);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.my_bands));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        bandsMusicianRef = FirebaseDatabase.getInstance().getReference().child("BandsMusicians");
        bandsRef = FirebaseDatabase.getInstance().getReference().child("Bands");

        bandsAddedRV.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        bandsAddedRV.setLayoutManager(linearLayoutManager);

        displayMyBands();

    }

    private void displayMyBands() {

        Query query = FirebaseDatabase.getInstance().getReference().child("BandsMusicians").child(currentUserId);

        FirebaseRecyclerOptions<UsersBands> options = new FirebaseRecyclerOptions.Builder<UsersBands>().setQuery(query,
                new SnapshotParser<UsersBands>()
                {
                    @NonNull
                    @Override
                    public UsersBands parseSnapshot(@NonNull DataSnapshot snapshot)
                    {
                        return new UsersBands(
                                snapshot.child("date").getValue().toString());

                    }
                }).build();


        recyclerAdapter = new FirebaseRecyclerAdapter<UsersBands, MyBandsViewHolder>(options)
        {

            @NonNull
            @Override
            public MyBandsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_bands_per_musician, parent, false);
                return new MyBandsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final MyBandsViewHolder holder, int position, @NonNull final UsersBands model)
            {
                final String bandKey = getRef(position).getKey();
                holder.dateTV.setText(model.getmDate());
                bandsRef.child(bandKey).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            final String bandName = String.valueOf(dataSnapshot.child("mBandName").getValue());
                            String bandImage = String.valueOf(dataSnapshot.child("mBandImage").getValue());

                            holder.bandNameTV.setText(bandName);
                            Picasso.get().load(bandImage).placeholder(R.drawable.profile).into(holder.bandImageCIV);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });


            }
        };

        bandsAddedRV.setAdapter(recyclerAdapter);
        recyclerAdapter.startListening();

    }

    public class MyBandsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        @BindView(R.id.all_bands_per_musician_name_TV)
        TextView bandNameTV;
        @BindView(R.id.all_bands_per_musician_image_CIV)
        CircleImageView bandImageCIV;
        @BindView(R.id.all_bands_per_musician_date_TV)
        TextView dateTV;

        public MyBandsViewHolder(@NonNull final View itemView)
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
        Intent mainIntent = new Intent(MyBandsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }
}
