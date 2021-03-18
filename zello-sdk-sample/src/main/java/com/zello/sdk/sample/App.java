package com.zello.sdk.sample;

import android.app.Application;

import com.zello.sdk.Log;
import com.zello.sdk.Logger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class App extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		// Route SDK messages to Android log
		Log.setLogger(new Logger() {
			@Override
			public void i(@NotNull String s) {
				android.util.Log.i("zello-sdk", s);
			}

			@Override
			public void e(@NotNull String s, @Nullable Throwable throwable) {
				android.util.Log.e("zello-sdk", s);
			}
		});
	}

}
