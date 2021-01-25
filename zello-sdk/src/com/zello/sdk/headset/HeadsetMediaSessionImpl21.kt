package com.zello.sdk.headset

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.session.MediaSession
import android.os.Build
import android.view.KeyEvent
import androidx.annotation.MainThread
import androidx.annotation.RequiresApi
import com.zello.sdk.Log

/**
 * [HeadsetMediaSession] implementation that's suitable for Android API 21+.
 */
@MainThread
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class HeadsetMediaSessionImpl21 : HeadsetMediaSession {

	companion object {
		private const val TAG = "(HeadsetMediaSessionImpl21)"
	}

	private var session: MediaSession? = null

	override fun start(context: Context, onKeyEvent: (KeyEvent) -> Unit) {
		val session = try {
			MediaSession(context, "media buttons")
		} catch (t: Throwable) {
			Log.e("$TAG Failed to create media session", t)
			return
		}
		val intent = Intent(context.applicationContext, HeadsetBroadcastReceiver::class.java)
		val pendingIntent = PendingIntent.getBroadcast(context.applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
		try {
			session.setMediaButtonReceiver(pendingIntent)
			session.isActive = true
		} catch (t: Throwable) {
			Log.e("$TAG Failed to register media button event receiver", t)
			return
		}
		this.session = session
		HeadsetBroadcastReceiver.onKeyEvent = onKeyEvent
	}

	override fun stop() {
		session?.let {
			try {
				it.isActive = false
				it.setMediaButtonReceiver(null)
			} catch (t: Throwable) {
				Log.e("$TAG Failed to unregister media button receiver", t)
			}
		}
		session = null
		HeadsetBroadcastReceiver.onKeyEvent = null
	}

}
