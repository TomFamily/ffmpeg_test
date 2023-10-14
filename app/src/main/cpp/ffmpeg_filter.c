//
// Created by yk on 2023/10/14.
//
#include "loger.h"
#include <jni.h>
#include "android/native_window.h"
#include "android/native_window_jni.h"
#include "libavcodec/avcodec.h"
#include "libavfilter/avfilter.h"
#include "libavformat/avformat.h"
#include "libswscale/swscale.h"
#include "libavfilter/buffersink.h"
#include "libavutil/imgutils.h"
#include "libavfilter/buffersrc.h"


AVFilterContext *buffersink_ctx;
AVFilterContext *buffersrc_ctx;
AVFilterGraph *filter_graph;
const char *filters_descr = "lutyuv='u=128:v=128'";

JNIEXPORT void JNICALL
Java_com_example_ffmpeg_1test_jni_FFmpegJni_playVideoWithFilter(
        JNIEnv *env,
        jobject thiz,
        jstring path,
        jobject surface
        ) {
    const char *j_path = (*env)->GetStringUTFChars(env, path, NULL);
    logd("%s", j_path)

    av_register_all();
    avfilter_register_all();

    AVFormatContext *avFormatContext = avformat_alloc_context();
    if (avformat_open_input(&avFormatContext, j_path, NULL, NULL) < 0) {
        (*env)->ReleaseStringUTFChars(env, path, j_path);
        return;
    }
    (*env)->ReleaseStringUTFChars(env, path, j_path);

    if (avformat_find_stream_info(avFormatContext, NULL) < 0) {
        return;
    }

    int target_video_index = -1;
    for (int i = 0; i < avFormatContext->nb_streams; ++i) {
        if (avFormatContext->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
            target_video_index = i;
            break;
        }
    }
    if (target_video_index == -1) {
        return;
    }

    AVCodecContext *avCodecContext = avFormatContext->streams[target_video_index]->codec;

    char args[512];
    int ret;
    // avfilter_get_by_name：通过给定的过滤器名称来查找并返回相应的 AVFilter 结构体指针。
    // AVFilter 结构体包含有关特定过滤器的信息，如名称、类型、输入输出端口等。
    AVFilter *buffersrc = avfilter_get_by_name("buffer");
    //新版的ffmpeg库必须为buffersink
    AVFilter *buffersink = avfilter_get_by_name("buffersink");
    AVFilterInOut *outputs = avfilter_inout_alloc();
    AVFilterInOut *inputs = avfilter_inout_alloc();
    enum AVPixelFormat pix_fmts[] = {AV_PIX_FMT_YUV420P, AV_PIX_FMT_NONE};
    AVBufferSinkParams *avBufferSinkParams;
    // avfilter_inout_free(&inputs);
    filter_graph = avfilter_graph_alloc();

    /* buffer video source: the decoded frames from the decoder will be inserted here. */
    snprintf(args, sizeof(args),
             "video_size=%dx%d:pix_fmt=%d:time_base=%d/%d:pixel_aspect=%d/%d",
             avCodecContext->width, avCodecContext->height, avCodecContext->pix_fmt,
             avCodecContext->time_base.num, avCodecContext->time_base.den,
             avCodecContext->sample_aspect_ratio.num, avCodecContext->sample_aspect_ratio.den);

    ret = avfilter_graph_create_filter(&buffersrc_ctx, buffersrc, "in",
                                       args, NULL, filter_graph);
    if (ret < 0) {
        logd("Cannot create buffer source\n")
        return;
    }

    /* buffer video sink: to terminate the filter chain. */
    avBufferSinkParams = av_buffersink_params_alloc();
    avBufferSinkParams->pixel_fmts = pix_fmts;
    ret = avfilter_graph_create_filter(&buffersink_ctx, buffersink, "out",
                                       NULL, avBufferSinkParams, filter_graph);
    av_free(avBufferSinkParams);
    if (ret < 0) {
        logd("Cannot create buffer sink\n")
        return;
    }

    /* Endpoints for the filter graph. */
    outputs->name = av_strdup("in");
    outputs->filter_ctx = buffersrc_ctx;
    outputs->pad_idx = 0;
    outputs->next = NULL;

    inputs->name = av_strdup("out");
    inputs->filter_ctx = buffersink_ctx;
    inputs->pad_idx = 0;
    inputs->next = NULL;

    // avfilter_link(buffersrc_ctx, 0, buffersink_ctx, 0);

    if ((ret = avfilter_graph_parse_ptr(filter_graph, filters_descr,
                                        &inputs, &outputs, NULL)) < 0) {
        logd("Cannot avfilter_graph_parse_ptr\n")
        return;
    }

    if ((ret = avfilter_graph_config(filter_graph, NULL)) < 0) {
        logd("Cannot avfilter_graph_config\n");
        return;
    }

    //added by ws for AVfilter start------------init AVfilter------------------------------ws

    // Find the decoder for the video stream
    AVCodec *pCodec = avcodec_find_decoder(avCodecContext->codec_id);
    if (pCodec == NULL) {
        logd("Codec not found.");
        return; // Codec not found
    }

    if (avcodec_open2(avCodecContext, pCodec, NULL) < 0) {
        logd("Could not open codec.");
        return; // Could not open codec
    }

    // 获取native window
    ANativeWindow *nativeWindow = ANativeWindow_fromSurface(env, surface);

    // 获取视频宽高
    int videoWidth = avCodecContext->width;
    int videoHeight = avCodecContext->height;

    // 设置native window的buffer大小,可自动拉伸
    ANativeWindow_setBuffersGeometry(nativeWindow, videoWidth, videoHeight,
                                     WINDOW_FORMAT_RGBA_8888);
    ANativeWindow_Buffer windowBuffer;

    if (avcodec_open2(avCodecContext, pCodec, NULL) < 0) {
        logd("Could not open codec.");
        return; // Could not open codec
    }

    // Allocate video frame
    AVFrame *pFrame = av_frame_alloc();

    // 用于渲染
    AVFrame *pFrameRGBA = av_frame_alloc();
    if (pFrameRGBA == NULL || pFrame == NULL) {
        logd("Could not allocate video frame.");
        return;
    }

    // Determine required buffer size and allocate buffer
    // buffer中数据就是用于渲染的,且格式为RGBA
    int numBytes = av_image_get_buffer_size(AV_PIX_FMT_RGBA, avCodecContext->width, avCodecContext->height,
                                            1);
    uint8_t *buffer = (uint8_t *) av_malloc(numBytes * sizeof(uint8_t));
    av_image_fill_arrays(pFrameRGBA->data, pFrameRGBA->linesize, buffer, AV_PIX_FMT_RGBA,
                         avCodecContext->width, avCodecContext->height, 1);

    // 由于解码出来的帧格式不是RGBA的,在渲染之前需要进行格式转换
    struct SwsContext *sws_ctx = sws_getContext(avCodecContext->width,
                                                avCodecContext->height,
                                                avCodecContext->pix_fmt,
                                                avCodecContext->width,
                                                avCodecContext->height,
                                                AV_PIX_FMT_RGBA,
                                                SWS_BILINEAR,
                                                NULL,
                                                NULL,
                                                NULL);

    int frameFinished;
    AVPacket packet;
    while (av_read_frame(avFormatContext, &packet) >= 0) {
        // Is this a packet from the video stream?
        if (packet.stream_index == target_video_index) {

            // Decode video frame
            avcodec_decode_video2(avCodecContext, pFrame, &frameFinished, &packet);



            // 并不是decode一次就可解码出一帧
            if (frameFinished) {

                //added by ws for AVfilter start
                pFrame->pts = av_frame_get_best_effort_timestamp(pFrame);

                //* push the decoded frame into the filtergraph
                if (av_buffersrc_add_frame(buffersrc_ctx, pFrame) < 0) {
                    logd("Could not av_buffersrc_add_frame");
                    break;
                }

                ret = av_buffersink_get_frame(buffersink_ctx, pFrame);
                if (ret < 0) {
                    logd("Could not av_buffersink_get_frame");
                    break;
                }
                //added by ws for AVfilter end

                // lock native window buffer
                ANativeWindow_lock(nativeWindow, &windowBuffer, 0);

                // 格式转换
                sws_scale(sws_ctx, (uint8_t const *const *) pFrame->data,
                          pFrame->linesize, 0, avCodecContext->height,
                          pFrameRGBA->data, pFrameRGBA->linesize);

                // 获取stride
                uint8_t *dst = (uint8_t *) windowBuffer.bits;
                int dstStride = windowBuffer.stride * 4;
                uint8_t *src = (pFrameRGBA->data[0]);
                int srcStride = pFrameRGBA->linesize[0];

                // 由于window的stride和帧的stride不同,因此需要逐行复制
                int h;
                for (h = 0; h < videoHeight; h++) {
                    memcpy(dst + h * dstStride, src + h * srcStride, srcStride);
                }

                ANativeWindow_unlockAndPost(nativeWindow);
            }

        }
        av_packet_unref(&packet);
    }

    av_free(buffer);
    av_free(pFrameRGBA);

    // Free the YUV frame
    av_free(pFrame);

    avfilter_graph_free(&filter_graph); //added by ws for avfilter
    // Close the codecs
    avcodec_close(avCodecContext);

    // Close the video file
    avformat_close_input(&avFormatContext);
    return;
}

void openVideo(jstring path) {

}