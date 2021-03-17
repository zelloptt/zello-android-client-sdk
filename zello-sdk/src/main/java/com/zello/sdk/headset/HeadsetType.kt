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
	 * This mode represents a regular wired headset.
	 * Regular wired headsets all have a limitation preventing them
	 * from being able to record audio while the button is pressed.
	 * Therefore regular headsets are usually used in a toggle mode.
	 */
	RegularHeadsetToggle,

	/**
	 * <b>Legacy PTT headset</b>
	 * Pressing the button generates a key down + key up sequence.
	 * Releasing the button generates a key down + key up sequence.
	 * This mode represents a specialized headset that simulates a button click
	 * whenever the button is pressed or released.
	 */
	LegacyPttHeadset,

	/**
	 * <b>PTT headset</b>
	 * Pressing the button generates a key down + key up sequence.
	 * Releasing the button generates a key down + key up + key down + key up sequence.
	 * This mode represents a specialized headset that simulates a button click
	 * when the button is pressed and a double click when the button is released.
	 */
	PttHeadset

}
