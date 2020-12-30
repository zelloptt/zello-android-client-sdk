package com.zello.sdk.headset

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.KeyEvent

/**
 * Broadcast receiver that is used with the media session
 * and allows to receive headset media key events while the app is running
 * in the background.
 */
class HeadsetReceiver: BroadcastReceiver() {

	override fun onReceive(context: Context, intent: Intent) {
		if (intent.action != Intent.ACTION_MEDIA_BUTTON) return
		val event = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT) ?: return
		val action = event.action
		// Drop the ACTION_MULTIPLE event
		if (action != KeyEvent.ACTION_DOWN && action != KeyEvent.ACTION_UP) return
		HeadsetHandler.onKeyEvent(event)
	}

}
