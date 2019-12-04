package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hilbing.mybands.models.User;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class SetUpActivity extends AppCompatActivity {

    @BindView(R.id.setup_user_image_CIV)
    CircleImageView profileImageCIV;
    @BindView(R.id.setup_user_full_name_ET)
    EditText fullNameET;
    @BindView(R.id.setup_user_phone)
    EditText phoneET;
    @BindView(R.id.setup_country_ET)
    EditText countryET;
    @BindView(R.id.setup_save_BT)
    Button saveBT;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference usersReference;

    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);

        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);

        progressDialog = new ProgressDialog(this);


        saveBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();
            }
        });

    }

    private void saveUserInformation() {
        String fullName = fullNameET.getText().toString();
        String phone = phoneET.getText().toString();
        String country = countryET.getText().toString();
        String status = "I am musician";
        final String profileUrl = "none";
        boolean available = true;


        if (TextUtils.isEmpty(fullName)) {
            fullNameET.setError(getResources().getString(R.string.enter_your_full_name));
            fullNameET.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            phoneET.setError(getResources().getString(R.string.enter_your_phone));
            phoneET.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(country)) {
            countryET.setError(getResources().getString(R.string.enter_your_country));
            countryET.requestFocus();
            return;
        }
        else {

            progressDialog.setTitle(getResources().getString(R.string.creating_user));
            progressDialog.setMessage(getResources().getString(R.string.please_wait_while_we_are_creating_your_new_account));
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(true);

            User user = new User(currentUserId, fullName, phone, profileUrl, country, status, available);
            usersReference.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        sendUserToMainActivity();
                        Toast.makeText(SetUpActivity.this, getResources().getString(R.string.your_user_account_is_created_succesfully),Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(SetUpActivity.this, getResources().getString(R.string.error_occurred) + ": " + message, Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                }
            });

        }

    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SetUpActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }
}
