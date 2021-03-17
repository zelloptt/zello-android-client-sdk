package com.zello.sdk

import android.content.Context
import android.os.Message

/**
 * Android-specific timer implementation.
 * @param context Application context
 */
class TimerImpl(context: Context) : Timer, SafeHandlerEvents {

	private val handler = SafeHandler(this, context.applicationContext)

	override fun start(timeoutMs: Long, onDone: () -> Unit) {
		if (timeoutMs < 1) {
			onDone()
			return
		}
		handler.sendMessageDelayed(handler.obtainMessage(0, onDone), timeoutMs)
	}

	override fun stop() {
		handler.removeMessages(0)
	}

	override val started: Boolean
		get() {
			return handler.hasMessages(0)
		}

	@Suppress("UNCHECKED_CAST")
	override fun handleMessageFromSafeHandler(message: Message) {
		(message.obj as? () -> Unit)?.invoke()
	}

}
