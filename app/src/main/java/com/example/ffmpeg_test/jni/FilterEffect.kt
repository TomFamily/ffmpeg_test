package com.example.ffmpeg_test.jni

/**
 * 滤镜效果
 */
enum class FilterEffect(val value: String) {
    BLACK_AND_WHITE("lutyuv='u=128:v=128'"),
    GREEN("hue='h=60:s=-3'" ),
    DRAW_RECT("drawbox=x=200:y=200:w=300:h=300:color=pink@0.5"),
    DRAW_RECTS("drawgrid=width=100:height=100:thickness=4:color=pink@0.9"),

    // TODO: 无法添加文字水印，表达式应该是没问题的，可能是编译的 SO库 有问题
    DRAW_TEXT33("[in]drawtext=text='Hello, World':fontsize=24:fontcolor=white:x=10:y=10[out]"),
    DRAW_TEXT4("drawtext=fontcolor=green:fontsize=30:text='Hello world'"),
    // DRAW_TEXT4("drawtext=fontfile='arial.ttf':fontcolor=green:fontsize=30:text='Hello world'"),

    /**
     * 水印：
     * 图片位置：/storage/emulated/0/ws.jpg
     * overlay=5:5：这个命令是将水印图片放在视频的哪个位置。在这个例子中，水印图片被放在视频的第5行和第5列（从0开始计数）的位置
     */
    WATER_MARK("movie=/storage/emulated/0/water_mark.png[wm];[in][wm]overlay=115:115[out]"),

    /**
     * 锐化
     */
    SHARPENING("unsharp=5:5:1.0:5:5:0.0"),
    filters_descr1("hflip"),
    filters_descr2("hue='h=60:s=-3'"),
}