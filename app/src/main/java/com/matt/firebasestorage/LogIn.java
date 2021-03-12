package com.matt.firebasestorage;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LogIn extends AppCompatActivity {
    private static final int RC_SIGN_IN = 120;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private CardView btnGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        getSupportActionBar().hide();

        TextView text1 = findViewById(R.id.text1);
        // bounce animation
        ScaleAnimation animation = new ScaleAnimation(0, (float) 1, 0, (float) 1,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setStartOffset(100);
        animation.setDuration(500);
        text1.startAnimation(animation);

        btnGoogle = findViewById(R.id.btnGoogle);
        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();
    }

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInClient.signOut(); // sign out the user from google client.
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            if (task.isSuccessful()) {
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
//                    Log.d("LogIn", "firebaseAuthWithGoogle:" + account.getId());
                    firebaseAuthWithGoogle(account.getIdToken());
//                    Toast.makeText(this, "Logged in as " + account.getDisplayName(), Toast.LENGTH_SHORT).show();
                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
//                    Log.w("LogIn", "Google sign in failed", e);
                    Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show();
                    // ...
                }
            } else {
                // If sign in fails, display a message to the user.
                Snackbar snackbar = Snackbar
                        .make(findViewById(android.R.id.content), "Connection error, check your internet.", 20000)
                        .setAction("Retry", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent signInIntent = googleSignInClient.getSignInIntent();
                                startActivityForResult(signInIntent, RC_SIGN_IN);
                            }
                        });

                snackbar.show();
//                Toast.makeText(this, "Connection error. Try Again.", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void firebaseAuthWithGoogle(String idToken) {

        final ProgressDialog dialog = ProgressDialog.show(this, "",
                "Logging in...", true);
        dialog.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String device_token = FirebaseInstanceId.getInstance().getToken();
                            String user = mAuth.getCurrentUser().getUid();
                            String email = mAuth.getCurrentUser().getEmail();
                            String username = mAuth.getCurrentUser().getDisplayName();
                            String profileUrl = mAuth.getCurrentUser().getPhotoUrl().toString();

                            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Users");
                            // setting user values in database
                            mDatabase.child(user).child("device_token").setValue(device_token);
                            mDatabase.child(user).child("email").setValue(email);
                            mDatabase.child(user).child("username").setValue(username);
                            mDatabase.child(user).child("profileURL").setValue(profileUrl);

                            // shared prefs to save the login instance
                            /*SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            String first = sharedPreferences.getString("first", null);

                            // if existing login is void
                            if (first == null) {
                                WelcomeDialog();
                                dialog.dismiss();

                                // sets value to existing login
                                sharedPreferences.edit().putString("first", "0").apply();
                            } else {
                                // existing login available
                                openNavigation(null);
                            }*/

                            WelcomeDialog();
                            dialog.dismiss();

                        } else {

                            dialog.dismiss();

                            // If sign in fails, display a message to the user.
                            Snackbar snackbar = Snackbar
                                    .make(findViewById(android.R.id.content), "Authentication Failed.", 20000)
                                    .setAction("Retry", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent signInIntent = googleSignInClient.getSignInIntent();
                                            startActivityForResult(signInIntent, RC_SIGN_IN);
                                        }
                                    });
                            snackbar.show();
                        }

                    }
                });
    }

    public void openNavigation(View v) {
        Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);
        startActivity(intent);
        finish();
        // name of login User
        Toast.makeText(this, "Logged in as " + mAuth.getCurrentUser().getDisplayName(), Toast.LENGTH_LONG).show();
    }

    private void WelcomeDialog() {
        final Dialog d = new Dialog(this, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        d.setContentView(R.layout.welcome_dialog);
        d.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        d.setCancelable(false);
        d.show();
    }
}