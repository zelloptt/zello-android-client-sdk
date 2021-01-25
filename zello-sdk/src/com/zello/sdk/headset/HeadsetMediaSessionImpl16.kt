package com.zello.sdk.headset

import android.content.ComponentName
import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent
import androidx.annotation.MainThread
import com.zello.sdk.Log

/**
 * [HeadsetMediaSession] implementation that's suitable for Android API 8-20.
 */
@Suppress("DEPRECATION")
@MainThread
class HeadsetMediaSessionImpl16 : HeadsetMediaSession {

	companion object {
		private const val TAG = "(HeadsetMediaSession16)"
	}

	private var receiver: ComponentName? = null
	private var audioManager: AudioManager? = null

	override fun start(context: Context, onKeyEvent: (KeyEvent) -> Unit) {
		val receiver = ComponentName(context.applicationContext, HeadsetBroadcastReceiver::class.java)
		val audioManager: AudioManager
		try {
			audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: throw RuntimeException("No audio manager")
			audioManager.registerMediaButtonEventReceiver(receiver)
		} catch (t: Throwable) {
			Log.e("$TAG Failed to register media button event receiver", t)
			return
		}
		this.receiver = receiver
		this.audioManager = audioManager
		HeadsetBroadcastReceiver.onKeyEvent = onKeyEvent
	}

	override fun stop() {
		receiver?.let {
			try {
				audioManager?.unregisterMediaButtonEventReceiver(it)
			} catch (t: Throwable) {
				Log.e("$TAG Failed to unregister media button event receiver", t)
			}
		}
		receiver = null
		audioManager = null
		HeadsetBroadcastReceiver.onKeyEvent = null
	}

}
