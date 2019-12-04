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
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.navigationView_NV)
    NavigationView navigationViewNV;
    @BindView(R.id.drawerLayout_DL)
    DrawerLayout drawerLayout;
    @BindView(R.id.events_RV)
    RecyclerView recyclerView;
    Toolbar mToolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private FirebaseAuth mAuth;
    private DatabaseReference usersReference;



    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mToolbar = findViewById(R.id.main_page_toolbar);

        mAuth = FirebaseAuth.getInstance();
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationViewNV.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                userSelector(menuItem);
                return false;
            }
        });

    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {

            super.onBackPressed();
        }
    }

    private void userSelector(MenuItem menuItem) {

        switch (menuItem.getItemId()){
            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
             //   getSupportActionBar().setTitle("Home");
                //  getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
                break;
            case R.id.nav_profile:
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
            //    getSupportActionBar().setTitle("Profile");
                //   getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
                break;
            case R.id.nav_instruments:
                Toast.makeText(this, "Instruments", Toast.LENGTH_SHORT).show();
                //    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new InstrumentFragment()).commit();
                break;
            case R.id.nav_band:
                Toast.makeText(this, "Bands", Toast.LENGTH_SHORT).show();
                //    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new BandFragment()).commit();
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
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                //    Toast.makeText(getApplicationContext(), "Settings option menu", Toast.LENGTH_LONG).show();
                break;
            case R.id.nav_logout:
                mAuth.signOut();
                sendUserToLoginActivity();
                break;
            }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            sendUserToLoginActivity();
        } else {
            checkIfUserExists();
        }

    }

    private void checkIfUserExists() {
        final String currentUserId = mAuth.getCurrentUser().getUid();
        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(currentUserId)){
                    Log.e("", "ENTRA");
                    sendUserToSetUpActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToSetUpActivity() {
        Intent setUpIntent = new Intent(MainActivity.this, SetUpActivity.class);
        setUpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setUpIntent);
        finish();
    }

    private void sendUserToLoginActivity() {

        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();

    }
}
