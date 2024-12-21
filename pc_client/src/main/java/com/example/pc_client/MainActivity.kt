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
import com.android.withCamera.CameraPlayWithOpenglActivity
import com.example.base.handler.HandlerAssistant
import com.example.base.rxjava.testRxjava
import com.example.camera.GLCameraActivity
import com.example.pc_client.aidl.ICallbacklInterface
import com.example.pc_client.aidl.IManagerInterface
import com.example.pc_client.databinding.ActivityMainBinding

// AIDL简单Demo： https://juejin.cn/post/6844903661403914254
/**
 * 数据通讯方式：
 * 客户端 -》 服务端： 1、客户端绑定服务端
 *                  2、绑定成功后，服务端 回给 客户端的 ServiceConnection.onServiceConnected 一个 IBinder
 *                  3、客户端从 IBinder 拿到 IManagerInterface 对象，调用 IManagerInterface 中的方法，实现 客户端 -》服务端 通讯
 *
 * 服务端 -》 客户端：1、给 IManagerInterface 预定一个 set接口（interface2）对象 的方法
 *                 2、客户端实现 interface2 的方法，并通过 IManagerInterface set接口方法 将 interface2 的实现传递给服务端
 *                 3、服务端 通过 set接口方法的实现 拿到 interface2 后，调用 interface2 中的方法，实现 服务端 -》 客户端 的通讯
 */
class MainActivity : AppCompatActivity() {

    private var managerInterface: IManagerInterface? = null
    private lateinit var viewBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(viewBinding.root)

        initEvent()
        testHilt()
        // testRxjava(this)
    }

    private fun testHilt() {
        // startActivity(Intent(this, HiltActivity::class.java))
        HandlerAssistant.test(2)
    }

    //<editor-fold desc="AIDL 客户端">
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

        viewBinding.mainBtnTestWatermark.setOnClickListener {
            startActivity(Intent(this, CameraPlayWithOpenglActivity::class.java))
        }

        viewBinding.mainBtnTestGLCamera.setOnClickListener {
            startActivity(Intent(this, GLCameraActivity::class.java))
        }
    }
    //</editor-fold>

    companion object {
        private const val TAG = "MainActivity2"
    }
}