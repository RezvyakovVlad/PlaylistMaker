package com.example.playlistmaker

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object DebounceUtils {

    private val jobs = mutableMapOf<String, Job>()

    fun debounce(
        key: String,
        delayMillis: Long,
        scope: CoroutineScope,
        action: () -> Unit
    ): Job {
        jobs[key]?.cancel()

        val newJob = scope.launch {
            delay(delayMillis)
            action()
            jobs.remove(key)
        }

        jobs[key] = newJob
        return newJob
    }

    fun throttleFirst(
        key: String,
        intervalMillis: Long,
        scope: CoroutineScope,
        action: () -> Unit
    ): Job {
        if (jobs.containsKey(key)) {
            return jobs[key]!!
        }

        val newJob = scope.launch {
            action()
            delay(intervalMillis)
            jobs.remove(key)
        }

        jobs[key] = newJob
        return newJob
    }

    fun cancel(key: String) {
        jobs[key]?.cancel()
        jobs.remove(key)
    }

    fun cancelAll() {
        jobs.values.forEach { it.cancel() }
        jobs.clear()
    }
}