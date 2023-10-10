package com.example.ffmpeg_test

import android.view.Surface


external fun ffmpegInfo(): String

 external fun nativePlayVideo(path: String, surface: Surface)