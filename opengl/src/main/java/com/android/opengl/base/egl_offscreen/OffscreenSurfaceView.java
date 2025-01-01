package com.android.opengl.base.egl_offscreen;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLContext;

public class OffscreenSurfaceView implements MyCallback {
    private static final String TAG = "OffscreenSurfaceView";
    private EGLContext eglContext;
    private YEGLThread yEGLThread;
    private OffscreenGLRender yGLRender;
    public final static int RENDERMODE_WHEN_DIRTY = 0;//手动刷新
    public final static int RENDERMODE_CONTINUOUSLY = 1;//自动刷新

    private int mRenderMode = RENDERMODE_CONTINUOUSLY;

    public OffscreenSurfaceView(Context context) {
        this(context, null);
    }

    public OffscreenSurfaceView(Context context, AttributeSet attrs) {
    }

    public void setRender(OffscreenGLRender yRender) {
        this.yGLRender = yRender;
    }

    public void setRenderMode(int mRenderMode) {
        if (yGLRender == null) {
            throw new RuntimeException("must set render before set RenderMode");
        }
        this.mRenderMode = mRenderMode;
    }

    //添加设置Surface和EglContext的方法
    public void setSurfaceAndEglContext(EGLContext eglContext) {
        this.eglContext = eglContext;
    }

    public EGLContext getEglContext() {
        if (yEGLThread != null) {
            return yEGLThread.getEglContext();
        }
        return null;
    }

    public void requestRender() {
        if (yEGLThread != null) {
            yEGLThread.requestRender();
        }
    }


    @Override
    public void surfaceCreated() {
        yEGLThread = new YEGLThread(new WeakReference<OffscreenSurfaceView>(this));
        yEGLThread.isCreate = true;
        yEGLThread.start();
    }

    @Override
    public void surfaceChanged(int width, int height) {
        yEGLThread.width = width;
        yEGLThread.height = height;
        yEGLThread.isChange = true;

    }

    @Override
    public void surfaceDestroyed() {
        yEGLThread.onDestroy();
        yEGLThread = null;
        eglContext = null;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Log.d(TAG, "finalize: ");
    }

    public interface OffscreenGLRender {
        void onSurfaceCreated();

        void onSurfaceChanged(int width, int height);

        void onDrawFrame();

        void surfaceDestroyed();
    }

    static class YEGLThread extends Thread {

        private WeakReference<OffscreenSurfaceView> yGlSurfaceViewWeakReference;
        private EglHelper eglHelper = null;
        private Object object = null;

        private boolean isExit = false;
        private boolean isCreate = false;
        private boolean isChange = false;
        private boolean isStart = false;

        private int width;
        private int height;

        public YEGLThread(WeakReference<OffscreenSurfaceView> yglSurfaceViewWeakReference) {
            this.yGlSurfaceViewWeakReference = yglSurfaceViewWeakReference;
        }

        @Override
        public void run() {
            super.run();
            isExit = false;
            isStart = false;
            object = new Object();
            eglHelper = new EglHelper();
            eglHelper.initEglOffscreen(yGlSurfaceViewWeakReference.get().eglContext);

            while (true) {
                if (isExit) {
                    //释放资源
                    release();
                    break;
                }
                if (isStart) {
                    if (yGlSurfaceViewWeakReference.get().mRenderMode == RENDERMODE_WHEN_DIRTY) {
                        synchronized (object) {
                            try {
                                object.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (yGlSurfaceViewWeakReference.get().mRenderMode == RENDERMODE_CONTINUOUSLY) {
                        try {
                            Thread.sleep(1000 / 100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        throw new RuntimeException("mRenderMode is wrong value");
                    }
                }
                onCreate();
                onChange(width, height);
                onDraw();
                isStart = true;
            }
        }

        private void onCreate() {
            if (isCreate && yGlSurfaceViewWeakReference.get().yGLRender != null) {
                isCreate = false;
                yGlSurfaceViewWeakReference.get().yGLRender.onSurfaceCreated();
            }
        }

        private void onChange(int width, int height) {
            if (isChange && yGlSurfaceViewWeakReference.get().yGLRender != null) {
                isChange = false;
                yGlSurfaceViewWeakReference.get().yGLRender.onSurfaceChanged(width, height);
            }
        }

        private void onDraw() {
            if (yGlSurfaceViewWeakReference.get().yGLRender != null && eglHelper != null) {
                yGlSurfaceViewWeakReference.get().yGLRender.onDrawFrame();
                if (!isStart) {
                    yGlSurfaceViewWeakReference.get().yGLRender.onDrawFrame();
                }
                eglHelper.swapBuffers();
            }
        }

        private void requestRender() {
            if (object != null) {
                synchronized (object) {
                    object.notifyAll();
                }
            }
        }

        public void onDestroy() {
            Log.d(TAG, "onDestroy: ");
            isExit = true;
            requestRender();
            yGlSurfaceViewWeakReference.get().yGLRender.surfaceDestroyed();
            interrupt();
        }

        public void release() {
            if (eglHelper != null) {
                eglHelper.destroyEgl();
                eglHelper = null;
                object = null;
                yGlSurfaceViewWeakReference = null;
            }
        }

        public EGLContext getEglContext() {
            if (eglHelper != null) {
                return eglHelper.getEglContext();
            }
            return null;
        }
    }
}
