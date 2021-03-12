package com.matt.firebasestorage.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matt.firebasestorage.ChatActivity;
import com.matt.firebasestorage.Objects.Contact;
import com.matt.firebasestorage.R;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MessagesFragment extends Fragment {
    private RecyclerView MessagesList;
    private LinearLayoutManager mLayoutManager;
    private DatabaseReference mDatabase;
    private View noMessage;

    public static String durationFromNow(Date startDate) {

        long different = System.currentTimeMillis() - startDate.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;
        long weeksInMilli = daysInMilli * 7;
        long monthsInMilli = daysInMilli * 30;
        long yearsInMilli = monthsInMilli * 12;

        long elapsedYears = different / yearsInMilli;
        different = different % yearsInMilli;

        long elapsedMonths = different / monthsInMilli;
        different = different % monthsInMilli;

        long elapsedWeeks = different / weeksInMilli;
        different = different % weeksInMilli;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        String output = "";
        if (elapsedYears > 0) {
            output += elapsedYears + "y";
        } else if (elapsedYears > 0 || elapsedMonths > 0) {
            output += elapsedMonths + " mon";
        } else if (elapsedMonths > 0 || elapsedWeeks > 0) {
            output += elapsedWeeks + "w";
        } else if (elapsedWeeks > 0 || elapsedDays > 0) {
            output += elapsedDays + "d";
        } else if (elapsedDays > 0 || elapsedHours > 0) {
            output += elapsedHours + "h";
        } else if (elapsedHours > 0 || elapsedMinutes > 0) {
            output += elapsedMinutes + "m";
        } else if (elapsedMinutes > 0 || elapsedSeconds > 0) {
            output += elapsedSeconds + "s";
        }

        return output;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_messages, container, false);

        MessagesList = rootView.findViewById(R.id.recView);
        MessagesList.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        MessagesList.setLayoutManager(mLayoutManager);

        noMessage = rootView.findViewById(R.id.noMessage);
        getData();
        return rootView;
    }

    public void getData() {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference("Users")
                .child(user.getUid()).child("Contacts");

        FirebaseRecyclerAdapter<Contact, MessagesFragment.MessageViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Contact, MessagesFragment.MessageViewHolder>
                (Contact.class, R.layout.message_row, MessagesFragment.MessageViewHolder.class,
                        // here we order our contact database using the *time child
                        // the data is arranged in then format of the highest time going down
                        mDatabase.orderByChild("timestamp")) {
            private String convertTime;

            @Override
            protected void populateViewHolder(final MessagesFragment.MessageViewHolder messageViewHolder, final Contact contact, final int i) {
                // getting data into adapter
                String itemID = getRef(i).getKey(); // storing key of data
                noMessage.setVisibility(View.GONE);
                // we listen to every item in the database using its Key
                // its data is returned (message) and set to the adapter into list

                // TODO: this method specifically updates list items when changed
                mDatabase.child(itemID).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                mDatabase.child(itemID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        /*Alternative
                        dataSnapshot.child(contact.getLast_message());*/
                        String message = dataSnapshot.child("last_message").getValue().toString();
                        String userID = dataSnapshot.child("userID").getValue().toString();
                        String timestamp = dataSnapshot.child("timestamp").getValue().toString();

                        messageViewHolder.setMessage(message); // set message

                        // check to see if message is from other user
                        if (userID.equals("1")) {
                            messageViewHolder.itemView.setBackgroundColor(Color.parseColor("#FFEBF5FE"));// setting background
                        } else {
                            messageViewHolder.itemView.setBackgroundColor(0); // reset background
                        }
                        if (timestamp.length() > 15) {
                            // add zero to front(leading zero)
                            // remove two zeros at end
                            convertTime = "0" + timestamp.substring(1, timestamp.length() - 2);
                        } else {
                            convertTime = timestamp.substring(1); // removing the negative value from string
                        }
                        // date converter
                        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmss");
                        try {
                            Date d = sdf.parse(convertTime);

                            /*sdf.applyPattern("HH:mm • dd MMM yy");
                            String newTime = sdf.format(d);*/

                            String g = durationFromNow(d); // formatter in duration ago
                            messageViewHolder.setTime(g); // set time
                        } catch (ParseException ignored) {
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                // getting User profile pic and Username from User database reference
                DatabaseReference userData = FirebaseDatabase.getInstance().getReference("Users" + "/" + itemID);
                userData.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            final String username = dataSnapshot.child("username").getValue().toString();
                            final String profileImage = dataSnapshot.child("profileURL").getValue().toString();
                            // call methods for setting data to adapter
                            messageViewHolder.setUsername(username);
                            messageViewHolder.setImageURL(getContext(), profileImage);

                            // on item clicked
                            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String itemID = getRef(i).getKey();

                                    mDatabase.child(itemID).child("userID").setValue("0"); // reset value to default

                                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                                    intent.putExtra("userID", itemID); // save userID of designer
                                    intent.putExtra("username", username); // username of designer
                                    intent.putExtra("profileImage", profileImage); // username of designer
                                    startActivity(intent);
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        };

        MessagesList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public MessageViewHolder(final View itemView) {
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

        public void setMessage(String message) {
            TextView vMessage = mView.findViewById(R.id.vMessage);
            vMessage.setText(message);
            vMessage.setMaxLines(2);
        }

        public void setTime(String timestamp) {
            TextView vTime = mView.findViewById(R.id.timestamp);
            vTime.setText(timestamp);
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