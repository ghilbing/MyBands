package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.navigationView_NV)
    NavigationView navigationViewNV;
    @BindView(R.id.drawerLayout_DL)
    DrawerLayout drawerLayout;
    @BindView(R.id.main_search_BT)
    ImageButton searchBT;
    @BindView(R.id.events_RV)
    RecyclerView recyclerView;
    Toolbar mToolbar;
    private CircleImageView navProfileCIV;
    private TextView navUserNameTV;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private FirebaseAuth mAuth;
    private DatabaseReference usersReference;
    String currentUserID;



    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mToolbar = findViewById(R.id.main_page_toolbar);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        View navHeader = navigationViewNV.inflateHeaderView(R.layout.nav_header);
        navUserNameTV = navHeader.findViewById(R.id.nav_header_user_name_TV);
        navProfileCIV = navHeader.findViewById(R.id.nav_header_user_image_CIV);
        usersReference.child(currentUserID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {

                    if(dataSnapshot.hasChild("mUserName"))
                    {
                        String fullName = dataSnapshot.child("mUserName").getValue().toString();
                        navUserNameTV.setText(fullName);
                    }
                    if(dataSnapshot.hasChild("mUserProfileImage"))
                    {
                        String imagePath = dataSnapshot.child("mUserProfileImage").getValue().toString();
                        Picasso.get().load(imagePath).placeholder(R.drawable.profile).into(navProfileCIV);
                    }
                    else
                        {
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.profile_name_does_not_exist), Toast.LENGTH_LONG).show();
                       // sendUserToSetUpActivity();
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });



        navigationViewNV.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
            {
                userSelector(menuItem);
                return false;
            }
        });

    }

    @Override
    public void onBackPressed()
    {
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else
            {

            super.onBackPressed();
        }
    }

    private void userSelector(MenuItem menuItem)
    {

        switch (menuItem.getItemId())
        {
            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
             //   getSupportActionBar().setTitle("Home");
                //  getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
                break;
            case R.id.nav_profile:
                sendUserToProfileActivity();
                break;
            case R.id.nav_instruments:
                sendUserToAddInstrumentActivity();
                //    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new InstrumentFragment()).commit();
                break;
            case R.id.nav_band:
               sendUserToAddBandActivity();
                break;
            case R.id.nav_musicians:
                sendUserToMusiciansActivity();
                break;
            case R.id.nav_messages:
                sendUserToMusiciansActivity();
                break;
            case R.id.nav_song:
                Toast.makeText(this, "Songs", Toast.LENGTH_SHORT).show();
                //    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SongFragment()).commit();
                break;
            case R.id.nav_playlists:
                Toast.makeText(this, "Playlists", Toast.LENGTH_SHORT).show();
                //    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PlaylistFragment()).commit();
                break;
            case R.id.nav_rehearsals:
                Toast.makeText(this, "Rehearsals", Toast.LENGTH_SHORT).show();
                //     getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new RehearsalFragment()).commit();
                break;
            case R.id.nav_concerts:
                Toast.makeText(this, "Concerts", Toast.LENGTH_SHORT).show();
                //     getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ConcertFragment()).commit();
                break;
            case R.id.nav_share:
                Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
                //    Toast.makeText(getApplicationContext(), "Share option menu", Toast.LENGTH_LONG).show();
                break;
            case R.id.nav_settings:
               sendUserToSettingsActivity();
                break;
            case R.id.nav_logout:
                mAuth.signOut();
                sendUserToLoginActivity();
                break;
            }

    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {

        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null)
        {
            sendUserToLoginActivity();
        }
        else
            {
            checkIfUserExists();
             }

    }

    private void checkIfUserExists()
    {
        final String currentUserId = mAuth.getCurrentUser().getUid();
        usersReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(!dataSnapshot.hasChild(currentUserId)){
                    Log.e("", "ENTRA");
                    sendUserToSetUpActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void sendUserToSetUpActivity()
    {
        Intent setUpIntent = new Intent(MainActivity.this, SetUpActivity.class);
        setUpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setUpIntent);
        finish();
    }

    private void sendUserToLoginActivity()
    {

        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    public void sendUserToAddInstrumentActivity()
    {
        Intent addInstrumentIntent = new Intent(MainActivity.this, AddInstrumentActivity.class);
        addInstrumentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(addInstrumentIntent);
        finish();
    }

    private void sendUserToSettingsActivity()
    {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    public void sendUserToProfileActivity()
    {
        Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
        profileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(profileIntent);
        finish();
    }

    public void sendUserToAddBandActivity()
    {
        Intent addBandIntent = new Intent(MainActivity.this, AddBandActivity.class);
        addBandIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(addBandIntent);
        finish();
    }

    private void sendUserToMusiciansActivity()
    {
        Intent musiciansIntent = new Intent(MainActivity.this, MusiciansActivity.class);
        musiciansIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(musiciansIntent);
        finish();
    }

    /*Calendar calForDate = Calendar.getInstance();
    SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
    String saveCurrentDate = currentDate.format(calForDate.getTime());

    Calendar calForTime = Calendar.getInstance();
    SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
    String saveCurrentTime = currentDate.format(calForTime.getTime());*/
}
