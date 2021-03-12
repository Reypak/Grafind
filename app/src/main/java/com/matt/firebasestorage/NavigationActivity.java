package com.matt.firebasestorage;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.matt.firebasestorage.Fragments.FavouritesFragment;
import com.matt.firebasestorage.Fragments.HomeFragment;
import com.matt.firebasestorage.Fragments.MessagesFragment;
import com.matt.firebasestorage.Objects.Location;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class NavigationActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener,
        NavigationView.OnNavigationItemSelectedListener {
    private static FirebaseUser user;
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersData;
    private BottomNavigationView navigation;

    private DrawerLayout drawer;
    private NavigationView navView;
    private ActionBarDrawerToggle toggle;
    private String currentLocation, currentPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setLogo(R.drawable.ic_baseline_add_24);
//        getSupportActionBar().setDisplayUseLogoEnabled(true);

        setContentView(R.layout.activity_drawer);

        CheckSession();

        drawer = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);

        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);

        getDrawerData();

        // Toggle button 3 Lines
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navView.setNavigationItemSelectedListener(this);

        //loading the default fragment
        loadFragment(new HomeFragment());

        //getting bottom navigation view and attaching the listener
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);

        messageCounter();
        // loading fragment from notification intent
        onNewIntent(getIntent());

    }

    private void messageCounter() {
        DatabaseReference mUsersMessages = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).child("Contacts");
        Query query = mUsersMessages.orderByChild("userID").equalTo("1");

        // use a value listener to constantly listen for changes
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int num = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.exists()) {
                        // increment the existing value
                        num++;
                    }
                }
                // value is not null
                if (num != 0) {
                    BottomMenuHelper.showBadge(NavigationActivity.this, navigation, R.id.navigation_messages, String.valueOf(num));
                } else {
                    BottomMenuHelper.removeBadge(navigation, R.id.navigation_messages);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getDrawerData() {

        if (user != null) {
            View headerView = navView.getHeaderView(0);

            final TextView NavTitle = headerView.findViewById(R.id.nav_label);
            TextView NavEmail = headerView.findViewById(R.id.textView);
            final ImageView NavImage = headerView.findViewById(R.id.imageView);

            NavEmail.setText(user.getEmail());

            // getting User profile pic and Username from User database reference
            DatabaseReference userData = FirebaseDatabase.getInstance().getReference("Users" + "/" + user.getUid());
            userData.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {

                        String username = dataSnapshot.child("username").getValue().toString();
                        NavTitle.setText(username);
                        if (dataSnapshot.child("profileURL").exists()) {
                            String profileImage = dataSnapshot.child("profileURL").getValue().toString();
                            Picasso.with(getApplicationContext()).load(profileImage)
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .into(NavImage);
                        }

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        if (extras != null) {

            for (String key : extras.keySet()) {
                if (key.equals("sender")) {

                    String itemID = extras.getString(key);
                    intent = new Intent(this, ChatActivity.class);
                    intent.putExtra("userID", itemID); // save userID of designer
                    startActivity(intent);
                    navigation.setSelectedItemId(R.id.navigation_messages);

                } else if (key.equals("postID")) {

                    String postID = extras.getString(key);
                    intent = new Intent(this, Post_Info.class);
                    intent.putExtra("itemKey", postID); // save userID of designer
                    startActivity(intent);

                }
            }

           /* if (extras.containsKey("postInfo")) {

                // loading messages fragment from notification intent
                navigation.setSelectedItemId(R.id.navigation_messages);
            }*/
        }
    }

    private void CheckSession() {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

      /*  new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

            } },2000);*/

        if (user != null) {

            mUsersData = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
            mUsersData.keepSynced(true);
            mUsersData.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.child("phone").exists()) {
                        addDetails();
                    } else if (!dataSnapshot.child("location").exists()) {
                        addDetails();
                    } else {

                        currentPhone = dataSnapshot.child("phone").getValue().toString();
                        currentLocation = dataSnapshot.child("location").getValue().toString();

                        if (dataSnapshot.child("phone").getValue().toString().matches("")) {
                            addDetails();
                        } else if (dataSnapshot.child("location").getValue().toString().matches("")) {
                            addDetails();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } else {
            Intent intent = new Intent(getApplication(), LogIn.class);
            startActivity(intent);
            finish();
        }
    }

    private void addDetails() {

        final Dialog d = new Dialog(NavigationActivity.this);
        d.setContentView(R.layout.edit_profile_dialog);
        d.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        TextView label = d.findViewById(R.id.label);
        TextView label2 = d.findViewById(R.id.label2);
        label.setText("Add Your Details");

        final EditText etPhone = d.findViewById(R.id.etPhone);
        final AutoCompleteTextView etLocation = d.findViewById(R.id.etLocation);
        TextInputLayout textInputLayout = d.findViewById(R.id.etLayout2);
        TextInputLayout textInputLayout3 = d.findViewById(R.id.etLayout3);
        MaterialButton btnSave = d.findViewById(R.id.save);

        textInputLayout.setVisibility(View.VISIBLE);
        textInputLayout3.setVisibility(View.VISIBLE);
        label2.setVisibility(View.VISIBLE);

        Location location = new Location();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, location.getLocations());
        etLocation.setThreshold(1);
        etLocation.setAdapter(adapter);

        etPhone.setText(currentPhone);
        etLocation.setText(currentLocation);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String newPhone = etPhone.getText().toString().trim();
                final String newLocation = etLocation.getText().toString().trim();

                // create hash map to hold object data
                HashMap<String, Object> userMap = new HashMap<>();
                userMap.put("phone", newPhone);
                userMap.put("location", newLocation);

                if (!newPhone.matches("")) {
                    if (!(newPhone.length() < 10)) {
                        // updating or setting values
                        mUsersData.updateChildren(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
                                d.dismiss();
                            }
                        });
                    } else {
                        etPhone.setError("Can't be less than 10 digits");
                    }
                } else {
                    etPhone.setError("Empty!");
                }

            }
        });
        d.show();
    }

    public void addPost(View v) {
        Intent addPost = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(addPost);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.navigation_home:
                fragment = new HomeFragment();
                getSupportActionBar().setTitle(R.string.app_name);
                break;

            case R.id.navigation_favourites:
                fragment = new FavouritesFragment();
                getSupportActionBar().setTitle(R.string.title_favourites);
                break;

            case R.id.navigation_messages:
                fragment = new MessagesFragment();
                getSupportActionBar().setTitle(R.string.title_messages);
                // remove badge
                BottomMenuHelper.removeBadge(navigation, R.id.navigation_messages);
                break;

            case R.id.navigation_profile:
                Profile(null);
                break;

            case R.id.feedback:
                composeEmail();
                break;

            case R.id.share:
                shareIntent();
                break;

            case R.id.logout:
                SignOut();
                break;
        }

//        CardView btnFloat = findViewById(R.id.btnFloat);

        drawer.closeDrawer(GravityCompat.START);
        return loadFragment(fragment);
    }

    private void shareIntent() {

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Reach out to us: " + getResources().getString(R.string.app_email));
        sendIntent.putExtra(Intent.EXTRA_TITLE, "Tell your friends about Grafind!");
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);

      /*  Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Tell your friends about Grafind!");

        // (Optional) Here we're setting the title of the content
        sendIntent.putExtra(Intent.EXTRA_TITLE, "Share");

        // (Optional) Here we're passing a content URI to an image to be displayed
                sendIntent.setData(contentUri);
                sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Show the Sharesheet
        startActivity(Intent.createChooser(sendIntent, null));*/
    }

    public void composeEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + getResources().getString(R.string.app_email)));
        startActivity(emailIntent);
    }

    public void Profile(View v) {
        // pass userID to Activity
        Intent intent = new Intent(getApplicationContext(), ViewProfile.class);
        intent.putExtra("userID", user.getUid());
        startActivity(intent);
    }

    private void SignOut() {
        mAuth.signOut();
        Toast.makeText(this, "Logged out.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplication(), LogIn.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // closes drawer if its open
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            AboutDialog();
            return true;
        }
        /*else if (id == R.id.action_settings) {

            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }

    private void AboutDialog() {
        final Dialog d = new Dialog(this);
        d.setContentView(R.layout.about_dialog);
        // window animation
        d.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        d.show();
    }
}