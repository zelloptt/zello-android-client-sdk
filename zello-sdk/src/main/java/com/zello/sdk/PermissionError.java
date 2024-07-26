package com.zello.sdk;

/**
 * The <code>PermissionError</code> enum defines permission-related errors.
 */
@SuppressWarnings("unused")
public enum PermissionError {

	/**
	 * No error.
	 */
	NONE,

	/**
	 * Unknown permission error.
	 */
	UNKNOWN,

	/**
	 * Microphone permission error.
	 */
	MICROPHONE_NOT_GRANTED,

	/**
	 * Foreground service cannot start.
	 */
	FOREGROUND_SERVICE_NOT_ALLOWED

}
