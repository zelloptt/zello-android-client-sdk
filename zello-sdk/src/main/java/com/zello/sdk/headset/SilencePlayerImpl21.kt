package com.zello.sdk.headset

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import androidx.annotation.RequiresApi
import com.zello.sdk.Log
import com.zello.sdk.R

/**
 * An implementation of [SilencePlayer] suitable for API 21+.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class SilencePlayerImpl21(context: Context) : SilencePlayer {

	companion object {
		private const val TAG = "(SilencePlayerImpl26)"
	}

	private var asset: AssetFileDescriptor? = null
	private var player: MediaPlayer? = null

	init {
		try {
			asset = context.resources.openRawResourceFd(R.raw.silence)
		} catch (t: Throwable) {
			Log.e("$TAG Can't open a resource", t)
		}
	}

	override fun playOnce() {
		play(false)
	}

	override fun playForever() {
		play(true)
	}

	override fun release() {
		player?.release()
		player = null
		asset?.close()
		asset = null
	}

	private fun play(looping: Boolean) {
		player?.release()
		asset?.let { descriptor ->
			player = MediaPlayer().also { player ->
				player.setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
				player.isLooping = looping
				player.setOnPreparedListener {
					it.start()
				}
				player.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
				player.prepareAsync()
			}
		}
	}

}
