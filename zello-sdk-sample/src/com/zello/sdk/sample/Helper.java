package com.zello.sdk.sample;

import android.app.Activity;
import android.os.Build;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Helper {

	private static Method _invalidateOptionsMenu = null;
	private static boolean _triedInvalidateOptionsMenu = false;

	public static void invalidateOptionsMenu(Activity activity) {
		if (activity != null && getApiLevel() >= 11) {
			if (!_triedInvalidateOptionsMenu) {
				try {
					_invalidateOptionsMenu = Activity.class.getMethod("invalidateOptionsMenu", new Class[]{});
				} catch (NoSuchMethodException e) {
				}
				_triedInvalidateOptionsMenu = true;
			}
			if (_invalidateOptionsMenu != null) {
				try {
					_invalidateOptionsMenu.invoke(activity);
				} catch (IllegalArgumentException e) {
				} catch (IllegalAccessException e) {
				} catch (InvocationTargetException e) {
				}
			}
		}
	}

	static int _version = -1;

	public static int getApiLevel() {
		if (_version < 0) {
			try {
				Field SDK_INT_field = Build.VERSION.class.getField("SDK_INT");
				_version = (Integer) SDK_INT_field.get(null);
			} catch (Exception e) {
				_version = 3;
			}
		}
		return _version;
	}

}
