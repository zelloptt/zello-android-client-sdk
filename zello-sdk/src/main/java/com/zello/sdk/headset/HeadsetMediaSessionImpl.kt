package com.zello.sdk.headset

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
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
	private var handler: Handler? = null
	private var callback: MediaSessionCompat.Callback? = null

	companion object {
		private const val TAG = "(HeadsetMediaSessionImpl)"
	}

	/**
	 * Start the session.
	 */
	override fun start(context: Context, onKeyEvent: (KeyEvent) -> Unit) {
		handler = Handler(Looper.getMainLooper())
		HeadsetBroadcastReceiver.onKeyEvent = onKeyEvent
		val mediaButtonReceiver = ComponentName(context.applicationContext, HeadsetBroadcastReceiver::class.java)
		mediaSession = MediaSessionCompat(context.applicationContext, "zello sdk", mediaButtonReceiver, null).also { mediaSession ->
			callback = object : MediaSessionCompat.Callback() {
				override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
					return (mediaButtonEvent != null && HeadsetBroadcastReceiver.handleIntent(mediaButtonEvent)) || super.onMediaButtonEvent(mediaButtonEvent)
				}
			}

			mediaSession.setCallback(callback)
			val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
			mediaButtonIntent.setClass(context.applicationContext, MediaButtonReceiver::class.java)
			val pendingIntent = PendingIntent.getBroadcast(context.applicationContext, 0, mediaButtonIntent, 0)
			mediaSession.setMediaButtonReceiver(pendingIntent)
			mediaSession.isActive = true;
		}
		setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING)
		createSilencePlayer(context)
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
		handler = null
		callback = null
	}

	private fun setMediaPlaybackState(state: Int) {
		val builder = PlaybackStateCompat.Builder()
		if (state == PlaybackStateCompat.STATE_PLAYING) {
			builder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PAUSE)
		} else {
			builder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PLAY)
		}
		builder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f)
		mediaSession?.setPlaybackState(builder.build())
	}

	private fun createSilencePlayer(context: Context) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
		silencePlayer = SilencePlayerImpl21(context)
	}

}
