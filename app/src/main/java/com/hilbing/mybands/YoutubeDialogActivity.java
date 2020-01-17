package com.hilbing.mybands;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubePlayerView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class YoutubeDialogActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    @BindView(R.id.dialog_song_youtube_view)
    YouTubePlayerView youTubePlayerView;
    private String key = "AIzaSyAWjEqiLz9z9ZBrd1mynjQnHPQdKiQYCno";
    private String video_id;
    private YouTubePlayer myYouTubePlayer;
    private final int RQS_ErrorDialog = 1;
    private MyPlayerStateChangeListener myPlayerStateChangeListener;
    String log = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Make us non-modal, so that others can receive touch events.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

        // ...but notify us that it happened.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        setContentView(R.layout.activity_youtube_dialog);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        video_id = bundle.getString("VIDEO_ID");
        youTubePlayerView.initialize(key, this);
        myPlayerStateChangeListener = new MyPlayerStateChangeListener();

    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        YouTubePlayer.PlayerStyle style = YouTubePlayer.PlayerStyle.MINIMAL;
        youTubePlayer.setPlayerStyle(style);
        myYouTubePlayer = youTubePlayer;
        myYouTubePlayer.setPlayerStateChangeListener(myPlayerStateChangeListener);
        if(!b){
            youTubePlayer.cueVideo(video_id);
        }
    }


    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        if(youTubeInitializationResult.isUserRecoverableError()){
            youTubeInitializationResult.getErrorDialog(this, RQS_ErrorDialog).show();
        }
    }

    private final class MyPlayerStateChangeListener implements YouTubePlayer.PlayerStateChangeListener {

        @Override
        public void onLoading() {

        }

        @Override
        public void onLoaded(String s) {

        }

        @Override
        public void onAdStarted() {

        }

        @Override
        public void onVideoStarted() {

        }

        @Override
        public void onVideoEnded() {
            finish();
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // If we've received a touch notification that the user has touched
        // outside the app, finish the activity.
        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
            finish();
            return true;
        }

        // Delegate everything else to Activity.
        return super.onTouchEvent(event);

    }
}
