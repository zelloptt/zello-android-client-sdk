package com.zello.sdk;

/**
 * The <code>AudioMode</code> enum is a representation of the different mediums for audio output while using the Zello SDK.
 */
public enum AudioMode {

	/**
	 * Speaker for the device.
	 */
	SPEAKER,
	/**
	 * Earpiece or headphones plugged into the device.
	 */
	EARPIECE,
	/**
	 * Bluetooth output connected to the device.
	 */
	BLUETOOTH,
	/**
	 * Wearable device.
	 */
	WEARABLE

}
