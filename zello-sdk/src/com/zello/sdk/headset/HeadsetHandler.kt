package com.zello.sdk.headset

import androidx.annotation.MainThread
import com.zello.sdk.Logger
import com.zello.sdk.Time

/**
 * This class implements the logic behind supported headset types.
 * @param headsetType Headset type
 * @param onPress Callback to invoke when a headset button press is detected
 * @param onRelease Callback to invoke when a headset button release is detected
 * @param onToggle Callback to invoke when a headset button toggle is detected
 * @param openMicTimeoutMs Optional open microphone timeout to protect against lost key events
 * @param time Time dependency
 * @param logger Logger dependency
 */
@MainThread
class HeadsetHandler(
		private val headsetType: HeadsetType,
		onPress: Runnable,
		onRelease: Runnable,
		onToggle: Runnable,
		private val openMicTimeoutMs: Int,
		private val time: Time,
		private val logger: Logger?) {

	companion object {
		private const val TAG = "(HeadsetHandler)"

		/**
		 * Two key presses received within this time window for a legacy mic will trigger resetting to a released state and
		 * ignore key events until [LOCKOUT_DURATION_MS] has expired.
		 * NOTE: 1.5 seconds may seem like a very long time, but in practice it takes the OS a while to deliver rapid clicks.
		 */
		private const val DOUBLE_CLICK_THRESHOLD_MS = 1500L

		/**
		 * Following a double click for a legacy speaker mic, all input will be ignored until this amount of time has elapsed with no input.
		 */
		private const val LOCKOUT_DURATION_MS = 1000L
	}

	private var onPress: Runnable? = onPress
	private var onRelease: Runnable? = onRelease
	private var onToggle: Runnable? = onToggle

	private var safetyTimer: HeadsetSafetyTimer?
	private val backStack = HeadsetBackStack(time)
	private var lockoutExpires = -1L
	private var lastPress = 0L
	private var headsetDown = false

	init {
		safetyTimer = if (openMicTimeoutMs > 0) {
			HeadsetSafetyTimer(headsetType, openMicTimeoutMs, ::onHeadsetKeyMissed, time, logger)
		} else {
			null
		}
	}

	/**
	 * Process an event.
	 *
	 * @return Indicates whether a message should be started, ended, or nothing should be done
	 */
	fun process(event: HeadsetEvent) {
		val result = if (event.down) {
			headsetKeyDown()
		} else {
			headsetKeyUp()
		}
		when (result) {
			HeadsetEventResult.PRESS -> onPress?.run()
			HeadsetEventResult.RELEASE -> onRelease?.run()
			HeadsetEventResult.TOGGLE -> onToggle?.run()
			else -> {
			}
		}
		backStack.add(event)
	}

	fun reset() {
		onPress = null
		onRelease = null
		backStack.clear()
		lockoutExpires = -1L
		lastPress = 0L
		safetyTimer?.reset()
		safetyTimer = null
	}

	private fun headsetKeyDown(): HeadsetEventResult? {
		if (checkLockout()) {
			return null // In a lockout state, don't do anything
		}
		val pulseCount = backStack.pulseCount
		if (headsetType == HeadsetType.PttHeadset && (pulseCount >= 1 || headsetDown)) {
			// Already had a key down + key up on the stack or we think the button is down, so treat as a key release
			if (!headsetDown) {
				return null
			}
			return release()
		}

		if (headsetType == HeadsetType.LegacyPttHeadset && headsetDown) {
			// Even though this is a key down, it is actually a release of the button based on the flag
			return release()
		}

		// Either standard headset OR specialized 1 flagged as up OR specialized 2 with an empty stack
		return press()
	}

	private fun headsetKeyUp(): HeadsetEventResult? {
		if (headsetType == HeadsetType.RegularHeadsetToggle) {
			// Standard headset - up means up
			return release()
		}

		if (backStack.previousKeyDown) {
			// For either pulse mic type, if we get a key up while a key down is the last thing on the stack,
			// this is a standard pulse, so the key up does nothing
			return null
		}

		// A key up with a missing key down should be treated just like the key down would have been treated
		return headsetKeyDown()
	}

	/**
	 * Called by [HeadsetSafetyTimer] upon detecting an open mic.
	 */
	private fun onHeadsetKeyMissed() {
		if (headsetDown) {
			backStack.clear()
			release()
			onRelease?.run()
		}
	}

	private fun press(): HeadsetEventResult {
		if (startLockoutIfNeeded()) {
			return HeadsetEventResult.RELEASE
		}
		logger?.i("$TAG Headset hook down")
		headsetDown = true
		safetyTimer?.onHeadsetPress()
		if (headsetType == HeadsetType.RegularHeadsetToggle) {
			// Skip pressing of the button on regular headset - wait for the release
			return HeadsetEventResult.NONE
		}
		return HeadsetEventResult.PRESS
	}

	private fun release(): HeadsetEventResult {
		logger?.i("$TAG Headset hook up")
		headsetDown = false
		safetyTimer?.onHeadsetRelease()
		return when (headsetType) {
			HeadsetType.RegularHeadsetToggle -> HeadsetEventResult.TOGGLE
			else -> HeadsetEventResult.RELEASE
		}
	}

	/**
	 * This should be invoked whenever a mic key press is detected, though currently it is a NOOP for anything besides the legacy speaker mics.
	 * Because press and release events are indistinguishable for these mics, it is possible that the state of [headsetDown] may lose synchronization
	 * with the physical button. If the user double clicks the mic, this method will simulate of a release of the mic and set a value for [lockoutExpires].
	 * All headset events occurring before the timestamp of [lockoutExpires] should be ignored.
	 * @return true if lockout has started
	 */
	private fun startLockoutIfNeeded(): Boolean {
		if (headsetType != HeadsetType.LegacyPttHeadset) {
			return false // Lockout is only relevant for legacy mics
		}
		val currentTime = time.tickCount
		if (lastPress < 1L) {
			lastPress = currentTime
			return false
		}
		val delta = currentTime - lastPress
		lastPress = currentTime
		if (delta < DOUBLE_CLICK_THRESHOLD_MS) {
			logger?.i("$TAG Got double click for legacy headset within $delta ms, starting lockout")
			lockoutExpires = currentTime + LOCKOUT_DURATION_MS
			release()
			return true
		}
		return false
	}

	/**
	 * Invoke this whenever a headset pulse is received. If the headset is currently in a lockout state, the value of [lockoutExpires]
	 * will be reset such that another [LOCKOUT_DURATION_MS] milliseconds must expire before the lockout ends. This ensures that the user must stop
	 * interacting with the button before we end the lockout.
	 * @return true if lockout is active
	 */
	private fun checkLockout(): Boolean {
		val currentTime = time.tickCount
		if (currentTime > lockoutExpires) {
			lockoutExpires = -1L
			return false
		}

		logger?.i("$TAG Got another event before lockout expired, restarting lockout")
		lockoutExpires = currentTime + LOCKOUT_DURATION_MS
		return true
	}

}
