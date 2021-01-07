package com.zello.sdk.headset

import androidx.annotation.MainThread
import com.zello.sdk.Time
import java.util.LinkedList

/**
 * A short-lived stack of [HeadsetEvent].
 * Used to identify the pulse sequences relied upon by certain specialized speaker-mics
 * as well as to make decisions about handling key up events which correspond to previous key down events.
 */
@MainThread
class HeadsetBackStack(private val time: Time) {

	companion object {
		/**
		 * Max number of key events persisted to identify devices. Currently set to 12 because the max for a single press and release for any supported hardware
		 * is 6, and this allows us to keep record of up to 2 press and release events from that type of device.
		 */
		private const val BACKSTACK_MAX_SIZE = 12

		/**
		 * Time in MS after which the backstack is discarded during normal operation. Note that this does not apply when adding new keys
		 */
		private const val BACKSTACK_MAX_AGE_MS = 800L
	}

	private val backStack = LinkedList<HeadsetEvent>()

	/**
	 * Check if the last stored event is a key down.
	 */
	val previousKeyDown: Boolean
		get() {
			clearOldBackStack()
			return backStack.lastOrNull()?.down ?: false
		}

	/**
	 * Get the amount of pulses present in the stack.
	 */
	val pulseCount: Int
		get() {
			clearOldBackStack()
			var pulses = 0
			var lastDown = false
			backStack.forEach {
				if (it.down || !lastDown) {
					// A key down is always the start of a pulse. A key up can be the start of the pulse only if we missed the key down
					pulses++
				}
				lastDown = it.down
			}
			return pulses
		}

	/**
	 * Adds a [HeadsetEvent] to the stack. It will be removed
	 * later if the stack timer expires or the stack exceeds its maximum size.
	 * @param event
	 */
	fun add(event: HeadsetEvent) {
		clearOldBackStack()
		backStack.add(event)
		if (backStack.size > BACKSTACK_MAX_SIZE) {
			backStack.removeAt(0)
		}
	}

	/**
	 * Reset the stack.
	 */
	fun clear() {
		backStack.clear()
	}

	private fun clearOldBackStack() {
		val currentTime = time.tickCount
		backStack.removeAll { currentTime - it.time > BACKSTACK_MAX_AGE_MS }
	}

}
