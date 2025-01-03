package com.android.opengl.watermark.withCamera.java;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = "TAG";
    Context mContext;
    //以OpenGL ES纹理的形式从图像流中捕获帧,我把叫做纹理层
    SurfaceTexture SurfaceTexture;
    //使用的纹理id
    int mTextureID = -1;
    DirectDrawer mDirectDrawer;
    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setEGLContextClientVersion(2);
        setRenderer(this);
        //根据纹理层的监听，有数据就绘制
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //得到view表面的纹理id
        mTextureID = createTextureID();
        //使用这个纹理id得到纹理层SurfaceTexture
        SurfaceTexture = new SurfaceTexture(mTextureID);
        //监听纹理层
        SurfaceTexture.setOnFrameAvailableListener(this);
        mDirectDrawer = new DirectDrawer(mTextureID);
        //打开相机，并未预览
        CameraInterface.getInstance().doOpenCamera();
    }
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        //如果还未预览，就开始预览
        if(!CameraInterface.getInstance().isPreviewing()){
            CameraInterface.getInstance().doStartPreview(SurfaceTexture);
        }
    }
    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        //从图像流中将纹理图像更新为最近的帧
        SurfaceTexture.updateTexImage();
        mDirectDrawer.draw();
    }

    @Override
    public void onPause() {
        super.onPause();
        CameraInterface.getInstance().doStopCamera();
    }

    private int createTextureID() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return texture[0];
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        //回调接口，用于通知新的流帧可用。
        Log.i(TAG, "onFrameAvailable...");
        //纹理层有新数据，就通知view绘制
        this.requestRender();
    }

}
