#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>

#include "com_young_ffmpeg_player1_YoungPlayer.h"

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

JNIEXPORT void JNICALL Java_com_young_ffmpeg_1player1_YoungPlayer_render
        (JNIEnv *env, jobject instance, jstring input, jobject surface) {
//    isStop = false;
    // 视频路径，转换为 c 语言的字符串
    const char *input_cstr = (const char *) (*env)->GetStringUTFChars(env, input, NULL);

    // 1、注册组件
    av_register_all();

    // 获取封装格式上下文
    AVFormatContext *avFormatContext = avformat_alloc_context();

    // 2、打开输入视频文件
    if (avformat_open_input(&avFormatContext, input_cstr, NULL, NULL) != 0) {
        loge("%s", "打开输入视频文件失败");
        // 释放字符串资源
        (*env)->ReleaseStringUTFChars(env, input, input_cstr);
        return;
    }
    (*env)->ReleaseStringUTFChars(env, input, input_cstr);

    // 3、获取视频信息
    if (avformat_find_stream_info(avFormatContext, NULL) < 0) {
        loge("%s", "获取视频信息失败");
        return;
    }
}

//    //视频解码，需要找到视频对应的AVStream所在pFormatCtx->streams的索引位置
//    int video_stream_index = -1;
//    int i = 0;
//    for (; i < avFormatContext->nb_streams; i++) {
//        //根据类型判断，是否是视频流
//        if (avFormatContext->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
//            // 视频流
//            video_stream_index = i;
//            break;
//        }
//    }
//
//    if (video_stream_index == -1) {
//        loge("%s", "无法获取视频流的索引");
//        return;
//    }
//
//    // 4、获取视频解码器
//    // 利用视频流索引，获取解码器上下文
//    AVCodecContext *avCodecContext = avFormatContext->streams[video_stream_index]->codec;
//
//    // 利用解码器上下文获取解码器 id，从而获取解码器
//    AVCodec *avCodec = avcodec_find_decoder(avCodecContext->codec_id);
//    if (avCodec == NULL) {
//        loge("%s", "没有找到匹配的解码器");
//        return;
//    }
//
//    // 打开解码器
//    if (avcodec_open2(avCodecContext, avCodec, NULL) < 0) {
//        loge("%s", "解码器无法打开");
//        return;
//    }
//
//    // 获取编码数据
//    AVPacket *avPacket = (AVPacket *) av_malloc(sizeof(AVPacket));
//    // 像素数据（解码数据）
//    AVFrame *yuv_frame = av_frame_alloc();
//    AVFrame *rgb_frame = av_frame_alloc();
//
//    // native绘制
//
//    // 窗体
//    ANativeWindow *aNativeWindow = ANativeWindow_fromSurface(env, surface);
//    // 绘制时的缓冲区
//    ANativeWindow_Buffer outBuffer;
//
//    ANativeWindow_setBuffersGeometry(aNativeWindow, avCodecContext->width,
//                                     avCodecContext->height, WINDOW_FORMAT_RGBA_8888);
//
//
//    // 每次解码 AVPacket->AvFrame 的长度
//    int len = 0;
//    // 是否正在解码的标致
//    int got_frame = 0;
//    // 帧的数量
//    int frame_count = 0;
//    //6.一阵一阵读取压缩的视频数据AVPacket
//    // 循环读取每一帧的数据
//
//    while (av_read_frame(avFormatContext, avPacket) >= 0 ) {
//        // 解码 AVPacket->AvFrame
//
//        if (avPacket->stream_index == video_stream_index) {
//            // 视频流
//            len = avcodec_decode_video2(avCodecContext, yuv_frame, &got_frame, avPacket);
//            if (len < 0) {
//                loge("%s", "解码错误");
//                break;
//            }
//
//            //Zero if no frame could be decompressed
//            //非零，正在解码
//            if (got_frame) {
//                LOGI("解码%d帧", frame_count++);
//                // lock
//                ANativeWindow_lock(aNativeWindow, &outBuffer, NULL);
//                //设置rgb_frame的属性（像素格式、宽高）和缓冲区
//                //rgb_frame缓冲区与outBuffer.bits是同一块内存
//                avpicture_fill((AVPicture *) rgb_frame, outBuffer.bits, PIX_FMT_RGBA,
//                               avCodecContext->width, avCodecContext->height);
//
//                //YUV->RGBA_8888
//                // 用这个比较贴近原视频
//                I420ToABGR(yuv_frame->data[0], yuv_frame->linesize[0],
//                           yuv_frame->data[1], yuv_frame->linesize[1],
//                           yuv_frame->data[2], yuv_frame->linesize[2],
//                           rgb_frame->data[0], rgb_frame->linesize[0],
//                           avCodecContext->width, avCodecContext->height);
//
//                // unlock
//                ANativeWindow_unlockAndPost(aNativeWindow);
//
//                // 休眠 16 秒
//                usleep(1000 * 16);
//            }
//
//            // 释放压缩的视频数据AVPacket
//            av_free_packet(avPacket);
//        }
//
//    }
//
//    // 释放窗口
//    ANativeWindow_release(aNativeWindow);
//
//    // 释放像素数据AVFrame
//    av_frame_free(&yuv_frame);
//    av_frame_free(&rgb_frame);
//    // 关闭解码器
//    avcodec_close(avCodecContext);
//    // 释放封装格式上下文
//    avformat_free_context(avFormatContext);
//
//}
//
//JNIEXPORT jstring JNICALL
//Java_com_young_ffmpeg_1player1_YoungPlayer_ffmpegInfo(JNIEnv *env, jclass clazz) {
//
//    char info[40000] = {0};
//    AVCodec *c_temp = av_codec_next(NULL);
//    while (c_temp != NULL) {
//        if (c_temp->decode != NULL) {
//            sprintf(info, "%sdecode:", info);
//        } else {
//            sprintf(info, "%sencode:", info);
//        }
//        switch (c_temp->type) {
//            case AVMEDIA_TYPE_VIDEO:
//                sprintf(info, "%s(video):", info);
//                break;
//            case AVMEDIA_TYPE_AUDIO:
//                sprintf(info, "%s(audio):", info);
//                break;
//            default:
//                sprintf(info, "%s(other):", info);
//                break;
//        }
//        sprintf(info, "%s[%s]\n", info, c_temp->name);
//        c_temp = c_temp->next;
//    }
//    // return env->NewStringUTF(info);
//    return (*env)->NewStringUTF(env, info);
//}