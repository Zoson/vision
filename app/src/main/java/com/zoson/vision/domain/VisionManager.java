package com.zoson.vision.domain;

import android.view.SurfaceHolder;

import com.zoson.cycle.ffmpeg.FFmpegService;
import com.zoson.cycle.service.CameraService;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by zoson on 17-2-9.
 */

public class VisionManager{
    String RTMP_IP = "rtmp://192.168.31.209/live/";

    CameraService cameraService;
    FFmpegService fFmpegService;
    private static VisionManager instance;

    private List<VisionSession> sessions;
    private Queue<VisionSession> sessionPool;

    private VisionManager(){
        cameraService = new CameraService();
        fFmpegService = new FFmpegService();
        sessionPool = new LinkedBlockingQueue<>(10);
        sessions = new LinkedList<>();
    }

    public static VisionManager getDefault(){
        if (instance == null){
            synchronized (VisionManager.class){
                instance = new VisionManager();
            }
        }
        return instance;
    }


    public void applyCamera(SurfaceHolder surfaceHolder){
        cameraService.init(surfaceHolder);
    }

    public void reclaimSession(VisionSession session){
        if (session.isClosed == true) return;
        session.resset();
        sessions.remove(session);
        session.isClosed = true;
        sessionPool.offer(session);
    }

    public VisionSession newSession() {
        VisionSession visionSession = sessionPool.poll();
        if (visionSession == null)return new VisionSession(this,cameraService,fFmpegService);
        return visionSession;
    }

    public void test(String file,String url) {
        fFmpegService.testRtmp(file,url);
    }
    public class Format extends com.zoson.cycle.ffmpeg.Format{}
    public class Protocol extends com.zoson.cycle.ffmpeg.Protocol{}
}
