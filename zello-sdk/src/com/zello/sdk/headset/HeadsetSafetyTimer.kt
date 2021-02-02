package com.zello.sdk.headset

import androidx.annotation.MainThread
import com.zello.sdk.Logger
import com.zello.sdk.Time
import com.zello.sdk.Timer
import kotlin.math.max

/**
 * A class that helps to simulate a button release after the button help for too long
 * which usually means a missed key up sequence.
 * @param headsetType Headset type
 * @param timeoutMs The longest message duration, anything above it is considered an open mic
 * @param onCancel Callback to be invoked upon detecting an open mic, called on the UI thread
 * @param time Time dependency
 * @param logger Logger dependency
 */
@MainThread
class HeadsetSafetyTimer(private val headsetType: HeadsetType, private val timeoutMs: Int, onCancel: () -> Unit, private val time: Time, private val logger: Logger?) {

	companion object {
		private const val TAG = "(HeadsetSafetyTimer)"

		/**
		 * Even if the timeout is set to something very short, don't fire fake key up events too early
		 */
		private const val MIN_TIMEOUT_MS = 30_000L
	}

	private var timer: Timer? = null
	private var onCancel: (() -> Unit)? = onCancel

	/**
	 * Invoke this method when a headset press is detected.
	 */
	fun onHeadsetPress() {
		timer?.stop()
		timer = null
		if (headsetType == HeadsetType.PttHeadset || headsetType == HeadsetType.LegacyPttHeadset) {
			val timeout = calculateTimeoutMs()
			logger?.i("$TAG Starting a $timeout ms headset fail safe timer")
			timer = time.createTimer().also {
				it.start(timeout, ::onFailTimer)
			}
		}
	}

	/**
	 * Invoke this method when a headset release is detected.
	 */
	fun onHeadsetRelease() {
		timer?.let {
			logger?.i("$TAG Got key release, canceling fail safe timer")
			it.stop()
		}
		timer = null
	}

	/**
	 * Invoke this method to cancel any pending timers.
	 */
	fun reset() {
		timer?.stop()
		timer = null
		onCancel = null
	}

	private fun onFailTimer() {
		timer = null
		onCancel?.invoke()
	}

	private fun calculateTimeoutMs(): Long {
		return max(MIN_TIMEOUT_MS, timeoutMs.toLong())
	}

}
