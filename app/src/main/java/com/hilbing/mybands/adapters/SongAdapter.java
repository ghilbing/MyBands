package com.hilbing.mybands.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hilbing.mybands.R;
import com.hilbing.mybands.models.Song;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SongAdapter extends ArrayAdapter<Song> {

    @BindView(R.id.songNameTV)
    TextView songNameTV;
    @BindView(R.id.artistNameTV)
    TextView artistName;
    @BindView(R.id.durationTV)
    TextView durationTV;
    @BindView(R.id.youtubeTV)
    TextView youtubeTV;


    private Context context;
    private List<Song> songsList;

    public SongAdapter(Context context, List<Song> songsList) {
        super(context, R.layout.song_layout, songsList);
        this.context = context;
        this.songsList = songsList;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View listItem = inflater.inflate(R.layout.song_layout, null, true);
        ButterKnife.bind(this, listItem);

        Song song = songsList.get(position);
        songNameTV.setText(song.getmName());
        artistName.setText(song.getmArtist());
        durationTV.setText(song.getmDuration());
        youtubeTV.setText(song.getmUrlYoutube());


        return listItem;


    }

}
