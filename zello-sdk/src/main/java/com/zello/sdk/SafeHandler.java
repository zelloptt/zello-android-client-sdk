package com.zello.sdk;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;

public class SafeHandler<T extends SafeHandlerEvents> extends Handler {

	private final @NonNull WeakReference<T> _t;

	public SafeHandler(@NonNull T t, @NonNull Context context) {
		super(context.getMainLooper());
		_t = new WeakReference<T>(t);
	}

	@Override
	public void handleMessage(@NonNull Message message) {
		T t = _t.get();
		if (t != null) {
			t.handleMessageFromSafeHandler(message);
		}
	}

}
