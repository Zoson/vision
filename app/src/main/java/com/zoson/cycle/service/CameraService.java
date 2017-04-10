package com.zoson.cycle.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by zoson on 4/2/15.
 */
public class CameraService implements Camera.PreviewCallback{
    private final String TAG = "CameraService";
    Camera camera;
    byte[] imageByte;
    Camera.Parameters parameters;
    int width;
    int height;
    int count = 0;
    int frequency = 1;
    boolean if_getImg = false;
    List<CatchImageCallback> cbs;
    HandleBitmapThread handleBitmapThread;

    public void addListener(CatchImageCallback listeners){
        cbs.add(listeners);
    }

    public boolean rmListeners(CatchImageCallback listener){
        return cbs.remove(listener);
    }

    public void notifyListeners(byte[] data){
        for(int i=0;i<cbs.size();++i){
            cbs.get(i).getBytes(data);
        }
    }

    public void autoFocus(){
        camera.autoFocus(null);
    }


    public CameraService(){
        cbs = new LinkedList<CatchImageCallback>();
    }


    public void init(SurfaceHolder surfaceHolder){
        camera = Camera.open(0);
        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageByte = ("").getBytes();
    }

    public void startPreview(int width,int height){
        parameters = camera.getParameters();
        parameters.setPreviewSize(width, height);
        parameters.setPreviewFrameRate(25);
        this.width = width;
        this.height = height;
        parameters.setPictureFormat(PixelFormat.JPEG);
        parameters.setPreviewFormat(PixelFormat.YCbCr_420_SP);
        camera.setParameters(parameters);

        handleBitmapThread = new HandleBitmapThread();
        handleBitmapThread.start();
        camera.startPreview();
        //camera.setDisplayOrientation(90);
        camera.setPreviewCallback(this);
    }

    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
        if (cbs.size() == 0)return;
        if (count == frequency){
            if (handleBitmapThread != null){
                Message message = handleBitmapThread.handler.obtainMessage();
                message.what = HandleBitmapThread.GETDATA;
                message.obj = data;
                handleBitmapThread.handler.sendMessage(message);
            }
            count = 0;
        }
        count++;
    }

    public void getResolution(){

    }


    class HandleBitmapThread extends Thread{

        static final int GETDATA = 0x1;
        Handler handler;

        public HandleBitmapThread(){

        }

        public void handleDataToImageByte(byte[] data){
//            Log.i(TAG, "handleDataToImageByte");
//            int imageFormat = parameters.getPreviewFormat();
//            Rect rect = new Rect(0,0,width,height);
//            YuvImage yuvImage = new YuvImage(data,imageFormat,width,height,null);
//            try {
//                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//                yuvImage.compressToJpeg(rect,100,outputStream);
//                imageByte = outputStream.toByteArray();
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//            Bitmap mp = BitmapFactory.decodeByteArray(imageByte,0,imageByte.length);
//            Matrix m = new Matrix();
//            m.setRotate(270);
//            Bitmap mpp = Bitmap.createBitmap(mp,0,0,width,height,m,true);
//            imageByte = BitmapToBytes(mpp);
            notifyListeners(data);
        }
        @Override
        public void run() {
            Looper.prepare();
            handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what){
                        case GETDATA:
                            handleDataToImageByte((byte[]) msg.obj);
                            break;
                    }
                }
            };
            Looper.loop();
        }
    }

    public Camera getCamera(){
        return camera;
    }
    public byte[] getImageByte(){
        return imageByte;
    }
    public byte[] BitmapToBytes(Bitmap bm){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG,100,baos);
        return baos.toByteArray();
    }
    public void deleteCamera(){
        camera.setPreviewCallbackWithBuffer(null);
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    public void stopCatch(){
        handleBitmapThread = null;
        camera.stopPreview();
    }

    public void startCatch(){
        handleBitmapThread = new HandleBitmapThread();
        handleBitmapThread.start();
        camera.startPreview();
    }

    public interface CatchImageCallback{
    	public void getBytes(byte[] bytes);
    }
}
