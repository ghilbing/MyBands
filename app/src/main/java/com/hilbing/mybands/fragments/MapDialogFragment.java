package com.hilbing.mybands.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.DialogFragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hilbing.mybands.R;
import com.hilbing.mybands.models.Playlist;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class MapDialogFragment extends DialogFragment implements OnMapReadyCallback {

    GoogleMap mMap;
    private double mLat;
    private double mLng;



    public MapDialogFragment() {
        // Required empty public constructor
    }

    public static MapDialogFragment newInstance(double lat, double lng) {
        final MapDialogFragment fragmentDialog = new MapDialogFragment();
        Bundle args = new Bundle();
        args.putDouble("lat", lat);
        args.putDouble("lng", lng);
        fragmentDialog.setArguments(args);
        return fragmentDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_map_dialog, container, false);

        mLat = getArguments().getDouble("lat", 0.0);
        mLng = getArguments().getDouble("lng", 0.0);


        SupportMapFragment mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        LatLng latLng = new LatLng(mLat,mLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,11));

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        mMap.addMarker(markerOptions);
    }

}
