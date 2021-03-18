package com.zello.sdk.headset

/**
 * An abstraction for a silence player.
 * The only reason why a dummy silence player may be needed is
 * for the purpose of making our own media session the one that's
 * processing the media keys, including the headset hook button,
 * which is important on API 26+ as the app that played audio the last
 * is going to be the one to receive the media keys though its
 * media session.
 */
interface SilencePlayer {

	/**
	 * Release the resources associated with the player.
	 */
	fun release()

}
