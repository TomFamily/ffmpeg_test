package com.android.opengl.watermark.triangle;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by aserbao on 2018 2018/1/16.23:15
 * Email:aserbao@163.com
 * weixin: aserbao
 */

public class OneGlRenderer implements GLSurfaceView.Renderer {
    private Triangle mTriangle;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        // initialize a triangle
        mTriangle = new Triangle();
    }
    private float[] mRotationMatrix = new float[16];
    public volatile float mAngle;
    public volatile float mTranslationX;
    public volatile float mTranslationY;

    public float getAngle() {
        return mAngle;
    }

    public void setAngle(float angle) {
        mAngle = angle;
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        // 设置相机位置（查看矩阵）
       Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // 计算投影和视图变换
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        float[] scratch = new float[16];
        // 为三角形创建一个旋转变换
//        long time = SystemClock.uptimeMillis() % 4000L;
//        float angle = 9f * ((int) time);
        Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, -1.0f);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);
        // 绘制形状
//        mTriangle.draw(scratch);

//        float x = mTranslationX / (float) AserbaoApplication.screenWidth;
//        float y = mTranslationY / (float) AserbaoApplication.screenHeight;
//        Matrix.translateM(scratch,0,0.5f,0f,0);
//        Matrix.translateM(scratch,0,x,-y,0);
//        Log.e("Matrix.translateM", "onDrawFrame: x=" +x + " y="+ y  );
        mTriangle.draw(scratch);
    }

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
//    private final float[] mMVPMatrix = new float[16];
    private final float[] mMVPMatrix = new float[]{
        1,0,0,0,
        0,1,0,0,
        0,0,1,0,
        0,0,0,1
    };
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // 这个投影矩阵被应用于对象坐标在 onDrawFrame（）方法中
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }
}
