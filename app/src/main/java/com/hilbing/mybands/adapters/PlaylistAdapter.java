package com.hilbing.mybands.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hilbing.mybands.R;
import com.hilbing.mybands.interfaces.PlaylistClickListener;
import com.hilbing.mybands.models.Playlist;
import com.hilbing.mybands.models.Song;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlaylistAdapter extends ArrayAdapter<Playlist> {

    @BindView(R.id.playlistNameTV)
    TextView playlistNameTV;
    @BindView(R.id.playlistCreatorTV)
    TextView creatorTV;

    private Context context;
    private List<Playlist> playlistsList;
    private List<Playlist> completePlaylistsList;
    private String currentBandId;
    private String currentPlaylistId;
    private String playlistName;
    private String playlistCreator;
    private String playlistId;

    private PlaylistClickListener clickListener;

    public PlaylistClickListener getClickListener() {
        return clickListener;
    }

    public void setClickListener(PlaylistClickListener clickListener){
        this.clickListener = clickListener;
    }

    private DatabaseReference playlistReference;
    private DatabaseReference eventsReference;

    public PlaylistAdapter(Context context, List<Playlist> playlistsList, String currentBandId, List<Playlist> completePlaylistsList) {
        super(context, R.layout.playlist_layout, playlistsList);
        this.context = context;
        this.playlistsList = playlistsList;
        this.currentBandId = currentBandId;
        this.completePlaylistsList = completePlaylistsList;

    }

    public PlaylistAdapter(Context context, List<Playlist> playlistsList) {
        super(context, R.layout.playlist_layout, playlistsList);
        this.context = context;
        this.playlistsList = playlistsList;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View listItem = inflater.inflate(R.layout.playlist_layout, null, true);
        ButterKnife.bind(this, listItem);

        playlistReference = FirebaseDatabase.getInstance().getReference().child("PlaylistsSongs").child(currentBandId);
        playlistReference.keepSynced(true);
        eventsReference = FirebaseDatabase.getInstance().getReference().child("Events").child(currentBandId);
        eventsReference.keepSynced(true);

        final Playlist playlist = playlistsList.get(position);
        playlistId = playlist.getmId();
        playlistName = playlist.getmPlaylistName();
        playlistCreator = playlist.getmCreator();
        playlistNameTV.setText(playlistName);
        creatorTV.setText(playlistCreator);

        listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // addPlaylistToEvent(playlist);

                if(getClickListener() != null){
                    getClickListener().onPlaylistClick(playlist.getmId(), playlist.getmPlaylistName());
                }
                Toast.makeText(getContext().getApplicationContext(), playlistName, Toast.LENGTH_SHORT).show();
            }
        });




        return listItem;


    }



    @Override
    public int getCount() {
        return CollectionUtils.isEmpty(playlistsList) ? 0 :playlistsList.size();
    }

    private void addPlaylistToEvent(Playlist playlist) {

        String id = playlistReference.push().getKey();

        // Song song = new Song(id, songName, songArtist, songYoutubeUrl);
        playlistReference.child(id).setValue(playlist);
        Toast.makeText(getContext().getApplicationContext(), getContext().getResources().getString(R.string.playlist_added), Toast.LENGTH_LONG).show();
    }

    @Override
    public Filter getFilter() {

        return new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                playlistsList = (List<Playlist>) results.values;
                PlaylistAdapter.this.notifyDataSetChanged();

            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Playlist> filteredResults;
                if (constraint.length() == 0) {
                    filteredResults = completePlaylistsList;
                } else {
                    filteredResults = getFilteredResults(constraint.toString().toLowerCase());
                }

                FilterResults results = new FilterResults();
                results.values = filteredResults;

                return results;
            }
        };
    }


    private List<Playlist> getFilteredResults(String constraint) {
        List<Playlist> results = new ArrayList<>();

        for (Playlist playlist : playlistsList) {
            if (playlist.getmPlaylistName().toLowerCase().contains(constraint)) {
                results.add(playlist);
            }
        }
        return results;
    }

}
