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

int flush_encoder(AVFormatContext *fmt_ctx,unsigned int stream_index){
    int ret;
    int got_frame;
    AVPacket enc_pkt;
    if (!(fmt_ctx->streams[stream_index]->codec->codec->capabilities &
          CODEC_CAP_DELAY))
        return 0;
    while (1) {
        enc_pkt.data = NULL;
        enc_pkt.size = 0;
        av_init_packet(&enc_pkt);
        ret = avcodec_encode_video2 (fmt_ctx->streams[stream_index]->codec, &enc_pkt,
                                     NULL, &got_frame);
        av_frame_free(NULL);
        if (ret < 0)
            break;
        if (!got_frame){
            ret=0;
            break;
        }
        printf("Flush Encoder: Succeed to encode 1 frame!\tsize:%5d\n",enc_pkt.size);
        /* mux encoded frame */
        ret = av_write_frame(fmt_ctx, &enc_pkt);
        if (ret < 0)
            break;
    }
    return ret;
}




//without syn
int width;
int height;
AVCodecID codec_id ;
AVFormatContext *pFormatCtx;
AVOutputFormat* ofmt;
AVStream *video_stream;
AVCodecContext *pCodecCtx;
AVCodec* pCodec;
AVPacket pkt;
uint8_t *picture_buf;
AVFrame *pFrame;
int picture_size;
int y_size;
int framecnt = 0;
int64_t start_time ;

unsigned char* u = NULL;

void transYuvSpToYuvp(unsigned char* data,int size)
{
    if(u == NULL){
        u = new unsigned char[size/6];
    }
    int total_size = size*3/2;
    int j = 0;
    int i = size;
    int l = size;
    for(;i<total_size;i+=2,j++,l++)
    {
        u[j] = data[i];
        data[l] = data[i+1];
    }
    int k = size*5/4;
    int dis = k;
    for(;k<total_size;++k)
    {
        data[k] = u[k-dis];
    }
    for(i=0;i<size/6;++i)u[i] = 0;

}

JNIEXPORT jint JNICALL Java_com_zoson_cycle_ffmpeg_FFmpegService_init2(JNIEnv *env,jobject obj,jstring filename,jint w,jint h,jint c_w,jint c_h)
{
    LOGI("%s\n",__func__);
    width = w;
    height = h;

    LOGI("width : %d height : %d \n",width,height);

    char* out_name = (char*)env->GetStringUTFChars(filename,0);
    LOGI("out file ::: %s\n",out_name);
    av_register_all();
    avformat_network_init();
    avcodec_register_all();


    avformat_alloc_output_context2(&pFormatCtx,NULL,"flv",out_name);
    ofmt = pFormatCtx->oformat;
    if(ofmt==NULL){
        LOGE("ofmt is null\n");
        return -1;
    }
    if(pFormatCtx==NULL){
        LOGE("pFOrmat is null\n");
        return -1;
    }
    LOGE("JNICALL Java_com_hua_cameraandroidtest_MainActivity_init1");

    if(avio_open(&pFormatCtx->pb,out_name,AVIO_FLAG_WRITE)<0){
        LOGE("%s\n","Failed to open out file");
        return -1;
    }

    LOGE("JNICALL Java_com_hua_cameraandroidtest_MainActivity_init1-1");
    video_stream = avformat_new_stream(pFormatCtx,0);
    video_stream->codec->codec_tag = 0;
    if (pFormatCtx->oformat->flags & AVFMT_GLOBALHEADER)
        video_stream->codec->flags |= CODEC_FLAG_GLOBAL_HEADER;

    if (video_stream==NULL){
        LOGE("video stream is null\n");
        return -1;
    }
    //video_stream->time_base.num = 1;
    //video_stream->time_base.den = 25;
    //Param that must set
    pCodecCtx = video_stream->codec;
    //pCodecCtx->codec_id =AV_CODEC_ID_HEVC;
    pCodecCtx->codec_id = AV_CODEC_ID_H264;
    pCodecCtx->codec_type = AVMEDIA_TYPE_VIDEO;
    pCodecCtx->pix_fmt = AV_PIX_FMT_YUV420P;
    pCodecCtx->width = c_w;
    pCodecCtx->height = c_h;

    LOGI("codec width:%d height:%d \n",pCodecCtx->width,pCodecCtx->height);

    pCodecCtx->bit_rate = 400000;
    pCodecCtx->gop_size=250;
    pCodecCtx->time_base.num = 1;
    pCodecCtx->time_base.den = 25;

    //H264
//    pCodecCtx->me_range = 16;
//    pCodecCtx->max_qdiff = 4;
//    pCodecCtx->qcompress = 1;

    //Optional Param
    pCodecCtx->max_b_frames=1;
    pCodecCtx->qmin = 10;
    pCodecCtx->qmax = 51;
    pCodecCtx->coded_height = width;
    pCodecCtx->coded_width = height;

    // Set Option
    AVDictionary *param = 0;
    //H.264
    if(pCodecCtx->codec_id == AV_CODEC_ID_H264) {
        av_dict_set(&param, "preset", "slow", 0);
        av_dict_set(&param, "tune", "zerolatency", 0);
        //av_dict_set(¶m, "profile", "main", 0);
    }

    //Show some Information
    //av_dump_format(pFormatCtx, 0, out_name, 1);
    pCodec = avcodec_find_encoder(AV_CODEC_ID_H264);
    if (!pCodec){
        LOGE("Can not find encoder! \n");
        return -1;
    }
    if (avcodec_open2(pCodecCtx, pCodec,&param) < 0){
        LOGE("Failed to open encoder! \n");
        return -1;
    }

    pFrame = av_frame_alloc();
    picture_size = avpicture_get_size(pCodecCtx->pix_fmt, pCodecCtx->width, pCodecCtx->height);
    picture_buf = (uint8_t *)av_malloc(picture_size);
    avpicture_fill((AVPicture *)pFrame, picture_buf, pCodecCtx->pix_fmt, pCodecCtx->width, pCodecCtx->height);

    //Write File Header
    avformat_write_header(pFormatCtx,NULL);

    av_new_packet(&pkt,picture_size);

    y_size = pCodecCtx->width * pCodecCtx->height;

    env->ReleaseStringUTFChars(filename,out_name);
    start_time = av_gettime();
    return 0;
}

JNIEXPORT jint JNICALL Java_com_zoson_cycle_ffmpeg_FFmpegService_start(JNIEnv *env, jclass obj, jbyteArray yuvdata)
{

    unsigned char* data = (unsigned char *) env->GetByteArrayElements(yuvdata, 0);
    transYuvSpToYuvp(data,y_size);
    pFrame->data[0] = data;
    pFrame->data[1] = data + y_size;
    pFrame->data[2] = data + y_size*5/4;

    LOGE("JNICALL Java_com_hua_cameraandroidtest_MainActivity_start1");
    pFrame->pts = framecnt*(video_stream->time_base.den)/((video_stream->time_base.num)*25);
    int got;
    int ret = avcodec_encode_video2(pCodecCtx,&pkt,pFrame,&got);
    if(ret<0){
        LOGE("Failed to encode\n");
        return -1;
    }

    if(got==1){
        LOGI("SUcced to encode\n");
        framecnt++;
        pkt.stream_index = video_stream->index;
        ret = av_write_frame(pFormatCtx,&pkt);
        av_free_packet(&pkt);
    }

    env->ReleaseByteArrayElements(yuvdata, (jbyte *) data, 0);
    return 0;

}

JNIEXPORT jint JNICALL Java_com_zoson_cycle_ffmpeg_FFmpegService_close(JNIEnv *env, jclass obj)
{
    int ret = flush_encoder(pFormatCtx,0);
    if (ret < 0) {
        printf("Flushing encoder failed\n");
        return -1;
    }

    //Write file trailer
    av_write_trailer(pFormatCtx);

    //Clean
    if (video_stream){
        avcodec_close(video_stream->codec);
        av_free(pFrame);
        //av_free(picture_buf);
    }
    av_frame_free(&pFrame);
    delete picture_buf;
    avio_close(pFormatCtx->pb);
    avformat_free_context(pFormatCtx);

    return 0;
}


//test rtmp
void custom_log(void *ptr, int level, const char* fmt, va_list vl){

    //To TXT file
    FILE *fp=fopen("/storage/emulated/0/av_log.txt","a+");
    if(fp){
        vfprintf(fp,fmt,vl);
        fflush(fp);
        fclose(fp);
    }
    //To Logcat
    //LOGE(fmt, vl);
}

int end(AVFormatContext* ifmt_ctx,AVFormatContext *ofmt_ctx,AVOutputFormat *ofmt,int ret) {
    avformat_close_input(&ifmt_ctx);
    /* close output */
    if (ofmt_ctx && !(ofmt->flags & AVFMT_NOFILE))
        avio_close(ofmt_ctx->pb);
    avformat_free_context(ofmt_ctx);
    if (ret < 0 && ret != AVERROR_EOF) {
        LOGE("Error occurred.\n");
        return -1;
    }
    return 0;
}
JNIEXPORT jint JNICALL Java_com_zoson_cycle_ffmpeg_FFmpegService_test
        (JNIEnv *env, jobject obj,jstring file,jstring url)
{
    AVOutputFormat *ofmt = NULL;
    AVFormatContext *ifmt_ctx = NULL, *ofmt_ctx = NULL;
    AVPacket pkt;

    LOGI("%s","1");
    int ret, i;
    char *input_str = (char*)env->GetStringUTFChars(file,0);
    char *output_str= (char*)env->GetStringUTFChars(url,0);

    //FFmpeg av_log() callback
    av_log_set_callback(custom_log);

    av_register_all();
    //Network
    avformat_network_init();

    LOGI("%s","2");
    //Input
    if ((ret = avformat_open_input(&ifmt_ctx, input_str, 0, 0)) < 0) {
        LOGE( "Could not open input file.");
        return end(ifmt_ctx,ofmt_ctx,ofmt,ret);
    }
    if ((ret = avformat_find_stream_info(ifmt_ctx, 0)) < 0) {
        LOGE( "FaiLled to retrieve input stream information");
        return end(ifmt_ctx,ofmt_ctx,ofmt,ret);
    }

    int videoindex=-1;
    for(i=0; i<ifmt_ctx->nb_streams; i++)
        if(ifmt_ctx->streams[i]->codec->codec_type==AVMEDIA_TYPE_VIDEO){
            videoindex=i;
            break;
        }
    //Output
    avformat_alloc_output_context2(&ofmt_ctx, NULL, "flv",output_str); //RTMP
    //avformat_alloc_output_context2(&ofmt_ctx, NULL, "mpegts", output_str);//UDP
    LOGI("%s","3");

    if (!ofmt_ctx) {
        LOGE( "Could not create output context\n");
        ret = AVERROR_UNKNOWN;
        return end(ifmt_ctx,ofmt_ctx,ofmt,ret);
    }
    ofmt = ofmt_ctx->oformat;
    for (i = 0; i < ifmt_ctx->nb_streams; i++) {
        //Create output AVStream according to input AVStream
        AVStream *in_stream = ifmt_ctx->streams[i];
        AVStream *out_stream = avformat_new_stream(ofmt_ctx, in_stream->codec->codec);
        if (!out_stream) {
            LOGE( "Failed allocating output stream\n");
            ret = AVERROR_UNKNOWN;
            return end(ifmt_ctx,ofmt_ctx,ofmt,ret);
        }
        //Copy the settings of AVCodecContext
        ret = avcodec_copy_context(out_stream->codec, in_stream->codec);
        if (ret < 0) {
            LOGE( "Failed to copy context from input to output stream codec context\n");
            return end(ifmt_ctx,ofmt_ctx,ofmt,ret);
        }
        out_stream->codec->codec_tag = 0;
        if (ofmt_ctx->oformat->flags & AVFMT_GLOBALHEADER)
            out_stream->codec->flags |= CODEC_FLAG_GLOBAL_HEADER;
    }
    LOGI("%s","4");

    //Open output URL
    if (!(ofmt->flags & AVFMT_NOFILE)) {
        ret = avio_open(&ofmt_ctx->pb, output_str, AVIO_FLAG_WRITE);
        if (ret < 0) {
            LOGE( "Could not open output URL '%s'", output_str);
            return end(ifmt_ctx,ofmt_ctx,ofmt,ret);
        }
    }

    LOGI("%s","5");
    //Write file header
    ret = avformat_write_header(ofmt_ctx, NULL);
    if (ret < 0) {
        LOGE( "Error occurred when opening output URL\n");
        return end(ifmt_ctx,ofmt_ctx,ofmt,ret);
    }
    LOGI("%s","6");
    int frame_index=0;

    int64_t start_time=av_gettime();
    while (1) {
        AVStream *in_stream, *out_stream;
        //Get an AVPacket
        ret = av_read_frame(ifmt_ctx, &pkt);
        if (ret < 0)
            break;
        //FIX：No PTS (Example: Raw H.264)
        //Simple Write PTS
        if(pkt.pts==AV_NOPTS_VALUE){
            //Write PTS
            AVRational time_base1=ifmt_ctx->streams[videoindex]->time_base;
            //Duration between 2 frames (us)
            int64_t calc_duration=(double)AV_TIME_BASE/av_q2d(ifmt_ctx->streams[videoindex]->r_frame_rate);
            LOGI("time base :%d %d",ifmt_ctx->streams[videoindex]->r_frame_rate.num,ifmt_ctx->streams[videoindex]->r_frame_rate.den);
            //Parameters
            pkt.pts=(double)(frame_index*calc_duration)/(double)(av_q2d(time_base1)*AV_TIME_BASE);
            pkt.dts=pkt.pts;
            pkt.duration=(double)calc_duration/(double)(av_q2d(time_base1)*AV_TIME_BASE);
        }
        //Important:Delay
        if(pkt.stream_index==videoindex){
            LOGI("video index : %d\n",videoindex);
            AVRational time_base=ifmt_ctx->streams[videoindex]->time_base;
            LOGI("time base :%d %d",time_base.num,time_base.den);
            AVRational time_base_q={1,AV_TIME_BASE};
            int64_t pts_time = av_rescale_q(pkt.dts, time_base, time_base_q);
            int64_t now_time = av_gettime() - start_time;
            LOGI("diff time : %d",pts_time - now_time);
            if (pts_time > now_time)
                av_usleep(pts_time - now_time);
        }

        LOGI("%s","7");
        in_stream  = ifmt_ctx->streams[pkt.stream_index];
        out_stream = ofmt_ctx->streams[pkt.stream_index];
        /* copy packet */
        //Convert PTS/DTS
        pkt.pts = av_rescale_q(pkt.pts, in_stream->time_base, out_stream->time_base);//, AV_ROUND_NEAR_INF|AV_ROUND_PASS_MINMAX);
        pkt.dts = av_rescale_q(pkt.dts, in_stream->time_base, out_stream->time_base);//, AV_ROUND_NEAR_INF|AV_ROUND_PASS_MINMAX);
        pkt.duration = av_rescale_q(pkt.duration, in_stream->time_base, out_stream->time_base);
        LOGE("out time base %d %d",out_stream->time_base.num,out_stream->time_base.den);
        pkt.pos = -1;
        //Print to Screen
        if(pkt.stream_index==videoindex){
            LOGE("Send %8d video frames to output URL\n",frame_index);
            frame_index++;
        }
        //ret = av_write_frame(ofmt_ctx, &pkt);
        ret = av_interleaved_write_frame(ofmt_ctx, &pkt);

        if (ret < 0) {
            LOGE( "Error muxing packet\n");
            break;
        }
        av_free_packet(&pkt);

    }
    LOGI("%s","8");
    //Write file trailer
    av_write_trailer(ofmt_ctx);
    end:
    avformat_close_input(&ifmt_ctx);
    /* close output */
    if (ofmt_ctx && !(ofmt->flags & AVFMT_NOFILE))
        avio_close(ofmt_ctx->pb);
    avformat_free_context(ofmt_ctx);
    if (ret < 0 && ret != AVERROR_EOF) {
        LOGE( "Error occurred.\n");
        return -1;
    }

    env->ReleaseStringUTFChars(file,input_str);
    env->ReleaseStringUTFChars(url,output_str);
    return 0;
}

}


