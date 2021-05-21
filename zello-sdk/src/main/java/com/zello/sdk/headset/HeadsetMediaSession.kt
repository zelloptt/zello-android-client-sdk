package com.zello.sdk.headset

import android.content.Context
import android.view.KeyEvent

/**
 * A media session abstraction.
 */
interface HeadsetMediaSession {

	/**
	 * Start the session.
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

}
