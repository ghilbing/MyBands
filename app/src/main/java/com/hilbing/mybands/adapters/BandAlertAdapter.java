package com.hilbing.mybands.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hilbing.mybands.R;
import com.hilbing.mybands.models.Band;
import com.hilbing.mybands.models.MusiciansBands;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class BandAlertAdapter extends BaseAdapter {

    Context context;
    ArrayList<Band> bands;


    public BandAlertAdapter(Context context, ArrayList<Band> bands) {
        this.context = context;
        this.bands = bands;
    }

    @Override
    public int getCount() {
        return bands.size();
    }

    @Override
    public Object getItem(int i) {
        return bands.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.row_item_alert_bands, viewGroup, false);
        CircleImageView image = (CircleImageView) row.findViewById(R.id.alert_band_image);
        TextView name = (TextView) row.findViewById(R.id.alert_band_name);
        TextView id = (TextView) row.findViewById(R.id.alert_band_id);

        Band tempBand = bands.get(i);

        name.setText(tempBand.getmBandName());
        id.setText(tempBand.getmBandId());
        Picasso.get().load(tempBand.getmBandImage()).placeholder(R.drawable.profile).into(image);

        return row;
    }
}
