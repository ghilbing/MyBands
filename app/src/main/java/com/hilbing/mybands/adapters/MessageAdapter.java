package com.hilbing.mybands.adapters;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hilbing.mybands.R;
import com.hilbing.mybands.models.Message;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{

    private List<Message> messages;
    private FirebaseAuth mAuth;
    private DatabaseReference usersDataReference;

    public MessageAdapter(List<Message> messages)
    {
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_layout, parent, false);
        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position)
    {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Message message = messages.get(position);

        String fromUserId = message.getFrom();
        String fromMessage = message.getMessage();
        String fromMessageType = message.getType();

        usersDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
        usersDataReference.keepSynced(true);
        usersDataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    final String profileImage = String.valueOf(dataSnapshot.child("mUserProfileImage").getValue());
                    Picasso.get().load(profileImage).networkPolicy(NetworkPolicy.OFFLINE).into(holder.profileImageCIV, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(holder.profileImageCIV);
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(fromMessageType.equals("text"))
        {
            holder.messageReceiverTV.setVisibility(View.INVISIBLE);
            holder.profileImageCIV.setVisibility(View.INVISIBLE);

            if(fromUserId.equals(messageSenderId))
            {
                holder.messageSenderTV.setBackgroundResource(R.drawable.sender_message_background);
                holder.messageSenderTV.setTextColor(Color.WHITE);
                holder.messageSenderTV.setGravity(Gravity.LEFT);
                holder.messageSenderTV.setText(fromMessage);
            }
            else
            {
                holder.messageSenderTV.setVisibility(View.INVISIBLE);
                holder.messageReceiverTV.setVisibility(View.VISIBLE);
                holder.profileImageCIV.setVisibility(View.VISIBLE);

                holder.messageReceiverTV.setBackgroundResource(R.drawable.receiver_message_background);
                holder.messageReceiverTV.setTextColor(Color.BLACK);
                holder.messageReceiverTV.setGravity(Gravity.RIGHT);
                holder.messageReceiverTV.setText(fromMessage);
            }
        }
    }

    @Override
    public int getItemCount()
    {
        return messages.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        @BindView(R.id.item_message_profile_CIV)
        CircleImageView profileImageCIV;
        @BindView(R.id.item_message_receiver_TV)
        TextView messageReceiverTV;
        @BindView(R.id.item_message_sender_TV)
        TextView messageSenderTV;


        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
