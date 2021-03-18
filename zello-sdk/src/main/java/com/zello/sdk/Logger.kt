package com.zello.sdk

/**
 * Log abstraction.
 */
interface Logger {

	/**
	 * Write an information message to the log.
	 * @param s Message
	 */
	fun i(s: String)

	/**
	 * Write an error message to the log.
	 * @param s Message
	 * @param t Optional throwable
	 */
	fun e(s: String, t: Throwable?)

}
