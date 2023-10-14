#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include "android/log.h"
#include "android/native_window.h"
#include "android/native_window_jni.h"
#include <strings.h>
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
JNIEXPORT jstring JNICALL
Java_com_example_ffmpeg_1test_jni_FFmpegJni_initConfig(JNIEnv *env, jobject thiz, jstring input) {
    const char * j_path = (const char *) (*env)->GetStringUTFChars(env, input, NULL);
    logd("%s", j_path)

    av_register_all();
    AVFormatContext  *avFormatContext = avformat_alloc_context();
    if (avformat_open_input(&avFormatContext, j_path, NULL, NULL) != 0) {
        (*env) -> ReleaseStringUTFChars(env, input, j_path);
        return (*env)->NewStringUTF(env, "打开输入文件失败！");
    }
    (*env) -> ReleaseStringUTFChars(env,input, j_path);

    if (avformat_find_stream_info(avFormatContext, NULL) < 0) {
        return (*env)->NewStringUTF(env, "获取视频信息失败！");
    }

    int target_video_index = -1;
    for (int i = 0; i < avFormatContext->nb_streams; ++i) {
        if (avFormatContext -> streams[i] -> codec -> codec_type == AVMEDIA_TYPE_VIDEO) {
            target_video_index = i;
            break;
        }
    }
    if (target_video_index == -1) {
        return (*env)->NewStringUTF(env, "无视频流数据");
    }

    AVCodecContext *avCodecContext = avFormatContext->streams[target_video_index]->codec;
    logd("视频的宽高：%d %d", avCodecContext->width, avCodecContext->height)

    // 分配足够的空间来存储整数到字符串的转换结果
    char* video_size = malloc(sizeof(char) * 50);
    // 使用 snprintf 将整数转换为字符串，并确保不会溢出
    snprintf(video_size, 50, "平均比特率：%d 宽：%d 高：%d", avCodecContext->bit_rate, avCodecContext->width, avCodecContext->height);

    avcodec_close(avCodecContext);
    avformat_free_context(avFormatContext);
    return (*env)->NewStringUTF(env, video_size);
}
