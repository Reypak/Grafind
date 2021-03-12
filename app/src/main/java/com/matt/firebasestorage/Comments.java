package com.matt.firebasestorage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class Comments extends AppCompatActivity {

    private String Key;
    private DatabaseReference mDatabase;
    private RecyclerView mCommentList;
    private LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        Key = intent.getStringExtra("itemKey");

        mDatabase = FirebaseDatabase.getInstance().getReference("Images" + "/" + Key).child("Comments");
        mDatabase.keepSynced(true);

        mCommentList = findViewById(R.id.recView);
        mCommentList.setHasFixedSize(true);

        // Here you modify your LinearLayoutManager
        mLayoutManager = new LinearLayoutManager(this);
//        mLayoutManager.setReverseLayout(true);
//        mLayoutManager.setStackFromEnd(true);
        mCommentList.setLayoutManager(mLayoutManager);

        getComments();

    }

    public void getComments() {

        FirebaseRecyclerAdapter<Comment, Comments.CommentViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comment, CommentViewHolder>
                (Comment.class, R.layout.comment_row, Comments.CommentViewHolder.class, mDatabase) {
            @Override
            protected void populateViewHolder(final Comments.CommentViewHolder commentViewHolder, final Comment comment, final int i) {
                // getting data into adapter
                commentViewHolder.setComment(comment.comment);
                String userID = comment.userID; // get userID of the comment

                // getting User profile pic and Username from User database reference
                DatabaseReference userData = FirebaseDatabase.getInstance().getReference("Users" + "/" + userID);
                userData.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String username = dataSnapshot.child("username").getValue().toString();
                            String profileImage = dataSnapshot.child("profileURL").getValue().toString();
                            // call methods for setting data to adapter
                            commentViewHolder.setUsername(username);
                            commentViewHolder.setImageURL(getApplicationContext(), profileImage);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                commentViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        // todo: remove in final version
                        getRef(i).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Comments.this, "Comment Deleted", Toast.LENGTH_SHORT).show();
                            }
                        }); // storing key of data
                        return false;
                    }
                });

            }
        };
        mCommentList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public CommentViewHolder(final View itemView) {
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

        public void setComment(String comment) {
            TextView vComment = mView.findViewById(R.id.comment);
            vComment.setText(comment);
        }

        public void setUsername(String username) {
            TextView vUsername = mView.findViewById(R.id.username);
            vUsername.setText(username);
        }

        public void setImageURL(final Context ctx, String imageURL) {
            ImageView img = mView.findViewById(R.id.profile_image);
            Picasso.with(ctx)
                    .load(imageURL)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_foreground_invert)
                    .into(img);
        }
    }
}