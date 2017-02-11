#include <stdio.h>
#include <time.h>

extern "C"
{
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libswscale/swscale.h"
#include "libavutil/log.h"
#include <libavutil/opt.h>
#include <libavutil/imgutils.h>
#include <libavutil/time.h>
#include <libavutil/mathematics.h>
}



#ifdef ANDROID
#include <jni.h>
#include <android/log.h>


#define LOGE(format, ...)  __android_log_print(ANDROID_LOG_ERROR, "(>_<)", format, ##__VA_ARGS__)
#define LOGI(format, ...)  __android_log_print(ANDROID_LOG_INFO,  "(^_^)", format, ##__VA_ARGS__)
#else
#define LOGE(format, ...)  printf("(>_<) " format "\n", ##__VA_ARGS__)
#define LOGI(format, ...)  printf("(^_^) " format "\n", ##__VA_ARGS__)
#endif




/*
* encording init
*/
extern "C"
{

//test different codec
#define TEST_H264  1
#define TEST_HEVC  0

AVCodec *pCodec;
AVCodecContext *pCodecCtx= NULL;
int i, ret, got_output;
FILE *fp_out;
AVFrame *pFrame;
AVPacket pkt;
int y_size;
int framecnt=0;
int mwidth;
int mheight;

JNIEXPORT jint JNICALL Java_com_zoson_vision_activity_MainActivity_videoinit(JNIEnv *env,
                                                                             jclass obj,
                                                                             jbyteArray filename,jint w,jint h) {
    mwidth = w;
    mheight = h;
    LOGI("%d %d\n",mwidth,mheight);
    LOGI("%s\n", __func__);
    avcodec_register_all();

#if TEST_HEVC
    AVCodecID codec_id=AV_CODEC_ID_HEVC;
    //char filename_out[]="/storage/emulated/0/ds.hevc";
#else
    AVCodecID codec_id=AV_CODEC_ID_H264;
    //char filename_out[]="/storage/emulated/0/ds.mp4";
#endif

    pCodec = avcodec_find_encoder(codec_id);
    if (!pCodec) {
        printf("Codec not found\n");
        return -1;
    }
    pCodecCtx = avcodec_alloc_context3(pCodec);
    if (!pCodecCtx) {
        printf("Could not allocate video codec context\n");
        return -1;
    }
    pCodecCtx->bit_rate = 400000;
    pCodecCtx->width = mwidth;
    pCodecCtx->height = mheight;
    pCodecCtx->time_base.num=1;
    pCodecCtx->time_base.den=25;
    pCodecCtx->gop_size = 10;
    pCodecCtx->max_b_frames = 1;
    pCodecCtx->pix_fmt = AV_PIX_FMT_YUV420P;

    if (codec_id == AV_CODEC_ID_H264)
        av_opt_set(pCodecCtx->priv_data, "preset", "slow", 0);

    if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
        printf("Could not open codec\n");
        return -1;
    }

    pFrame = av_frame_alloc();
    if (!pFrame) {
        printf("Could not allocate video frame\n");
        return -1;
    }
    pFrame->format = pCodecCtx->pix_fmt;
    pFrame->width  = pCodecCtx->width;
    pFrame->height = pCodecCtx->height;

    ret = av_image_alloc(pFrame->data, pFrame->linesize, pCodecCtx->width, pCodecCtx->height,
                         pCodecCtx->pix_fmt, 16);
    if (ret < 0) {
        printf("Could not allocate raw picture buffer\n");
        return -1;
    }
    //Output bitstream


    y_size = pCodecCtx->width * pCodecCtx->height;
    jbyte *filename_out = (jbyte *) env->GetByteArrayElements( filename, 0);
    fp_out = fopen("/storage/emulated/0/video.mp4", "wb");
    if (!fp_out) {
        printf("Could not open %s\n", filename_out);
        return -1;
    }
    LOGE("file name ::: %s",filename_out);
    env->ReleaseByteArrayElements(filename, filename_out, 0);
    return 1;
}

JNIEXPORT jint JNICALL Java_com_zoson_vision_activity_MainActivity_videostart(JNIEnv *env,
                                                                              jclass obj,
                                                                              jbyteArray yuvdata) {
    int frameFinished = 0, size = 0;
    unsigned char *ydata = (unsigned char *) env->GetByteArrayElements( yuvdata, 0);
    //AVFrame * yuv422frame=NULL;
    //struct SwsContext *swsctx = NULL;
    av_init_packet(&pkt);
    pkt.data = NULL;    // packet data will be allocated by the encoder
    pkt.size = 0;
    pFrame->data[0] = (uint8_t *) ydata;  //PCM Data
    pFrame->data[1] = (uint8_t *) (ydata + y_size);      // U
    pFrame->data[2] = (uint8_t *) (ydata + y_size * 5/ 4);  // V
    pFrame->pts = i;
    ret = avcodec_encode_video2(pCodecCtx, &pkt, pFrame, &got_output);
    if (ret < 0) {
        printf("Error encoding frame\n");
        return -1;
    }
    if (got_output) {
        printf("Succeed to encode frame: %5d\tsize:%5d\n",framecnt,pkt.size);
        framecnt++;
        fwrite(pkt.data, 1, pkt.size, fp_out);
        av_free_packet(&pkt);
    }
    //av_frame_free(&pFrame);
    env->ReleaseByteArrayElements( yuvdata, (jbyte*)ydata, 0);
    return 0;
}

JNIEXPORT jint JNICALL Java_com_zoson_vision_activity_MainActivity_videoclose(JNIEnv *env,
                                                                              jclass obj) {

    //Flush Encoder
    for (got_output = 1; got_output; i++) {
        ret = avcodec_encode_video2(pCodecCtx, &pkt, NULL, &got_output);
        if (ret < 0) {
            printf("Error encoding frame\n");
            return -1;
        }
        if (got_output) {
            printf("Flush Encoder: Succeed to encode 1 frame!\tsize:%5d\n",pkt.size);
            fwrite(pkt.data, 1, pkt.size, fp_out);
            av_free_packet(&pkt);
        }
    }

    fclose(fp_out);
    avcodec_close(pCodecCtx);
    av_free(pCodecCtx);
    //av_freep(&pFrame->data[0]);
    av_frame_free(&pFrame);
    return 0;
}

}


