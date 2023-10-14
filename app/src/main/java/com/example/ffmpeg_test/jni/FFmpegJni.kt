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
    external fun playVideoWithFilter(path: String, surface: Surface, filterDescription: String)
}

/**
 * 滤镜效果
 */
enum class FilterEffect(val value: String) {
    BLACK_AND_WHITE("lutyuv='u=128:v=128'"),
    GREEN("hue='h=60:s=-3'" ),
    DRAW_RECT("drawbox=x=200:y=200:w=300:h=300:color=pink@0.5"),
    DRAW_RECTS("drawgrid=width=100:height=100:thickness=4:color=pink@0.9"),

    /**
     * 水印：
     * 图片位置：/storage/emulated/0/ws.jpg
     */
    WATER_MARK("movie=/storage/emulated/0/water_mark.png[wm];[in][wm]overlay=5:5[out]"),

    /**
     * 锐化
     */
    SHARPENING("unsharp=5:5:1.0:5:5:0.0")
}