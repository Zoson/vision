package com.zoson.vision.activity;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.zoson.cycle.service.CameraService;
import com.zoson.vision.R;

import java.util.Calendar;

public class testActivity extends AppCompatActivity{

    // Used to load the 'native-lib' library on application startup.

    static {
        System.loadLibrary("avutil");
        System.loadLibrary("swresample");
        System.loadLibrary("avcodec");
        System.loadLibrary("avformat");
        System.loadLibrary("swscale");
        System.loadLibrary("main");
        System.loadLibrary("ffmpeg-jni");
    }
    boolean isStart = false;
    SurfaceView sv_camera;
    CameraService camera;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sv_camera = (SurfaceView)findViewById(R.id.sv_camera);

        camera = new CameraService();

        sv_camera.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(final SurfaceHolder surfaceHolder) {
                camera.init(1, new CameraService.CatchImageCallback() {
                    @Override
                    public void getBytes(byte[] bytes) {

                    }
                },surfaceHolder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                camera.startPreview();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        });
    }

    public void onStart(View view) {
        Calendar cc = Calendar.getInstance();
        cc.setTimeInMillis(System.currentTimeMillis());
        String filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"
                + String.valueOf(cc.get(Calendar.YEAR))
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
                + ".mp4";
        videoinit(filename.getBytes());
        isStart = true;
    }

    public void onEnd(View view){
        isStart = false;
        camera.deleteCamera();
        videoclose();
    }

    public synchronized void addVideoData(byte[] data) {
        if(!isStart) return;
        videostart(data);
    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String testJni();

    public native int videoinit(byte[] filename);

    public native int videostart(byte[] yuvdata);

    public native int videoclose();
}
