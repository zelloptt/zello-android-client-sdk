package com.zello.sdk.headset

import androidx.annotation.MainThread
import com.zello.sdk.Logger
import com.zello.sdk.Time
import com.zello.sdk.Timer
import kotlin.math.max

/**
 * A class that helps to simulate a button release after the button help for too long
 * which usually means a missed key up sequence.
 */
@MainThread
class HeadsetSafetyTimer(private val headsetType: HeadsetType, private val timeoutMs: Int, onCancel: () -> Unit, private val time: Time, private val logger: Logger?) {

	companion object {
		private const val TAG = "(HeadsetSafetyTimer) "

		/**
		 * Even if the timeout is set to something very short, don't fire fake key up events until 2 minutes are up
		 */
		private const val MIN_TIMEOUT_MS = 120_000L

		/**
		 * Padding applied to the global message duration cap before simulating a key release. In a typical case, the user would only hit the message cap during an open mic,
		 * meaning that this padding doesn't do much at all. However, if the user truly was holding the button down, they may release it once the message manager kills the message.
		 * In that case, this gives a couple extra seconds for the real button release to come through before simulating one.
		 */
		private const val BUTTON_RELEASE_THRESHOLD_MS = 2_000L
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
			logger?.i("$TAG Starting a $timeout ms fail safe timer for headset")
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
			logger?.i("$TAG Got key release, cancelling fail safe timer")
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
		return max(MIN_TIMEOUT_MS, timeoutMs.toLong()) + BUTTON_RELEASE_THRESHOLD_MS
	}

}
