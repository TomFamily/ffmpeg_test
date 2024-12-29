package com.android.opengl.codec

import android.media.MediaFormat
import java.util.concurrent.TimeUnit

/**
 * 是否打印高频log（10Hz 及以上）
 */
const val PRINT_FREQUENT_LOG = false

/**
 * 是否要保存高频log 以及 多少帧保存一次
 */
val SAVE_FREQUENT_DEBUG_LOG = true to 120

/**
 * 编解码线程睡眠的时长
 */
val PROGRESS_SLEEP_INTERVAL = 16L to TimeUnit.MILLISECONDS

const val H264 = MediaFormat.MIMETYPE_VIDEO_AVC
const val H265 = MediaFormat.MIMETYPE_VIDEO_HEVC
val VIDEO_SIZE_NORMAL = 1280 to 720
val VIDEO_SIZE_2K = 1920 to 1080
const val VIDEO_FPS = 30