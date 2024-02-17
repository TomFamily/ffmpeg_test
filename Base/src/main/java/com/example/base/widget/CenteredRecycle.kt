package com.example.base.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.example.base.R
import com.example.base.databinding.ViewCenteredRecycleBinding

/**
 * [LinearSnapHelper]： https://www.jianshu.com/p/e54db232df62
 */

//class CenteredRecycleView(context: Context, attributeSet: AttributeSet): ConstraintLayout(context, attributeSet) {
//
//    private var binding: ViewCenteredRecycleBinding
//
//    init {
//        LayoutInflater.from(context).inflate(R.layout.view_centered_recycle, this, true)
//
//        val root = inflate(context, R.layout.view_centered_recycle, this)
//        binding = ViewCenteredRecycleBinding.bind(root)
//    }
//
//
//    override fun onFinishInflate() {
//        super.onFinishInflate()
//
//        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
//        binding.viewCenterdRecycleRv.layoutManager = layoutManager
//
//        // 初始化 LinearSnapHelper
//        val snapHelper = LinearSnapHelper()
//        snapHelper.attachToRecyclerView(binding.viewCenterdRecycleRv)
//
//        snapHelper.setOnSnapListener(object : LinearSnapHelper.OnSnapListener {
//            override fun onSnapped(view: View, position: Int) {
//                // 当 item 吸附到中心位置时的回调
//            }
//        })
//    }
//
//    override fun onAttachedToWindow() {
//        super.onAttachedToWindow()
//    }
//
//    companion object {
//        private const val TAG = "CenteredRecycleView"
//    }
//}