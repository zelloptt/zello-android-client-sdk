package com.zello.sdk.headset

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.KeyEvent
import androidx.annotation.MainThread
import androidx.annotation.RequiresApi
import com.zello.sdk.Log

/**
 * [HeadsetMediaSession] implementation that's suitable for Android API 26+.
 */
@MainThread
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class HeadsetMediaSessionImpl26: HeadsetMediaSession {

	private var session: MediaSession? = null

	override fun start(context: Context, onKeyEvent: (KeyEvent) -> Unit) {
		val session = try {
			MediaSession(context, "media buttons")
		} catch (t: Throwable) {
			Log.e("Failed to create media session", t)
			return
		}
		val intent = Intent(context.applicationContext, HeadsetBroadcastReceiver::class.java)
		val pendingIntent = PendingIntent.getBroadcast(context.applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
		try {
			session.setMediaButtonReceiver(pendingIntent)
			session.isActive = true
			val state0 = PlaybackState.Builder()
					.setActions(PlaybackState.ACTION_FAST_FORWARD or PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PLAY_PAUSE or PlaybackState.ACTION_SKIP_TO_NEXT or PlaybackState.ACTION_SKIP_TO_PREVIOUS or PlaybackState.ACTION_STOP)
					.setState(PlaybackState.STATE_PLAYING, 0, 1f, SystemClock.elapsedRealtime())
					.build()
			session.setPlaybackState(state0)
			val state1 = PlaybackState.Builder()
					.setActions(PlaybackState.ACTION_FAST_FORWARD or PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PLAY_PAUSE or PlaybackState.ACTION_SKIP_TO_NEXT or PlaybackState.ACTION_SKIP_TO_PREVIOUS or PlaybackState.ACTION_STOP)
					.setState(PlaybackState.STATE_STOPPED, 0, 1f, SystemClock.elapsedRealtime())
					.build()
			session.setPlaybackState(state1)
			session.setCallback(object : MediaSession.Callback() {
				override fun onMediaButtonEvent(intent: Intent): Boolean {
					return HeadsetBroadcastReceiver.handleIntent(intent) || super.onMediaButtonEvent(intent)
				}
			}, Handler(Looper.getMainLooper()))
		} catch (t: Throwable) {
			Log.e("Failed to register media button callback", t)
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
				it.setCallback(null)
			} catch (t: Throwable) {
				Log.e("Failed to unregister media button callback", t)
			}
		}
		session = null
		HeadsetBroadcastReceiver.onKeyEvent = null
	}

}
