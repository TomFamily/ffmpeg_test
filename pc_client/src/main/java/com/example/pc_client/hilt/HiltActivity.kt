package com.example.pc_client.hilt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.pc_client.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HiltActivity : AppCompatActivity() {

    @Inject 
    lateinit var person: Person

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hilt)
        
        initEvent()
    }

    private fun initEvent() {
        Log.d(TAG, "initEvent: $person")
    }
    
    companion object {
        private const val TAG = "HiltActivity2"
    }
}