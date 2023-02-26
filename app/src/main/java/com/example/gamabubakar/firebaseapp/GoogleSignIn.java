package com.example.gamabubakar.firebaseapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class GoogleSignIn extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    // Request sing in code. Could be anything as you required.
    public static final int RequestSignInCode = 7;

    // Firebase Auth Object.
    public FirebaseAuth firebaseAuth;

    // Google API Client object.
    public GoogleApiClient googleApiClient;

    // Sing out button.
    Button SignOutButton;

    // Google Sign In button .
    com.google.android.gms.common.SignInButton signInButton;

    // TextView to Show Login User Email and Name.
    TextView LoginUserName, LoginUserEmail;

    FirebaseUser firebaseUser;

    public ProgressDialog mProgressDialog;

    public FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_sign_in);

        SignOutButton= findViewById(R.id.sign_out);

        LoginUserName = findViewById(R.id.textViewName);

        LoginUserEmail = findViewById(R.id.textViewEmail);

        signInButton = findViewById(R.id.sign_in_button);

// Getting Firebase Auth Instance into firebaseAuth object.
        firebaseAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + firebaseUser.getUid());
                    Toast.makeText(getApplicationContext(),firebaseUser.getUid(),Toast.LENGTH_SHORT).show();
                    SignOutButton.setVisibility(View.VISIBLE);
                    signInButton.setVisibility(View.GONE);
                    LoginUserEmail.setVisibility(View.VISIBLE);
                    LoginUserName.setVisibility(View.VISIBLE);
                    // Setting up name into TextView.
                    LoginUserName.setText("NAME = "+ firebaseUser.getDisplayName().toString());

// Setting up Email into TextView.
                    LoginUserEmail.setText("Email = "+ firebaseUser.getEmail().toString());

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Toast.makeText(getApplicationContext(),"signout",Toast.LENGTH_SHORT).show();
                    LoginUserEmail.setVisibility(View.GONE);
                    LoginUserName.setVisibility(View.GONE);
                    SignOutButton.setVisibility(View.GONE);
                    signInButton.setVisibility(View.VISIBLE);

                }
            }
        };

// Hiding the TextView on activity start up time.

// Creating and Configuring Google Sign In object.
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

// Creating and Configuring Google Api Client.
        googleApiClient = new GoogleApiClient.Builder(GoogleSignIn.this)
                .enableAutoManage(GoogleSignIn.this , new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                } /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();


// Adding Click listener to User Sign in Google button.
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
showProgressDialog();
                UserSignInMethod();

            }
        });

// Adding Click Listener to User Sign Out button.
        SignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                UserSignOutFunction();

            }
        });

    }

    public void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            firebaseAuth.removeAuthStateListener(mAuthListener);
        }
        hideProgressDialog();
    }
    // Sign In function Starts From Here.
    public void UserSignInMethod(){
// Passing Google Api Client into Intent.
        Intent AuthIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        hideProgressDialog();
        startActivityForResult(AuthIntent, RequestSignInCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        showProgressDialog();
        if (requestCode == RequestSignInCode){

            GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (googleSignInResult.isSuccess()){
                GoogleSignInAccount googleSignInAccount = googleSignInResult.getSignInAccount();
                FirebaseUserAuth(googleSignInAccount);
            }

        }
    }

    public void FirebaseUserAuth(GoogleSignInAccount googleSignInAccount) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);

        Toast.makeText(GoogleSignIn.this,""+ authCredential.getProvider(),Toast.LENGTH_LONG).show();
        firebaseAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(GoogleSignIn.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> AuthResultTask) {

                        if (AuthResultTask.isSuccessful()){
                            Toast.makeText(GoogleSignIn.this,"Success",Toast.LENGTH_LONG).show();

                        }else {
                            Toast.makeText(GoogleSignIn.this,"Something Went Wrong",Toast.LENGTH_LONG).show();
                        }
                    }
                });
        hideProgressDialog();
    }

    public void UserSignOutFunction() {

// Sing Out the User.
        firebaseAuth.signOut();

        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {

// Write down your any code here which you want to execute After Sign Out.

// Printing Logout toast message on screen.
                        Toast.makeText(GoogleSignIn.this, "Logout Successfully", Toast.LENGTH_LONG).show();

                    }
                });

        firebaseUser.delete();
    }
    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(GoogleSignIn.this);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public void facebookActivity(View view) {
        Intent i=new Intent(GoogleSignIn.this,FacebookActivity.class);
        startActivity(i);
    }
}
