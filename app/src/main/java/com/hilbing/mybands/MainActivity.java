package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hilbing.mybands.adapters.EventAdapter;
import com.hilbing.mybands.adapters.ExpandableListAdapter;
import com.hilbing.mybands.fragments.MapDialogFragment;
import com.hilbing.mybands.fragments.SongsFragmentDialog;
import com.hilbing.mybands.models.Band;
import com.hilbing.mybands.models.Event;
import com.hilbing.mybands.models.MenuModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
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
    @BindView(R.id.main_events_RV)
    RecyclerView recyclerView;

    private Toolbar mToolbar;
    private CircleImageView navProfileCIV;
    private TextView navUserNameTV;
    private TextView navBandNameTV;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;


    private FirebaseAuth mAuth;
    private DatabaseReference usersReference;
    private DatabaseReference bandsMusiciansReference;
    private DatabaseReference bandsReference;
    private DatabaseReference bandsRequestReference;
    private DatabaseReference allEventsReference;
    private FirebaseRecyclerAdapter recyclerAdapter;
    String currentUserID;
    String currentBandId;
    String currentBandIdPref;
    private boolean openDialog;
    private EventAdapter eventAdapter;

    ExpandableListAdapter expandableListAdapter;
    @BindView(R.id.nav_expandableListView)
    ExpandableListView expandableListView;
    List<MenuModel> headerList = new ArrayList<>();
    HashMap<MenuModel, List<MenuModel>> childList = new HashMap<>();
    List<Band> bands = new ArrayList();
    private List<Event> events = new ArrayList<>();
    private boolean savedInstanceStateDone;


    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        SharedPreferences preferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
        currentBandIdPref = preferences.getString("currentBandIdPref", "");


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        usersReference.keepSynced(true);
        bandsMusiciansReference = FirebaseDatabase.getInstance().getReference().child("BandsMusicians");
        bandsMusiciansReference.keepSynced(true);
        bandsRequestReference = FirebaseDatabase.getInstance().getReference().child("BandUsersRequests");
        bandsMusiciansReference.keepSynced(true);
        bandsReference = FirebaseDatabase.getInstance().getReference().child("Bands");
        bandsReference.keepSynced(true);
        allEventsReference = FirebaseDatabase.getInstance().getReference().child("Events");
        allEventsReference.keepSynced(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        isNetworkAvailable(this);

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

            if(!TextUtils.isEmpty(currentBandIdPref)) {
                Log.d("CURRENT BAND ID PREF FROM MAIN ACTIVITY", currentBandIdPref);
                showEvents();
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
                        final String imagePath = dataSnapshot.child("mUserProfileImage").getValue().toString();
                        Picasso.get().load(imagePath).networkPolicy(NetworkPolicy.OFFLINE).into(navProfileCIV, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(imagePath).placeholder(R.drawable.profile).into(navProfileCIV);
                            }
                        });

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


    private void showEvents(){

        SharedPreferences preferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
        currentBandIdPref = preferences.getString("currentBandIdPref", "");

        Query query = allEventsReference.child(currentBandIdPref).orderByChild("mTimestamp").startAt(System.currentTimeMillis());



        if(!TextUtils.isEmpty(query.toString())) {

            FirebaseRecyclerOptions<Event> options = new FirebaseRecyclerOptions.Builder<Event>().setQuery(query,
                    new SnapshotParser<Event>() {
                        @NonNull
                        @Override
                        public Event parseSnapshot(@NonNull DataSnapshot snapshot) {
                            return new Event(
                                    snapshot.child("idEvent").getValue().toString(),
                                    snapshot.child("mEventType").getValue().toString(),
                                    snapshot.child("mName").getValue().toString(),
                                    snapshot.child("mDate").getValue().toString(),
                                    snapshot.child("mTime").getValue().toString(),
                                    snapshot.child("mPlace").getValue().toString(),
                                    snapshot.child("mPlaylistName").getValue().toString(),
                                    snapshot.child("idPlaylist").getValue().toString(),
                                    snapshot.child("mCurrentUser").getValue().toString(),
                                    (Long) snapshot.child("mTimestamp").getValue(),
                                    snapshot.child("mAddressLine").getValue().toString(),
                                    (Double) snapshot.child("mLat").getValue(),
                                    (Double) snapshot.child("mLng").getValue());


                        }
                    }).build();


            recyclerAdapter = new FirebaseRecyclerAdapter<Event, MainActivity.EventViewHolder>(options) {

                @NonNull
                @Override
                public MainActivity.EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_events_layout, parent, false);
                    return new MainActivity.EventViewHolder(view);
                }

                @Override
                protected void onBindViewHolder(@NonNull final MainActivity.EventViewHolder holder, int position, @NonNull final Event model) {
                    final String eventKey = getRef(position).getKey();

                    holder.eventTypeTV.setText(model.getmEventType());
                    if(model.getmEventType().equals("Rehearsal")){
                        holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.rehearsal));
                    } else {
                        holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.concert));
                    }
                    holder.eventNameTV.setText(model.getmName());
                    holder.eventPlaceTV.setText(getResources().getString(R.string.location) + ": " + model.getmPlace());
                    holder.eventDateTV.setText(getResources().getString(R.string.date) + ": " + model.getmDate());
                    holder.eventTimeTV.setText(getResources().getString(R.string.time) + ": " +model.getmTime());
                    holder.eventPlaylistTV.setText(getString(R.string.playlist_name) + " " + model.getmPlaylistName());

                    holder.eventPlaceTV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(!savedInstanceStateDone) {
                              //  showMapEvent(model.getmLat(), model.getmLng());
                            }
                        }
                    });

                    String user = model.getmCurrentUser();
                    if(!user.equals(currentUserID)){
                        holder.editionLL.setVisibility(View.INVISIBLE);
                    } else {
                        holder.editionLL.setVisibility(View.VISIBLE);
                    }


                    holder.editIV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                               sendUserToUpdateEvent(eventKey);
                        }
                    });


                    holder.deleteIV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            deleteEvent(eventKey);
                        }
                    });
                }
            };

            recyclerView.setAdapter(recyclerAdapter);
            recyclerAdapter.startListening();
            recyclerAdapter.notifyDataSetChanged();
        }
        else
            {
            Toast.makeText(this, getString(R.string.no_data_available), Toast.LENGTH_SHORT).show();
        }

    }

    private void showMapEvent(double getmLat, double getmLng) {

        DialogFragment dialogFragment = MapDialogFragment.newInstance(getmLat, getmLng);
        dialogFragment.show(getSupportFragmentManager(), getString(R.string.event_map));

    }

    private void deleteEvent(String eventKey) {

        allEventsReference.child(currentBandIdPref).child(eventKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.event_deleted_successfully), Toast.LENGTH_LONG).show();
                }
                else
                {
                    String message = task.getException().getMessage();
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    private void sendUserToUpdateEvent(String eventKey) {

        Intent updateEventIntent = new Intent(MainActivity.this, UpdateEventActivity.class);
        updateEventIntent.putExtra("eventKey", eventKey);
        startActivity(updateEventIntent);


    }

    public class EventViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        @BindView(R.id.all_events_cardView_CV)
        CardView cardView;
        @BindView(R.id.all_events_type_TV)
        TextView eventTypeTV;
        @BindView(R.id.all_events_name_TV)
        TextView eventNameTV;
        @BindView(R.id.all_events_place_TV)
        TextView eventPlaceTV;
        @BindView(R.id.all_events_date_TV)
        TextView eventDateTV;
        @BindView(R.id.all_events_time_TV)
        TextView eventTimeTV;
        @BindView(R.id.all_events_playlist_TV)
        TextView eventPlaylistTV;
        @BindView(R.id.all_events_edit_IV)
        ImageView editIV;
        @BindView(R.id.all_events_delete_IV)
        ImageView deleteIV;
        @BindView(R.id.all_events_edition)
        LinearLayout editionLL;


        public EventViewHolder(@NonNull final View itemView)
        {
            super(itemView);
            mView = itemView;
            ButterKnife.bind(this, itemView);

            itemView.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View view)
                {
                    int itemClicked = getAdapterPosition();
                    Toast.makeText(MainActivity.this, String.valueOf(itemClicked), Toast.LENGTH_LONG).show();

                    return false;
                }
            });

        }
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
        childModel = new MenuModel(getResources().getString(R.string.band_members), false, false);
        childModelList.add(childModel);
        childModel = new MenuModel(getResources().getString(R.string.quit_band), false, false);
        childModelList.add(childModel);


        if(menuModel.hasChildren)
        {
            childList.put(menuModel, childModelList);
        }

        childModelList = new ArrayList<>();
        menuModel = new MenuModel(getResources().getString(R.string.songs), true, true);
        headerList.add(menuModel);
        childModel = new MenuModel(getResources().getString(R.string.add_song), false, false);
        childModelList.add(childModel);
        childModel = new MenuModel(getResources().getString(R.string.my_songs), false, false);
        childModelList.add(childModel);

        if(menuModel.hasChildren)
        {
            childList.put(menuModel, childModelList);
        }

        childModelList = new ArrayList<>();
        menuModel = new MenuModel(getResources().getString(R.string.playlists), true, true);
        headerList.add(menuModel);
        childModel = new MenuModel(getResources().getString(R.string.my_playlists), false, false);
        childModelList.add(childModel);

        if (menuModel.hasChildren)
        {
            childList.put(menuModel, childModelList);
        }

        childModelList = new ArrayList<>();
        menuModel = new MenuModel(getResources().getString(R.string.events), true, true);
        headerList.add(menuModel);
        childModel = new MenuModel(getResources().getString(R.string.create_event), false, false);
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
                            sendUserToSongActivity();
                        }
                        else if(title.equals(getResources().getString(R.string.playlists))){
                            Toast.makeText(MainActivity.this, "Playlists", Toast.LENGTH_LONG).show();
                        }
                        else if (title.equals(getResources().getString(R.string.settings))){
                            sendUserToSettingsActivity();
                        }
                        else if(title.equals(getResources().getString(R.string.logout))){
                            mAuth.signOut();
                            SharedPreferences preferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
                            preferences.edit().clear().commit();
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
                       else if(subTitle.equals(getResources().getString(R.string.band_members))){
                           sendUserToMusiciansActivity();
                       }
                       else if(subTitle.equals(getResources().getString(R.string.send_message))){
                           sendUserToMessagesActivity();
                       }
                       else if(subTitle.equals(getResources().getString(R.string.quit_band))){
                           sendUserToQuitBandActivity(currentBandIdPref, currentUserID);
                       }
                       else if(subTitle.equals(getResources().getString(R.string.add_song))){
                           sendUserToSongActivity();
                        }
                       else if(subTitle.equals(getResources().getString(R.string.my_songs))){
                           sendUserToMySongsActivity();
                       }
                       else if(subTitle.equals(getResources().getString(R.string.my_playlists))){
                           sendUserToMyPlaylistsActivity();
                       }
                       else if(subTitle.equals(getResources().getString(R.string.create_event))){
                           sendUserToCreateEventActivity();
                       }
                       else if(subTitle.equals(getResources().getString(R.string.rehearsals))){
                           sendUserToRehearsalActivity();
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
        savedInstanceStateDone = false;

        if(null != recyclerAdapter)
        {
            recyclerAdapter.startListening();
            recyclerAdapter.notifyDataSetChanged();
        }

        SharedPreferences preferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
        currentBandIdPref = preferences.getString("currentBandIdPref", "");
        Log.d("MainActivitySharedPreferences................................", currentBandIdPref);

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
                           // Toast.makeText(MainActivity.this, getResources().getString(R.string.band_does_not_exist), Toast.LENGTH_LONG).show();
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
            checkIfUserHasARequest();
            showEvents();
             }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(null != recyclerAdapter)
        {
            recyclerAdapter.startListening();
            recyclerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(null != recyclerAdapter)
        {
            recyclerAdapter.stopListening();
          //  recyclerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        savedInstanceStateDone = false;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        savedInstanceStateDone = true;
    }

    //Verify Network connection
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() !=  null
                && connectivityManager.getActiveNetworkInfo().isConnected();
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
                  //  checkIfUserHasARequest();
                }
                else
                    {
                    if (dataSnapshot.hasChild(currentUserID))
                    {
                        int count = (int) dataSnapshot.child(currentUserID).getChildrenCount();
                        if (count > 1 && TextUtils.isEmpty(currentBandIdPref)) {

                            sendUserToMyBands();

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

    private void checkIfUserHasARequest()
    {

        bandsRequestReference.child(currentUserID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    for(DataSnapshot dataSnapshot1 :dataSnapshot.getChildren())
                    {
                        final String bandId = dataSnapshot1.getKey();
                        bandsRequestReference.child(currentUserID).child(bandId).addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                for(DataSnapshot dataSnapshot2 :dataSnapshot.getChildren())
                                {
                                    final String senderId = dataSnapshot2.getKey();
                                    bandsRequestReference.child(currentUserID).child(bandId).child(senderId).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()) {
                                                String request_type = dataSnapshot.child("request_type").getValue().toString();
                                                if(!TextUtils.isEmpty(request_type) && request_type.equals("received")){
                                                //if (request_type.equals("received")) {

                                                    bandsRequestReference.addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            if (dataSnapshot.hasChild(currentUserID)) {
                                                                for (DataSnapshot ds : dataSnapshot.child(currentUserID).getChildren()) {
                                                                    final String bandKey = ds.getKey();
                                                                    bandsRequestReference.child(currentUserID).child(bandKey).addValueEventListener(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                            if (dataSnapshot.exists()) {
                                                                                for (DataSnapshot ds1 : dataSnapshot.getChildren()) {
                                                                                    final String idSender = ds1.getKey();
                                                                                    Log.d("BAND and SENDER...........................", bandKey + " - " + idSender);
                                                                                    sendUserToBandRequestActivity(bandKey, idSender);
                                                                                }
                                                                            }
                                                                        }

                                                                        @Override
                                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                        }
                                                                    });
                                                                }
                                                            }
                                                            bandsRequestReference.removeEventListener(this);
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
        findMusicianIntent.putExtra("currentBandId", currentBandIdPref);
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
        finish();
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

    private void sendUserToQuitBandActivity(String idBand, String idCurrentUser) {
        Intent bandQuitIntent = new Intent(MainActivity.this, QuitBandActivity.class);
        bandQuitIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        bandQuitIntent.putExtra("idBand", currentBandIdPref);
        bandQuitIntent.putExtra("idUser", currentUserID);
        startActivity(bandQuitIntent);
        finish();

    }

    private void sendUserToSongActivity(){
        Intent songIntent = new Intent(MainActivity.this, SongActivity.class);
        songIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(songIntent);
        finish();
    }

    private void sendUserToMySongsActivity(){
        Intent mySongsIntent = new Intent(MainActivity.this, MySongsActivity.class);
        mySongsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mySongsIntent);
        finish();
    }

    private void sendUserToAddPlaylistActivity(){
        Intent playlistIntent = new Intent(MainActivity.this, PlaylistActivity.class);
        playlistIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(playlistIntent);
        finish();
    }

    private void sendUserToCreateEventActivity(){
        Intent createEventIntent = new Intent(MainActivity.this, CreateEventActivity.class);
        createEventIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(createEventIntent);
        finish();
    }

    private void sendUserToMyPlaylistsActivity(){
        Intent myPlaylistsIntent = new Intent(MainActivity.this, MyPlaylistsActivity.class);
        myPlaylistsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(myPlaylistsIntent);
        finish();
    }

    private void sendUserToRehearsalActivity(){
        Intent rehearsalIntent = new Intent(MainActivity.this, RehearsalActivity.class);
        rehearsalIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(rehearsalIntent);
        finish();
    }


}
