package com.zello.sdk.headset

/**
 * The result of processing of an [HeadsetEvent] by [HeadsetHandler].
 */
enum class HeadsetEventResult {

	/**
	 * A beginning of a message was detected.
	 */
	START,

	/**
	 * An end of a message was detected.
	 */
	STOP,

	/**
	 * No change.
	 */
	NONE

}
