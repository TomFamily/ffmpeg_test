package com.android.airhockey;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// 参考文章： https://blog.csdn.net/chong_lai/article/details/123610160

public class OpenGlActivity extends AppCompatActivity {

    public static final String TAG ="AirHockey" ;
    private boolean rendererSet = false ;

    private GLSurfaceView glSurfaceView ;

    final AirHockeyRenderer airHockeyRenderer = new AirHockeyRenderer(this) ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        glSurfaceView = new GLSurfaceView(this) ;

        boolean isSupportEs2 = isSupportEs2() ;
        Log.d(TAG ,"isSupportEs2:"+isSupportEs2) ;
        if(isSupportEs2){
            glSurfaceView.setEGLContextClientVersion(2);
            glSurfaceView.setRenderer(airHockeyRenderer);

            glSurfaceView.setOnTouchListener((v, event) -> {
                if(event != null){
                    final float normalizedX = (event.getX() / v.getWidth()) * 2 - 1 ;
                    final float normalizedY = -( (event.getY() / (float) v.getHeight()) * 2 - 1 ) ;
                    if(event.getAction() == MotionEvent.ACTION_DOWN){
                        glSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                airHockeyRenderer.handleTouchPress(normalizedX , normalizedY) ;
                            }
                        });
                    }else if(event.getAction() == MotionEvent.ACTION_MOVE){
                        glSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                airHockeyRenderer.handleTouchDrag(normalizedX ,normalizedY) ;
                            }
                        });
                    }
                    return true ;
                }else{
                    return false ;
                }
            });
            setContentView(glSurfaceView);
            rendererSet = true ;

        }else{
            Toast.makeText(this ,"The device does not support es2 ." ,Toast.LENGTH_SHORT).show();
            rendererSet = false ;
            return;
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        if(rendererSet){
            glSurfaceView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(rendererSet){
            glSurfaceView.onPause();
        }
    }

    private boolean isSupportEs2(){
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo() ;
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000 ;
        return supportsEs2 ;
    }
}
