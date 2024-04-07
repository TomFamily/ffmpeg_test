package com.example.base.exit

import android.app.Activity
import android.content.ContextWrapper
import android.view.View
import androidx.annotation.MainThread
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import kotlin.reflect.KClass

@MainThread
inline fun <reified VM : ViewModel> View.activityViewModels(
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
) = createViewModelLazy(VM::class, { requireActivity().viewModelStore },
    factoryProducer ?: { requireActivity().defaultViewModelProviderFactory })

/**
 * Helper method for creation of [ViewModelLazy], that resolves `null` passed as [factoryProducer]
 * to default factory.
 */
@MainThread
fun <VM : ViewModel> View.createViewModelLazy(
    viewModelClass: KClass<VM>,
    storeProducer: () -> ViewModelStore,
    factoryProducer: (() -> ViewModelProvider.Factory)
): Lazy<VM> {
    val factoryPromise = factoryProducer
    return ViewModelLazy(viewModelClass, storeProducer, factoryPromise)
}


@MainThread
fun View.requireActivity(): FragmentActivity {
    var context = context
    while (context is ContextWrapper) {
        if (context is Activity) {
            break
        }
        context = (context as ContextWrapper).baseContext
    }
    if (context == null) {
        throw RuntimeException("the view is not attach to an Activity")
    } else if (context !is FragmentActivity) {
        throw RuntimeException("the view is not attach to an FragmentActivity")
    }
    return context
}