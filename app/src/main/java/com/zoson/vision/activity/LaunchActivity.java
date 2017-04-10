package com.zoson.vision.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.zoson.vision.R;
import com.zoson.vision.domain.VisionManager;
import com.zoson.vision.domain.VisionSession;

/**
 * Created by zoson on 17-2-7.
 */

public class LaunchActivity extends Activity {

    EditText et_addr;
    public final static String ADDR = "ADDR";
    public final static String PROTOCOL = "PROTOCOL";
    public final static String RESOLUTION = "RESOLUTION";
    Spinner sp_protocol;
    Spinner sp_resolution;
    String protocol;
    String[] protocols;
    String[] resolutions;
    String resolution;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        et_addr = (EditText) findViewById(R.id.et_addr);
        sp_protocol = (Spinner) findViewById(R.id.sp_protocol);
        protocols = getResources().getStringArray(R.array.protocol);
        resolutions = getResources().getStringArray(R.array.resolution);

        protocol = protocols[0];
        resolution = resolutions[0];

        sp_protocol.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                protocol = protocols[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        sp_resolution  = (Spinner)findViewById(R.id.sp_resolution);
        sp_resolution.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                resolution = resolutions[i];
                Toast.makeText(LaunchActivity.this,resolution,Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void clickToConnect(View view) {
        String addr = et_addr.getText().toString();

        Intent intent = new Intent(this, VisionActivity.class);
        intent.putExtra(ADDR, addr);
        intent.putExtra(PROTOCOL, protocol);
        intent.putExtra(RESOLUTION,resolution);

        startActivity(intent);
    }


}
