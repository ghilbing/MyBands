package com.hilbing.mybands.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.hilbing.mybands.R;
import com.hilbing.mybands.adapters.SongAdapter;
import com.hilbing.mybands.models.Song;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class SongsFragmentDialog extends DialogFragment {

    private static Song song;
    @BindView(R.id.dialog_searchView_SV)
    SearchView searchViewSV;
    @BindView(R.id.dialog_listView_songs_LV)
    ListView listViewLV;
    @BindView(R.id.dialog_dismiss_BT)
    Button dismissBT;

    private FirebaseAuth mAuth;
    private DatabaseReference songsReference;
    private DatabaseReference playlistReference;
    private SongAdapter adapter;
    private List<Song> mSongs = new ArrayList<>();



    public SongsFragmentDialog() {
        // Required empty public constructor
    }

    public static SongsFragmentDialog newInstance(List<Song> songs){
        SongsFragmentDialog fragmentDialog = new SongsFragmentDialog();
        Bundle args = new Bundle();
        args.putParcelableArrayList("list", (ArrayList<Song>) songs);
        fragmentDialog.setArguments(args);
        Log.d("SONGS ARRAY FROM DIALOG", String.valueOf(songs.size()));
        return fragmentDialog;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_songs_dialog, null);

        getDialog().setTitle(getResources().getString(R.string.select_a_song));
        ButterKnife.bind(this, rootView);

        mSongs = getArguments().getParcelableArrayList("list");

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


        adapter = new SongAdapter(getActivity().getApplicationContext(), mSongs);

        listViewLV.setAdapter(adapter);

        dismissBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return rootView;

    }


}
