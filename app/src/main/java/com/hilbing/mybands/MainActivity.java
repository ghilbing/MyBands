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
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ListView;
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
import com.hilbing.mybands.adapters.BandAlertAdapter;
import com.hilbing.mybands.adapters.ExpandableListAdapter;
import com.hilbing.mybands.models.Band;
import com.hilbing.mybands.models.MenuModel;
import com.hilbing.mybands.models.MusiciansBands;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.navigationView_NV)
    NavigationView navigationViewNV;
    @BindView(R.id.drawerLayout_DL)
    DrawerLayout drawerLayout;
    private Toolbar mToolbar;
    private CircleImageView navProfileCIV;
    private TextView navUserNameTV;
    private TextView navBandNameTV;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private FirebaseAuth mAuth;
    private DatabaseReference usersReference;
    private DatabaseReference bandsMusiciansReference;
    private DatabaseReference bandsReference;
    private DatabaseReference bandsRequestReference;
    String currentUserID;
    String currentBandId;
    String currentBandIdPref;
    private boolean openDialog;

    ExpandableListAdapter expandableListAdapter;
    @BindView(R.id.nav_expandableListView)
    ExpandableListView expandableListView;
    List<MenuModel> headerList = new ArrayList<>();
    HashMap<MenuModel, List<MenuModel>> childList = new HashMap<>();
    List<Band> bands = new ArrayList();




    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        bandsMusiciansReference = FirebaseDatabase.getInstance().getReference().child("BandsMusicians");
        bandsRequestReference = FirebaseDatabase.getInstance().getReference().child("BandUsersRequests");
        bandsReference = FirebaseDatabase.getInstance().getReference().child("Bands");

        Intent intent = getIntent();
        if(intent != null){
            currentBandId = intent.getStringExtra("currentBandId");

            if(currentBandId != null){
                bandsReference.child(currentBandId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            if(dataSnapshot.hasChild("mBandName"))
                            {
                                String name = dataSnapshot.child("mBandName").getValue().toString();
                                Log.d("MainActivity", name);
                            }
                            else
                            {
                                Toast.makeText(MainActivity.this, getResources().getString(R.string.band_does_not_exist), Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }


        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        prepareMenuData();
        populateExpandableList();

        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        View navHeader = navigationViewNV.inflateHeaderView(R.layout.nav_header);
        navUserNameTV = navHeader.findViewById(R.id.nav_header_user_name_TV);
        navProfileCIV = navHeader.findViewById(R.id.nav_header_user_image_CIV);
        navBandNameTV = navHeader.findViewById(R.id.nav_header_current_band_TV);

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
              //  userSelector(menuItem);
                return false;
            }
        });

    }

    private void prepareMenuData() {

        MenuModel menuModel = new MenuModel(getResources().getString(R.string.home), false, true);
        headerList.add(menuModel);
        if (!menuModel.hasChildren)
        {
            childList.put(menuModel, null);
        }

        menuModel = new MenuModel(getResources().getString(R.string.user_profile), true, true);
        headerList.add(menuModel);
        List<MenuModel> childModelList = new ArrayList<>();
        MenuModel childModel = new MenuModel(getResources().getString(R.string.profile), false, false);
        childModelList.add(childModel);
        childModel = new MenuModel(getResources().getString(R.string.instruments), false, false);
        childModelList.add(childModel);

        if(menuModel.hasChildren)
        {
            childList.put(menuModel, childModelList);
        }

        childModelList = new ArrayList<>();
        menuModel = new MenuModel(getResources().getString(R.string.bands), true, true);
        headerList.add(menuModel);
        childModel = new MenuModel(getResources().getString(R.string.create_band), false, false);
        childModelList.add(childModel);
        childModel = new MenuModel(getResources().getString(R.string.my_bands), false, false);
        childModelList.add(childModel);
        childModel = new MenuModel(getResources().getString(R.string.find_musicians), false, false);
        childModelList.add(childModel);
        childModel = new MenuModel(getResources().getString(R.string.send_message), false, false);
        childModelList.add(childModel);

        if(menuModel.hasChildren)
        {
            childList.put(menuModel, childModelList);
        }

        childModelList = new ArrayList<>();
        menuModel = new MenuModel(getResources().getString(R.string.songs), false, true);
        headerList.add(menuModel);
        if (!menuModel.hasChildren)
        {
            childList.put(menuModel, null);
        }
        menuModel = new MenuModel(getResources().getString(R.string.playlists), false, true);
        headerList.add(menuModel);
        if (!menuModel.hasChildren)
        {
            childList.put(menuModel, null);
        }
        menuModel = new MenuModel(getResources().getString(R.string.events), true, true);
        headerList.add(menuModel);
        childModel = new MenuModel(getResources().getString(R.string.rehearsals), false, false);
        childModelList.add(childModel);
        childModel = new MenuModel(getResources().getString(R.string.concerts), false, false);
        childModelList.add(childModel);
        if(menuModel.hasChildren)
        {
            childList.put(menuModel, childModelList);
        }
        childModelList = new ArrayList<>();
        menuModel = new MenuModel(getResources().getString(R.string.settings), false, true);
        headerList.add(menuModel);
        if (!menuModel.hasChildren)
        {
            childList.put(menuModel, null);
        }
        menuModel = new MenuModel(getResources().getString(R.string.logout), false, true);
        headerList.add(menuModel);
        if (!menuModel.hasChildren)
        {
            childList.put(menuModel, null);
        }

    }

    public void populateExpandableList(){
        expandableListAdapter = new ExpandableListAdapter(this, headerList, childList);
        expandableListView.setAdapter(expandableListAdapter);

        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                if(headerList.get(i).isGroup){
                    if(!headerList.get(i).hasChildren){
                        String title = headerList.get(i).menuName;
                        if(title.equals(getResources().getString(R.string.home))){
                            Toast.makeText(MainActivity.this, "Home", Toast.LENGTH_SHORT).show();
                        }
                        else if(title.equals(getResources().getString(R.string.songs))){
                            Toast.makeText(MainActivity.this, "Songs", Toast.LENGTH_LONG).show();
                        }
                        else if(title.equals(getResources().getString(R.string.playlists))){
                            Toast.makeText(MainActivity.this, "Playlists", Toast.LENGTH_LONG).show();
                        }
                        else if (title.equals(getResources().getString(R.string.settings))){
                            sendUserToSettingsActivity();
                        }
                        else if(title.equals(getResources().getString(R.string.logout))){
                            mAuth.signOut();
                            sendUserToLoginActivity();
                        }

                    }
                }
                return false;
            }
        });
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                if(childList.get(headerList.get(i))!= null){
                    MenuModel menuModel = childList.get(headerList.get(i)).get(i1);
                    if(menuModel.menuName.length() > 0){
                       String subTitle = menuModel.menuName;
                       if(subTitle.equals(getResources().getString(R.string.profile))){
                           sendUserToProfileActivity();
                       }
                       else if(subTitle.equals(getResources().getString(R.string.instruments))){
                           sendUserToAddInstrumentActivity();
                       }
                       else if(subTitle.equals(getResources().getString(R.string.create_band))){
                           sendUserToAddBandActivity();
                       }
                       else if(subTitle.equals(getResources().getString(R.string.find_musicians))){
                           sendUsertoFindMusicians();
                       }
                       else if(subTitle.equals(getResources().getString(R.string.my_bands))){
                           sendUserToMyBands();
                       }
                       else if(subTitle.equals(getResources().getString(R.string.send_message))){
                           sendUserToMessagesActivity();
                       }
                       else if(subTitle.equals(getResources().getString(R.string.rehearsals))){
                           Toast.makeText(MainActivity.this, "Rehearsals", Toast.LENGTH_LONG).show();
                       }
                       else if(subTitle.equals(getResources().getString(R.string.concerts))){
                           Toast.makeText(MainActivity.this, "Concerts", Toast.LENGTH_LONG).show();
                       }
                    }
                }
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

        SharedPreferences preferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
        currentBandIdPref = preferences.getString("currentBandIdPref", "");
        Log.d("MainActivitySharedPreferences", currentBandIdPref);

        if(TextUtils.isEmpty(currentBandIdPref)){
            bandsReference.child(currentBandIdPref).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        if(dataSnapshot.hasChild("mBandName"))
                        {
                            String name = dataSnapshot.child("mBandName").getValue().toString();
                            Log.d(".......................", name);
                            navBandNameTV.setText(name);
                        }
                        else
                        {
                            Toast.makeText(MainActivity.this, getResources().getString(R.string.band_does_not_exist), Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null)
        {
            sendUserToLoginActivity();
        }
        else
            {
            checkIfUserExists();
            checkIfUserBelongsToBand();
           // checkIfUserHasARequest();
             }

    }

    private void checkIfUserHasARequest() {



        bandsRequestReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(currentUserID)){
                    for(DataSnapshot ds : dataSnapshot.child(currentUserID).getChildren()){
                        final String bandKey = ds.getKey();
                        bandsReference.child(bandKey).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    String idSender = dataSnapshot.child("mBandCreator").getValue().toString();

                                        sendUserToBandRequestActivity(bandKey, idSender);


                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void checkIfUserBelongsToBand() {

        bandsMusiciansReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(!dataSnapshot.hasChild(currentUserID))
                {

                    Toast.makeText(MainActivity.this, getResources().getString(R.string.you_need_to_belong_to_a_band), Toast.LENGTH_LONG).show();
                    checkIfUserHasARequest();
                }
                else
                    {
                    if (dataSnapshot.hasChild(currentUserID))
                    {
                        int count = (int) dataSnapshot.child(currentUserID).getChildrenCount();
                        if (count > 1 && TextUtils.isEmpty(currentBandIdPref)) {

                            sendUserToMyBands();

                           /* Toast.makeText(MainActivity.this, getResources().getString(R.string.please_select_a_band), Toast.LENGTH_LONG).show();
                            for(DataSnapshot ds :dataSnapshot.child(currentUserID).getChildren()){
                                final String key = ds.getKey();

                                bandsReference.child(key).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()){
                                            String name = dataSnapshot.child("mBandName").getValue().toString();
                                            String image = dataSnapshot.child("mBandImage").getValue().toString();
                                            String id = dataSnapshot.child("mBandId").getValue().toString();
                                            Band band = new Band(id,name, image);
                                            bands.add(band);
                                        }
                                        if(openDialog == false) {
                                           // openBandDialog();
                                            openDialog = true;
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                Log.d("MainActivity", key);

                            }*/


                        } else if (count > 1 && !TextUtils.isEmpty(currentBandIdPref)) {

                                bandsReference.child(currentBandIdPref).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()){
                                            if(dataSnapshot.hasChild("mBandName"))
                                            {
                                                String name = dataSnapshot.child("mBandName").getValue().toString();
                                                navBandNameTV.setText(name);
                                            }
                                            else
                                            {
                                                Toast.makeText(MainActivity.this, getResources().getString(R.string.band_does_not_exist), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });


                    }

                        else {

                            for(DataSnapshot ds : dataSnapshot.child(currentUserID).getChildren()){
                                currentBandId = ds.getKey();

                                if(currentBandId != null){
                                    bandsReference.child(currentBandId).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()){
                                                if(dataSnapshot.hasChild("mBandName"))
                                                {
                                                    String name = dataSnapshot.child("mBandName").getValue().toString();
                                                    navBandNameTV.setText(name);
                                                }
                                                else
                                                {
                                                    Toast.makeText(MainActivity.this, getResources().getString(R.string.band_does_not_exist), Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

 /*   private void openBandDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.alert_bands, null);
        ListView listView = view.findViewById(R.id.alert_band_list_LV);
        listView.setAdapter(new BandAlertAdapter(this, (ArrayList<Band>) bands));

        builder.setView(view);

        builder.setTitle(getResources().getString(R.string.select_a_band));

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                sendUserToMainActivity();
                alertDialog.dismiss();
                openDialog = false;
            }
        });

    }*/

    private void sendUserToMainActivity() {
        Intent mainActivityIntent = new Intent(MainActivity.this, MainActivity.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mainActivityIntent.putExtra("currentBandId", currentBandId);
        startActivity(mainActivityIntent);
        finish();

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

    private void sendUserToMyBands() {
        Intent myBandsIntent = new Intent(MainActivity.this, MyBandsActivity.class);
        myBandsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(myBandsIntent);
        finish();


    }

    private void sendUsertoFindMusicians() {
        Intent findMusicianIntent = new Intent(MainActivity.this, FindMusicianActivity.class);
        findMusicianIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        findMusicianIntent.putExtra("currentBandId", currentBandId);
        startActivity(findMusicianIntent);
        finish();

    }

    private void sendUserToMessagesActivity() {

        Intent sendMessageIntent = new Intent(MainActivity.this, MessagesActivity.class);
        sendMessageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(sendMessageIntent);
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

    private void sendUserToBandRequestActivity(String idBand, String idSender) {
        Intent bandRequestIntent = new Intent(MainActivity.this, BandRequestActivity.class);
        bandRequestIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        bandRequestIntent.putExtra("idBand", idBand);
        bandRequestIntent.putExtra("idSender", idSender);
        startActivity(bandRequestIntent);
        finish();

    }


}
