package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity
{

    @BindView(R.id.profile_toolbar)
    Toolbar toolbar;
    @BindView(R.id.profile_image_CIV)
    CircleImageView profileImageCIV;
    @BindView(R.id.profile_full_name_TV)
    TextView fullNameTV;
    @BindView(R.id.profile_status_TV)
    TextView statusTV;
    @BindView(R.id.profile_country_TV)
    TextView countryTV;
    @BindView(R.id.profile_phone_TV)
    TextView phoneTV;
    @BindView(R.id.profile_available_CB)
    CheckBox availableCB;
    @BindView(R.id.profile_singer_CB)
    CheckBox singerCB;
    @BindView(R.id.profile_composer_CB)
    CheckBox composerCB;

    private DatabaseReference profileUserReference;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.profile));

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        profileUserReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);

        profileUserReference.addValueEventListener(new ValueEventListener()
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
        Intent mainIntent = new Intent(ProfileActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }
}
