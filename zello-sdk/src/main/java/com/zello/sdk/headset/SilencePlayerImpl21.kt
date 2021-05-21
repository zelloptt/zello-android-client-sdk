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
 * A implementation of [SilencePlayer] suitable for API 21+.
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
			context.resources.openRawResourceFd(R.raw.silence)?.let { descriptor ->
				asset = descriptor
				replay()
			}
		} catch (t: Throwable) {
			Log.e("${TAG} Can't create a player", t)
		}
	}

	override fun replay() {
		player?.release()
		asset?.let { descriptor ->
			player = MediaPlayer().also { player ->
				player.setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
				player.setOnPreparedListener {
					it.start()
				}
				player.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
				player.prepareAsync()
			}
		}
	}

	override fun release() {
		player?.release()
		player = null
		asset?.close()
		asset = null
	}

}
