package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.hilbing.mybands.models.FindMusician;
import com.hilbing.mybands.models.UsersInstruments;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class FindMusicianActivity extends AppCompatActivity {

    @BindView(R.id.find_musician_appbar_layout)
    Toolbar toolbar;
    @BindView(R.id.search_musician_ET)
    EditText searchET;
    @BindView(R.id.search_musician_BT)
    ImageButton searchBT;
    @BindView(R.id.search_musician_RV)
    RecyclerView recyclerViewRV;

    private FirebaseRecyclerAdapter recyclerAdapter;
    private DatabaseReference allMusiciansReference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_musician);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.find_musicians));

        allMusiciansReference = FirebaseDatabase.getInstance().getReference().child("Users");

        recyclerViewRV.setHasFixedSize(true);
        recyclerViewRV.setLayoutManager(new LinearLayoutManager(this));

        searchBT.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String searchString = searchET.getText().toString();
                searchMusicians(searchString);
            }
        });

            //displayMusicians();

    }

    private void searchMusicians(String searchString)
    {
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
                          //  sendUserToClickMusicianActivity(musicianKey);
                            Toast.makeText(FindMusicianActivity.this, musicianKey, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            };

            recyclerViewRV.setAdapter(recyclerAdapter);
        }

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
}
