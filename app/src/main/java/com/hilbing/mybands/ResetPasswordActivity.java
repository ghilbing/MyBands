package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ResetPasswordActivity extends AppCompatActivity {

    @BindView(R.id.reset_password_toolbar)
    Toolbar toolbar;
    @BindView(R.id.reset_email_ET)
    EditText emailET;
    @BindView(R.id.reset_password_BT)
    Button resetPasswordBT;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.reset_password_email));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        resetPasswordBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailString = emailET.getText().toString();
                if (TextUtils.isEmpty(emailString)) {
                    emailET.setError(getResources().getString(R.string.enter_email_address));
                    emailET.requestFocus();
                    return;
                } else {
                    mAuth.sendPasswordResetEmail(emailString).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ResetPasswordActivity.this, getResources().getString(R.string.please_check_your_email_account), Toast.LENGTH_LONG).show();
                                sendUserToLoginActivity();
                            } else {
                                String message = task.getException().getMessage();
                                Toast.makeText(ResetPasswordActivity.this, getResources().getString(R.string.error_occurred) + ": " + message, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });


    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }
}
