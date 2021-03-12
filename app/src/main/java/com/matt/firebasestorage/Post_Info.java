package com.matt.firebasestorage;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Post_Info extends AppCompatActivity {
    private static final int MENU_ADD = Menu.FIRST;
    private static final int MENU_DELETE = Menu.FIRST + 2;
    private static final int MENU_EDIT = Menu.FIRST + 1;
    private ImageView imagePost;
    private CircleImageView circleImageView;
    private TextView postTitle, postDesc, timestamp, owner, Likes, Views, viewComments;
    private String URL, Key, username, profileImage, userID;
    private MaterialButton hire;
    private DatabaseReference mDatabase, likesData;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private int num;
    private RecyclerView mProjectList;
    private View btnLike, btnSend;
    private GridLayoutManager mLayoutManager;
    private List<Blog> mUploads;
    private ImageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post__info);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        Key = intent.getStringExtra("itemKey");

        imagePost = findViewById(R.id.imageV);
        postTitle = findViewById(R.id.postTitle);
        postDesc = findViewById(R.id.description);
        Likes = findViewById(R.id.likes);
        Views = findViewById(R.id.views);
        hire = findViewById(R.id.btnHire);
        btnLike = findViewById(R.id.btnLike);
        btnSend = findViewById(R.id.btnSend);
        viewComments = findViewById(R.id.viewComments);
        timestamp = findViewById(R.id.timestamp);
        circleImageView = findViewById(R.id.profile_image);
        owner = findViewById(R.id.owner);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        getData();
        getLikes();
        getViewsComments();


        hire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra("userID", userID); // save userID of designer
                intent.putExtra("username", username); // username of designer
                intent.putExtra("profileImage", profileImage); // username of designer
                startActivity(intent);
            }
        });

        viewComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openComments();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addComment();

            }
        });

        scrollEffect();

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkExist();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();

        } else if (item.getItemId() == MENU_ADD) {
            addFavourites(null);

        } else if (item.getItemId() == MENU_DELETE) {
            Snackbar snackbar = Snackbar
                    .make(findViewById(android.R.id.content), "Confirm Delete?", Snackbar.LENGTH_LONG)
                    .setAction("YES", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Snackbar mSnackbar = Snackbar.make(findViewById(android.R.id.content),
                                    "Post successfully deleted.", Snackbar.LENGTH_SHORT);
                            deletePost();
                            mSnackbar.show();
                        }
                    });

            snackbar.show();


        } else if (item.getItemId() == MENU_EDIT) {
            editPostDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_ADD, Menu.NONE, "Add to Favourites");
        // checks if current user is author of the post
        if (userID != null) {
            if (userID.equals(user.getUid())) {
                menu.add(Menu.NONE, MENU_EDIT, Menu.NONE, "Edit");
                menu.add(Menu.NONE, MENU_DELETE, Menu.NONE, "Delete");
            }
        }

        return true;
    }

    private void scrollEffect() {
        final ScrollView scrollView = findViewById(R.id.scrollView1);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = scrollView.getScrollY();

                CardView cardView = findViewById(R.id.cardview);
                if (scrollY > 2) {
                    if (scrollY < 40) {
                        cardView.setCardElevation(scrollY);
                    }
                }

                // setting parameters to cardView
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) cardView.getLayoutParams();
                if (scrollY <= 60) {
                    if (scrollY == 0) {
                        layoutParams.topMargin = 15;
                        /* layoutParams.setMargins(20, 15, 20, 0):*/
                    } else {
                        // scrolling upward
                        layoutParams.topMargin = -scrollY;
                        /*layoutParams.setMargins(20, -scrollY, 20, 0);*/
                    }
                    cardView.requestLayout();
                }

            }
        });
    }

    private void addComment() {
        final EditText etComment = findViewById(R.id.etComment);
        String comment = etComment.getText().toString().trim();
        if (!comment.matches("")) {
            // creating a new comment picking the current userID and comment to push
            Comment comment1 = new Comment(user.getUid(), comment);
            mDatabase.child("Comments").push().setValue(comment1)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            openComments();
                            Toast.makeText(Post_Info.this, "Posted", Toast.LENGTH_SHORT).show();
                            etComment.setText(null);
                        }
                    });
        } else {
            etComment.setError("Empty!");
        }
    }

    private void openComments() {
        Intent intent = new Intent(getApplicationContext(), Comments.class);
        intent.putExtra("itemKey", Key);
        startActivity(intent);
    }

    private void getUserProjects() {
        mProjectList = findViewById(R.id.recView2);
        mLayoutManager = new GridLayoutManager(this, 2);
//        mLayoutManager.setReverseLayout(true);
        mProjectList.setLayoutManager(mLayoutManager);
        mUploads = new ArrayList<>();

      /*  // Set Horizontal Layout Manager
        LinearLayoutManager HorizontalLayout;
        HorizontalLayout
                = new LinearLayoutManager(
                getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,
                false);
        mProjectList.setLayoutManager(HorizontalLayout);*/

        /** getting other projects
         create a query to find all nodes with userID
         limitToFirst only choose the first 4 nodes
         */
        Query userProjects = FirebaseDatabase.getInstance().getReference("Images")
                .orderByChild("userID").equalTo(userID).limitToLast(5);

        userProjects.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Blog blog = postSnapshot.getValue(Blog.class);

                    // if imageURL is same as the one in post then do not add to array
                    if (!blog.imageURL.equals(URL)) {

                        // add strings into array until they reach 4 then stop
                        if (!(mUploads.size() == 4))
                            mUploads.add(blog);
                    }

                }
                mAdapter = new ImageAdapter(getApplicationContext(), mUploads);
                mProjectList.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void checkExist() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    finish();
                    Toast.makeText(Post_Info.this, "This post is no longer available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void editPostDialog() {
        final Dialog d = new Dialog(this);
        d.setContentView(R.layout.edit_post_dialog);


        final EditText etTitle = d.findViewById(R.id.etTitle);
        final EditText etDesc = d.findViewById(R.id.etDesc);
        MaterialButton btnSave = d.findViewById(R.id.save);
        etTitle.setText(postTitle.getText());
        etDesc.setText(postDesc.getText());
        etTitle.requestFocus();
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!etTitle.getText().toString().trim().matches("")) {
                    if (!etDesc.getText().toString().trim().matches("")) {
                        mDatabase.child("title").setValue(etTitle.getText().toString().trim());
                        mDatabase.child("description").setValue(etDesc.getText().toString().trim())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(Post_Info.this, "Updated", Toast.LENGTH_SHORT).show();
                                        getData(); // to refresh the data
                                        d.dismiss();
                                    }
                                });
                    } else {
                        etDesc.setError("Empty!");
                    }
                } else {
                    etTitle.setError("Empty!");
                }

            }
        });
        d.show();

    }

    private void deletePost() {
        // deleting image from firebase storage
        FirebaseStorage mStorage = FirebaseStorage.getInstance();
        StorageReference imageRef = mStorage.getReferenceFromUrl(URL);
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // deleting record from realtime database
                mDatabase = FirebaseDatabase.getInstance().getReference("Images");
                mDatabase.child(Key).removeValue();
                finish();
            }
        });
    }

    private void getViewsComments() {
        // get last available comment
        mDatabase.child("Comments").limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String comment = snapshot.child("comment").getValue().toString();
                    String userID = snapshot.child("userID").getValue().toString();

                    View linearComment = findViewById(R.id.linearComment);
                    linearComment.setVisibility(View.VISIBLE);
                    TextView commentView = findViewById(R.id.comment);
                    final TextView userView = findViewById(R.id.commentUser);
                    commentView.setText(comment);

                    DatabaseReference userData = FirebaseDatabase.getInstance().getReference("Users" + "/" + userID);
                    userData.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                username = dataSnapshot.child("username").getValue().toString();
                                userView.setText(username);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //number of children in *Comments reference
        mDatabase.child("Comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int num = (int) dataSnapshot.getChildrenCount();
                //if no comments available comments button is disabled
                if (!(num == 0)) {
                    String commentCount = String.valueOf(num);
                    viewComments.setText("View All Comments (" + commentCount + ")");
                    viewComments.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // number of views in *Views field
        DatabaseReference TotalViews = FirebaseDatabase.getInstance().getReference("Images")
                .child(Key).child("Views");
        TotalViews.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int num = (int) dataSnapshot.getChildrenCount();
                String LikeCount = String.valueOf(num);
                Views.setText(LikeCount + " Views");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void viewImage() {
        imagePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ViewImage.class);
                intent.putExtra("imageURL", URL);
                startActivity(intent);
            }
        });
    }

    private void getData() {
        mDatabase = FirebaseDatabase.getInstance().getReference("Images" + "/" + Key);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String title = dataSnapshot.getValue(Blog.class).title;
                    String desc = dataSnapshot.getValue(Blog.class).description;
                    String time = dataSnapshot.getValue(Blog.class).timestamp;
                    userID = dataSnapshot.getValue(Blog.class).userID;
                    URL = dataSnapshot.getValue(Blog.class).imageURL;

                    getSupportActionBar().setTitle(title);
                    getUserProjects();

                    // getting User profile pic and Username from User database reference
                    DatabaseReference userData = FirebaseDatabase.getInstance().getReference("Users" + "/" + userID);
                    userData.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                username = dataSnapshot.child("username").getValue().toString();
                                owner.setText(username);
                                profileImage = dataSnapshot.child("profileURL").getValue().toString();
                                Picasso.with(getApplicationContext()).load(profileImage)
                                        .placeholder(R.drawable.ic_launcher_foreground_invert)
                                        .into(circleImageView);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

//                String title = dataSnapshot.child("title").getValue().toString();
                    postTitle.setText(title);
                    postDesc.setText(desc);
                    setImageURL(getApplicationContext(), URL);

                    final ProgressBar progressBar = findViewById(R.id.progressC);
                    // hide progressbar after 20sec
                    progressBar.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                        }
                    }, 40000);

                    // date converter
                    SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmm");
                    try {
                        Date d = sdf.parse(time);
                        sdf.applyPattern("HH:mm · dd MMM yy");
                        String newTime = sdf.format(d);
                        timestamp.setText(newTime);
                    } catch (ParseException ignored) {
                    }

                    checkLikes();
                    viewPost();
                    viewImage();

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkLikes() {
        // *Likes reference for user under a particular post
        likesData = FirebaseDatabase.getInstance().getReference("Images")
                .child(Key).child("Likes").child(user.getUid());

        likesData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // if user reference exists in *Likes
                if (dataSnapshot.exists()) {
                    // setting the like button to block
                    btnLike.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_thumb_up_24));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void viewPost() {

        // creates a Views reference under a particular post with UserID
        final DatabaseReference viewData = FirebaseDatabase.getInstance().getReference("Images")
                .child(Key).child("Views").child(user.getUid());

        viewData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    viewData.setValue("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void getLikes() {
        // number of likes in *Likes parent
        DatabaseReference TotalLikes = FirebaseDatabase.getInstance().getReference("Images")
                .child(Key).child("Likes");
        TotalLikes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // getting the number of users in the *Likes reference
                num = (int) dataSnapshot.getChildrenCount();
                String LikeCount = String.valueOf(num);
                Likes.setText(LikeCount + " Likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setImageURL(Context ctx, String imageURL) {
        Picasso.with(ctx).load(imageURL)
                .placeholder(R.drawable.ic_launcher_foreground_invert)
                .into(imagePost);
    }

    public void likePost(View v) {
        // checking if the post still exists before pushing the values
        DatabaseReference postExist = FirebaseDatabase.getInstance().getReference("Images").child(Key);
        postExist.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // like button setting
                    likesData.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            // remove like value if exists
                            if (dataSnapshot.exists()) {
                                likesData.getRef().removeValue();

//                                num--; // decrementing the existing value
                                String LikeCount = String.valueOf(num);
                                Likes.setText(LikeCount + " Likes");
                                // setting the like button to outline
                                btnLike.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_outline_thumb_up_alt_24));

                            } else {
//                                num++; // incrementing the existing value
                                String LikeCount = String.valueOf(num);

                                Likes.setText(LikeCount + " Likes");
                                // creates a child record with userID under the post
                                likesData.setValue("1");
                                Toast.makeText(getApplicationContext(), "Liked", Toast.LENGTH_SHORT).show();
                                btnLike.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_thumb_up_24));

                                likeAnimation();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                } else {
                    finish();
                    Toast.makeText(Post_Info.this, "Post was removed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public void viewProfile(View v) {
        // pass userID to Activity
        Intent intent = new Intent(getApplicationContext(), ViewProfile.class);
        intent.putExtra("userID", userID);
        startActivity(intent);
    }

    public void likeAnimation() {
        View likeIcon = findViewById(R.id.likeIcon);
        // bounce animation
        ScaleAnimation animation = new ScaleAnimation(1, (float) 1.2, 1, (float) 1.2,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(300);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setRepeatCount(3);
        likeIcon.startAnimation(animation);
    }

    public void addFavourites(View v) {
        View btnFav = findViewById(R.id.btnFav);
        // bounce animation
        ScaleAnimation animation = new ScaleAnimation(1, (float) 1.2, 1, (float) 1.2,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(300);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setRepeatCount(3);
        btnFav.startAnimation(animation);
        btnFav.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_favorite_24));
        Toast.makeText(this, "Added to Favourites", Toast.LENGTH_SHORT).show();

        // push itemKey to User Favourites
        DatabaseReference userData = FirebaseDatabase.getInstance().getReference("Users" + "/" + user.getUid());
        userData.child("Favourites").child(Key).setValue("");
    }
}