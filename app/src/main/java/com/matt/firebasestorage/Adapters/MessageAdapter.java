package com.matt.firebasestorage.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.matt.firebasestorage.Objects.nMessage;
import com.matt.firebasestorage.R;
import com.matt.firebasestorage.ViewProfile;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<nMessage> userMessagesList;
    private FirebaseAuth mAuth;
    private String imageURL;
    private Context mContext;


    public MessageAdapter(List<nMessage> userMessagesList, String imageURL, Context mContext) {
        this.userMessagesList = userMessagesList;
        this.imageURL = imageURL;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.messages_layout, parent, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {
        String senderID = mAuth.getCurrentUser().getUid();
        nMessage nMessage = userMessagesList.get(position);
        final String fromUserID = nMessage.getUserID();
        String status = nMessage.getStatus();
        final long timestamp = nMessage.getTimestamp();

        Picasso.with(holder.itemView.getContext()).load(imageURL)
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.receiverProfileImage);

        // visibility off for all
        holder.receiverMessage.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.senderMessage.setVisibility(View.GONE);
        holder.senderStatus.setVisibility(View.GONE);
        holder.senderTime.setVisibility(View.GONE);
        holder.receiverTime.setVisibility(View.GONE);

        if (fromUserID.equals(senderID)) {
            // visibility on for sender
            holder.senderMessage.setVisibility(View.VISIBLE);
            holder.senderStatus.setVisibility(View.VISIBLE);
            holder.senderMessage.setText(nMessage.getMessage());


            if (status.equals("1")) {
                // status is read
                holder.senderStatus.setBackgroundResource(R.drawable.ic_round_check_color_24);
            }

        } else {
            // visibility on for receiver
            holder.receiverMessage.setVisibility(View.VISIBLE);
            holder.receiverProfileImage.setVisibility(View.VISIBLE);
            holder.receiverMessage.setText(nMessage.getMessage());
        }

        holder.senderMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // date converter
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmm");
                try {
                    Date d = sdf.parse(String.valueOf(timestamp));
                    sdf.applyPattern("dd MMM, HH:mm");
                    String newTime = sdf.format(d);
                    holder.senderTime.setText(newTime);
                    ToggleVisibility(holder.senderTime);
                } catch (ParseException ignored) {
                }
//                userMessagesList.get(position);
//                Toast.makeText(view.getContext(), userMessagesList.get(position).getStatus(), Toast.LENGTH_SHORT).show();
            }

        });

        holder.receiverMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmm");
                try {
                    Date d = sdf.parse(String.valueOf(timestamp));
                    sdf.applyPattern("dd MMM, HH:mm");
                    String newTime = sdf.format(d);
                    holder.receiverTime.setText(newTime);
                    ToggleVisibility(holder.receiverTime);
                } catch (ParseException ignored) {
                }
            }
        });

        holder.receiverProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // view profile
                Intent intent = new Intent(mContext, ViewProfile.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("userID", fromUserID);
                mContext.startActivity(intent);
            }
        });

    }

    private void ToggleVisibility(TextView textView) {
        if (textView.getVisibility() == View.VISIBLE) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView senderMessage, senderTime, receiverTime, receiverMessage;
        public View senderStatus;
        public CircleImageView receiverProfileImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessage = itemView.findViewById(R.id.sender);
            senderTime = itemView.findViewById(R.id.timestamp);
            receiverTime = itemView.findViewById(R.id.timestampR);
            senderStatus = itemView.findViewById(R.id.status);
            receiverMessage = itemView.findViewById(R.id.receiver);
            receiverProfileImage = itemView.findViewById(R.id.profile_image);
        }
    }


}
