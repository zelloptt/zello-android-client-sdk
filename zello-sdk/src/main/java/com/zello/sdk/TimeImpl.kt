package com.zello.sdk

import android.content.Context
import android.os.SystemClock

/**
 * Android-specific [Time] implementation.
 */
class TimeImpl(context: Context) : Time {

	private val context = context.applicationContext

	override val tickCount: Long
		get() = SystemClock.elapsedRealtime()

	override fun createTimer(): Timer {
		return TimerImpl(context)
	}

}
