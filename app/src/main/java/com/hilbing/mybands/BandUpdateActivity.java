package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class BandUpdateActivity extends AppCompatActivity {

    @BindView(R.id.update_band_toolbar)
    Toolbar toolbar;
    @BindView(R.id.update_band_image_CIV)
    CircleImageView imageCIV;
    @BindView(R.id.update_band_name_ET)
    EditText bandNameET;
    @BindView(R.id.update_band_available_CB)
    CheckBox availableCB;
    @BindView(R.id.update_band_story_ET)
    EditText bandStoryET;
    @BindView(R.id.update_country_TV)
    TextView countryTV;
    @BindView(R.id.update_band_country_SP)
    Spinner countrySP;
    @BindView(R.id.update_band_save_BT)
    Button saveBT;

    private String currentBandId;
    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference bandsReference;
    private StorageReference bandsImageReference;

    private String saveCurrentDate;
    private String saveCurrentTime;

    final static int GALLERY = 1;
    private String downloadUri;
    private ProgressDialog progressDialog;
    private String bandName;
    private String story;
    private String country;
    private boolean available;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_band_update);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.update_band));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();


        Intent intent = getIntent();

        currentBandId = intent.getStringExtra("bandIdSelected");



        if(!TextUtils.isEmpty(currentBandId))
        {
            Log.d("CURRENT BAND ID FROM MY BANDS", currentBandId);
            SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("currentBandIdSelected", currentBandId);
        } else {

            SharedPreferences preferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
            currentBandId = preferences.getString("currentBandIdSelected", "");
            Log.d("CURRENT BAND ID FROM SELF ACTIVITY PREFERENCES", currentBandId);
            downloadUri = intent.getStringExtra("uri");
        }


        progressDialog = new ProgressDialog(this);


        if (!TextUtils.isEmpty(currentBandId)) {

            bandsReference = FirebaseDatabase.getInstance().getReference().child("Bands").child(currentBandId);
            bandsReference.keepSynced(true);
            bandsImageReference = FirebaseStorage.getInstance().getReference().child("band_images");

            bandsReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        final String bandProfileImage = dataSnapshot.child("mBandImage").getValue().toString();
                        boolean mAvailable = (boolean) dataSnapshot.child("mAvailable").getValue();
                        String bandName = dataSnapshot.child("mBandName").getValue().toString();
                        String bandStory = dataSnapshot.child("mBandStory").getValue().toString();
                        String bandCountry = dataSnapshot.child("mCountry").getValue().toString();

                        Picasso.get().load(bandProfileImage).networkPolicy(NetworkPolicy.OFFLINE).into(imageCIV, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(bandProfileImage).placeholder(R.drawable.profile).into(imageCIV);
                            }
                        });

                        availableCB.setChecked(mAvailable);
                        bandNameET.setText(bandName);
                        bandStoryET.setText(bandStory);
                        countryTV.setText(bandCountry);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        countrySP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                countryTV.setText(countrySP.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        imageCIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToGallery();
            }
        });

        saveBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBand();
            }
        });


        ArrayAdapter<String> countriesAdapter = new ArrayAdapter<String>(BandUpdateActivity.this,
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


    private void saveBand() {

        bandName = bandNameET.getText().toString();
        story = bandStoryET.getText().toString();
        country = countrySP.getSelectedItem().toString();
        available = true;

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

            progressDialog.setTitle(getResources().getString(R.string.updating_band));
            progressDialog.setMessage(getResources().getString(R.string.please_wait_while_we_are_updating_your_band));
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(true);

            HashMap bandMap = new HashMap();
            bandMap.put("mBandId", currentBandId);
            bandMap.put("mBandImage", downloadUri);
            bandMap.put("mBandCreator", currentUserId);
            bandMap.put("mBandName", bandName);
            bandMap.put("mBandStory", story);
            bandMap.put("mAvailable", available);
            bandMap.put("mCountry", country);
            bandsReference.updateChildren(bandMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        //  sendUserToMainActivity();
                        Toast.makeText(BandUpdateActivity.this, getResources().getString(R.string.your_band_is_created_succesfully), Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(BandUpdateActivity.this, getResources().getString(R.string.error_occurred) + ": " + message, Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                }
            });

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUri = uri.toString();
                            }
                        });

                       // currentBandId = currentUserId + bandRandomId;
                 /*       taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUri = uri.toString();
                                HashMap bandImage = new HashMap();
                                bandImage.put("mBandImage", downloadUri);
                                bandsReference.child(currentBandId).updateChildren(bandImage).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            sendUserToBandUpdateActivity(currentBandId);
                                            progressDialog.dismiss();
                                            Toast.makeText(BandUpdateActivity.this, getResources().getString(R.string.image_stored_in_database_successfully), Toast.LENGTH_LONG).show();
                                        } else {
                                            progressDialog.dismiss();
                                            String message = task.getException().getMessage();
                                            Toast.makeText(BandUpdateActivity.this, getResources().getString(R.string.error_occurred) + ": " + message, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        });*/
                    }
                });

            } else {
                progressDialog.dismiss();
                Toast.makeText(BandUpdateActivity.this, getResources().getString(R.string.error_occurred_image_cannot_be_cropped_please_try_again), Toast.LENGTH_LONG).show();
            }
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
        Intent mainIntent = new Intent(BandUpdateActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }


    private void sendUserToBandUpdateActivity(String currentBandId) {
        Intent selfIntent = new Intent(BandUpdateActivity.this, BandUpdateActivity.class);
        selfIntent.putExtra("currentBandId", currentBandId);
        selfIntent.putExtra("from", "selfIntent");
        selfIntent.putExtra("uri", downloadUri);
        selfIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(selfIntent);
        finish();
    }


}
