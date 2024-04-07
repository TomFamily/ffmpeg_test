package com.example.ffmpeg_test.mvi

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.ffmpeg_test.databinding.ViewContainerBinding
import com.example.ffmpeg_test.mvi.base.UserIntent
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable

class ContainerView(context: Context, attributeSet: AttributeSet): ConstraintLayout(context, attributeSet) {
    private val binding: ViewContainerBinding
    private val viewModel by lazy { ContainerViewModel() }
    private var mDisposable: Disposable? = null
    private var mDisposableMap: io.reactivex.disposables.Disposable? = null
    private var mDisposableAss: io.reactivex.disposables.Disposable? = null
    private var mDisposableAtt: io.reactivex.disposables.Disposable? = null

    init {
        binding = ViewContainerBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initObservable()
        initEvent()
    }

    private fun initObservable() {
        mDisposable = viewModel.getStateObservable()
            .doOnNext { Log.d(TAG, "onAttachedToWindow: $it") }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.viewContainerMap.text = if (it.isBigMap()) "map: 大" else "map: 小"
                binding.viewContainerAssistant.text = if (it.isBigAssistant()) "ass: 大" else "ass: 小"
                binding.viewContainerAttitude.text = if (it.isBigAttitude()) "att: 大" else "att: 小"
            }
    }

    private fun initEvent() {
        mDisposableMap = RxView.clicks(binding.viewContainerMap)
            .map { viewModel.getMyState().isBigMap() }
            .subscribe {
            viewModel.dispatchIntent(if (it) UserIntent.SmallMap else UserIntent.BigMap)
        }

        mDisposableAss = RxView.clicks(binding.viewContainerAssistant)
            .map { viewModel.getMyState().isBigAssistant() }
            .subscribe {
                viewModel.dispatchIntent(if (it) UserIntent.SmallAssistant else UserIntent.BigAssistant)
            }

        mDisposableAtt = RxView.clicks(binding.viewContainerAttitude)
            .map { viewModel.getMyState().isBigAttitude() }
            .subscribe {
                viewModel.dispatchIntent(if (it) UserIntent.SmallAttitude else UserIntent.BigAttitude)
            }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mDisposable?.dispose()
        mDisposableMap?.dispose()
        mDisposableAss?.dispose()
        mDisposableAtt?.dispose()
    }

    companion object {
        private const val TAG = "ContainerView"
    }
}