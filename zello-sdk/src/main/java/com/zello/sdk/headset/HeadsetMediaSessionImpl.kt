package com.zello.sdk.headset

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import androidx.media.session.MediaButtonReceiver

/**
 * [HeadsetMediaSession] implementation that's suitable for Android API 4-30.
 */
class HeadsetMediaSessionImpl : HeadsetMediaSession {

	private var mediaSession: MediaSessionCompat? = null
	private var silencePlayer: SilencePlayer? = null
	private var callback: MediaSessionCompat.Callback? = null

	companion object {
		private const val TAG = "(HeadsetMediaSessionImpl)"
	}

	/**
	 * Start the session.
	 */
	override fun start(context: Context, onKeyEvent: (KeyEvent) -> Unit) {
		HeadsetBroadcastReceiver.onKeyEvent = onKeyEvent
		val mediaButtonReceiver = ComponentName(context.applicationContext, HeadsetBroadcastReceiver::class.java)
		callback = object : MediaSessionCompat.Callback() {
			override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
				return (mediaButtonEvent != null && HeadsetBroadcastReceiver.handleIntent(mediaButtonEvent)) || super.onMediaButtonEvent(mediaButtonEvent)
			}
		}
		mediaSession = MediaSessionCompat(context.applicationContext, "zello sdk", mediaButtonReceiver, null).also { session ->
			session.setCallback(callback)
			val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON).run {
				setClass(context.applicationContext, MediaButtonReceiver::class.java)
			}
			session.setMediaButtonReceiver(PendingIntent.getBroadcast(context.applicationContext, 0, mediaButtonIntent, 0))
			session.isActive = true;
			// Switch the session to "playing" state
			val playbackState = PlaybackStateCompat.Builder().run {
				setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PAUSE)
				setState(PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f)
			}.build()
			session.setPlaybackState(playbackState)
		}
		// While not strictly necessary, playing a dummy sound may help with "stealing" an active media session from another app
		startSilencePlayer(context)
	}

	/**
	 * Stop the session.
	 */
	override fun stop() {
		mediaSession?.release()
		mediaSession = null
		silencePlayer?.release()
		silencePlayer = null
		HeadsetBroadcastReceiver.onKeyEvent = null
		callback = null
	}

	private fun startSilencePlayer(context: Context) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
		silencePlayer = SilencePlayerImpl21(context)
	}

}
