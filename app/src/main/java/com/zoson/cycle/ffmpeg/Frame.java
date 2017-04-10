package com.zoson.cycle.ffmpeg;

import static com.zoson.cycle.ffmpeg.Protocol.FILE;

/**
 * Created by zoson on 17-2-13.
 */

public class Frame {
    int protocal;
    int format;
    byte[] data;

    int width;
    int height;

    public Frame(int format,byte[] data,int width,int height){
        this.protocal = FILE;
        this.data = data;
        this.width = width;
        this.height = height;
    }

    public Frame(int protocal,int format,byte[] data,int width,int height){
        this.protocal = protocal;
        this.format = format;
        this.data = data;
        this.width = width;
        this.height = height;
    }

    public Frame(byte[] data){
        this.data =data;
    }

}
