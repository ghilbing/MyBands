package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.hilbing.mybands.models.User;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class SetUpActivity extends AppCompatActivity
{

    @BindView(R.id.setup_user_image_CIV)
    CircleImageView profileImageCIV;
    @BindView(R.id.setup_user_full_name_ET)
    EditText fullNameET;
    @BindView(R.id.setup_user_phone)
    EditText phoneET;
    @BindView(R.id.setup_country_SP)
    Spinner countrySP;
    @BindView(R.id.setup_save_BT)
    Button saveBT;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference usersReference;
    private StorageReference usersProfileImageReference;

    private String currentUserId;
    private String downloadUri;
    private String intentString;
    final static int GALLERY = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);

        ButterKnife.bind(this);

        Intent intentGetData = getIntent();
        Bundle bundle = intentGetData.getExtras();
        if(bundle != null)
        {
            intentString = bundle.getString("downloadUri");
        }

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        usersProfileImageReference = FirebaseStorage.getInstance().getReference().child("profile_images");

        progressDialog = new ProgressDialog(this);

        profileImageCIV.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                sendUserToGallery();
            }
        });


        saveBT.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                saveUserInformation();
            }
        });

        usersReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("mUserProfileImage"))
                    {
                        String imagePath = dataSnapshot.child("mUserProfileImage").getValue().toString();
                        Picasso.get().load(imagePath).placeholder(R.drawable.profile).into(profileImageCIV);
                    }
                    else{
                        Toast.makeText(SetUpActivity.this, getResources().getString(R.string.please_select_profile_image_first), Toast.LENGTH_LONG).show();
                        profileImageCIV.requestFocus();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        ArrayAdapter<String> countriesAdapter = new ArrayAdapter<String>(SetUpActivity.this,
                android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.countries_array));
        countrySP.setAdapter(countriesAdapter);
        countrySP.setSelection(222);

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
                                usersReference.child("mUserProfileImage").setValue(downloadUri).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            sendUserToSetUpActivity();
                                            progressDialog.dismiss();
                                            Toast.makeText(SetUpActivity.this, getResources().getString(R.string.image_stored_in_database_successfully), Toast.LENGTH_LONG).show();
                                        } else {
                                            progressDialog.dismiss();
                                            String message = task.getException().getMessage();
                                            Toast.makeText(SetUpActivity.this, getResources().getString(R.string.error_occurred) + ": " + message, Toast.LENGTH_LONG).show();
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
                Toast.makeText(SetUpActivity.this, getResources().getString(R.string.error_occurred_image_cannot_be_cropped_please_try_again),Toast.LENGTH_LONG).show();
            }
        }
    }

    private void sendUserToSetUpActivity()
    {

        Intent selfIntent = new Intent(SetUpActivity.this, SetUpActivity.class);
        selfIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(selfIntent);
        finish();

    }

    private void saveUserInformation()
    {

        String fullName = fullNameET.getText().toString();
        String phone = phoneET.getText().toString();
        String country = countrySP.getSelectedItem().toString();
        String status = "I am musician";
        boolean available = true;


        if (TextUtils.isEmpty(fullName))
        {
            fullNameET.setError(getResources().getString(R.string.enter_your_full_name));
            fullNameET.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone))
        {
            phoneET.setError(getResources().getString(R.string.enter_your_phone));
            phoneET.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(country))
        {
            countrySP.requestFocus();
            return;
        }
        else
            {

            progressDialog.setTitle(getResources().getString(R.string.creating_user));
            progressDialog.setMessage(getResources().getString(R.string.please_wait_while_we_are_creating_your_new_account));
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(true);

            HashMap userMap = new HashMap();
            userMap.put("mUserName", fullName);
            userMap.put("mUserPhone", phone);
            userMap.put("mUserCountry", country);
            userMap.put("mUserStatus", status);
            userMap.put("mUserAvailable", available);
            usersReference.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener()
            {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if(task.isSuccessful())
                    {
                        sendUserToMainActivity();
                        Toast.makeText(SetUpActivity.this, getResources().getString(R.string.your_user_account_is_created_succesfully),Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    } else
                        {
                        String message = task.getException().getMessage();
                        Toast.makeText(SetUpActivity.this, getResources().getString(R.string.error_occurred) + ": " + message, Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                }
            });
        }
    }



    private void sendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SetUpActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }
}
