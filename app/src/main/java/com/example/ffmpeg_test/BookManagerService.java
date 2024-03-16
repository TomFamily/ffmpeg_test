package com.example.ffmpeg_test;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import androidx.annotation.Nullable;
import com.example.pc_client.ICallbacklInterface;
import com.example.pc_client.IManagerInterface;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by duanyuanjin
 * on 2018/8/3.
 */
public class BookManagerService extends Service {

    private static final String TAG = "BookManagerService";

    private ICallbacklInterface callbackInterface;

    private final Binder mBinder = new IManagerInterface.Stub() {
        @Override
        public void test() {
            Log.d(TAG, "[Server] client call server test:" + Thread.currentThread().getName());
        }
        @Override
        public void setCallBack(ICallbacklInterface callback) {
            Log.d(TAG, "setCallBack: ");
            callbackInterface = callback;
        }

        @Override
        public void basicTypes2(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {
            Log.d(TAG, "basicTypes2: " + anInt);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "[Server] [onBind] :" + Thread.currentThread().getName());
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "run: ");
                try {
                    if (callbackInterface != null) {
                        callbackInterface.call();
                        callbackInterface.basicTypes(1,1L, true, 1f, 1.0, "1");
                    }
                } catch (Exception e) {
                    Log.d(TAG, "onBind: " + e);
                    throw new RuntimeException(e);
                }
            }
        }, 0, 2000);
        return mBinder;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "[Server] [onCreate]");
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(TAG, "[Server] [onStart] startId:" + startId);
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "[Server] [onStartCommand] startId:" + startId + ",flags:" + flags);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "[Server] [onDestroy]");
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "[Server] [onUnbind]");
        return super.onUnbind(intent);
    }
}
