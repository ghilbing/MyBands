package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.login_email_ET)
    EditText emailET;
    @BindView(R.id.login_password_ET)
    EditText passwordET;
    @BindView(R.id.login_forgot_passw_TV)
    TextView forgotPasswordTV;
    @BindView(R.id.login_sign_up_TV)
    TextView registerTV;
    @BindView(R.id.login_login_BT)
    Button loginBT;
    @BindView(R.id.login_facebook_IV)
    ImageView facebookLoginIV;
    @BindView(R.id.login_twitter_IV)
    ImageView twitterLoginIV;
    @BindView(R.id.login_google_IV)
    ImageView googleLoginIV;

    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);


        registerTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToRegisterActivity();
            }
        });

        loginBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginVerification();
            }
        });


    }

    private void loginVerification() {

        String email = emailET.getText().toString();
        String password = passwordET.getText().toString();

        if(TextUtils.isEmpty(email)){
            emailET.setError(getResources().getString(R.string.enter_email_address));
            emailET.requestFocus();
            return;
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailET.setError(getResources().getString(R.string.please_enter_a_valid_email));
            emailET.requestFocus();
            return;
        }

        else if (TextUtils.isEmpty(password)) {
            passwordET.setError(getResources().getString(R.string.prompt_password));
            passwordET.requestFocus();
            return;
        }
        else {

            progressDialog.setTitle(getResources().getString(R.string.login));
            progressDialog.setMessage(getResources().getString(R.string.please_wait_while_we_are_allowing_you_into_your_account));
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(true);

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        sendUserToMainActivity();
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.you_are_logged_in), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        progressDialog.dismiss();
                        String message = task.getException().getMessage();
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.error_occurred) + ": " + message, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

    }

    private void sendUserToMainActivity() {

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void sendUserToRegisterActivity() {

        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);

    }
}
