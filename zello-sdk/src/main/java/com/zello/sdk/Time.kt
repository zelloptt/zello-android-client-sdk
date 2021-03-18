package com.zello.sdk

/**
 * Time abstraction.
 */
interface Time {

	/**
	 * Tick count from the moment the system has started.
	 */
	val tickCount: Long

	/**
	 * Create a timer object.
	 */
	fun createTimer(): Timer

}
