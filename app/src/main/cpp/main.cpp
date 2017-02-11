#include <jni.h>
#include "include/net/net.hpp"
extern "C" {
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libswscale/swscale.h"
#include "libavutil/imgutils.h"
}

#include <jni.h>
#include <android/log.h>
#define LOGE(format, ...)  __android_log_print(ANDROID_LOG_ERROR, "(>_<)", format, ##__VA_ARGS__)
#define LOGI(format, ...)  __android_log_print(ANDROID_LOG_INFO,  "(^_^)", format, ##__VA_ARGS__)

using namespace zoson;
extern "C"
{
jstring Java_com_zoson_vision_activity_testActivity_testJni(JNIEnv *env, jobject /* this */)
{
    return env->NewStringUTF("HelloVision");
}

}


