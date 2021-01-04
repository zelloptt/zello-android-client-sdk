package com.zello.sdk

/**
 * A simple logging hook-up that allows an app
 * to process log messages generated by the SDK.
 * Does nothing by default.
 */
object Log {

	private var events: LogEvents? = null

	/**
	 * Assign to provide a custom handler for log entries generated by the SDK.
	 */
	@JvmStatic
	fun setEvents(events: LogEvents) {
		this.events = events
	}

	/**
	 * Write an information message to the log.
	 * @param s Message
	 */
	@JvmStatic
	fun writeInfo(s: String) {
		events?.onWriteInfo(s)
	}

	/**
	 * Write an error message to the log.
	 * @param s Message
	 * @param t Optional throwable
	 */
	@JvmStatic
	fun writeError(s: String, t: Throwable?) {
		events?.onWriteError(s, t)
	}

}
