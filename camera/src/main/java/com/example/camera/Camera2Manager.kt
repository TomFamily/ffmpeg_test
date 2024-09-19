package com.example.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.*
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.nio.ByteBuffer
import java.util.concurrent.LinkedBlockingQueue

/**
 *Created by arno.yang
 *Created on 2024/5/28 10:26
 *PackageName com.example.jni_test.camera
 *
 * mediaDeCodec + imageReader + camera2 + surfaceView
 */
class Camera2Manager : CameraDevice.StateCallback(), ImageReader.OnImageAvailableListener {
    companion object {
        private const val TAG = "Camera2Manager"
        private const val VIDEO_WIDTH = 640
        private const val VIDEO_HEIGHT = 480
        private const val KEY_BIT_RATE = 1024 * 1024 * 4
        private const val DECODECRHANDLER_MSG = 11
    }

    private lateinit var mCameraDevice: CameraDevice
    private lateinit var surfaceViewRef: WeakReference<SurfaceView>
    private lateinit var deSurfaceViewRef: WeakReference<SurfaceView>
    private lateinit var imageReader: ImageReader
    private lateinit var handler: Handler
    private lateinit var mSession: CameraCaptureSession
    private lateinit var mediaCodec: MediaCodec
    private val queue by lazy { LinkedBlockingQueue<Pair<ByteBuffer, MediaCodec.BufferInfo>>(3) }
    private lateinit var mFileOutputStream: FileOutputStream
    private val format =
        MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, VIDEO_WIDTH, VIDEO_HEIGHT).also { format ->
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            format.setInteger(MediaFormat.KEY_BIT_RATE, KEY_BIT_RATE)
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5)
        }

    fun startPreview(surfaceView: SurfaceView, deCodecSurfaceView: SurfaceView, context: Context) {
        Log.d(TAG, "startPreview: ")
        surfaceViewRef = WeakReference(surfaceView)
        deSurfaceViewRef = WeakReference(deCodecSurfaceView)
        if (surfaceView.holder.surface.isValid) {
            initCamera(context, surfaceView)
            return
        }
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback2 {
            override fun surfaceCreated(holder: SurfaceHolder) {
                Log.d(TAG, "surfaceCreated: ")
                initCamera(context, surfaceView)
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) = Unit
            override fun surfaceDestroyed(holder: SurfaceHolder) = Unit
            override fun surfaceRedrawNeeded(holder: SurfaceHolder) = Unit

        })
    }

    @SuppressLint("MissingPermission")
    private fun initCamera(context: Context, surfaceView: SurfaceView) {
        Log.d(TAG, "initCamera: ${surfaceView.width} ${surfaceView.height}")
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        //建立数据传输的桥梁
        imageReader = ImageReader.newInstance(
            surfaceView.width, surfaceView.height, ImageFormat.YUV_420_888, 2
        )
        val handlerThread = HandlerThread("Camera2Manager")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
        imageReader.setOnImageAvailableListener(this, handler)
        //真正地开启摄像头
        cameraManager.openCamera("0", this, handler)
    }

    override fun onOpened(camera: CameraDevice) {
        Log.d(TAG, "onOpened: ")
        mCameraDevice = camera
        createPreviewSession()
    }

    private fun createPreviewSession() {
        if (!::surfaceViewRef.isInitialized) return

        val codecSurface: Surface = initMediaCodec()
        // 因为摄像头设备可以同时输出多个流，所以可以传入多个surface
        val targets = listOf(surfaceViewRef.get()!!.holder.surface, imageReader.surface, codecSurface)
        mCameraDevice.createCaptureSession(targets, object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(captureSession: CameraCaptureSession) {
                mSession = captureSession

                val captureRequest = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                    addTarget(surfaceViewRef.get()!!.holder.surface)
                    addTarget(imageReader.surface)
                    addTarget(codecSurface)
                }

                mSession.setRepeatingRequest(captureRequest.build(), null, handler)
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {

            }
        }, handler)
    }

    private fun initMediaCodec(): Surface {
        mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        val mEncoderHandlerThread = HandlerThread("MediaCodecHardwareEncoderThread")
        mEncoderHandlerThread.start()
        val mEncoderHandler = Handler(mEncoderHandlerThread.looper)
        mediaCodec.setCallback(object : MediaCodec.Callback() {
            override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
            }

            override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
                mediaCodec.getOutputBuffer(index)?.let {
                    Log.d(TAG, "onOutputBufferAvailable: ${it.capacity()} ${info.size} ${info.flags}")
                    if (queue.remainingCapacity() <= 0) queue.poll()
                    queue.add(it to info)
                    // saveData(it, info)
                    deCodecData(it, info)
                }
                mediaCodec.releaseOutputBuffer(index, false)
            }

            override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {

            }

            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {

            }

        }, mEncoderHandler)
        mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val surface = mediaCodec.createInputSurface()
        mediaCodec.start()

        return surface
    }

    override fun onDisconnected(camera: CameraDevice) = Unit

    override fun onError(camera: CameraDevice, error: Int) {
        Log.d(TAG, "onError: $camera $error")
    }

    override fun onImageAvailable(reader: ImageReader?) {
        val image: Image = reader!!.acquireLatestImage()
        // Log.d(TAG, "onImageAvailable: ${image.width} ${image.height} ")
        image.close()
    }

    //<editor-fold desc="保存数据到文件">
    private fun saveData(bytes: ByteBuffer, info: MediaCodec.BufferInfo) {
        if (!::mFileOutputStream.isInitialized) initFile()

        val byteArray = ByteArray(info.size)
        bytes.get(byteArray)
        mFileOutputStream.write(byteArray)
        mFileOutputStream.flush()
    }

    private fun initFile() {
        val file = File("/storage/emulated/0/Android/data/com.example.jni_test/files/encoder3.h264")
        if (!file.exists()) {
            file.createNewFile()
        } else {
            file.delete()
            file.createNewFile()
        }
        mFileOutputStream = FileOutputStream(file)
    }
    //</editor-fold>

    private lateinit var mediaDeCodec2: MediaCodec
    private fun deCodecData(byteBuffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        if (!::deSurfaceViewRef.isInitialized || deSurfaceViewRef.get() == null) return
        if (!::mediaDeCodec2.isInitialized) initMediaDeCodec()

        Message.obtain().also {
            it.what = DECODECRHANDLER_MSG
            val byteArray = ByteArray(info.size)
            byteBuffer.get(byteArray)
            it.obj = byteArray to info
            decodecrHandler.sendMessage(it)
        }
    }

    private lateinit var decodecrHandler: Handler
    private fun initMediaDeCodec() {
        Log.d(TAG, "initMediaDeCodec: ${deSurfaceViewRef.get()!!.holder.surface.isValid}")
        mediaDeCodec2 = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        mediaDeCodec2.configure(format, deSurfaceViewRef.get()!!.holder.surface, null, 0)
        mediaDeCodec2.start()
        val handlerThread = HandlerThread("Camera2Manager2")
        handlerThread.start()
        decodecrHandler = MyHandler(handlerThread.looper)

    }

    inner class MyHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                DECODECRHANDLER_MSG -> {
                    val data = msg.obj as Pair<ByteArray, MediaCodec.BufferInfo>
                    val byteArr = data.first
                    val info = data.second
                    val index = mediaDeCodec2.dequeueInputBuffer(100)
                    Log.d(TAG, "deCodecData: $index ${byteArr.size}")

                    if (index >= 0) {
                        val bf = mediaDeCodec2.getInputBuffer(index)
                        bf?.put(byteArr, 0, info.size)
                        mediaDeCodec2.queueInputBuffer(index, 0, info.size, info.presentationTimeUs, info.flags)

                        releaseOutputBuffer()
                    }
                }
            }
        }

        private fun releaseOutputBuffer() {
            val info = MediaCodec.BufferInfo()
            var flag = true
            var count = 0
            while (flag && count < 20) {
                try {
                    count++
                    val outIndex = mediaDeCodec2.dequeueOutputBuffer(info, 1000L)
                    if (outIndex >= 0) {
                        flag = false
                        mediaDeCodec2.releaseOutputBuffer(outIndex, true)
                    }
                } catch (e: IllegalStateException) {
                    Log.e(TAG, "dequeueOutputBuffer $e")
                    flag = false
                }
            }
        }
    }
}