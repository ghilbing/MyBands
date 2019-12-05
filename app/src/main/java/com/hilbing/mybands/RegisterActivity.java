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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity {

    @BindView(R.id.register_email_ET)
    EditText emailET;
    @BindView(R.id.register_password_ET)
    EditText passwordET;
    @BindView(R.id.register_confirm_password_ET)
    EditText confirmPasswordET;
    @BindView(R.id.register_already_TV)
    TextView alreadyRegisteredTV;
    @BindView(R.id.register_create_account_BT)
    Button createAccountBT;

    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        createAccountBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewUserAccount();
            }
        });

        alreadyRegisteredTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToLoginActivity();
            }
        });

    }

    private void sendUserToLoginActivity() {

        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void createNewUserAccount(){
        String email = emailET.getText().toString();
        String password = passwordET.getText().toString();
        String confirmPassword = confirmPasswordET.getText().toString();

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
        else if (password.length() < 6){
            passwordET.setError(getResources().getString(R.string.minimum_length_of_password_should_be_6));
            passwordET.requestFocus();
            return;
        }

        else if (TextUtils.isEmpty(password)) {
            passwordET.setError(getResources().getString(R.string.prompt_password));
            passwordET.requestFocus();
            return;
        }

        else if(TextUtils.isEmpty(confirmPassword)){
            confirmPasswordET.setError(getResources().getString(R.string.prompt_password));
            confirmPasswordET.requestFocus();
            return;
        }

        else if(!password.equals(confirmPassword)){
            confirmPasswordET.setError(getResources().getString(R.string.passwords_are_not_equal));
            confirmPasswordET.requestFocus();
            return;
        }
        else if (TextUtils.isEmpty(email) && TextUtils.isEmpty(password) && TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(RegisterActivity.this, getResources().getString(R.string.fields_are_empty), Toast.LENGTH_LONG).show();
        }

        else {
            if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

                progressDialog.setTitle(getResources().getString(R.string.creating_account));
                progressDialog.setMessage(getResources().getString(R.string.please_wait_while_we_are_creating_your_new_account));
                progressDialog.show();
                progressDialog.setCanceledOnTouchOutside(true);

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sendUserToSetUpActivity();
                            Toast.makeText(RegisterActivity.this, getResources().getString(R.string.you_are_authenticated_succesfully), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        } else {
                            String message = task.getException().getMessage();
                            Toast.makeText(RegisterActivity.this, getResources().getString(R.string.error_occurred) + ": " + message, Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    }
                });
            }
        }
    }

    private void sendUserToSetUpActivity() {
        Intent setUpIntent = new Intent(RegisterActivity.this, SetUpActivity.class);
        setUpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setUpIntent);
        finish();

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            sendUserToMainActivity();
        }
    }

    private void sendUserToMainActivity() {

        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
