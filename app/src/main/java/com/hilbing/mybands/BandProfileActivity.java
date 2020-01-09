package com.hilbing.mybands;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class BandProfileActivity extends AppCompatActivity {

    @BindView(R.id.profile_band_toolbar)
    Toolbar toolbar;
    @BindView(R.id.profile_band_image_CIV)
    CircleImageView imageCIV;
    @BindView(R.id.profile_band_name_ET)
    EditText bandNameET;
    @BindView(R.id.profile_band_available_CB)
    CheckBox availableCB;
    @BindView(R.id.profile_band_story_ET)
    EditText bandStoryET;
    @BindView(R.id.profile_band_country_SP)
    Spinner countrySP;
    @BindView(R.id.profile_band_save_BT)
    Button createBT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_band_profile);

        ButterKnife.bind(this);
    }
}
