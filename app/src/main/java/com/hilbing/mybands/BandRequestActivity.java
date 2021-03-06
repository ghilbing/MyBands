package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class BandRequestActivity extends AppCompatActivity {

    @BindView(R.id.band_request_toolbar)
    Toolbar toolbar;
    @BindView(R.id.band_request_image_CIV)
    CircleImageView bandImageCIV;
    @BindView(R.id.band_request_name_TV)
    TextView bandNameTV;
    @BindView(R.id.band_request_person_full_name_TV)
    TextView personNameTV;
    @BindView(R.id.band_request_person_country_TV)
    TextView countryTV;
    @BindView(R.id.band_request_person_phone_TV)
    TextView phoneTV;
    @BindView(R.id.band_request_person_send_add_to_band_request_BT)
    Button acceptRequestBT;
    @BindView(R.id.band_request_person_decline_add_band_request_BT)
    Button declineRequestBT;

    private DatabaseReference usersRef;
    private DatabaseReference addToBandRequestRef;
    private DatabaseReference bandsMusiciansRef;
    private DatabaseReference bandsDataRef;
    private FirebaseAuth mAuth;

    private String receiverUserId;
    private String senderUserId;
    private String CURRENT_STATE;
    private String saveCurrentDate;
    private String currentBandId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_band_request);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        actionBar.setTitle(R.string.band_request);


        senderUserId = getIntent().getExtras().get("idSender").toString();
        currentBandId = getIntent().getExtras().get("idBand").toString();
        Log.d(">>>>>>>>>>>>>.", "Band: " + currentBandId + " Sender: " + senderUserId);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        receiverUserId = mAuth.getCurrentUser().getUid();

        if (senderUserId.equals(receiverUserId)) {
            Toast.makeText(BandRequestActivity.this, "ARE THE SAME", Toast.LENGTH_LONG).show();
            // sendUserToMainActivity();
        }

        initialize();

        acceptRequestBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptRequest();
            }
        });

        declineRequestBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelRequest();
            }
        });


        addToBandRequestRef = FirebaseDatabase.getInstance().getReference().child("BandUsersRequests");
        addToBandRequestRef.keepSynced(true);
        bandsMusiciansRef = FirebaseDatabase.getInstance().getReference().child("BandsMusicians");
        bandsMusiciansRef.keepSynced(true);
        bandsDataRef = FirebaseDatabase.getInstance().getReference().child("Bands");
        bandsDataRef.keepSynced(true);

        usersRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // String userProfileImage = dataSnapshot.child("mUserProfileImage").getValue().toString();
                    String userName = dataSnapshot.child("mUserName").getValue().toString();
                    String userPhone = dataSnapshot.child("mUserPhone").getValue().toString();
                    String userCountry = dataSnapshot.child("mUserCountry").getValue().toString();

                    //   Picasso.get().load(userProfileImage).placeholder(R.drawable.profile).into(bandImageCIV);

                    personNameTV.setText(userName);
                    phoneTV.setText(userPhone);
                    countryTV.setText(userCountry);

                    //  keepButtonsText();
                } else {
                    Toast.makeText(BandRequestActivity.this, getResources().getString(R.string.error_occurred), Toast.LENGTH_LONG).show();
                    sendUserToMainActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        bandsDataRef.child(currentBandId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final String bandImage = dataSnapshot.child("mBandImage").getValue().toString();
                    String bandName = dataSnapshot.child("mBandName").getValue().toString();
                    bandNameTV.setText(bandName);
                    Picasso.get().load(bandImage).networkPolicy(NetworkPolicy.OFFLINE).into(bandImageCIV, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(bandImage).placeholder(R.drawable.profile).into(bandImageCIV);
                        }
                    });

                } else {
                    Toast.makeText(BandRequestActivity.this, getResources().getString(R.string.error_occurred), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void sendRequestToAPerson() {
        addToBandRequestRef.child(senderUserId).child(currentBandId).child(receiverUserId).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    addToBandRequestRef.child(receiverUserId).child(currentBandId).child(senderUserId).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //  sendRequestBT.setEnabled(true);
                                CURRENT_STATE = getResources().getString(R.string.request_sent);
                                //   sendRequestBT.setText(getResources().getString(R.string.cancel_request));

                                declineRequestBT.setVisibility(View.INVISIBLE);
                                declineRequestBT.setEnabled(false);
                            } else {
                                String message = task.getException().getMessage();
                                Toast.makeText(BandRequestActivity.this, message, Toast.LENGTH_LONG).show();
                                sendUserToMainActivity();
                            }
                        }
                    });
                } else {
                    String message = task.getException().getMessage();
                    Toast.makeText(BandRequestActivity.this, message, Toast.LENGTH_LONG).show();
                    sendUserToMainActivity();
                }
            }
        });

    }

    private void cancelRequest() {
        addToBandRequestRef.child(senderUserId).child(currentBandId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    addToBandRequestRef.child(receiverUserId).child(currentBandId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                CURRENT_STATE = getResources().getString(R.string.not_from_same_band);
                                acceptRequestBT.setVisibility(View.INVISIBLE);
                                acceptRequestBT.setEnabled(false);

                            } else {
                                String message = task.getException().getMessage();
                                Toast.makeText(BandRequestActivity.this, message, Toast.LENGTH_LONG).show();
                                sendUserToMainActivity();
                            }
                        }
                    });
                } else {
                    String message = task.getException().getMessage();
                    Toast.makeText(BandRequestActivity.this, message, Toast.LENGTH_LONG).show();
                    sendUserToMainActivity();
                }
            }
        });

    }

    private void acceptRequest() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        bandsMusiciansRef.child(receiverUserId).child(currentBandId).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    bandsMusiciansRef.child(currentBandId).child(receiverUserId).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                addToBandRequestRef.child(senderUserId).child(currentBandId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            addToBandRequestRef.child(receiverUserId).child(currentBandId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        //  sendRequestBT.setEnabled(true);
                                                        CURRENT_STATE = getResources().getString(R.string.from_same_band);
                                                        //  sendRequestBT.setText(getResources().getString(R.string.quit_band));

                                                        declineRequestBT.setVisibility(View.INVISIBLE);
                                                        declineRequestBT.setEnabled(false);
                                                    } else {
                                                        String message = task.getException().getMessage();
                                                        Toast.makeText(BandRequestActivity.this, message, Toast.LENGTH_LONG).show();
                                                        sendUserToMainActivity();
                                                    }
                                                }
                                            });
                                        } else {
                                            String message = task.getException().getMessage();
                                            Toast.makeText(BandRequestActivity.this, message, Toast.LENGTH_LONG).show();
                                            sendUserToMainActivity();
                                        }
                                    }
                                });
                            } else {
                                String message = task.getException().getMessage();
                                Toast.makeText(BandRequestActivity.this, message, Toast.LENGTH_LONG).show();
                                sendUserToMainActivity();
                            }
                        }
                    });

                } else {
                    String message = task.getException().getMessage();
                    Toast.makeText(BandRequestActivity.this, message, Toast.LENGTH_LONG).show();
                    sendUserToMainActivity();
                }
            }
        });


    }


    private void quitBand() {
        bandsMusiciansRef.child(senderUserId).child(currentBandId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    bandsMusiciansRef.child(receiverUserId).child(currentBandId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //  sendRequestBT.setEnabled(true);
                                CURRENT_STATE = getResources().getString(R.string.not_from_same_band);
                                //   sendRequestBT.setText(getResources().getString(R.string.add_to_band_request));

                                declineRequestBT.setVisibility(View.INVISIBLE);
                                declineRequestBT.setEnabled(false);
                            } else {
                                String message = task.getException().getMessage();
                                Toast.makeText(BandRequestActivity.this, message, Toast.LENGTH_LONG).show();
                                sendUserToMainActivity();
                            }
                        }
                    });
                } else {
                    String message = task.getException().getMessage();
                    Toast.makeText(BandRequestActivity.this, message, Toast.LENGTH_LONG).show();
                    sendUserToMainActivity();
                }
            }
        });


    }

    private void initialize() {

        CURRENT_STATE = getResources().getString(R.string.not_from_same_band);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            sendUserToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(BandRequestActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);

    }

}
