package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    @BindView(R.id.login_facebook_BT)
    LoginButton facebookLoginBT;
    @BindView(R.id.login_google_IV)
    ImageView googleLoginIV;

    private FirebaseAuth mAuth;
    private DatabaseReference usersReference;
    private ProgressDialog progressDialog;
    private CallbackManager mCallbackManager;

    private static final int RC_SIGN_IN = 1;
    private static final int FC_SIGN_IN = 2;
    private static final String TAG = LoginActivity.class.getCanonicalName();
    private GoogleApiClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        usersReference.keepSynced(true);

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

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //Set GoogleApi
        mGoogleSignInClient = new GoogleApiClient.Builder(this).enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                Toast.makeText(LoginActivity.this, getResources().getString(R.string.connection_to_google_sign_in_failed), Toast.LENGTH_LONG).show();
            }
        }).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

        googleLoginIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInGoogle();
            }
        });

        forgotPasswordTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToResetPasswordActivity();
            }
        });

        facebookLoginBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInFacebook();
            }
        });


    }


    private void signInGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RC_SIGN_IN) {
            progressDialog.setTitle(getResources().getString(R.string.google_sign_in));
            progressDialog.setMessage(getResources().getString(R.string.please_wait_while_we_are_allowing_you_to_login_to_your_google_account));
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();


            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                Toast.makeText(LoginActivity.this, getResources().getString(R.string.please_wait_while_we_are_getting_your_google_auth_result), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(LoginActivity.this, getResources().getString(R.string.cannot_get_google_auth_result), Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        } else {

            mCallbackManager.onActivityResult(requestCode, resultCode, data);
            if (!TextUtils.isEmpty(mCallbackManager.toString())) {

                // Pass the activity result back to the Facebook SDK

                progressDialog.setTitle(getResources().getString(R.string.facebook_sign_in));
                progressDialog.setMessage(getResources().getString(R.string.please_wait_while_we_are_allowing_you_to_login_to_your_facebook_account));
                progressDialog.setCanceledOnTouchOutside(true);
                progressDialog.show();

            }
        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            sendUserToMainActivity();
                            Log.d(TAG, mAuth.getUid());
                            progressDialog.dismiss();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            String message = task.getException().getMessage();
                            sendUserToLoginActivity();
                            Log.d(TAG + " Failure", mAuth.getUid());
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.not_authenticated) + " " + message, Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();

                        }


                    }
                });
    }

    private void sendUserToLoginActivity() {

        Intent loginIntent = new Intent(LoginActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);

    }


    private void loginVerification() {

        String email = emailET.getText().toString();
        String password = passwordET.getText().toString();

        if (TextUtils.isEmpty(email)) {
            emailET.setError(getResources().getString(R.string.enter_email_address));
            emailET.requestFocus();
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailET.setError(getResources().getString(R.string.please_enter_a_valid_email));
            emailET.requestFocus();
            return;
        } else if (TextUtils.isEmpty(password)) {
            passwordET.setError(getResources().getString(R.string.prompt_password));
            passwordET.requestFocus();
            return;
        } else {

            progressDialog.setTitle(getResources().getString(R.string.login));
            progressDialog.setMessage(getResources().getString(R.string.please_wait_while_we_are_allowing_you_into_your_account));
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();


            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        sendUserToMainActivity();
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.you_are_logged_in), Toast.LENGTH_SHORT).show();
                    } else {
                        progressDialog.dismiss();
                        String message = task.getException().getMessage();
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.error_occurred) + ": " + message, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

    }

    private void sendUserToRegisterActivity() {

        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);

    }

    private void sendUserToResetPasswordActivity() {
        Intent resetIntent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
        startActivity(resetIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
          //  sendUserToMainActivity();
        }

    }


    private void sendUserToMainActivity() {

        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);

    }


    private void signInFacebook() {
        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        facebookLoginBT.setReadPermissions("email", "public_profile");
        facebookLoginBT.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            sendUserToMainActivity();
                            progressDialog.dismiss();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                            progressDialog.dismiss();

                        }
                    }
                });
    }


}
