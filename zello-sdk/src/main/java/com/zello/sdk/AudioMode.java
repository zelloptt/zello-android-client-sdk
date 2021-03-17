package com.zello.sdk;

/**
 * The <code>AudioMode</code> enum represents the audio modes available with Zello SDK.
 * @see Audio
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public enum AudioMode {

	/**
	 * <p>
	 *     Phone's loudspeaker.
	 * </p>
	 * <p>
	 *     The default mode. When a wired headset is connected, audio is automatically routed via
	 *     the headset. When a Bluetooth stereo headset is connected, audio playback is usually routed via
	 *     the headset while the built-in microphone is used for recording.
	 * </p>
	 */
	SPEAKER,
	/**
	 * <p>
	 *     Phone's earpiece.
	 * </p>
	 * <p>
	 *     When a headset is connected, this option usually works exactly as <code>SPEAKER</code>.
	 * </p>
	 */
	EARPIECE,
	/**
	 * <p>
	 *     Bluetooth headset.
	 * </p>
	 * <p>
	 *     This mode is required to record from Bluetooth or when using mono headsets.
	 * </p>
	 */
	BLUETOOTH,
	/**
	 * <p>
	 *     Android Wear device.
	 * </p>
	 * <p>
	 *     Zello supports multiple paired Android Wear devices and lets you select the one you want
	 *     to use. The device microphone (when available) is used when PTT is activated from the wearable.
	 * </p>
	 */
	WEARABLE

}
