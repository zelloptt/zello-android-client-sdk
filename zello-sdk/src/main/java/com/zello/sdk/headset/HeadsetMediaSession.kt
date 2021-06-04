package com.zello.sdk.headset

import android.content.Context
import android.view.KeyEvent

/**
 * A media session abstraction.
 */
interface HeadsetMediaSession {

	/**
	 * Start the session.
	 * @param context App context
	 * @param onKeyEvent A callback to call when a headset key occurs
	 */
	fun start(context: Context, onKeyEvent: (KeyEvent) -> Unit)

	/**
	 * Stop the session.
	 */
	fun stop()

	/**
	 * Re-acquire media session after it's been taken away.
	 */
	fun reacquire()

	/**
	 * Called when the app is brought to the foreground.
	 */
	fun onForeground()

	/**
	 * Called when the app is switched to the background.
	 */
	fun onBackground()

}
