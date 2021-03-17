package com.zello.sdk.headset

/**
 * The result of processing of an [HeadsetEvent] by [HeadsetHandler].
 */
enum class HeadsetEventResult {

	/**
	 * A headset down event was detected.
	 * Used with [HeadsetType.PttHeadset] and [HeadsetType.LegacyPttHeadset]
	 * but not with [HeadsetType.RegularHeadsetToggle].
	 */
	PRESS,

	/**
	 * A headset down event was detected.
	 * Used with [HeadsetType.PttHeadset] and [HeadsetType.LegacyPttHeadset]
	 * but not with [HeadsetType.RegularHeadsetToggle].
	 */
	RELEASE,

	/**
	 * A headset click event was detected.
	 * Used with [HeadsetType.RegularHeadsetToggle] but not with [HeadsetType.PttHeadset]
	 * but not with [HeadsetType.LegacyPttHeadset].
	 */
	TOGGLE,

	/**
	 * No change.
	 */
	NONE

}
