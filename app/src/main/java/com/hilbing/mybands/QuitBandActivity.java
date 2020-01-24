package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;

public class QuitBandActivity extends AppCompatActivity {

    @BindView(R.id.quit_band_BT)
    Button quitBandBT;
    @BindView(R.id.quit_band_message_TV)
    TextView message;
    @BindView(R.id.quit_band_scrollView_SV)
    ScrollView scrollViewSV;
    @BindView(R.id.quit_band_toolbar)
    Toolbar toolbar;

    DatabaseReference bandsMusiciansRef;
    FirebaseAuth mAuth;

    private String currentUserId;
    private String currentBandId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quit_band);

        ButterKnife.bind(this);

        SharedPreferences preferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
        currentBandId = preferences.getString("currentBandIdPref", "");

        mAuth = FirebaseAuth.getInstance();


        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle(R.string.quit_band);

        if(TextUtils.isEmpty(currentBandId)){
            message.setVisibility(View.VISIBLE);
            scrollViewSV.setVisibility(View.INVISIBLE);
        } else {
            message.setVisibility(View.INVISIBLE);
            scrollViewSV.setVisibility(View.VISIBLE);
        }

        bandsMusiciansRef = FirebaseDatabase.getInstance().getReference().child("BandsMusicians");
        bandsMusiciansRef.keepSynced(true);
        currentUserId = mAuth.getCurrentUser().getUid();

        quitBandBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quitBand();
            }
        });


    }

    private void quitBand()
    {
        bandsMusiciansRef.child(currentUserId).child(currentBandId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    bandsMusiciansRef.child(currentBandId).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(QuitBandActivity.this, getResources().getString(R.string.you_do_not_belong_to_the_band_any_more), Toast.LENGTH_LONG).show();
                                sendUserToMainActivity();
                            }
                        }
                    });
                }
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

    private void sendUserToMainActivity() {

        Intent mainIntent = new Intent(QuitBandActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


}
