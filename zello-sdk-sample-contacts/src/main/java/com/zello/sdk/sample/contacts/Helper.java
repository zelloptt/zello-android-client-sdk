package com.zello.sdk.sample.contacts;

import android.os.Build;

import java.lang.reflect.Field;

public class Helper {

	private static int _version = -1;

	private static int getApiLevel() {
		if (_version < 0) {
			try {
				Field SDK_INT_field = Build.VERSION.class.getField("SDK_INT");
				_version = (Integer) SDK_INT_field.get(null);
			} catch (Exception ignored) {
				_version = 3;
			}
		}
		return _version;
	}

}
