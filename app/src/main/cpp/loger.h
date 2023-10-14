//
// Created by yk on 2023/10/14.
//

#ifndef FFMPEG_TEST_LOGER_H
#define FFMPEG_TEST_LOGER_H

#ifdef ANDROID

#include <android/log.h>
#include <libavutil/time.h>

#define TAG "FFmpeg_LOG_Tag"
#define logd(FORMAT, ...) __android_log_print(ANDROID_LOG_INFO,TAG,FORMAT,##__VA_ARGS__);
#define loge(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,TAG,FORMAT,##__VA_ARGS__);
#else
#define LOGE(format, ...)  printf(LOG_TAG format "\n", ##__VA_ARGS__)
#define LOGI(format, ...)  printf(LOG_TAG format "\n", ##__VA_ARGS__)
#endif


#endif //FFMPEG_TEST_LOGER_H
