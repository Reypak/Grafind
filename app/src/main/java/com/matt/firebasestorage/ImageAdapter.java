package com.matt.firebasestorage;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private Context mContext;
    private List<Blog> mUploads;

    public ImageAdapter(Context context, List<Blog> uploads) {
        this.mContext = context;
        this.mUploads = uploads;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.grid_item, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageViewHolder holder, final int position) {
        final Blog blog = mUploads.get(position);
//        holder.textViewTitle.setText(blog.getTitle());
        Picasso.with(mContext).load(blog.getImageURL())
                .fit().centerCrop()
                .placeholder(R.drawable.ic_launcher_foreground_invert)
                .into(holder.imageView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String postTitle = mUploads.get(position).title;
                final String postDesc = mUploads.get(position).description;

                Query mDatabase = FirebaseDatabase.getInstance().getReference("Images")
                        .orderByChild("title").equalTo(postTitle);
                mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnap : dataSnapshot.getChildren()) {

                            // double check to match title with description
                            // if they are same then post is loaded
                            if (postSnap.child("description").getValue().equals(postDesc)) {

                                String itemID = postSnap.getRef().getKey();
                                Intent intent = new Intent(mContext, Post_Info.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("itemKey", itemID);
                                mContext.startActivity(intent);
                            }
//                            Toast.makeText(mContext, itemID, Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUploads.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        public ImageViewHolder(@NonNull final View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.projectImage);
            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    itemView.startAnimation(AnimationUtils.loadAnimation(itemView.getContext(), R.anim.zoom_out));
                    return false;
                }
            });
            itemView.startAnimation(AnimationUtils.loadAnimation(itemView.getContext(), R.anim.zoom_in));

        }
    }

}
