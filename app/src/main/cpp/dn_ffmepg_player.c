#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include "android/log.h"
#include "android/native_window.h"
#include "android/native_window_jni.h"

// 编码
#include "libavcodec/avcodec.h"
// 封装格式处理
#include "libavformat/avformat.h"
// 像素处理
#include "libswscale/swscale.h"

// 像素格式转换
#include "libyuv.h"

#define LOG_TAG "ffmpeg_tag"
#define logd(FORMAT, ...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,FORMAT,##__VA_ARGS__);
#define loge(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,FORMAT,##__VA_ARGS__);


/**
* Class:     com_young_ffmpeg_player1_YoungPlayer
* Method:    initConfig
* Signature: (Ljava/lang/String;)V
*/
JNIEXPORT void JNICALL Java_com_example_ffmpeg_1test_jni_FFmpegJni_initConfig(JNIEnv *env, jobject thiz, jstring input) {
    const char * j_path = (const char *) (*env)->GetStringUTFChars(env, input, NULL);
    logd("%s", j_path)

    av_register_all();
    AVFormatContext  *avFormatContext = avformat_alloc_context();
    if (avformat_open_input(&avFormatContext, j_path, NULL, NULL) != 0) {
        logd("%s", "打开输入文件失败！")
        (*env) -> ReleaseStringUTFChars(env, input, j_path);
        return;
    }
    (*env) -> ReleaseStringUTFChars(env,input, j_path);

    if (avformat_find_stream_info(avFormatContext, NULL) < 0) {
        logd("%s", "获取视频信息失败！")
        return;
    }

    int target_video_index = -1;
    for (int i = 0; i < avFormatContext->nb_streams; ++i) {
        if (avFormatContext -> streams[i] -> codec -> codec_type == AVMEDIA_TYPE_VIDEO) {
            target_video_index = i;
            break;
        }
    }
    if (target_video_index == -1) {
        logd("%s", "无视频流数据！")
        return;
    }

    AVCodecContext *avCodecContext = avFormatContext->streams[target_video_index]->codec;
    logd("视频的宽高：%d %d", avCodecContext->width, avCodecContext->height)

    avcodec_close(avCodecContext);
    avformat_free_context(avFormatContext);
}
