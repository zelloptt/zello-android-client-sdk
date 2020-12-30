package com.zello.sdk.headset

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
object HeadsetHandler {

	/**
	 * Start handling of the headset hook button.
	 * @param headsetType Headset type
	 * @param onStart Callback to invoke when a headset button press is detected
	 * @param onStop Callback to invoke when a headset button release is detected
	 */
	fun start(headsetType: HeadsetType, onStart: () -> Unit, onStop: () -> Unit) {

	}

	/**
	 * Stop handling of the headset hook button.
	 */
	fun stop() {

	}

	/**
	 * Process a key event intercepted in <code>Activity.onKeyDown()</code> and
	 * <code>Activity.onKeyUp()</code>. The calling code should call the base
	 * <code>Activity.onKeyDown()</code> and <code>Activity.onKeyUp()</code> in
	 * case this call returns <code>true</code>.
	 * @param event Key event
	 * @return true if the event was consumed, false to allow default processing
	 */
	fun onKeyEvent(event: KeyEvent): Boolean {
		return true
	}

}
