package de.datlag.openfe.commons

import androidx.lifecycle.LiveDataScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun <T> mutableLiveData(
    context: CoroutineContext = EmptyCoroutineContext,
    timeoutInMs: Long = 5000L,
    block: suspend LiveDataScope<T>.() -> Unit
) = liveData<T>(context, timeoutInMs, block) as MutableLiveData<T>
