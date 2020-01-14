package com.hilbing.mybands;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.hilbing.mybands.models.Item;

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

    private String key = "AIzaSyAWjEqiLz9z9ZBrd1mynjQnHPQdKiQYCno";
    private static final int RECOVERY_REQUEST = 1;
    private Item video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detail);
        ButterKnife.bind(this);

        video = getIntent().getExtras().getParcelable("VIDEO");

        title.setText(video.getSnippet().getTitle());
        description.setText(video.getSnippet().getDescription());
        channel.setText(video.getSnippet().getChannelTitle());
        Log.d("YOUTUBE............ ", video.getSnippet().getTitle() + " " + video.getSnippet().getDescription() + " " + video.getSnippet().getChannelTitle());

        youTubeView.initialize(key, this);
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
