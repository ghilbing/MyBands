package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;

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
    @BindView(R.id.click_instrument_update_BT)
    Button updateBT;
    @BindView(R.id.click_instrument_delete_BT)
    Button deleteBT;

    private String instrumentKey;
    private String userKey;

    private DatabaseReference instrumentsUsersReference;
    private DatabaseReference usersInstrumentsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_instrument);

        ButterKnife.bind(this);

        instrumentKey = getIntent().getExtras().get("InstrumentKey").toString();
        userKey = getIntent().getExtras().get("UserKey").toString();


        instrumentsUsersReference = FirebaseDatabase.getInstance().getReference().child("instruments_users").child(instrumentKey);
        usersInstrumentsReference = FirebaseDatabase.getInstance().getReference().child("users_instruments").child(userKey);

        usersInstrumentsReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String instrument = dataSnapshot.child(instrumentKey).getValue().toString();
                instrumentSP.setSelection(getIndexSpinner(instrumentSP, instrument));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

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
}
