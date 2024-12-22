package com.android.opengl.watermark.withCamera.java;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraInterface {
    private Camera mCamera;
    private Camera.Parameters mParams;
    private boolean isPreviewing = false;
    private static CameraInterface mCameraInterface;

    private CameraInterface(){
    }
    public static synchronized CameraInterface getInstance(){
        if(mCameraInterface == null){
            mCameraInterface = new CameraInterface();
        }
        return mCameraInterface;
    }
    //打开相机
    public void doOpenCamera(){
        if(mCamera == null){
            mCamera = Camera.open();
        }else{
            doStopCamera();
        }
    }
    /*使用TextureView预览Camera*/
    public void doStartPreview(SurfaceTexture surface){
        if(isPreviewing){
            mCamera.stopPreview();
            return;
        }
        if(mCamera != null){
            try {
                //将相机画面预览到纹理层上,纹理层有数据了，再通知view绘制,此时未开始预览
                mCamera.setPreviewTexture(surface);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //真正开启预览,Camera.startPrieView()
            initCamera();
        }
    }

    /**
     * 停止预览，释放Camera
     */
    public void doStopCamera(){
        if(null != mCamera)
        {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            isPreviewing = false;
            mCamera.release();
            mCamera = null;
        }
    }
    /**
     * 拍照
     */
    public void doTakePicture(){
        if(isPreviewing && (mCamera != null)){
            mCamera.takePicture(mShutterCallback, null, mJpegPictureCallback);
        }
    }

    public boolean isPreviewing(){
        return isPreviewing;
    }

    private void initCamera(){
        if(mCamera != null){
            mParams = mCamera.getParameters();
            mParams.setPictureFormat(PixelFormat.JPEG);//设置拍照后存储的图片格式
            mCamera.setDisplayOrientation(90);
            //设置摄像头为持续自动聚焦模式
            mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            mCamera.setParameters(mParams);
            mCamera.startPreview();//开启预览
            //设置预览标志位
            isPreviewing = true;
        }
    }

    Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback()
            //如果这个参数设置为Null,将没有卡擦的声音
    {
        public void onShutter() {
        }
    };

    Camera.PictureCallback mJpegPictureCallback = new Camera.PictureCallback()
            //对jpeg图像数据的回调,最重要的一个回调
    {
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap b = null;
            if(null != data){
                b = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
                mCamera.stopPreview();
                isPreviewing = false;
            }
            //保存图片到sdcard
            if(null != b)
            {
                //图片这里要旋转下,相机拍出来的照片是倒着的
                Bitmap rotaBitmap = getRotateBitmap(b, 90.0f);
                saveBitmap(rotaBitmap);
            }
            //再次进入预览
            mCamera.startPreview();
            isPreviewing = true;
        }

    };

    //旋转图片
    private Bitmap getRotateBitmap(Bitmap b, float rotateDegree){
        Matrix matrix = new Matrix();
        matrix.postRotate((float)rotateDegree);
        return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, false);
    }
    private static String initPath(){
        String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/" + "PlayCamera";
        File f = new File(storagePath);
        if(!f.exists()){
            f.mkdir();
        }
        return storagePath;
    }
    private void saveBitmap(Bitmap b){
        String path = initPath();
        String jpegName = path + "/" + System.currentTimeMillis() +".jpg";
        try {
            BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream(jpegName));
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
