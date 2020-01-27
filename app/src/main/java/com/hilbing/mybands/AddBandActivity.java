package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.lang.ref.Reference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class AddBandActivity extends AppCompatActivity {

    @BindView(R.id.add_band_toolbar)
    Toolbar toolbar;
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

    private String currentUserId;
    private String currentBandId;
    private ProgressDialog progressDialog;
    private String saveCurrentDate;
    private String saveCurrentTime;

    private FirebaseAuth mAuth;
    private DatabaseReference userDataReference;
    private DatabaseReference bandDataReference;
    private DatabaseReference currentBandReference;
    private DatabaseReference usersBandsReference;
    private DatabaseReference bandsUsersReference;
    private StorageReference bandsImageReference;
    private DatabaseReference bandsMusiciansRef;
    private DatabaseReference addToBandRequestRef;

    final static int GALLERY = 1;
    private String downloadUri;
    private String uriString;
    private String currentBandIdPref;
    private boolean clicked = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_band);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.create_band));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        SharedPreferences preferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
        currentBandIdPref = preferences.getString("currentBandIdPref", "");

        Intent intent = getIntent();
        currentBandId = intent.getStringExtra("currentBandId");
        downloadUri = intent.getStringExtra("uri");

        userDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        userDataReference.keepSynced(true);
        bandDataReference = FirebaseDatabase.getInstance().getReference().child("Bands");
        bandDataReference.keepSynced(true);
        usersBandsReference = FirebaseDatabase.getInstance().getReference("users_bands");
        userDataReference.keepSynced(true);
        bandsUsersReference = FirebaseDatabase.getInstance().getReference("bands_users");
        bandsUsersReference.keepSynced(true);
        bandsImageReference = FirebaseStorage.getInstance().getReference().child("band_images");


        bandsMusiciansRef = FirebaseDatabase.getInstance().getReference().child("BandsMusicians");
        bandsMusiciansRef.keepSynced(true);
        addToBandRequestRef = FirebaseDatabase.getInstance().getReference().child("BandUsersRequests");
        addToBandRequestRef.keepSynced(true);


        if (currentBandId != null) {
            bandDataReference.child(currentBandId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChild("mBandImage")) {
                            //Picasso changes for offline case
                            final String imagePath = dataSnapshot.child("mBandImage").getValue().toString();
                            Picasso.get().load(imagePath).networkPolicy(NetworkPolicy.OFFLINE).into(imageCIV, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(imagePath).placeholder(R.drawable.profile).into(imageCIV);
                                }
                            });
                        } else {
                            Toast.makeText(AddBandActivity.this, getResources().getString(R.string.please_select_band_image_first), Toast.LENGTH_LONG).show();
                            imageCIV.requestFocus();
                        }
                    } else {
                        Toast.makeText(AddBandActivity.this, getResources().getString(R.string.error_occurred), Toast.LENGTH_LONG).show();
                        sendUserToMainActivity();
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


        ArrayAdapter<String> countriesAdapter = new ArrayAdapter<String>(AddBandActivity.this,
                android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.countries_array));
        countrySP.setAdapter(countriesAdapter);
        countrySP.setSelection(222);

    }


    private void sendUserToGallery() {

        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        clicked = true;
        if (requestCode == GALLERY && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1).start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                progressDialog.setTitle(getResources().getString(R.string.band_image));
                progressDialog.setMessage(getResources().getString(R.string.please_wait_while_we_are_updating_your_band_image));
                progressDialog.show();
                progressDialog.setCanceledOnTouchOutside(true);

                Uri resultUri = result.getUri();

                StorageReference filePath = bandsImageReference.child(currentUserId + ".jpg");

                final UploadTask uploadTask = filePath.putFile(resultUri);

                Calendar calForDate = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                saveCurrentDate = currentDate.format(calForDate.getTime());
                Calendar calForTime = Calendar.getInstance();
                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
                saveCurrentTime = currentTime.format(calForTime.getTime());

                final String bandRandomId = saveCurrentDate + saveCurrentTime;


                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        currentBandId = currentUserId + bandRandomId;
                        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("SHARED_PREFS", currentBandId);
                        editor.apply();
                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUri = uri.toString();
                                if(!TextUtils.isEmpty(downloadUri)){
                                    uriString = downloadUri;
                                } else {
                                    uriString = "No data";
                                }
                                bandDataReference.child(currentBandId).child("mBandImage").setValue(uriString).addOnCompleteListener(new OnCompleteListener<Void>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
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

            } else {
                progressDialog.dismiss();
                Toast.makeText(AddBandActivity.this, getResources().getString(R.string.error_occurred_image_cannot_be_cropped_please_try_again), Toast.LENGTH_LONG).show();
            }
        }
    }


    private void addBand() {

        String bandName = bandNameET.getText().toString();
        String story = bandStoryET.getText().toString();
        String country = countrySP.getSelectedItem().toString();
        boolean available = true;
        String uriString = downloadUri;

        if (TextUtils.isEmpty(downloadUri)) {
            uriString = "no data";
        } else {
            uriString = downloadUri;
        }


        if (TextUtils.isEmpty(bandName)) {
            bandNameET.setError(getResources().getString(R.string.enter_your_band_name));
            bandNameET.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(story)) {
            bandStoryET.setError(getResources().getString(R.string.enter_your_band_story));
            bandStoryET.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(country)) {
            countrySP.requestFocus();
            return;
        } else {

            progressDialog.setTitle(getResources().getString(R.string.creating_band));
            progressDialog.setMessage(getResources().getString(R.string.please_wait_while_we_are_creating_your_band));
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(true);

            if(clicked) {
                HashMap bandMap = new HashMap();
                bandMap.put("mBandId", currentBandId);
                bandMap.put("mBandImage", uriString);
                bandMap.put("mBandCreator", currentUserId);
                bandMap.put("mBandName", bandName);
                bandMap.put("mBandStory", story);
                bandMap.put("mAvailable", available);
                bandMap.put("mCountry", country);
                bandDataReference.child(currentBandId).updateChildren(bandMap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            //  sendUserToMainActivity();
                            Toast.makeText(AddBandActivity.this, getResources().getString(R.string.your_band_is_created_succesfully), Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        } else {
                            String message = task.getException().getMessage();
                            Toast.makeText(AddBandActivity.this, getResources().getString(R.string.error_occurred) + ": " + message, Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    }
                });
            } else {

                Calendar calForDate = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                saveCurrentDate = currentDate.format(calForDate.getTime());
                Calendar calForTime = Calendar.getInstance();
                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
                saveCurrentTime = currentTime.format(calForTime.getTime());

                final String bandRandomId = saveCurrentDate + saveCurrentTime;

                currentBandId = currentUserId + bandRandomId;

                HashMap bandMap = new HashMap();
                bandMap.put("mBandId", currentBandId);
                bandMap.put("mBandImage", uriString);
                bandMap.put("mBandCreator", currentUserId);
                bandMap.put("mBandName", bandName);
                bandMap.put("mBandStory", story);
                bandMap.put("mAvailable", available);
                bandMap.put("mCountry", country);
                bandMap.put("mBandImage", "No data");
                bandDataReference.child(currentBandId).updateChildren(bandMap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            //  sendUserToMainActivity();
                            Toast.makeText(AddBandActivity.this, getResources().getString(R.string.your_band_is_created_succesfully), Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        } else {
                            String message = task.getException().getMessage();
                            Toast.makeText(AddBandActivity.this, getResources().getString(R.string.error_occurred) + ": " + message, Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    }
                });
            }

            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentDate = currentDate.format(calForDate.getTime());

            bandsMusiciansRef.child(currentUserId).child(currentBandId).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        bandsMusiciansRef.child(currentBandId).child(currentUserId).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(AddBandActivity.this, getResources().getString(R.string.creating_band), Toast.LENGTH_LONG).show();

                                } else {
                                    String message = task.getException().getMessage();
                                    Toast.makeText(AddBandActivity.this, message, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(AddBandActivity.this, message, Toast.LENGTH_LONG).show();
                        sendUserToMainActivity();
                    }
                }
            });
        }
    }


    private int getIndexSpinner(Spinner spinner, String string) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(string)) {
                return i;
            }
        }

        return 0;
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
        Intent mainIntent = new Intent(AddBandActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);

    }

    private void sendUserToAddBandActivity() {

        Intent selfIntent = new Intent(AddBandActivity.this, AddBandActivity.class);
        selfIntent.putExtra("currentBandId", currentBandId);
        selfIntent.putExtra("uri", downloadUri);
        selfIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(selfIntent);

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
