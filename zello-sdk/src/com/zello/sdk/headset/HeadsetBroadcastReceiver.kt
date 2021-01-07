package com.zello.sdk.headset

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import com.zello.sdk.headset.HeadsetBroadcastReceiver.Companion.onKeyEvent
import java.lang.ref.WeakReference

/**
 * Broadcast receiver that is used with the media session
 * and allows to receive headset media key events while the app is running
 * in the background.
 * Instances of this class are created by Android framework and therefore
 * can't receive any parameters. A static [onKeyEvent] member should be
 * used to register an actual recipient of the key events.
 */
class HeadsetBroadcastReceiver : BroadcastReceiver() {

	override fun onReceive(context: Context, intent: Intent) {
		handleIntent(intent)
	}

	companion object {

		/**
		 * Handle a media button intent.
		 */
		fun handleIntent(intent: Intent): Boolean {
			if (intent.action != Intent.ACTION_MEDIA_BUTTON) return false
			val event = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT) ?: return false
			onKeyEvent?.invoke(event)
			return true
		}

		/**
		 * Register a recipient for the key events.
		 */
		var onKeyEvent: ((KeyEvent) -> Unit)?
			get() {
				return handler?.get()
			}
			set(value) {
				handler = if (value == null) null else WeakReference(value)
			}

		// Backing field for [onKeyEvent]
		private var handler: WeakReference<(KeyEvent) -> Unit>? = null
	}

}
