package com.zello.sdk

/**
 * Timer abstraction.
 */
interface Timer {

	/**
	 * Start the timer.
	 * The callback will fire on the UI thread.
	 * @param timeoutMs Timeout, ms
	 * @param onDone Callback to be called on completion
	 */
	fun start(timeoutMs: Long, onDone: () -> Unit)

	/**
	 * Stop the time if it's running.
	 */
	fun stop()

	/**
	 * Check if the timer is running.
	 */
	val started: Boolean

}
