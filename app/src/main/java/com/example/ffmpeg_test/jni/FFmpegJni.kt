package com.example.ffmpeg_test.jni

import android.view.Surface

class FFmpegJni {
    companion object {
        init {
            // 下面的都是 ffmpeg 的依赖库，这里的库是有顺序要求的
//            System.loadLibrary("avutil-54")
//            System.loadLibrary("swresample-1")
//            System.loadLibrary("avcodec-56")
//            System.loadLibrary("avformat-56")
//            System.loadLibrary("swscale-3")
//            System.loadLibrary("postproc-53")
//            System.loadLibrary("avfilter-5")
//            System.loadLibrary("avdevice-56")

            // 这个是我们自己 CMakeLists.txt 脚本编译生成的库
            System.loadLibrary("ffmpeg_test")
        }
    }

    external fun initConfig(input: String): String
    external fun playVideo(path: String, surface: Surface): String
}