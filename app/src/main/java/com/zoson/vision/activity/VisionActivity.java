package com.zoson.vision.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.zoson.cycle.ffmpeg.Format;
import com.zoson.vision.R;
import com.zoson.vision.domain.VisionManager;
import com.zoson.vision.domain.VisionSession;

import java.text.Normalizer;
import java.util.Calendar;

/**
 * Created by zoson on 17-2-13.
 */

public class VisionActivity extends Activity {

    SurfaceView sv_camera;
    SurfaceView sv_video;
    SurfaceHolder svholder_camera;
    Button bt_start ;
    Button bt_stop ;
    Button bt_test;
    VisionManager manager ;
    VisionSession sendSession ;
    VisionSession recSession ;

    String addr;
    String protocol;
    String resolution;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        setListeners();
        initData();
    }

    private void setListeners(){
        bt_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(VisionActivity.this,"test",Toast.LENGTH_LONG).show();
                String file = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+  "test.mp4";
                String url = "rtmp://192.168.31.208/live/test";
                manager.test(file,url);
            }
        });
        bt_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(VisionActivity.this,"start",Toast.LENGTH_LONG).show();
                sendSession.start();
            }
        });
        bt_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSession.close();
            }
        });
    }

    private void initData(){
        addr = getIntent().getStringExtra(LaunchActivity.ADDR);
        protocol = getIntent().getStringExtra(LaunchActivity.PROTOCOL);
        resolution = getIntent().getStringExtra(LaunchActivity.RESOLUTION);

        svholder_camera = sv_camera.getHolder();
        manager = VisionManager.getDefault();
        svholder_camera.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                manager.applyCamera(svholder_camera);
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        });
        sendSession = manager.newSession();

        VisionSession.Parameter params = sendSession.getParameter();
        params.in_protocol = VisionManager.Protocol.RAW;
        params.out_protocol = VisionManager.Protocol.FILE;
        params.in_width = Integer.parseInt(resolution.split("x")[0]);
        params.in_height = Integer.parseInt(resolution.split("x")[1]);
        params.lockCamera = true;
        params.in_video_format = VisionManager.Format.FORMAT_YUV420;
        params.out_audio_format = VisionManager.Format.FORMAT_H264;
        params.isVideo = true;
        Calendar cc = Calendar.getInstance();
        System.out.println("protocol:"+protocol);
        switch (protocol){
            case "file":
                System.out.println("file selection");
                params.out_url = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+ String.valueOf(cc.get(Calendar.YEAR))
                        + "-"
                        + String.valueOf(cc.get(Calendar.MONTH))
                        + "-"
                        + String.valueOf(cc.get(Calendar.DAY_OF_YEAR))
                        + "-"
                        + String.valueOf(cc.get(Calendar.HOUR_OF_DAY))
                        + "-"
                        + String.valueOf(cc.get(Calendar.MINUTE))
                        + "-"
                        + String.valueOf(cc.get(Calendar.SECOND))
                        +addr+ ".h264";
                params.out_protocol = VisionManager.Protocol.FILE;
                break;
            case "rtmp":
                System.out.println("protocol selection");
                params.out_url = "rtmp://192.168.31.208/live/test";
                params.out_protocol = VisionManager.Protocol.RTMP;
                break;
            default:
                params.out_url = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+ String.valueOf(cc.get(Calendar.YEAR))
                        + "-"
                        + String.valueOf(cc.get(Calendar.MONTH))
                        + "-"
                        + String.valueOf(cc.get(Calendar.DAY_OF_YEAR))
                        + "-"
                        + String.valueOf(cc.get(Calendar.HOUR_OF_DAY))
                        + "-"
                        + String.valueOf(cc.get(Calendar.MINUTE))
                        + "-"
                        + String.valueOf(cc.get(Calendar.SECOND))
                        +addr+ ".h264";
                params.out_protocol = VisionManager.Protocol.FILE;
                break;
        }
        sendSession.setParams(params);
    }



    private void findView(){
        sv_camera = (SurfaceView)findViewById(R.id.sv_camera);
        sv_video = (SurfaceView)findViewById(R.id.sv_video);
        bt_start = (Button)findViewById(R.id.bt_start);
        bt_stop = (Button)findViewById(R.id.bt_stop);
        bt_test = (Button)findViewById(R.id.bt_test);
    }
}
