package com.example.camera.render;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.TextureView;

import com.android.filter.FilterEngine;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * EGL是OpenGL ES与设备的系统屏幕进行通信的桥梁，因为TextureView是没有任何OpenGL ES相关的环境的，
 * 而上篇文章讲的GLSurfaceView是封装好了OpenGL ES相关的环境，包括EGL环境。当OpenGL ES需要绘制图像时，
 * 会找到EGL的EGLSurface，通过此对象请求SurfaceFlinger返回系统屏幕的图形访问接口，这个接口也就是屏幕的帧缓冲区，
 * 这样OpenGL就可以将图像渲染到屏幕的帧缓冲区中。
 */
public class CameraV1GLRenderer implements SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = "CameraV1GLRenderer";
    private Context mContext;
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private TextureView mTextureView;
    private int mOESTextureId;
    private FilterEngine mFilterEngine;
    private FloatBuffer mDataBuffer;
    private int mShaderProgram = -1;
    private float[] transformMatrix = new float[16];

    private EGL10 mEgl = null;
    private EGLDisplay mEGLDisplay = EGL10.EGL_NO_DISPLAY;
    private EGLContext mEGLContext = EGL10.EGL_NO_CONTEXT;
    private EGLConfig[] mEGLConfig = new EGLConfig[1];
    private EGLSurface mEglSurface;

    private static final int MSG_INIT = 1;
    private static final int MSG_RENDER = 2;
    private static final int MSG_DEINIT = 3;
    private SurfaceTexture mOESSurfaceTexture;

    public void init(TextureView textureView, int oesTextureId, Context context) {
        mContext = context;
        mTextureView = textureView;
        mOESTextureId = oesTextureId;
        mHandlerThread = new HandlerThread("Renderer Thread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_INIT:
                        initEGL();
                        return;
                    case MSG_RENDER:
                        drawFrame();
                        return;
                    case MSG_DEINIT:
                        return;
                    default:
                        return;
                }
            }
        };
        mHandler.sendEmptyMessage(MSG_INIT);
    }

    private void initEGL() {
        mEgl = (EGL10) EGLContext.getEGL();

        //获取显示设备
        mEGLDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        if (mEGLDisplay == EGL10.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed! " + mEgl.eglGetError());
        }

        //version中存放EGL版本号
        int[] version = new int[2];

        //初始化EGL
        if (!mEgl.eglInitialize(mEGLDisplay, version)) {
            throw new RuntimeException("eglInitialize failed! " + mEgl.eglGetError());
        }

        //构造需要的配置列表
        int[] attributes = {
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE,8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_BUFFER_SIZE, 32,
                EGL10.EGL_RENDERABLE_TYPE, 4,
                EGL10.EGL_SURFACE_TYPE, EGL10.EGL_WINDOW_BIT,
                EGL10.EGL_NONE
        };
        int[] configsNum = new int[1];

        //EGL选择配置
        if (!mEgl.eglChooseConfig(mEGLDisplay, attributes, mEGLConfig, 1, configsNum)) {
            throw new RuntimeException("eglChooseConfig failed! " + mEgl.eglGetError());
        }
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        if (surfaceTexture == null)
            return;

        //创建EGL显示窗口
        mEglSurface = mEgl.eglCreateWindowSurface(mEGLDisplay, mEGLConfig[0], surfaceTexture, null);

        //创建上下文
        int[] contextAttribs = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION,
                2,
                EGL10.EGL_NONE
        };
        mEGLContext = mEgl.eglCreateContext(mEGLDisplay, mEGLConfig[0], EGL10.EGL_NO_CONTEXT, contextAttribs);

        if (mEGLDisplay == EGL10.EGL_NO_DISPLAY || mEGLContext == EGL10.EGL_NO_CONTEXT){
            throw new RuntimeException("eglCreateContext fail failed! " + mEgl.eglGetError());
        }

        /**
         * eglMakeCurrent: 主要用于将当前的绘图上下文和表面绑定到当前线程。
             EGLDisplay：表示 EGL 的显示连接。
             EGLSurface：要绑定的渲染表面。
             EGLSurface：用于绘制的目标表面，通常与前一个参数相同。
             EGLContext：要绑定的上下文。
         */
        if (!mEgl.eglMakeCurrent(mEGLDisplay,mEglSurface, mEglSurface, mEGLContext)) {
            throw new RuntimeException("eglMakeCurrent failed! " + mEgl.eglGetError());
        }

        mFilterEngine = new FilterEngine(mOESTextureId, mContext);
        mDataBuffer = mFilterEngine.getPointBuffer();
        mShaderProgram = mFilterEngine.getShaderProgram();
    }

    private void drawFrame() {
        long t1, t2;
        t1 = System.currentTimeMillis();
        if (mOESSurfaceTexture != null) {
            mOESSurfaceTexture.updateTexImage();
            mOESSurfaceTexture.getTransformMatrix(transformMatrix);
        }
        mEgl.eglMakeCurrent(mEGLDisplay, mEglSurface, mEglSurface, mEGLContext);
        GLES20.glViewport(0,0,mTextureView.getWidth(),mTextureView.getHeight());
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1f, 1f, 0f, 0f);
        mFilterEngine.drawTexture(transformMatrix);

        /**
         * eglSwapBuffers 的主要作用是将后缓冲区的内容（即最近一次渲染的结果）显示到屏幕上，并准备好一个新的后缓冲区，以便下次渲染。
             EGLDisplay display：表示与显示设备的连接。
             EGLSurface surface：要交换的缓冲区所在的表面，通常是一个窗口或帧缓冲对象。
         *
         * 工作原理：
             双缓冲技术：使用两个缓冲区（前缓冲区和后缓冲区）来避免屏幕撕裂和闪烁。应用程序在后缓冲区中进行所有绘制操作，而前缓冲区则负责显示内容。
             交换过程：当调用 eglSwapBuffers 时，EGL 会将当前的后缓冲区与前缓冲区进行交换，确保用户看到的是最新渲染的图像。
         */
        mEgl.eglSwapBuffers(mEGLDisplay, mEglSurface);
        t2 = System.currentTimeMillis();
        Log.i(TAG, "drawFrame: time = " + (t2 - t1));
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if (mHandler != null) {
            mHandler.sendEmptyMessage(MSG_RENDER);
        }
    }

    public SurfaceTexture initOESTexture() {
        mOESSurfaceTexture = new SurfaceTexture(mOESTextureId);
        mOESSurfaceTexture.setOnFrameAvailableListener(this);
        return mOESSurfaceTexture;
    }
}
