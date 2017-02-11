package com.zoson.vision.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.zoson.vision.R;

/**
 * Created by zoson on 17-2-7.
 */

public class LaunchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
    }

    public void clickToConnect(View view){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }
}
