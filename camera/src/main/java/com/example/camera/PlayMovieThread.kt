package com.example.camera

import android.view.Choreographer

class PlayMovieThread: Thread(TAG) {

    companion object {
        private const val TAG = "PlayMovieThread"
    }
}

//class MoviePlayCallback(): Choreographer.FrameCallback {
//    override fun doFrame(frameTimeNanos: Long) {
//
//    }
//
//}