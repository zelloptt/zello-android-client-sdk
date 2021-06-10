package com.zello.sdk.headset

import android.content.Context
import android.os.Build
import android.view.KeyEvent
import androidx.annotation.MainThread
import com.zello.sdk.BuildConfig
import com.zello.sdk.Log
import com.zello.sdk.TimeImpl
import com.zello.sdk.Zello

/**
 * This class contains the high level headset hook processing logic.
 * Use it in your app to add a quick way of handling the headset PTT button.
 * All methods of this class are required to be called on the UI thread.
 * Three types of pulse sequences are supported:
 * <ul>
 * <li>
 *    Regular headset
 * </li>
 * <li>
 *    Legacy PTT headset
 * </li>
 * <li>
 *    PTT headset
 * </li>
 * </ul>
 */
@Suppress("unused")
@MainThread
object Headset {

	private var handler: HeadsetHandler? = null
	private var mediaSession: HeadsetMediaSession? = null

	private const val TAG = "(Headset)"

	/**
	 * Start handling of the headset hook button.
	 * @param context Android app context
	 * @param headsetType Headset type
	 * @param onPress Callback to invoke when a headset button press is detected
	 * @param onRelease Callback to invoke when a headset button release is detected
	 * @param onToggle Callback to invoke when a headset button toggle is detected
	 * @param openMicTimeoutMs Optional timeout to protect against lost key events; use 0 to disable
	 *
	 * [onPress] and [onRelease] are only called for [HeadsetType.PttHeadset] and [HeadsetType.LegacyPttHeadset]
	 * because the pressed state of the button is tracked for those.
	 *
	 * [onToggle] is only called for [HeadsetType.RegularHeadsetToggle].
	 * The app is expected to implement a logic that decides if it treats the event
	 * as a beginning or an ending of a message, most likely based on whether there's
	 * a live outgoing message at the time of the event.
	 *
	 * Open mic timeout should be something reasonably high, typically 2 minutes or even more.
	 * Short open mic timeouts have an increased chance of a misfire which may "flip" the operation
	 * of a legacy PTT accessory and start treating presses as releases and vice versa.
	 */
	@JvmStatic
	fun start(context: Context, headsetType: HeadsetType, onPress: Runnable, onRelease: Runnable, onToggle: Runnable, openMicTimeoutMs: Int) {
		if (mediaSession != null) {
			Log.e("$TAG Can't start: already started", null)
			return
		}
		mediaSession = HeadsetMediaSessionImpl().also {
			it.start(context) { event -> processKeyEvent(event) }
		}
		handler = HeadsetHandler(headsetType, onPress, onRelease, onToggle, openMicTimeoutMs, TimeImpl(context), Log)
		Zello.getInstance().setHeadsetActive(true)
	}

	/**
	 * Stop handling of the headset hook button.
	 */
	@JvmStatic
	fun stop() {
		handler?.reset()
		handler = null
		mediaSession?.stop()
		mediaSession = null
		Zello.getInstance().setHeadsetActive(false)
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
		return processKeyEvent(event)
	}

	/**
	 * Called when the app is brought to the foreground.
	 */
	@JvmStatic
	fun onForeground() {
		mediaSession?.onForeground()
	}

	/**
	 * Called when the app is switched to the background.
	 */
	@JvmStatic
	fun onBackground() {
		mediaSession?.onBackground()
	}

	/**
	 * Process an event coming from a media session or from the hosting activity.
	 * @param event The key event
	 */
	private fun processKeyEvent(event: KeyEvent): Boolean {
		// Drop non-headset events
		if (!isHeadsetEvent(event.keyCode)) return false
		// Drop cancelled events
		if ((event.flags and KeyEvent.FLAG_CANCELED) == KeyEvent.FLAG_CANCELED) return false
		// Drop the ACTION_MULTIPLE event
		val action = event.action
		if (action != KeyEvent.ACTION_DOWN && action != KeyEvent.ACTION_UP) return false
		// Send it down the line
		handler?.process(action == KeyEvent.ACTION_DOWN)
		return true
	}

	/**
	 * Check if a given event is coming from a hedset button.
	 * @param event The key event
	 */
	private fun isHeadsetEvent(event: KeyEvent) {
		return when (event.keyCode) {
			KeyEvent.KEYCODE_HEADSETHOOK -> true
			KeyEvent.KEYCODE_MEDIA_STOP,
			KeyEvent.KEYCODE_MEDIA_PAUSE,
			KeyEvent.KEYCODE_MEDIA_PLAY,
			KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
			else -> false
		}
	}

}
