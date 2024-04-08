package com.example.ffmpeg_test.mvi

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.ffmpeg_test.databinding.ViewContainerBinding
import com.example.ffmpeg_test.mvi.base.UserIntent
import com.example.base.exit.activityViewModels
import com.example.ffmpeg_test.mvi.base.Size
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable

class ContainerView(context: Context, attributeSet: AttributeSet): ConstraintLayout(context, attributeSet) {
    private val binding: ViewContainerBinding
    private val viewModel: ContainerViewModel
    private var mDisposable: Disposable? = null
    private var mDisposableMap: io.reactivex.disposables.Disposable? = null
    private var mDisposableAss: io.reactivex.disposables.Disposable? = null
    private var mDisposableAtt: io.reactivex.disposables.Disposable? = null

    init {
        binding = ViewContainerBinding.inflate(LayoutInflater.from(context), this, true)
        viewModel = activityViewModels<ContainerViewModel>().value
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
                binding.viewContainerMap.text = splicingStr("map", it.mapState)
                binding.viewContainerAssistant.text = splicingStr("ass", it.assistantState)
                binding.viewContainerAttitude.text = splicingStr("att", it.attitudeSTate)
            }
    }

    private fun splicingStr(title: String, size: Size): String {
        return when(size) {
            Size.BIG -> "$title：大"
            Size.MID -> "$title：中"
            Size.SMALL -> "$title：小"
        }
    }

    private fun initEvent() {
        mDisposableMap = RxView.clicks(binding.viewContainerMap)
            .map { viewModel.getMyState().mapState }
            .map {
                if (it == Size.BIG) return@map UserIntent.MidMap
                if (it == Size.MID) return@map UserIntent.SmallMap
                if (it == Size.SMALL) return@map UserIntent.BigMap
                return@map UserIntent.SmallMap
            }
            .subscribe {
            viewModel.dispatchIntent(it)
        }

        mDisposableAss = RxView.clicks(binding.viewContainerAssistant)
            .map { viewModel.getMyState().assistantState }
            .map {
                if (it == Size.BIG) return@map UserIntent.MidAssistant
                if (it == Size.MID) return@map UserIntent.SmallAssistant
                if (it == Size.SMALL) return@map UserIntent.BigAssistant
                return@map UserIntent.SmallAssistant
            }
            .subscribe {
                viewModel.dispatchIntent(it)
            }

        mDisposableAtt = RxView.clicks(binding.viewContainerAttitude)
            .map { viewModel.getMyState().attitudeSTate }
            .map {
                if (it == Size.BIG) return@map UserIntent.MidAttitude
                if (it == Size.MID) return@map UserIntent.SmallAttitude
                if (it == Size.SMALL) return@map UserIntent.BigAttitude
                return@map UserIntent.SmallAttitude
            }
            .subscribe {
                viewModel.dispatchIntent(it)
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