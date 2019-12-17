package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class PersonActivity extends AppCompatActivity
{
    @BindView(R.id.person_toolbar)
    Toolbar toolbar;
    @BindView(R.id.person_image_CIV)
    CircleImageView profileImageCIV;
    @BindView(R.id.person_full_name_TV)
    TextView fullNameTV;
    @BindView(R.id.person_status_TV)
    TextView statusTV;
    @BindView(R.id.person_country_TV)
    TextView countryTV;
    @BindView(R.id.person_phone_TV)
    TextView phoneTV;
    @BindView(R.id.person_available_CB)
    CheckBox availableCB;
    @BindView(R.id.person_singer_CB)
    CheckBox singerCB;
    @BindView(R.id.person_composer_CB)
    CheckBox composerCB;
    @BindView(R.id.person_send_add_to_band_request_BT)
    Button sendRequestBT;
    @BindView(R.id.person_decline_add_band_request_BT)
    Button declineRequestBT;

    private DatabaseReference usersRef;
    private DatabaseReference addToBandRequestRef;
    private DatabaseReference bandsMusiciansRef;
    private FirebaseAuth mAuth;

    private String receiverUserId;
    private String senderUserId;
    private String CURRENT_STATE;
    private String saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        ButterKnife.bind(this);

        receiverUserId = getIntent().getExtras().get("selectedUser").toString();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();
        initialize();

        addToBandRequestRef = FirebaseDatabase.getInstance().getReference().child("BandUsersRequests");
        bandsMusiciansRef = FirebaseDatabase.getInstance().getReference().child("BandsMusicians");

        usersRef.child(receiverUserId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    String userProfileImage = dataSnapshot.child("mUserProfileImage").getValue().toString();
                    boolean userAvailable = (boolean) dataSnapshot.child("mUserAvailable").getValue();
                    boolean userSinger = (boolean)dataSnapshot.child("mUserSinger").getValue();
                    boolean userComposer = (boolean)dataSnapshot.child("mUserComposer").getValue();
                    String userName = dataSnapshot.child("mUserName").getValue().toString();
                    String userPhone = dataSnapshot.child("mUserPhone").getValue().toString();
                    String userStatus = dataSnapshot.child("mUserStatus").getValue().toString();
                    String userCountry = dataSnapshot.child("mUserCountry").getValue().toString();

                    Picasso.get().load(userProfileImage).placeholder(R.drawable.profile).into(profileImageCIV);
                    availableCB.setChecked(userAvailable);
                    composerCB.setChecked(userComposer);
                    singerCB.setChecked(userSinger);
                    fullNameTV.setText(userName);
                    phoneTV.setText(userPhone);
                    countryTV.setText(userCountry);
                    statusTV.setText(userStatus);

                    keepButtonsText();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        declineRequestBT.setVisibility(View.INVISIBLE);
        declineRequestBT.setEnabled(false);

        if(!senderUserId.equals(receiverUserId))
        {
            sendRequestBT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendRequestBT.setEnabled(false);
                    if(CURRENT_STATE.equals(getResources().getString(R.string.not_from_same_band)))
                    {
                        sendRequestToAPerson();
                    }
                    if (CURRENT_STATE.equals(getResources().getString(R.string.request_sent)))
                    {
                        cancelRequest();
                    }
                    if (CURRENT_STATE.equals(getResources().getString(R.string.request_received)))
                    {
                        acceptRequest();
                    }
                    if (CURRENT_STATE.equals(getResources().getString(R.string.from_same_band)))
                    {
                        quitBand();
                    }
                }
            });
        }
        else
        {
            declineRequestBT.setVisibility(View.INVISIBLE);
            sendRequestBT.setVisibility(View.INVISIBLE);
        }

    }

    private void quitBand()
    {
        bandsMusiciansRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    bandsMusiciansRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                sendRequestBT.setEnabled(true);
                                CURRENT_STATE = getResources().getString(R.string.not_from_same_band);
                                sendRequestBT.setText(getResources().getString(R.string.add_to_band_request));

                                declineRequestBT.setVisibility(View.INVISIBLE);
                                declineRequestBT.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });


    }

    private void acceptRequest()
    {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        bandsMusiciansRef.child(senderUserId).child(receiverUserId).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    bandsMusiciansRef.child(receiverUserId).child(senderUserId).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                addToBandRequestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if(task.isSuccessful())
                                        {
                                            addToBandRequestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {
                                                    if(task.isSuccessful())
                                                    {
                                                        sendRequestBT.setEnabled(true);
                                                        CURRENT_STATE = getResources().getString(R.string.from_same_band);
                                                        sendRequestBT.setText(getResources().getString(R.string.quit_band));

                                                        declineRequestBT.setVisibility(View.INVISIBLE);
                                                        declineRequestBT.setEnabled(false);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });



    }

    private void cancelRequest()
    {
        addToBandRequestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    addToBandRequestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                sendRequestBT.setEnabled(true);
                                CURRENT_STATE = getResources().getString(R.string.not_from_same_band);
                                sendRequestBT.setText(getResources().getString(R.string.add_to_band_request));

                                declineRequestBT.setVisibility(View.INVISIBLE);
                                declineRequestBT.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });

    }

    private void keepButtonsText()
    {
        addToBandRequestRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.hasChild(receiverUserId))
                {
                    String request_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();
                    if(request_type.equals("sent"))
                    {
                        CURRENT_STATE = getResources().getString(R.string.request_sent);
                        sendRequestBT.setText(getResources().getString(R.string.cancel_request));

                        declineRequestBT.setVisibility(View.INVISIBLE);
                        declineRequestBT.setEnabled(false);
                    }
                    else if (request_type.equals("received"))
                    {
                        CURRENT_STATE = getResources().getString(R.string.request_received);
                        sendRequestBT.setText(getResources().getString(R.string.accept_request));

                        declineRequestBT.setVisibility(View.VISIBLE);
                        declineRequestBT.setEnabled(true);

                        declineRequestBT.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                cancelRequest();
                            }
                        });
                    }
                }
                else
                {
                    bandsMusiciansRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(receiverUserId))
                            {
                                CURRENT_STATE = getResources().getString(R.string.from_same_band);
                                sendRequestBT.setText(getResources().getString(R.string.quit_band));
                                declineRequestBT.setVisibility(View.INVISIBLE);
                                declineRequestBT.setEnabled(false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendRequestToAPerson()
    {
        addToBandRequestRef.child(senderUserId).child(receiverUserId).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    addToBandRequestRef.child(receiverUserId).child(senderUserId).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                sendRequestBT.setEnabled(true);
                                CURRENT_STATE = getResources().getString(R.string.request_sent);
                                sendRequestBT.setText(getResources().getString(R.string.cancel_request));

                                declineRequestBT.setVisibility(View.INVISIBLE);
                                declineRequestBT.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });

    }

    private void initialize()
    {

        CURRENT_STATE = getResources().getString(R.string.not_from_same_band);
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
        Intent mainIntent = new Intent(PersonActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }


}
