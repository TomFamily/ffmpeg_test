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

JNIEXPORT jstring JNICALL
Java_com_example_ffmpeg_1test_jni_FFmpegJni_playVideo(JNIEnv *env, jobject thiz, jstring path,
                                                      jobject surface) {
    const char * j_path = (const char *)(*env)->GetStringUTFChars(env, path, NULL);
    av_register_all();
    AVFormatContext *avFormatContext = avformat_alloc_context();
    if (avformat_open_input(&avFormatContext, j_path, NULL, NULL) != 0) {
        (*env)->ReleaseStringUTFChars(env, path, j_path);
        return (*env)->NewStringUTF(env, "打开视频流失败！");
    }
    (*env)->ReleaseStringUTFChars(env, path, j_path);

    if (avformat_find_stream_info(avFormatContext, NULL) < 0) {
        return (*env)->NewStringUTF(env, "无法获取到视频流信息！");
    }

    int target_codec_index = -1;
    for (int i = 0; i < avFormatContext->nb_streams; ++i) {
        if (avFormatContext->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
            target_codec_index = i;
            break;
        }
    }
    if (target_codec_index == -1) {
        return (*env)->NewStringUTF(env, "所给路径下，无视频流信息！");
    }

    AVCodecContext *avCodecContext = avFormatContext->streams[target_codec_index]->codec;
    AVCodec *avCodec = avcodec_find_decoder(avCodecContext->codec_id);
    if (avCodec == NULL) {
        return (*env)->NewStringUTF(env, "未知道解码器！");
    }
    if (avcodec_open2(avCodecContext, avCodec, NULL) < 0) {
        return (*env)->NewStringUTF(env, "无法打开解码器！");
    }

    AVPacket *avPacket = (AVPacket *) alloca(sizeof(AVPacket));
    AVFrame *yuv_frame = av_frame_alloc();
    AVFrame *rgb_frame = av_frame_alloc();

    // 窗体
    ANativeWindow *aNativeWindow = ANativeWindow_fromSurface(env, surface);
    // 绘制时的缓冲区
    ANativeWindow_Buffer outBuffer;
    ANativeWindow_setBuffersGeometry(aNativeWindow, avCodecContext->width,
                                     avCodecContext->height, WINDOW_FORMAT_RGBA_8888);

    // 每次解码 AVPacket->AvFrame 的长度
    int len;
    // 是否正在解码的标致
    int decoding = 0;
    // 帧的数量
    int frame_count = 0;
    // 循环一阵一阵读取压缩的视频数据AVPacket
    while (av_read_frame(avFormatContext, avPacket) >= 0 ) {
        // 解码 AVPacket->AvFrame
        if (avPacket->stream_index == target_codec_index) {
            // 视频流
            len = avcodec_decode_video2(avCodecContext, yuv_frame, &decoding, avPacket);
            if (len < 0) {
                return (*env)->NewStringUTF(env, "解码错误！");
            }

            //非零，正在解码
            if (decoding) {
                logd("解码%d帧", frame_count++)
                // lock
                ANativeWindow_lock(aNativeWindow, &outBuffer, NULL);
                // 设置rgb_frame的属性（像素格式、宽高）和缓冲区
                // rgb_frame 缓冲区与outBuffer.bits是同一块内存： 将 rgb_frame 通过 outBuffer 配置给 window
                // avpicture_fill：将输入的像素数据（即outBuffer.bits）填充到AVPicture结构体的相应位置中
                avpicture_fill((AVPicture *) rgb_frame, outBuffer.bits, PIX_FMT_RGBA,
                               avCodecContext->width, avCodecContext->height);

                //YUV->RGBA_8888:用这个比较贴近原视频
                I420ToABGR(yuv_frame->data[0], yuv_frame->linesize[0],
                           yuv_frame->data[1], yuv_frame->linesize[1],
                           yuv_frame->data[2], yuv_frame->linesize[2],
                           rgb_frame->data[0], rgb_frame->linesize[0],
                           avCodecContext->width, avCodecContext->height);

                // unlock
                ANativeWindow_unlockAndPost(aNativeWindow);
                // 休眠 16 秒
                usleep(1000 * 16);
            }

            // 释放压缩的视频数据AVPacket
            av_free_packet(avPacket);
        }
    }

    // 释放窗口
    ANativeWindow_release(aNativeWindow);
    // 释放像素数据AVFrame
    av_frame_free(&yuv_frame);
    av_frame_free(&rgb_frame);
    // 关闭解码器
    avcodec_close(avCodecContext);
    // 释放封装格式上下文
    avformat_free_context(avFormatContext);

    return (*env)->NewStringUTF(env, "success！");
}