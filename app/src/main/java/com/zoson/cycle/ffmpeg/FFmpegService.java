package com.zoson.cycle.ffmpeg;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.zoson.cycle.utils.ThreadPool;

/**
 * Created by zoson on 17-2-9.
 */

public class FFmpegService{

    static {
        System.loadLibrary("avutil");
        System.loadLibrary("swresample");
        System.loadLibrary("avcodec");
        System.loadLibrary("avformat");
        System.loadLibrary("swscale");
        System.loadLibrary("postproc");
        System.loadLibrary("ffmpeg-jni");

    }
    public FFmpegService(){}

    public void init(){

    }

    public void convformat(Description in,Description out,int outtype,Callback callback){

    }

    public void transformat(final Description in,final Description out,final Callback callback){
        ThreadPool.start(new Runnable() {
            @Override
            public void run() {
                if(init2(out.location,in.width,in.height,out.width,out.height)==-1)return;
                while(true){
                    Frame frame = callback.requireFrame();
                    if (frame == null){
                        break;
                    }else{
                        start(frame.data);
                    }
                }
                close();
            }
        });
    }


    private native int init2(String filename,int w,int h,int c_w ,int c_h);

    private native int start(byte[] yuvdata);

    private native int close();

    public void testRtmp(final String file,final String url)
    {
        ThreadPool.start(new Runnable() {
            @Override
            public void run() {
                test(file,url);
            }
        });
    }
    private native int test(String file,String url);

    public interface Callback{
        public void before(int ret,String msg);
        public void proceeding(int ret,String msg);
        public void after(int ret,String msg);
        public Frame requireFrame();
    }

}
