package com.hilbing.mybands.adapters;

import android.content.Context;
import android.content.Intent;
import android.icu.text.UnicodeSetSpanner;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hilbing.mybands.R;
import com.hilbing.mybands.SongActivity;
import com.hilbing.mybands.VideoDetailActivity;
import com.hilbing.mybands.models.Song;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SongAdapter extends ArrayAdapter<Song> {

    @BindView(R.id.songNameTV)
    TextView songNameTV;
    @BindView(R.id.artistNameTV)
    TextView artistNameTV;
    @BindView(R.id.youtubeTV)
    TextView youtubeTV;


    private Context context;
    private List<Song> songsList;
    private List<Song> completeSongsList;
    private String currentBandId;
    private String currentPlaylistId;
    private String songName;
    private String songArtist;
    private String songYoutubeUrl;

    private DatabaseReference songsPlaylistReference;

    public SongAdapter(Context context, List<Song> songsList, String currentBandId, String currentPlaylistId, List<Song> completeSongsList) {
        super(context, R.layout.song_layout, songsList);
        this.context = context;
        this.songsList = songsList;
        this.currentBandId = currentBandId;
        this.currentPlaylistId = currentPlaylistId;
        this.completeSongsList = completeSongsList;

    }

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

        songsPlaylistReference = FirebaseDatabase.getInstance().getReference().child("PlaylistsSongs").child(currentBandId).child(currentPlaylistId);
        songsPlaylistReference.keepSynced(true);

        final Song song = songsList.get(position);
        songName = song.getmName();
        songArtist = song.getmArtist();
        songYoutubeUrl = song.getmUrlYoutube();
        songNameTV.setText(songName);
        artistNameTV.setText(songArtist);
        youtubeTV.setText(songYoutubeUrl);

        listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSongToPlaylist(song);
                Toast.makeText(getContext().getApplicationContext(), songName, Toast.LENGTH_SHORT).show();


            }
        });


        return listItem;


    }

    @Override
    public int getCount() {
        return CollectionUtils.isEmpty(songsList) ? 0 :songsList.size();
    }

    private void addSongToPlaylist(Song song) {

        String id = songsPlaylistReference.push().getKey();

       // Song song = new Song(id, songName, songArtist, songYoutubeUrl);
        songsPlaylistReference.child(id).setValue(song);
        Toast.makeText(getContext().getApplicationContext(), getContext().getResources().getString(R.string.song_added), Toast.LENGTH_LONG).show();
    }

    @Override
    public Filter getFilter() {

        return new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                songsList = (List<Song>) results.values;
                SongAdapter.this.notifyDataSetChanged();

            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Song> filteredResults;
                if (constraint.length() == 0) {
                    filteredResults = completeSongsList;
                } else {
                    filteredResults = getFilteredResults(constraint.toString().toLowerCase());
                }

                FilterResults results = new FilterResults();
                results.values = filteredResults;

                return results;
            }
        };
    }


    private List<Song> getFilteredResults(String constraint) {
        List<Song> results = new ArrayList<>();

        for (Song song : songsList) {
            if (song.getmName().toLowerCase().contains(constraint)) {
                results.add(song);
            }
        }
        return results;
    }

}
