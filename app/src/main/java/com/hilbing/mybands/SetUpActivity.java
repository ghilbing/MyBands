package com.hilbing.mybands;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class SetUpActivity extends AppCompatActivity {

    @BindView(R.id.setup_user_image_CIV)
    CircleImageView profileImageCIV;
    @BindView(R.id.setup_user_full_name_ET)
    EditText fullNameET;
    @BindView(R.id.setup_user_phone)
    EditText phoneET;
    @BindView(R.id.setup_country_ET)
    EditText countryET;
    @BindView(R.id.setup_save_BT)
    Button saveBT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);

        ButterKnife.bind(this);
    }
}
