package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
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

import org.w3c.dom.Text;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity
{

    @BindView(R.id.settings_toolbar)
    Toolbar toolbar;
    @BindView(R.id.settings_profile_picture_CIV)
    CircleImageView profileImageCIV;
    @BindView(R.id.settings_available_CB)
    CheckBox availableCB;
    @BindView(R.id.settings_singer_CB)
    CheckBox singerCB;
    @BindView(R.id.settings_composer_CB)
    CheckBox composerCB;
    @BindView(R.id.settings_user_name_ET)
    EditText userNameET;
    @BindView(R.id.settings_phone_ET)
    EditText phoneET;
    @BindView(R.id.settings_user_status_ET)
    EditText userStatusET;
    @BindView(R.id.settings_countries_SP)
    Spinner countriesSP;
    @BindView(R.id.settings_update_account_BT)
    Button updateBT;

    private DatabaseReference settingsUserReference;
    private StorageReference usersProfileImageReference;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private String userProfileImage;

    private String downloadUri;

    final static int GALLERY = 1;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.account_settings));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progressDialog = new ProgressDialog(this);

        usersProfileImageReference = FirebaseStorage.getInstance().getReference().child("profile_images");

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        settingsUserReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        settingsUserReference.keepSynced(true);

        settingsUserReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    userProfileImage = dataSnapshot.child("mUserProfileImage").getValue().toString();
                    boolean userAvailable = (boolean) dataSnapshot.child("mUserAvailable").getValue();
                    boolean userSinger = (boolean)dataSnapshot.child("mUserSinger").getValue();
                    boolean userComposer = (boolean)dataSnapshot.child("mUserComposer").getValue();
                    String userName = dataSnapshot.child("mUserName").getValue().toString();
                    String userPhone = dataSnapshot.child("mUserPhone").getValue().toString();
                    String userStatus = dataSnapshot.child("mUserStatus").getValue().toString();
                    String userCountry = dataSnapshot.child("mUserCountry").getValue().toString();

                    Picasso.get().load(userProfileImage).networkPolicy(NetworkPolicy.OFFLINE).into(profileImageCIV, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(userProfileImage).placeholder(R.drawable.profile).into(profileImageCIV);
                        }
                    });

                    availableCB.setChecked(userAvailable);
                    composerCB.setChecked(userComposer);
                    singerCB.setChecked(userSinger);
                    userNameET.setText(userName);
                    phoneET.setText(userPhone);
                    countriesSP.setSelection(getIndexSpinner(countriesSP, userCountry));
                    userStatusET.setText(userStatus);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        profileImageCIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToGallery();
            }
        });

        updateBT.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                validateAccountInformation();
            }
        });

    }

    private void sendUserToGallery()
    {

        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY);
    }

    private void validateAccountInformation()
    {
        String userName = userNameET.getText().toString();
        String userImage = userProfileImage;
        boolean available = availableCB.isChecked();
        boolean singer = singerCB.isChecked();
        boolean composer = composerCB.isChecked();
        String phone = phoneET.getText().toString();
        String country = countriesSP.getSelectedItem().toString();
        String status = userStatusET.getText().toString();

        if(TextUtils.isEmpty(userName))
        {
            Toast.makeText(SettingsActivity.this, getResources().getString(R.string.please_enter_your_full_name), Toast.LENGTH_LONG).show();
        }

        else if(TextUtils.isEmpty(phone))
        {
            Toast.makeText(SettingsActivity.this, getResources().getString(R.string.please_enter_your_phone), Toast.LENGTH_LONG).show();
        }

        else if(TextUtils.isEmpty(country))
        {
            Toast.makeText(SettingsActivity.this, getResources().getString(R.string.please_enter_the_country_where_you_live), Toast.LENGTH_LONG).show();
        }

        else if(TextUtils.isEmpty(status))
        {
            Toast.makeText(SettingsActivity.this, getResources().getString(R.string.please_enter_your_status), Toast.LENGTH_LONG).show();
        }

        else if(TextUtils.isEmpty(userName))
        {
            Toast.makeText(SettingsActivity.this, getResources().getString(R.string.please_enter_your_full_name), Toast.LENGTH_LONG).show();
        }
        else
        {
            updateAccountInformation(userName, available, singer, composer, phone, country, status, userProfileImage);
        }




    }

    private void updateAccountInformation(String userName, boolean available, boolean singer, boolean composer, String phone, String country, String status, String userProfileImage)
    {
        HashMap userProfileInfo = new HashMap();
        userProfileInfo.put("mUserName", userName);
        userProfileInfo.put("mUserPhone", phone);
        userProfileInfo.put("mUserStatus", status);
        userProfileInfo.put("mUserCountry", country);
        userProfileInfo.put("mUserSinger", singer);
        userProfileInfo.put("mUserAvailable", available);
        userProfileInfo.put("mUserComposer", composer);
        userProfileInfo.put("mUserProfileImage", userProfileImage);

        settingsUserReference.updateChildren(userProfileInfo).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(SettingsActivity.this, getResources().getString(R.string.account_settings_updated_successfully), Toast.LENGTH_LONG).show();
                    sendUserToMainActivity();
                }
                else
                {
                    String message = task.getException().getMessage();
                    Toast.makeText(SettingsActivity.this, getResources().getString(R.string.error_occurred) + ": " + message, Toast.LENGTH_LONG).show();
                }
            }
        });


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

                progressDialog.setTitle(getResources().getString(R.string.profile_image));
                progressDialog.setMessage(getResources().getString(R.string.please_wait_while_we_are_updating_your_profile_image));
                progressDialog.show();
                progressDialog.setCanceledOnTouchOutside(true);

                Uri resultUri = result.getUri();
                StorageReference filePath = usersProfileImageReference.child(currentUserId + ".jpg");

                final UploadTask uploadTask = filePath.putFile(resultUri);

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUri = uri.toString();
                                settingsUserReference.child("mUserProfileImage").setValue(downloadUri).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            sendUserToSettingsActivity();
                                            progressDialog.dismiss();
                                            Toast.makeText(SettingsActivity.this, getResources().getString(R.string.image_stored_in_database_successfully), Toast.LENGTH_LONG).show();
                                        } else {
                                            progressDialog.dismiss();
                                            String message = task.getException().getMessage();
                                            Toast.makeText(SettingsActivity.this, getResources().getString(R.string.error_occurred) + ": " + message, Toast.LENGTH_LONG).show();
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
                Toast.makeText(SettingsActivity.this, getResources().getString(R.string.error_occurred_image_cannot_be_cropped_please_try_again),Toast.LENGTH_LONG).show();
            }
        }
    }

    private void sendUserToSettingsActivity()
    {

        Intent selfIntent = new Intent(SettingsActivity.this, SettingsActivity.class);
        selfIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(selfIntent);
        finish();

    }

    private void sendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

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
}
