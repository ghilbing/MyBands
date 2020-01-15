package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
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
import com.hilbing.mybands.models.Instrument;
import com.hilbing.mybands.models.UsersInstruments;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddInstrumentActivity extends AppCompatActivity
{

    @BindView(R.id.update_instrument_toolbar)
    Toolbar toolbar;
    @BindView(R.id.instruments_SP)
    Spinner instrumentsSP;
    @BindView(R.id.instruments_add_BT)
    Button addBT;
    @BindView(R.id.instruments_added_RV)
    RecyclerView instrumentsAddedRV;
    private String currentUserId;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference userDataReference;
    private DatabaseReference singersReference;
    private DatabaseReference composersReference;
    private DatabaseReference usersInstrumentsReference;
    private DatabaseReference instrumentsUsersReference;

    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter recyclerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_instrument);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.update_instrument));

        Intent intentGetData = getIntent();
        Bundle bundle = intentGetData.getExtras();
        if(bundle != null)
        {
            String intentString = bundle.getString("mUserId");
            currentUserId = intentString;
        }
        else {

            mAuth = FirebaseAuth.getInstance();
            currentUserId = mAuth.getCurrentUser().getUid();
        }

        userDataReference = FirebaseDatabase.getInstance().getReference().child("Users");
        userDataReference.keepSynced(true);
        singersReference = FirebaseDatabase.getInstance().getReference().child("Singers");
        singersReference.keepSynced(true);
        composersReference = FirebaseDatabase.getInstance().getReference().child("Composers");
        composersReference.keepSynced(true);
        usersInstrumentsReference = FirebaseDatabase.getInstance().getReference("users_instruments");
        usersInstrumentsReference.keepSynced(true);
        instrumentsUsersReference = FirebaseDatabase.getInstance().getReference("instruments_users");
        instrumentsUsersReference.keepSynced(true);

        progressDialog = new ProgressDialog(this);

        instrumentsAddedRV.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        instrumentsAddedRV.setLayoutManager(linearLayoutManager);

        addBT.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                progressDialog.setTitle(getResources().getString(R.string.add_instrument));
                progressDialog.setMessage(getResources().getString(R.string.please_wait_while_we_are_updating_your_instruments));
                progressDialog.show();
                progressDialog.setCanceledOnTouchOutside(true);
                addInstrumentsPlayedByUserToDataBase();
                addUsersForInstrumentsToDataBase();

            }
        });

        displayAllInstruments();

    }

    private void displayAllInstruments()
    {
        Query query = FirebaseDatabase.getInstance().getReference().child("users_instruments").child(currentUserId);

        FirebaseRecyclerOptions<UsersInstruments> options = new FirebaseRecyclerOptions.Builder<UsersInstruments>().setQuery(query,
                new SnapshotParser<UsersInstruments>()
                {
            @NonNull
            @Override
            public UsersInstruments parseSnapshot(@NonNull DataSnapshot snapshot)
            {
                return new UsersInstruments(
                        snapshot.child("mUserId").getValue().toString(),
                        snapshot.child("mUserName").getValue().toString(),
                        snapshot.child("mInstrumentName").getValue().toString());

            }
        }).build();


        recyclerAdapter = new FirebaseRecyclerAdapter<UsersInstruments, InstrumentViewHolder>(options)
        {

            @NonNull
            @Override
            public InstrumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_instruments_per_user, parent, false);
                return new InstrumentViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull InstrumentViewHolder holder, int position, @NonNull final UsersInstruments model)
            {
                final String instrumentKey = getRef(position).getKey();

                holder.instrumentNameTV.setText(model.getmUserInstrument());
                holder.userIdTV.setText(model.getmUserId());
                holder.userNameTV.setText(model.getmUserName());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendUserToClickInstrumentActivity(instrumentKey, currentUserId);
                        Toast.makeText(AddInstrumentActivity.this, instrumentKey + " " + currentUserId, Toast.LENGTH_LONG).show();
                    }
                });
            }
        };

        instrumentsAddedRV.setAdapter(recyclerAdapter);
    }

    private void sendUserToClickInstrumentActivity(String instrumentKey, String userKey) {

        Intent clickInstrumentIntent = new Intent(AddInstrumentActivity.this, ClickInstrumentActivity.class);
        clickInstrumentIntent.putExtra("InstrumentKey", instrumentKey);
        startActivity(clickInstrumentIntent);

    }

    public class InstrumentViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        @BindView(R.id.instrument_item_TV)
        TextView instrumentNameTV;
        @BindView(R.id.instrument_userIdTV)
        TextView userIdTV;
        @BindView(R.id.instrument_userNameTV)
        TextView userNameTV;

        public InstrumentViewHolder(@NonNull final View itemView)
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


    private void deleteInstrument(String instrument, String userId)
    {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user_instruments").child(currentUserId);
        databaseReference.keepSynced(true);
        databaseReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.instrument_deleted), Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean updateInstrument(String instrument, String userId, String userName)
    {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user_instruments").child(currentUserId);
        databaseReference.keepSynced(true);
        UsersInstruments usersInstruments = new UsersInstruments(instrument, userId, userName);
        databaseReference.setValue(usersInstruments);
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.instrument_updated_successfully), Toast.LENGTH_LONG).show();
        return true;

    }

    private int getIndexSpinner(Spinner spinner, String string)
    {
        for (int i = 0; i < spinner.getCount() ; i++)
        {
            if(spinner.getItemAtPosition(i).toString().equalsIgnoreCase(string))
            {
                return i;
            }
        }

        return 0;
    }


    private void addUsersForInstrumentsToDataBase()
    {
        userDataReference.child(currentUserId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("mUserName"))
                    {
                        String name = dataSnapshot.child("mUserName").getValue().toString();
                        String instrument = instrumentsSP.getSelectedItem().toString();

                        HashMap instrumentMap = new HashMap();
                        instrumentMap.put("mUserId", currentUserId);
                        instrumentMap.put("mUserName", name);
                        instrumentMap.put("mInstrumentName", instrument);

                        instrumentsUsersReference.child(instrument).child(currentUserId).updateChildren(instrumentMap).addOnCompleteListener(new OnCompleteListener()
                        {
                            @Override
                            public void onComplete(@NonNull Task task)
                            {
                                if (task.isSuccessful()){
                                    Toast.makeText(AddInstrumentActivity.this, getResources().getString(R.string.instrument_added_successfully), Toast.LENGTH_LONG).show();
                                    progressDialog.dismiss();
                                }
                                else
                                {
                                    String message = task.getException().getMessage();
                                    Toast.makeText(AddInstrumentActivity.this, getResources().getString(R.string.error_occurred) + ": " + message, Toast.LENGTH_LONG).show();
                                    progressDialog.dismiss();
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });


    }

    private void addInstrumentsPlayedByUserToDataBase()
    {
        userDataReference.child(currentUserId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("mUserName"))
                    {
                        String name = dataSnapshot.child("mUserName").getValue().toString();
                        String instrument = instrumentsSP.getSelectedItem().toString();

                        HashMap instrumentMap = new HashMap();
                        instrumentMap.put("mUserId", currentUserId);
                        instrumentMap.put("mUserName", name);
                        instrumentMap.put("mInstrumentName", instrument);

                        usersInstrumentsReference.child(currentUserId).child(instrument).updateChildren(instrumentMap).addOnCompleteListener(new OnCompleteListener()
                        {
                            @Override
                            public void onComplete(@NonNull Task task)
                            {
                                if (task.isSuccessful()){
                                    Toast.makeText(AddInstrumentActivity.this, getResources().getString(R.string.instrument_added_successfully), Toast.LENGTH_LONG).show();
                                    progressDialog.dismiss();
                                }
                                else
                                {
                                    String message = task.getException().getMessage();
                                    Toast.makeText(AddInstrumentActivity.this, getResources().getString(R.string.error_occurred) + ": " + message, Toast.LENGTH_LONG).show();
                                    progressDialog.dismiss();
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });


    }


    private void deleteSingerFromDataBase(String userId)
    {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Singers").child(userId);
        databaseReference.keepSynced(true);
        databaseReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful()) {
                    Toast.makeText(AddInstrumentActivity.this, getResources().getString(R.string.singer_deleted_from_singers), Toast.LENGTH_LONG).show();
                }
                else
                {
                    String message = task.getException().getMessage();
                    Toast.makeText(AddInstrumentActivity.this, getResources().getString(R.string.error_occurred) + ": " + message, Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void addUserToSingersDatabase() {
        userDataReference.child(currentUserId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    String fullName = dataSnapshot.child("mUserName").getValue().toString();
                    boolean available = (boolean) dataSnapshot.child("mUserAvailable").getValue();

                    HashMap singerMap = new HashMap();
                    singerMap.put("mUserId", currentUserId);
                    singerMap.put("mUserName", fullName);
                    singerMap.put("mUserAvailable", available);

                    singersReference.child(currentUserId).updateChildren(singerMap).addOnCompleteListener(new OnCompleteListener()
                    {
                        @Override
                        public void onComplete(@NonNull Task task)
                        {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(AddInstrumentActivity.this, getResources().getString(R.string.singer_database_updated), Toast.LENGTH_LONG).show();
                            }
                            else
                            {
                                String message = task.getException().getMessage();
                                Toast.makeText(AddInstrumentActivity.this, getResources().getString(R.string.error_occurred) + ": " + message, Toast.LENGTH_LONG).show();
                            }
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
        Intent mainIntent = new Intent(AddInstrumentActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }

    private void saveUserSingerToDataBase()
    {

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
}
