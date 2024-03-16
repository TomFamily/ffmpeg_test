package com.example.pc_client

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.example.pc_client.databinding.ActivityMainBinding

// AIDL简单Demo： https://juejin.cn/post/6844903661403914254
class MainActivity : AppCompatActivity() {

    private var managerInterface: IManagerInterface? = null
    private lateinit var viewBinding: ActivityMainBinding

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d(TAG, "[Client] onServiceConnected success :" + Thread.currentThread().name)
            managerInterface = IManagerInterface.Stub.asInterface(service)
            try {
                managerInterface?.setCallBack(callbackInterface)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            unbindService(this)
            Log.d(TAG, "[Client] onServiceConnected fail  :" + Thread.currentThread().name)
        }
    }

    private val callbackInterface: ICallbacklInterface = object : ICallbacklInterface.Stub() {
        @Throws(RemoteException::class)
        override fun call() {
            Log.d(TAG, "[Client] server call client :" + Thread.currentThread().name)
        }

        override fun basicTypes(
            anInt: Int,
            aLong: Long,
            aBoolean: Boolean,
            aFloat: Float,
            aDouble: Double,
            aString: String?
        ) {
            Log.d(TAG, "basicTypes: $anInt")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(viewBinding.root)

        initEvent()
    }

    private fun initEvent() {
        viewBinding.mainBtnBindService.setOnClickListener {
            val intent = Intent().apply {
                setClassName("com.example.ffmpeg_test", "com.example.pc.service.BookManagerService")
            }
            bindService(intent, serviceConnection, BIND_AUTO_CREATE).also {
                Log.d(TAG, "bindService: $it ")
            }
        }

        viewBinding.mainBtnUnbindService.setOnClickListener {
            try {
                unbindService(serviceConnection)
            } catch (e: Exception) {
                Log.d(TAG, "initEvent: $e")
            }
        }

        viewBinding.mainBtnSendData.setOnClickListener {
            if (managerInterface?.asBinder()?.isBinderAlive == true) {
                managerInterface?.test()
            } else {
                Log.d(TAG, "initEvent: isAlive ${managerInterface?.asBinder()?.isBinderAlive}")
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity2"
    }
}