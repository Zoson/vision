package com.zoson.cycle.ffmpeg;

import static com.zoson.cycle.ffmpeg.Format.FORMAT_H264;
import static com.zoson.cycle.ffmpeg.Protocol.FILE;
import static com.zoson.cycle.ffmpeg.Protocol.FILE;

/**
 * Created by zoson on 17-2-13.
 */

public class Description {
    int protocol;
    int format;
    String location;

    int width;
    int height;

    public Description(String location,int w,int h){
        this.location = location;
        this.protocol = FILE;
        this.format = FORMAT_H264;
        this.width = w;
        this.height = h;
    }

    public Description(String location,int w,int h,int format){
        this.location = location;
        this.format = format;
        this.protocol = FILE;
        this.width = w;
        this.height = h;
    }


    public Description(String name,int w,int h,int format,int type){
        this.location = name;
        this.protocol = type;
        this.format = format;
        this.width = w;
        this.height = h;
    }
}
