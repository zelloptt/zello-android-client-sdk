package com.zello.sdk.headset

import android.content.Intent
import android.view.KeyEvent
import androidx.media.session.MediaButtonReceiver
import com.zello.sdk.BundleUtils
import com.zello.sdk.headset.HeadsetBroadcastReceiver.Companion.onKeyEvent

/**
 * Broadcast receiver that is used with the media session
 * and allows to receive headset media key events while the app is running
 * in the background.
 * Instances of this class are created by Android framework and therefore
 * can't receive any parameters. A static [onKeyEvent] member should be
 * used to register an actual recipient of the key events.
 */
class HeadsetBroadcastReceiver : MediaButtonReceiver() {

	companion object {

		/**
		 * Handle a media button intent.
		 */
		fun handleIntent(intent: Intent): Boolean {
			if (intent.action != Intent.ACTION_MEDIA_BUTTON) return false
			val event = BundleUtils.getParcelableExtra<KeyEvent>(intent, Intent.EXTRA_KEY_EVENT) ?: return false
			onKeyEvent?.invoke(event)
			return true
		}

		/**
		 * Register a recipient for the key events.
		 */
		var onKeyEvent: ((KeyEvent) -> Unit)? = null

	}

}
