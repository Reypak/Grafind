package com.matt.firebasestorage.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matt.firebasestorage.Blog;
import com.matt.firebasestorage.Post_Info;
import com.matt.firebasestorage.R;
import com.squareup.picasso.Picasso;

public class FavouritesFragment extends Fragment {
    private String userID, itemID;
    private RecyclerView mBlogList;
    private DatabaseReference mDatabase, userDatabase;
    private LinearLayoutManager mLayoutManager;
    private View noFavourites;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_favourites, container, false);

        mBlogList = rootView.findViewById(R.id.recView);
        noFavourites = rootView.findViewById(R.id.noFavourites);
        getProjects();

        return rootView;
    }

    public void getProjects() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();

        // post database
        mDatabase = FirebaseDatabase.getInstance().getReference("Images");
        // favourites database
        userDatabase = FirebaseDatabase.getInstance().getReference("Users")
                .child(userID).child("Favourites");

        mDatabase.keepSynced(true);
        userDatabase.keepSynced(true);
        mBlogList.setHasFixedSize(true);

        // Here you modify your LinearLayoutManager
        mLayoutManager = new GridLayoutManager(getActivity(), 2);
        mBlogList.setLayoutManager(mLayoutManager);

        FirebaseRecyclerAdapter<String, FavouritesFragment.BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<String, BlogViewHolder>
                (String.class, R.layout.grid_item, FavouritesFragment.BlogViewHolder.class, userDatabase) {
            @Override
            protected void populateViewHolder(final FavouritesFragment.BlogViewHolder blogViewHolder, final String blog, final int i) {
                itemID = getRef(i).getKey();
                noFavourites.setVisibility(View.GONE);

                // get item key from *Favourites and pull its data from *Images
                mDatabase.child(itemID)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                // check if item exists in *Images
                                if (dataSnapshot.exists()) {
                                    String imageURL = dataSnapshot.getValue(Blog.class).getImageURL();

                                    // getting data into adapter
                                    blogViewHolder.setImageURL(getActivity(), imageURL);
                                } else {
                                    // delete item *Fav database
                                    getRef(i).removeValue();
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                blogViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        itemID = getRef(i).getKey(); // storing key of data

                        Intent intent = new Intent(getActivity(), Post_Info.class);
                        intent.putExtra("itemKey", itemID);
                        startActivity(intent);
                    }
                });

                blogViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        registerForContextMenu(view);
                        itemID = getRef(i).getKey();
                        return false;
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

    public void deleteFavourite() {
        Snackbar snackbar = Snackbar
                .make(getActivity().findViewById(android.R.id.content), "Confirm Delete?", Snackbar.LENGTH_LONG)
                .setAction("YES", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Snackbar mSnackbar = Snackbar.make(getActivity().findViewById(android.R.id.content),
                                "Post successfully deleted.", Snackbar.LENGTH_SHORT);
                        userDatabase.child(itemID).getRef().removeValue();
                        mSnackbar.show();
                    }
                });

        snackbar.show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, v.getId(), 0, "Delete");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle() == "Delete") {
            deleteFavourite();
        }
        return super.onContextItemSelected(item);
    }
}