package com.zello.sdk.sample;

import android.app.Application;

import com.zello.sdk.Log;
import com.zello.sdk.LogEvents;
import com.zello.sdk.Zello;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class App extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		// Route SDK messages to Android log
		Log.setEvents(new LogEvents() {
			@Override
			public void onWriteInfo(@NotNull String s) {
				android.util.Log.i("(sdk)", s);
			}

			@Override
			public void onWriteError(@NotNull String s, @Nullable Throwable throwable) {
				android.util.Log.e("(sdk)", s);
			}
		});

		// Automatically choose the app to connect to in the following order of preference: com.loudtalks, net.loudtalks, com.pttsdk
		// Alternatively, connect to a preferred app by supplying a package name, for example: Zello.getInstance().configure("net.loudtalks", this)
		Zello.getInstance().configure(this);
	}

}
