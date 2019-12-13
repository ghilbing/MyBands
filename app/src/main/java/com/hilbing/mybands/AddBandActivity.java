package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hilbing.mybands.models.UsersInstruments;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.lang.ref.Reference;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class AddBandActivity extends AppCompatActivity {

    @BindView(R.id.band_image_CIV)
    CircleImageView imageCIV;
    @BindView(R.id.band_name_ET)
    EditText bandNameET;
    @BindView(R.id.band_available_CB)
    CheckBox availableCB;
    @BindView(R.id.band_story_ET)
    EditText bandStoryET;
    @BindView(R.id.band_country_SP)
    Spinner countrySP;
    @BindView(R.id.band_save_BT)
    Button createBT;
    @BindView(R.id.band_find_musicians_BT)
    Button findMusiciansBT;
    private String currentUserId;
    private String currentBandId;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference userDataReference;
    private DatabaseReference bandDataReference;
    private DatabaseReference currentBandReference;
    private DatabaseReference usersBandsReference;
    private DatabaseReference bandsUsersReference;
    private StorageReference bandsImageReference;

    final static int GALLERY = 1;
    private String downloadUri;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_band);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        Intent intent = getIntent();
        currentBandId = intent.getStringExtra("currentBandId");
        downloadUri = intent.getStringExtra("uri");

        userDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        bandDataReference = FirebaseDatabase.getInstance().getReference().child("Bands");
        usersBandsReference = FirebaseDatabase.getInstance().getReference("users_bands");
        bandsUsersReference = FirebaseDatabase.getInstance().getReference("bands_users");
        bandsImageReference = FirebaseStorage.getInstance().getReference().child("band_images");

        if(currentBandId != null) {
            bandDataReference.child(currentBandId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChild("mBandImage")) {
                            String imagePath = dataSnapshot.child("mBandImage").getValue().toString();
                            Picasso.get().load(imagePath).placeholder(R.drawable.profile).into(imageCIV);
                        } else {
                            Toast.makeText(AddBandActivity.this, getResources().getString(R.string.please_select_band_image_first), Toast.LENGTH_LONG).show();
                            imageCIV.requestFocus();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        progressDialog = new ProgressDialog(this);

        imageCIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToGallery();
            }
        });

        createBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addBand();
            }
        });

        findMusiciansBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToFindMusiciansActivity();
            }
        });


    ArrayAdapter<String> countriesAdapter = new ArrayAdapter<String>(AddBandActivity.this,
            android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.countries_array));
        countrySP.setAdapter(countriesAdapter);
        countrySP.setSelection(222);

}

    private void sendUserToFindMusiciansActivity()
    {

        Intent findMusicianIntent = new Intent(AddBandActivity.this, FindMusicianActivity.class);
        findMusicianIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(findMusicianIntent);
        finish();
    }

    private void sendUserToGallery()
    {

        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY && resultCode == RESULT_OK && data != null)
        {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1).start(this);
        }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){

                progressDialog.setTitle(getResources().getString(R.string.band_image));
                progressDialog.setMessage(getResources().getString(R.string.please_wait_while_we_are_updating_your_band_image));
                progressDialog.show();
                progressDialog.setCanceledOnTouchOutside(true);

                Uri resultUri = result.getUri();

                StorageReference filePath = bandsImageReference.child(currentUserId + ".jpg");

                final UploadTask uploadTask = filePath.putFile(resultUri);

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        currentBandId = bandDataReference.push().getKey();
                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUri = uri.toString();
                                bandDataReference.child(currentBandId).child("mBandImage").setValue(downloadUri).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            sendUserToAddBandActivity();
                                            progressDialog.dismiss();
                                            Toast.makeText(AddBandActivity.this, getResources().getString(R.string.image_stored_in_database_successfully), Toast.LENGTH_LONG).show();
                                        } else {
                                            progressDialog.dismiss();
                                            String message = task.getException().getMessage();
                                            Toast.makeText(AddBandActivity.this, getResources().getString(R.string.error_occurred) + ": " + message, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        });
                    }
                });

            } else
            {
                progressDialog.dismiss();
                Toast.makeText(AddBandActivity.this, getResources().getString(R.string.error_occurred_image_cannot_be_cropped_please_try_again),Toast.LENGTH_LONG).show();
            }
        }
    }


    private void addBand() {

            String bandName = bandNameET.getText().toString();
            String story = bandStoryET.getText().toString();
            String country = countrySP.getSelectedItem().toString();
            boolean available = true;

            if (TextUtils.isEmpty(bandName))
            {
                bandNameET.setError(getResources().getString(R.string.enter_your_band_name));
                bandNameET.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(story))
            {
                bandStoryET.setError(getResources().getString(R.string.enter_your_band_story));
                bandStoryET.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(country))
            {
                countrySP.requestFocus();
                return;
            }
            else
            {

                progressDialog.setTitle(getResources().getString(R.string.creating_band));
                progressDialog.setMessage(getResources().getString(R.string.please_wait_while_we_are_creating_your_band));
                progressDialog.show();
                progressDialog.setCanceledOnTouchOutside(true);


                HashMap bandMap = new HashMap();
                bandMap.put("mBandId", currentBandId);
                bandMap.put("mBandImage", downloadUri);
                bandMap.put("mBandCreator", currentUserId);
                bandMap.put("mBandName", bandName);
                bandMap.put("mBandStory", story);
                bandMap.put("mAvailable", available);
                bandDataReference.child(currentBandId).updateChildren(bandMap).addOnCompleteListener(new OnCompleteListener()
                {
                    @Override
                    public void onComplete(@NonNull Task task)
                    {
                        if(task.isSuccessful())
                        {
                          //  sendUserToMainActivity();
                            Toast.makeText(AddBandActivity.this, getResources().getString(R.string.your_band_is_created_succesfully),Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        } else
                        {
                            String message = task.getException().getMessage();
                            Toast.makeText(AddBandActivity.this, getResources().getString(R.string.error_occurred) + ": " + message, Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    }
                });
            }
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


    /*private void addUsersForInstrumentsToDataBase()
    {
        userDataReference.child(currentUserId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("mUserName"))
                    {
                        String name = dataSnapshot.child("mUserName").getValue().toString();
                        String instrument = instrumentsSP.getSelectedItem().toString();

                        HashMap instrumentMap = new HashMap();
                        instrumentMap.put("mUserId", currentUserId);
                        instrumentMap.put("mUserName", name);
                        instrumentMap.put("mInstrumentName", instrument);

                        instrumentsUsersReference.child(instrument).child(currentUserId).updateChildren(instrumentMap).addOnCompleteListener(new OnCompleteListener()
                        {
                            @Override
                            public void onComplete(@NonNull Task task)
                            {
                                if (task.isSuccessful()){
                                    Toast.makeText(AddInstrumentActivity.this, getResources().getString(R.string.instrument_added_successfully), Toast.LENGTH_LONG).show();
                                    progressDialog.dismiss();
                                }
                                else
                                {
                                    String message = task.getException().getMessage();
                                    Toast.makeText(AddInstrumentActivity.this, getResources().getString(R.string.error_occurred) + ": " + message, Toast.LENGTH_LONG).show();
                                    progressDialog.dismiss();
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });


    }*/

    /*private void addInstrumentsPlayedByUserToDataBase()
    {
        userDataReference.child(currentUserId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("mUserName"))
                    {
                        String name = dataSnapshot.child("mUserName").getValue().toString();
                        String instrument = instrumentsSP.getSelectedItem().toString();

                        HashMap instrumentMap = new HashMap();
                        instrumentMap.put("mUserId", currentUserId);
                        instrumentMap.put("mUserName", name);
                        instrumentMap.put("mInstrumentName", instrument);

                        usersInstrumentsReference.child(currentUserId).child(instrument).updateChildren(instrumentMap).addOnCompleteListener(new OnCompleteListener()
                        {
                            @Override
                            public void onComplete(@NonNull Task task)
                            {
                                if (task.isSuccessful()){
                                    Toast.makeText(AddInstrumentActivity.this, getResources().getString(R.string.instrument_added_successfully), Toast.LENGTH_LONG).show();
                                    progressDialog.dismiss();
                                }
                                else
                                {
                                    String message = task.getException().getMessage();
                                    Toast.makeText(AddInstrumentActivity.this, getResources().getString(R.string.error_occurred) + ": " + message, Toast.LENGTH_LONG).show();
                                    progressDialog.dismiss();
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });


    }*/



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
        Intent mainIntent = new Intent(AddBandActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }

    private void sendUserToAddBandActivity()
    {

        Intent selfIntent = new Intent(AddBandActivity.this, AddBandActivity.class);
        selfIntent.putExtra("currentBandId", currentBandId);
        selfIntent.putExtra("uri", downloadUri);
        selfIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(selfIntent);
        finish();

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}
