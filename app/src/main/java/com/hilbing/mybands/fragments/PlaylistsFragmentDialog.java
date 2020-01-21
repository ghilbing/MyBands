package com.hilbing.mybands.fragments;


import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.hilbing.mybands.R;
import com.hilbing.mybands.adapters.PlaylistAdapter;
import com.hilbing.mybands.adapters.SongAdapter;
import com.hilbing.mybands.models.Playlist;
import com.hilbing.mybands.models.Song;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlaylistsFragmentDialog extends DialogFragment {

    private static Song song;
    @BindView(R.id.dialog_playlist_searchView_SV)
    SearchView searchViewSV;
    @BindView(R.id.dialog_playlist_listView_songs_LV)
    ListView listViewLV;
    @BindView(R.id.dialog_playlist_dismiss_BT)
    Button dismissBT;

    private FirebaseAuth mAuth;
    private DatabaseReference playlistReference;
    private PlaylistAdapter adapter;
    private List<Playlist> mPlaylist = new ArrayList<>();
    private String currentBandId;
    private String currentPlaylistId;


    public PlaylistsFragmentDialog() {
        // Required empty public constructor
    }

    public interface MyDialogFragmentListener{
        public void onReturnValues(String playlistId, String playlistName);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_playlists_dialog, null);

        getDialog().setTitle(getResources().getString(R.string.select_a_playlist));
        ButterKnife.bind(this, rootView);

        mPlaylist = getArguments().getParcelableArrayList("list");
        currentBandId = getArguments().getString("currentBandId");


        searchViewSV.setQueryHint(getString(R.string.search));
        searchViewSV.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });



        adapter = new PlaylistAdapter(getActivity().getApplicationContext(), mPlaylist,currentBandId, mPlaylist);

        listViewLV.setAdapter(adapter);

        dismissBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return rootView;
    }



    public static PlaylistsFragmentDialog newInstance(List<Playlist> playlists, String currentBandID){
        PlaylistsFragmentDialog fragmentDialog = new PlaylistsFragmentDialog();
        Bundle args = new Bundle();
        args.putParcelableArrayList("list", (ArrayList<Playlist>) playlists);
        args.putString("currentBandId", currentBandID);
        fragmentDialog.setArguments(args);
        return fragmentDialog;
    }

}
