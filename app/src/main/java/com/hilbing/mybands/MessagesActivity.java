package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hilbing.mybands.adapters.MessageAdapter;
import com.hilbing.mybands.models.Message;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesActivity extends AppCompatActivity
{

    @BindView(R.id.message_toolbar)
    Toolbar toolbar;
    @BindView(R.id.message_send_image_BT)
    ImageButton sendImageBT;
    @BindView(R.id.message_send_text_BT)
    ImageButton sendTextBT;
    @BindView(R.id.messages_RV)
    RecyclerView recyclerView;
    @BindView(R.id.message_ET)
    EditText messageET;

    private TextView userReceiverTV;
    private CircleImageView userReceiverCIV;

    private final List<Message> messages = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter adapter;

    private String currentUserId;
    private FirebaseAuth mAuth;
    private String messageReceiverId;
    private String messageReceiverName;
    private String messageBandId;


    private DatabaseReference rootReference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        SharedPreferences preferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
        messageBandId = preferences.getString("currentBandIdPref", "");

        ButterKnife.bind(this);

        Intent intentGetData = getIntent();
        Bundle bundle = intentGetData.getExtras();
        if(bundle != null)
        {
            messageReceiverId = String.valueOf(bundle.get("mUserId"));
            messageReceiverName = String.valueOf(bundle.get("mUserName"));

        }


        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.message_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        userReceiverTV = findViewById(R.id.message_custom_bar_TV);
        userReceiverCIV = findViewById(R.id.message_custom_bar_CIV);

        rootReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        displayReceiverInfo();

        sendTextBT.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                sendMessage();
            }
        });

        fetchMessages();


        adapter = new MessageAdapter(messages);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);


    }

    private void fetchMessages()
    {
        rootReference.child("Messages").child(messageBandId).child(currentUserId).child(messageReceiverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);
                messages.add(message);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists())
                {

                }
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

    }

    private void sendMessage()
    {

        String messageString = String.valueOf(messageET.getText());
        if(TextUtils.isEmpty(messageString))
        {
            messageET.setError(getResources().getString(R.string.enter_message));
            messageET.requestFocus();
            return;
        }
        else
        {
            String message_sender_ref = "Messages/" + messageBandId + "/" + currentUserId + "/" + messageReceiverId;
            String message_receiver_ref = "Messages/" + messageBandId + "/" + messageReceiverId + "/" + currentUserId;

            DatabaseReference userMessagesKey = rootReference.child("Messages").child(messageBandId).child(currentUserId).child(messageReceiverId).push();
            String messageKey = userMessagesKey.getKey();

            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            String saveCurrentDate = currentDate.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            String saveCurrentTime = currentTime.format(calForTime.getTime());

            Map messageBodyMap = new HashMap();
            messageBodyMap.put("message", messageString);
            messageBodyMap.put("date", saveCurrentDate);
            messageBodyMap.put("time", saveCurrentTime);
            messageBodyMap.put("type", "text");
            messageBodyMap.put("from", currentUserId);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(message_sender_ref + "/" + messageKey, messageBodyMap);
            messageBodyDetails.put(message_receiver_ref + "/" + messageKey, messageBodyMap);


            rootReference.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(MessagesActivity.this, getResources().getString(R.string.message_sent_succesfully), Toast.LENGTH_LONG).show();
                        messageET.setText("");
                    }
                    else
                    {
                        String message = task.getException().getMessage();
                        Toast.makeText(MessagesActivity.this, getResources().getString(R.string.error_occurred) + ": " + message, Toast.LENGTH_LONG).show();
                        messageET.setText("");
                    }

                }
            });

        }

    }

    private void displayReceiverInfo()
    {
        userReceiverTV.setText(messageReceiverName);
        rootReference.child("Users").child(messageReceiverId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists()){
                    String imageString = String.valueOf(dataSnapshot.child("mUserProfileImage").getValue());
                    Picasso.get().load(imageString).placeholder(R.drawable.profile).into(userReceiverCIV);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

    }
}
