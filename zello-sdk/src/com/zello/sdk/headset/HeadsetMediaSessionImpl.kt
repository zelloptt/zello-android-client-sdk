package com.zello.sdk.headset

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import androidx.media.session.MediaButtonReceiver

/**
 * [HeadsetMediaSession] implementation that's suitable for Android API 4-30.
 */
class HeadsetMediaSessionImpl : HeadsetMediaSession {

	private var mediaSession: MediaSessionCompat? = null

	companion object {
		private const val TAG = "(HeadsetMediaSessionImpl)"
	}

	/**
	 * Start the session.
	 */
	override fun start(context: Context, onKeyEvent: (KeyEvent) -> Unit) {
		val mediaButtonReceiver = ComponentName(context.applicationContext, HeadsetBroadcastReceiver::class.java)
		mediaSession = MediaSessionCompat(context.applicationContext, "zello sdk", mediaButtonReceiver, null).also { mediaSession ->
			val callback = object : MediaSessionCompat.Callback() {
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
			setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
			setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED);
		}
		HeadsetBroadcastReceiver.onKeyEvent = onKeyEvent
	}

	/**
	 * Stop the session.
	 */
	override fun stop() {
		mediaSession?.release()
		mediaSession = null
		HeadsetBroadcastReceiver.onKeyEvent = null
	}

	private fun setMediaPlaybackState(state: Int) {
		val playbackstateBuilder = PlaybackStateCompat.Builder()
		if (state == PlaybackStateCompat.STATE_PLAYING) {
			playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PAUSE)
		} else {
			playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PLAY)
		}
		playbackstateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f)
		mediaSession?.setPlaybackState(playbackstateBuilder.build())
	}

}
