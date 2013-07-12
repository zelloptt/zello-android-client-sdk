package com.zello.sdk;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

class SafeHandler<T extends SafeHandlerEvents> extends Handler {

	private final WeakReference<T> _t;

	public SafeHandler(T t) {
		_t = new WeakReference<T>(t);
	}

	@Override
	public void handleMessage(Message message) {
		T t = _t.get();
		if (t != null) {
			t.handleMessageFromSafeHandler(message);
		}
	}

}
