package com.zello.sdk.headset

import android.content.Context
import android.os.Build
import android.view.KeyEvent
import androidx.annotation.MainThread

/**
 * This class contains all headset hook processing logic.
 * Use it in your app to add a quick way of handling the headset PTT button.
 * All methods of this class are required to be called on the UI thread.
 * Three types of pulse sequences are supported:
 * <ul>
 * <li>
 *    <code>RegularHeadset</code>
 * </li>
 * <li>
 *    <code>LegacyPttHeadset</code>
 * </li>
 * <li>
 *     <code>PttHeadset</code>
 * </li>
 * </ul>
 */
@MainThread
object Headset {

	// Current headset type; null value means not started
	private var headsetType: HeadsetType? = null

	private var onMessageStart: Runnable? = null
	private var onMessageStop: Runnable? = null

	private var mediaSession: HeadsetMediaSession? = null

	/**
	 * Start handling of the headset hook button.
	 * @param context Android app context
	 * @param headsetType Headset type
	 * @param onMessageStart Callback to invoke when a headset button press is detected
	 * @param onMessageStop Callback to invoke when a headset button release is detected
	 */
	@JvmStatic
	fun start(context: Context, headsetType: HeadsetType, onMessageStart: Runnable, onMessageStop: Runnable) {
		this.headsetType = headsetType
		this.onMessageStart = onMessageStart
		this.onMessageStop = onMessageStop
		if (mediaSession == null) {
			mediaSession = when {
				Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> HeadsetMediaSessionImpl21()
				else -> HeadsetMediaSessionImpl16()
			}.also {
				it.start(context) { event -> processKeyEvent(event, true) }
			}
		}
	}

	/**
	 * Stop handling of the headset hook button.
	 */
	@JvmStatic
	fun stop() {
		headsetType = null
		onMessageStart = null
		onMessageStop = null
		mediaSession?.stop()
		mediaSession = null
	}

	/**
	 * Handle a key event intercepted in <code>Activity.onKeyDown()</code> and
	 * <code>Activity.onKeyUp()</code>. The calling code should call the base
	 * <code>Activity.onKeyDown()</code> and <code>Activity.onKeyUp()</code> in
	 * case this call returns <code>true</code>.
	 * @param event Key event
	 * @return true if the event was consumed, false to allow default processing
	 */
	@JvmStatic
	fun onKeyEvent(event: KeyEvent): Boolean {
		return processKeyEvent(event, false)
	}

	@JvmStatic
	private fun processKeyEvent(event: KeyEvent, fromMediaSession: Boolean): Boolean {
		return false
	}

}
