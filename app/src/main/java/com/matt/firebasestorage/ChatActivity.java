package com.matt.firebasestorage;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matt.firebasestorage.Adapters.MessageAdapter;
import com.matt.firebasestorage.Objects.Contact;
import com.matt.firebasestorage.Objects.nMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private static final int MENU_PROFILE = Menu.FIRST;
    private static final int MENU_DELETE = Menu.FIRST + 2;
    private static final int MENU_CALL = Menu.FIRST + 1;

    private String dUsername, duserID, profileImage;
    private DatabaseReference mDatabase, mDatabase2, userData;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private List<nMessage> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayout;
    private MessageAdapter messageAdapter;
    private RecyclerView MessagesList;
    private String message;
    private TextView noMessage;
    private ValueEventListener mDBListener;
    private long timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
     /*   dUsername = intent.getStringExtra("username");
        String profileImage = intent.getStringExtra("profileImage");*/

        duserID = intent.getStringExtra("userID");

        // get username and profile
        userData = FirebaseDatabase.getInstance().getReference("Users").child(duserID);
        userData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dUsername = dataSnapshot.child("username").getValue().toString();
                profileImage = dataSnapshot.child("profileURL").getValue().toString();
                getSupportActionBar().setTitle(dUsername);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                mAuth = FirebaseAuth.getInstance();
                user = mAuth.getCurrentUser();

                // customized adapter to capture string value of imageURL
                messageAdapter = new MessageAdapter(messageList, profileImage, getApplicationContext());
                MessagesList = findViewById(R.id.recView);
                linearLayout = new LinearLayoutManager(getApplicationContext());
                linearLayout.setStackFromEnd(true);
                MessagesList.setLayoutManager(linearLayout);
                MessagesList.setAdapter(messageAdapter);

                noMessage = findViewById(R.id.noMessage);
                noMessage.setText("Send a message to " + dUsername + " \uD83D\uDC4B"); // default screen message
                View btnSend = findViewById(R.id.btnSend);
                btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendMessage();
                    }
                });

                getMessages();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void getMessages() {
        // designer contacted user
        mDatabase = FirebaseDatabase.getInstance().getReference("Messages")
                .child(user.getUid()).child(duserID);
        // contacting designer
        mDatabase2 = FirebaseDatabase.getInstance().getReference("Messages")
                .child(duserID).child(user.getUid());

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mDatabase.keepSynced(true);
                    mDatabase.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            addMessages(dataSnapshot);

                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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

                    messageRead(mDatabase);

                } else {
                    mDatabase2.keepSynced(true);
                    mDatabase2.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            addMessages(dataSnapshot);
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            nMessage nMessage = dataSnapshot.getValue(nMessage.class);
                            messageList.remove(nMessage);
                            messageAdapter.notifyDataSetChanged();
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

                    messageRead(mDatabase2);

                }
            }

            private void addMessages(DataSnapshot dataSnapshot) {
                nMessage nMessage = dataSnapshot.getValue(nMessage.class);
                messageList.add(nMessage);
                messageAdapter.notifyDataSetChanged();
                MessagesList.smoothScrollToPosition(MessagesList.getAdapter().getItemCount());

                noMessage.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void messageRead(final DatabaseReference databaseReference) {
        // setting the last message(other user) status to 1 == read
        mDBListener = databaseReference.orderByChild("userID").equalTo(duserID).limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot messageSnap : dataSnapshot.getChildren()) {
                            String messageKey = messageSnap.getRef().getKey();
                            databaseReference.child(messageKey).child("status").setValue("1");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // detach listeners when activity is destroyed (to prevent continuous write of data)
        mDatabase.removeEventListener(mDBListener);
        mDatabase2.removeEventListener(mDBListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_PROFILE, Menu.NONE, "View Profile");
        // checks if current user is author of the post

        menu.add(Menu.NONE, MENU_CALL, Menu.NONE, "Call")
                .setIcon(R.drawable.ic_baseline_phone_enabled_24)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(Menu.NONE, MENU_DELETE, Menu.NONE, "Delete");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();

        } else if (item.getItemId() == MENU_CALL) {

            // database reference to other users phone contact
            DatabaseReference userData = FirebaseDatabase.getInstance().getReference("Users").child(duserID);
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

        } else if (item.getItemId() == MENU_PROFILE) {
            viewProfile();
        }
        /*
        } else if (item.getItemId() == MENU_EDIT) {
            editPostDialog();
        }*/
        return super.onOptionsItemSelected(item);
    }

    private void sendMessage() {

        final EditText etMessage = findViewById(R.id.etMessage);
        message = etMessage.getText().toString().trim();
        long currentDate = Long.parseLong(new SimpleDateFormat("ddMMyyyyHHmm", Locale.getDefault()).format(new Date()));
        final nMessage message1 = new nMessage(user.getUid(), message, currentDate, "0");

        etMessage.setText(null);
        if (!message.matches("")) {
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        mDatabase.push().setValue(message1).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                setContact();
                            }
                        });
                    } else {
                        mDatabase2.push().setValue(message1).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                setContact();
                            }
                        });
                    }
                    sendNotification();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            etMessage.setError("Empty!");
        }


    }

    private void setContact() {
        String stringTime = new SimpleDateFormat("ddMMyyyyHHmmss").format(new Date());

        if (stringTime.startsWith("0")) {
            // negate(-) all timestamp values to descend in database
            // add two zeros to end of long value
            timestamp = -1 * Long.parseLong(stringTime) * 100;
        } else {
            timestamp = -1 * Long.parseLong(stringTime);
        }

        DatabaseReference userData = FirebaseDatabase.getInstance().getReference("Users")
                .child(duserID).child("Contacts").child(user.getUid());
        Contact contact = new Contact(message, timestamp, "1"); // going to other user
        userData.setValue(contact);

        DatabaseReference userData2 = FirebaseDatabase.getInstance().getReference("Users")
                .child(user.getUid()).child("Contacts").child(duserID);
        Contact contact2 = new Contact(message, timestamp, "0"); // going to me
        userData2.setValue(contact2);
    }

    private void sendNotification() {
        DatabaseReference userNotification = FirebaseDatabase.getInstance().getReference("Notify")
                .child(duserID).child(user.getUid());
        String timestamp = String.valueOf(new Date().getTime());
        userNotification.setValue(timestamp);
    }

    public void viewProfile() {
        // pass userID to Activity
        Intent intent = new Intent(getApplicationContext(), ViewProfile.class);
        intent.putExtra("userID", duserID);
        startActivity(intent);
    }

}