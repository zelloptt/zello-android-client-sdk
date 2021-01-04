package com.zello.sdk

interface LogEvents {

	/**
	 * Called to report an information messages.
	 */
	fun onWriteInfo(s: String)

	/**
	 * Called to report an error messages.
	 */
	fun onWriteError(s: String, t: Throwable?)

}
