package com.zello.sdk

/**
 * A simple log hook-up that allows an app
 * to process log messages generated by the SDK.
 * Does nothing by default.
 */
object Log: Logger {

	private var logger: Logger? = null

	/**
	 * Assign to provide a custom handler for log entries generated by the SDK.
	 */
	@JvmStatic
	fun setLogger(events: Logger?) {
		logger = events
	}

	/**
	 * Write an information message to the log.
	 * @param s Message
	 */
	override fun i(s: String) {
		logger?.i(s)
	}

	/**
	 * Write an error message to the log.
	 * @param s Message
	 * @param t Optional throwable
	 */
	override fun e(s: String, t: Throwable?) {
		logger?.e(s, t)
	}

}
