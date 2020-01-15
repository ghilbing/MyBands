package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ClickInstrumentActivity extends AppCompatActivity {

    @BindView(R.id.spinner_instrument_SP)
    Spinner instrumentSP;
    @BindView(R.id.click_instrument_delete_BT)
    Button deleteBT;

    private String instrumentKey;
    private String currentUserId;
    private String databaseUserId;

    private DatabaseReference instrumentsUsersReference;
    private DatabaseReference usersInstrumentsReference;
    private DatabaseReference deleteUpdateInstrumentUserReference;
    private DatabaseReference deleteUpdateUserInstrumentReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_instrument);

        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();

        instrumentKey = getIntent().getExtras().get("InstrumentKey").toString();
        currentUserId = mAuth.getCurrentUser().getUid();

        deleteBT.setVisibility(View.INVISIBLE);

        instrumentsUsersReference = FirebaseDatabase.getInstance().getReference().child("instruments_users").child(instrumentKey);
        instrumentsUsersReference.keepSynced(true);
        usersInstrumentsReference = FirebaseDatabase.getInstance().getReference().child("users_instruments").child(currentUserId);
        usersInstrumentsReference.keepSynced(true);
        deleteUpdateInstrumentUserReference = FirebaseDatabase.getInstance().getReference().child("instruments_users").child(instrumentKey).child(currentUserId);
        deleteUpdateInstrumentUserReference.keepSynced(true);
        deleteUpdateUserInstrumentReference = FirebaseDatabase.getInstance().getReference().child("users_instruments").child(currentUserId).child(instrumentKey);
        deleteUpdateUserInstrumentReference.keepSynced(true);

        usersInstrumentsReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists()) {
                    instrumentSP.setSelection(getIndexSpinner(instrumentSP, instrumentKey));
                    databaseUserId = dataSnapshot.child(currentUserId).getKey();

                    if (currentUserId.equals(databaseUserId)) {
                        deleteBT.setVisibility(View.VISIBLE);

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        deleteBT.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                deleteInstrument();
            }
        });


    }


    private void deleteInstrument()
    {

        deleteUpdateUserInstrumentReference.removeValue();
        deleteUpdateInstrumentUserReference.removeValue();
        sendUserToMainActivity();
        Toast.makeText(ClickInstrumentActivity.this, getResources().getString(R.string.instrument_deleted), Toast.LENGTH_LONG).show();

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

    private void sendUserToMainActivity()
    {

        Intent mainIntent = new Intent(ClickInstrumentActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
