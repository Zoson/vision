package com.zoson.vision.domain;

import com.zoson.cycle.ffmpeg.Description;
import com.zoson.cycle.ffmpeg.FFmpegService;
import com.zoson.cycle.ffmpeg.Format;
import com.zoson.cycle.ffmpeg.Frame;
import com.zoson.cycle.service.CameraService;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by zoson on 17-2-13.
 */

public class VisionSession implements CameraService.CatchImageCallback,FFmpegService.Callback{

    Parameter mParams;
    CameraService cameraService;
    FFmpegService fFmpegService;
    VisionManager parent;
    boolean isClosed = false;
    BlockingQueue<byte[]> queue_bytes;
    VisionSession(VisionManager visionManager,CameraService camera,FFmpegService fFmpegService){
        mParams = new Parameter();
        parent = visionManager;
        this.cameraService = camera;
        this.fFmpegService = fFmpegService;
        queue_bytes = new LinkedBlockingQueue<>(10);
    }

    public Parameter getParameter(){
        return (Parameter) mParams.clone();
    }

    public void setParams(Parameter params){
        this.mParams = params;
        if(mParams.lockCamera)cameraService.addListener(this);
    }

    public void autofocus(){
        cameraService.autoFocus();
    }

    public void resset(){
        mParams = new Parameter();
    }

    public void start(){
        switch (mParams.in_protocol){
            case VisionManager.Protocol.FILE:
                break;
            case VisionManager.Protocol.RAW:
                cameraService.startPreview(mParams.in_width,mParams.in_height);
                Description in = new Description(mParams.in_url,mParams.in_width,mParams.in_height);
                Description out = new Description(mParams.out_url,mParams.in_width,mParams.in_height);
                fFmpegService.transformat(in,out,this);
                break;
            case VisionManager.Protocol.RTMP:
                System.out.println("VisionSession:"+mParams.in_width+" "+mParams.in_height);
                cameraService.startPreview(mParams.in_width,mParams.in_height);
                Description in1 = new Description(mParams.in_url,mParams.in_width,mParams.in_height);
                Description out1 = new Description(mParams.out_url,mParams.in_width,mParams.in_height);
                fFmpegService.transformat(in1,out1,this);
                break;
        }

        switch (mParams.out_protocol){
            case VisionManager.Protocol.FILE:
                break;
            case VisionManager.Protocol.RAW:
                break;
            case VisionManager.Protocol.RTMP:
                break;
        }

    }

    public void close(){
        System.out.println("close");
        this.isClosed = true;
        if (mParams.lockCamera) cameraService.rmListeners(this);
        queue_bytes.clear();
        parent.reclaimSession(this);
    }

    @Override
    public void getBytes(byte[] bytes) {
        queue_bytes.offer(bytes);
    }

    @Override
    public void before(int ret, String msg) {

    }

    @Override
    public void proceeding(int ret, String msg) {

    }

    @Override
    public void after(int ret, String msg) {

    }

    @Override
    public Frame requireFrame() {
        System.out.println("requireFrame");
        if(isClosed)return null;
        Frame frame = null;
        while((!isClosed)&&(frame==null)){
            byte[] data = queue_bytes.poll();
            if (data == null)continue;
            frame = new Frame(data);
        }
        if(isClosed) return null;
        if (frame==null) System.out.println("frame is null is about to close");
        return frame;
    }

    public class Parameter implements Cloneable{
        public String in_url;
        public String out_url;
        public int in_protocol;
        public int out_protocol;

        public boolean lockCamera = false;
        public boolean isVideo = false;
        public boolean isAudio = false;

        public int in_width = 352;
        public int in_height = 288;
        public int in_video_format = Format.FORMAT_YUV420;
        public int in_audio_format = 0;

        public int out_width = 352;
        public int out_height = 288;
        public int out_video_format = Format.FORMAT_H264;
        public int out_audio_format = 0;

        @Override
        protected Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return new Parameter();
        }
    }
}
