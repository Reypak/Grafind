package com.matt.firebasestorage;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.matt.firebasestorage.Objects.Location;
import com.matt.firebasestorage.Objects.User;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class ViewProfile extends AppCompatActivity {

    private static final int MENU_CALL = Menu.FIRST + 1;
    private static final int MENU_EDIT = Menu.FIRST + 2;
    private String userID, profileImage, username, itemID;
    private Menu menu;
    private DatabaseReference userData;
    private TextView likes, views;
    private RecyclerView mBlogList;
    private DatabaseReference mDatabase;
    private LinearLayoutManager mLayoutManager;
    private String currentUser;
    private String userphone, userEmail, userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");

        final Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AppBarLayout mAppBarLayout = findViewById(R.id.app_bar);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    isShow = true;
                    showOption(MENU_CALL);
                } else if (isShow) {
                    isShow = false;
                    hideOption(MENU_CALL);
                }
            }
        });

        getUserInfo();
        getSupportActionBar().setTitle(username);
        getProjects();
        getTotal("Likes");
        getTotal("Views");
    }

    private void getTotal(final String string) {
        likes = findViewById(R.id.likes);
        views = findViewById(R.id.views);
        Query query = mDatabase.orderByChild("userID").equalTo(userID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int sum = 0; // sum of likes
                for (DataSnapshot likesSnapshot : dataSnapshot.getChildren()) {
                    int num = (int) likesSnapshot.child(string).getChildrenCount(); // get likes per post by user
                    sum = sum + num; // add likes to sum

                    String Totallikes = String.valueOf(sum);
                    if (string.matches("Likes")) {
                        likes.setText(Totallikes);
                    }
                    views.setText(Totallikes);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserInfo() {
        final ImageView circleImageView = findViewById(R.id.profile_image);
        final TextView email, phone, location;
        phone = findViewById(R.id.phone);
        email = findViewById(R.id.email);
        location = findViewById(R.id.location);

        // bounce animation
        ScaleAnimation animation = new ScaleAnimation(0, (float) 1, 0, (float) 1,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setStartOffset(300);
        animation.setDuration(300);
        circleImageView.startAnimation(animation);

//        mAuth = FirebaseAuth.getInstance();
//        user = mAuth.getCurrentUser();

//        String userID = user.getUid();
//        String userEmail = user.getEmail();
//        email.setText(userEmail);

        // getting User profile pic and Username from User database reference
        userData = FirebaseDatabase.getInstance().getReference("Users" + "/" + userID);
        userData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    // get user data from database
                    userphone = dataSnapshot.getValue(User.class).getPhone();
                    userEmail = dataSnapshot.getValue(User.class).getEmail();
                    username = dataSnapshot.getValue(User.class).getUsername();
                    userLocation = dataSnapshot.getValue(User.class).getLocation();

                    email.setText(userEmail);

                    if (userphone != null && !userphone.matches("")) {
                        phone.setText(userphone);
                    }
                    if (userLocation != null && !userLocation.matches("")) {
                        location.setText(userLocation);
                    }

                    getSupportActionBar().setTitle(username);

                    if (dataSnapshot.child("profileURL").exists()) {
                        profileImage = dataSnapshot.child("profileURL").getValue().toString();
                        Picasso.with(getApplicationContext()).load(profileImage)
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .into(circleImageView);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;

        menu.add(Menu.NONE, MENU_CALL, Menu.NONE, "Call")
                .setIcon(R.drawable.ic_baseline_phone_enabled_24)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        hideOption(MENU_CALL);
        // if its current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userID.equals(currentUser)) {
            menu.add(Menu.NONE, MENU_EDIT, Menu.NONE, "Edit")
                    .setIcon(R.drawable.ic_edit_24)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            // disable call item
            MenuItem item = menu.findItem(MENU_CALL);
            item.setEnabled(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == MENU_CALL) {
            Call(null);
        } else if (item.getItemId() == MENU_EDIT) {
            editProfile();
        }
        return super.onOptionsItemSelected(item);
    }

    public void editProfile() {

        final Dialog d = new Dialog(this);
        d.setContentView(R.layout.edit_profile_dialog);
        d.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        final EditText etPhone = d.findViewById(R.id.etPhone);
        final AutoCompleteTextView etLocation = d.findViewById(R.id.etLocation);

        TextInputLayout Layout = d.findViewById(R.id.etLayout);
        TextInputLayout Layout2 = d.findViewById(R.id.etLayout2);
        TextInputLayout Layout3 = d.findViewById(R.id.etLayout3);

        Layout.setVisibility(View.VISIBLE);
        Layout2.setVisibility(View.VISIBLE);
        Layout3.setVisibility(View.VISIBLE);

        Location location = new Location();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, location.getLocations());
        etLocation.setThreshold(1);
        etLocation.setAdapter(adapter);

        final EditText etUsername = d.findViewById(R.id.etUsername);
        MaterialButton btnSave = d.findViewById(R.id.save);
        etUsername.setText(username);
        etPhone.setText(userphone);
        etLocation.setText(userLocation);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newUsername = etUsername.getText().toString().trim();
                String newPhone = etPhone.getText().toString().trim();
                String newLocation = etLocation.getText().toString().trim();

                // create hash map to hold object data
                HashMap<String, Object> userMap = new HashMap<>();
                userMap.put("username", newUsername);
                userMap.put("location", newLocation);
                userMap.put("phone", newPhone);

                if (!newUsername.matches("")) {
                    // Must use update children, setValue will overwrite all existing data
                    userData.updateChildren(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
//                            getUserInfo(); // to refresh the data
                            d.dismiss();
                            recreate();
                        }
                    });
                } else {
                    etUsername.setError("Empty!");
                }

            }
        });
        d.show();
    }

    public void sendEmail(View view) {
        composeEmail(userEmail);
    }

    public void composeEmail(String addresses) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + addresses));
        startActivity(emailIntent);
    }


    public void getProjects() {

        mDatabase = FirebaseDatabase.getInstance().getReference("Images");
        mDatabase.keepSynced(true);

        mBlogList = findViewById(R.id.recView);
        mBlogList.setHasFixedSize(true);

        // Here you modify your LinearLayoutManager
        mLayoutManager = new GridLayoutManager(this, 2);
        mBlogList.setLayoutManager(mLayoutManager);

        Query query = mDatabase.orderByChild("userID").equalTo(userID);
        FirebaseRecyclerAdapter<Blog, ViewProfile.BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, ViewProfile.BlogViewHolder>
                (Blog.class, R.layout.grid_item, ViewProfile.BlogViewHolder.class, query) {
            @Override
            protected void populateViewHolder(ViewProfile.BlogViewHolder blogViewHolder, final Blog blog, final int i) {
                // changing RecView heading
                TextView projectText = findViewById(R.id.projectsText);
                projectText.setText("Projects by " + username);

                // getting data into adapter
                blogViewHolder.setImageURL(getApplicationContext(), blog.getImageURL());

                blogViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        itemID = getRef(i).getKey(); // storing key of data

                        Intent intent = new Intent(getApplicationContext(), Post_Info.class);
                        intent.putExtra("itemKey", itemID);
                        startActivity(intent);
                    }
                });

            }
        };

        mBlogList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public BlogViewHolder(final View itemView) {
            super(itemView);
            mView = itemView;

            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    itemView.startAnimation(AnimationUtils.loadAnimation(itemView.getContext(), R.anim.zoom_out));
                    return false;
                }
            });

            itemView.startAnimation(AnimationUtils.loadAnimation(itemView.getContext(), R.anim.zoom_in));

        }

//        public void setTitle(String title) {
//            TextView post_title = mView.findViewById(R.id.imagetitle);
//            post_title.setText(title);
//        }

        public void setImageURL(final Context ctx, String imageURL) {
            ImageView img = mView.findViewById(R.id.projectImage);
            Picasso.with(ctx)
                    .load(imageURL)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_foreground_invert)
                    .into(img);
        }
    }

    public void Chat(View view) {
        // if its current user
        if (!userID.equals(currentUser)) {
            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
            intent.putExtra("userID", userID); // save userID of designer
            intent.putExtra("username", username); // username of designer
            intent.putExtra("profileImage", profileImage); // username of designer
            startActivity(intent);
        } else {
            Toast.makeText(this, "You can not chat with yourself!", Toast.LENGTH_SHORT).show();
        }

    }

    public void Call(View view) {
        // database reference to other users phone contact
        userData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("phone").exists()) {
                    String phone;
                    phone = dataSnapshot.child("phone").getValue().toString();

                    // opening the dial pad with number
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)); // Initiates the Intent
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "No phone details from this user", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void hideOption(int id) {
        MenuItem item = menu.findItem(id);
        item.setVisible(false);
    }

    private void showOption(int id) {
        MenuItem item = menu.findItem(id);
        item.setVisible(true);
    }

/*
    private boolean loadFragment(Fragment fragment) {

        //switching fragment
        if (fragment != null) {

            // pass userID to Fragment
            Bundle bundle = new Bundle();
            bundle.putString("userID", userID);
            fragment = new ProfileFragment();
            fragment.setArguments(bundle);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
*/
}