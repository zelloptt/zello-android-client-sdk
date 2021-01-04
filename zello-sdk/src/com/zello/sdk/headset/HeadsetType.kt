package com.zello.sdk.headset

/**
 * Supported headset types.
 * Headsets differ by the types of pulse sequences that they emit.
 */
enum class HeadsetType {

	/**
	 * <b>Regular headset</b>
	 * Pressing the button generates a key down event.
	 * Releasing the button generates a key up event.
	 * This mode is appropriate for regular headsets.
	 * Regular headsets only work in a "switch" mode, i.e. a message is started
	 * when the button is pressed and released, and stopped when the button
	 * is pressed and released one more time. This is due to a limitation of wired
	 * headsets where the audio can't be recorded while the button is pressed.
	 */
	RegularHeadset,

	/**
	 * <b>Legacy PTT headset</b>
	 * Pressing the button generates a key down event and a key up event.
	 * Releasing the button generates a key down event and a key up event.
	 * This mode is appropriate for specialized headsets that simulate a button click
	 * when the button is pressed and a button click when the button is released.
	 */
	LegacyPttHeadset,

	/**
	 * <b>PTT headset</b>
	 * Pressing the button generates a key down event and a key up event.
	 * Releasing the button generates two pairs of key down and key up sequences.
	 * This mode is appropriate for specialized headsets that simulate a button click
	 * when the button is pressed and a double click when the button is released.
	 */
	PttHeadset

}
