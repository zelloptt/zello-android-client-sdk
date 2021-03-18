package com.zello.sdk.headset

/**
 * Headset hook event.
 * @param down Down or up?
 * @param time System uptime at the time of the the event, milliseconds
 */
data class HeadsetEvent(val down: Boolean, val time: Long)
