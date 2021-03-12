package com.matt.firebasestorage.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.matt.firebasestorage.Blog;
import com.matt.firebasestorage.Post_Info;
import com.matt.firebasestorage.R;
import com.squareup.picasso.Picasso;

public class HomeFragment extends Fragment {
    private RecyclerView mBlogList;
    private DatabaseReference mDatabase;
    private String itemID;
    private SwipeRefreshLayout swipeLayout;
    private LinearLayoutManager mLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

//        FirebaseStorage mStorage = FirebaseStorage.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Images");
        mDatabase.keepSynced(true);

        swipeLayout = rootView.findViewById(R.id.swipe_container);
        mBlogList = rootView.findViewById(R.id.recView);
        mBlogList.setHasFixedSize(true);

        // Here you modify your LinearLayoutManager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        mBlogList.setLayoutManager(mLayoutManager);

        getData();
        refreshData();

        return rootView;
    }

    private void refreshData() {
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeLayout.setRefreshing(false);
                    }
                }, 2000);

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
//        mLayoutManager.scrollToPositionWithOffset(0, 0);
//        // scrolling to top of list
//        mBlogList.scrollToPosition(mBlogList.getAdapter().getItemCount() - 1);
    }

    private void getData() {
        Query query = mDatabase;
        final FirebaseRecyclerAdapter<Blog, HomeFragment.BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, HomeFragment.BlogViewHolder>
                (Blog.class, R.layout.blog_row, HomeFragment.BlogViewHolder.class, query) {
            @Override
            protected void populateViewHolder(HomeFragment.BlogViewHolder blogViewHolder, final Blog blog, final int i) {
                // getting data into adapter
                blogViewHolder.setTitle(blog.getTitle());
                blogViewHolder.setDescription(blog.getDescription());
                blogViewHolder.setImageURL(getActivity(), blog.getImageURL());


                // Todo: *FUTURE USE* to clean up the database of invalid posts
               /* if (blog.getUserID() == null && blog.getTimestamp() == null) {
                    getRef(i).removeValue();
                }*/

                blogViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                       /* String URL = blog.getImageURL();
                        String Title = blog.getTitle();
                        String Description = blog.getDescription();
                        Toast.makeText(view.getContext(), Title, Toast.LENGTH_SHORT).show();*/

                        itemID = getRef(i).getKey(); // storing key of data

                        // delay to allow pop animation
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(getActivity(), Post_Info.class);
                                intent.putExtra("itemKey", itemID);
                                startActivity(intent);
                            }
                        }, 200);


                    }
                });

                blogViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        itemID = getRef(i).getKey();
                        registerForContextMenu(view);
                        return false;
                    }
                });

                final FrameLayout btnLike = blogViewHolder.mView.findViewById(R.id.btnLike);
                btnLike.setOnClickListener(new View.OnClickListener() {
//                    public DatabaseReference mDatabase;

                    @Override
                    public void onClick(View view) {
                        // bounce animation
                        ScaleAnimation animation = new ScaleAnimation(1, (float) 1.2, 1, (float) 1.2,
                                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        animation.setDuration(300);
                        animation.setRepeatMode(Animation.REVERSE);
                        animation.setRepeatCount(3);

                        btnLike.startAnimation(animation);
                        btnLike.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ic_baseline_thumb_up_24));

                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        FirebaseUser user = mAuth.getCurrentUser();
                        String itemKey = getRef(i).getKey();

                        DatabaseReference likesData = FirebaseDatabase.getInstance().getReference("Images")
                                .child(itemKey).child("Likes").child(user.getUid());
                        likesData.setValue("1");

                        Toast.makeText(view.getContext(), "Liked", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        mBlogList.setAdapter(firebaseRecyclerAdapter);

        // new item inserted into the list
        firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                int friendlyMessageCount = firebaseRecyclerAdapter.getItemCount();
                int lastVisiblePosition = mLayoutManager.findLastCompletelyVisibleItemPosition();

                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition >= -1 ||
                        (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
//                    mBlogList.scrollToPosition(positionStart);
                    mBlogList.smoothScrollToPosition(positionStart);
                }
            }
        });
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

            // animations for incoming frame layouts with text
            FrameLayout Frame1 = mView.findViewById(R.id.Frame1);
            FrameLayout Frame2 = mView.findViewById(R.id.Frame2);
            final ProgressBar progressBar = mView.findViewById(R.id.progress);

            // hide progressbar after 50sec
            progressBar.postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                }
            }, 40000);

            Animation SlideIn = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.zoom_in);
            SlideIn.setStartOffset(850);
            Frame1.startAnimation(SlideIn);

            Animation SlideIn2 = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.zoom_in);
            SlideIn2.setStartOffset(1000);
            Frame2.startAnimation(SlideIn2);

        }

        public void setTitle(String title) {
            TextView post_title = mView.findViewById(R.id.imagetitle);
            post_title.setText(title);
        }

        public void setDescription(String description) {
            TextView post_desc = mView.findViewById(R.id.description);
            post_desc.setText(description);
        }

        public void setImageURL(final Context ctx, String imageURL) {
            ImageView img = mView.findViewById(R.id.imageV);
            Picasso.with(ctx)
                    .load(imageURL)
                    .fit()
                    .centerCrop()
//                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(img);
        }
    }

    public void addFavourites() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // push itemKey to User Favourites
        DatabaseReference userData = FirebaseDatabase.getInstance().getReference("Users" + "/" + user.getUid());
        userData.child("Favourites").child(itemID).setValue("");
        Toast.makeText(getActivity(), "Added to Favourites", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
//        menu.add(0, v.getId(), 0, "Info");
        menu.add(0, v.getId(), 0, "Add to Favorites");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle() == "Add to Favorites") {
            addFavourites();
        }
        return super.onContextItemSelected(item);
    }
}