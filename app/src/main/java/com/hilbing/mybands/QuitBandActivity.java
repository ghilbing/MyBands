package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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

        bandsMusiciansRef = FirebaseDatabase.getInstance().getReference().child("BandsMusicians");
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
                               /* //  sendRequestBT.setEnabled(true);
                                CURRENT_STATE = getResources().getString(R.string.not_from_same_band);
                                //   sendRequestBT.setText(getResources().getString(R.string.add_to_band_request));

                                declineRequestBT.setVisibility(View.INVISIBLE);
                                declineRequestBT.setEnabled(false);*/
                            }
                        }
                    });
                }
            }
        });


    }


}
