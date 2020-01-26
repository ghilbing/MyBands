package com.hilbing.mybands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hilbing.mybands.models.Item;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoDetailActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    @BindView(R.id.youtube_detail_title)
    TextView title;
    @BindView(R.id.youtube_detail_channel)
    TextView channel;
    @BindView(R.id.youtube_detail_description)
    TextView description;
    @BindView(R.id.youtube_detail_view)
    YouTubePlayerView youTubeView;
    @BindView(R.id.youtube_detail_save_song_BT)
    Button saveSongBT;


    private DatabaseReference songsReference;

    private String key = "AIzaSyAWjEqiLz9z9ZBrd1mynjQnHPQdKiQYCno";
    private static final int RECOVERY_REQUEST = 1;
    private Item video;

    private ProgressDialog progressDialog;

    private String videoTitle;
    private String videoDescription;
    private String videoChannel;
    private String videoId;
    private String currentBandIdPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detail);
        ButterKnife.bind(this);

        progressDialog = new ProgressDialog(this);

        SharedPreferences preferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
        currentBandIdPref = preferences.getString("currentBandIdPref", "");

        video = getIntent().getExtras().getParcelable("VIDEO");

        videoTitle = video.getSnippet().getTitle();
        videoDescription = video.getSnippet().getDescription();
        videoChannel = video.getSnippet().getChannelTitle();
        videoId = video.getId().getVideoId();

        title.setText(video.getSnippet().getTitle());
        description.setText(video.getSnippet().getDescription());
        channel.setText(video.getSnippet().getChannelTitle());

        songsReference = FirebaseDatabase.getInstance().getReference().child("SongYoutube");


        saveSongBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSongLink();
            }
        });

        youTubeView.initialize(key, this);
    }

    private void saveSongLink() {

        Intent songIntent = new Intent(VideoDetailActivity.this, SongActivity.class);
        songIntent.putExtra("YouTubeURL", videoId);
        songIntent.putExtra("YouTubeTitle", videoTitle);
        startActivity(songIntent);

    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
        if (!wasRestored) {
            player.cueVideo(video.getId().getVideoId());
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_REQUEST).show();
        } else {
            String error = String.format(getString(R.string.player_error), errorReason.toString());
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RECOVERY_REQUEST) {
            // Retry initialization if user performed a recovery action
            getYouTubePlayerProvider().initialize(key, this);
        }
    }

    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return youTubeView;
    }
}
