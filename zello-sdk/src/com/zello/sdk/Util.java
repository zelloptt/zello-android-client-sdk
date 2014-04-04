package com.zello.sdk;

import android.os.Build;

import java.lang.reflect.Field;
import java.util.UUID;

class Util {

	static String toLowerCaseLexicographically(CharSequence s) {
		if (s != null) {
			char[] c = new char[s.length()];
			for (int i = 0; i < s.length(); ++i) {
				c[i] = Character.toLowerCase(Character.toUpperCase(s.charAt(i)));
			}
			return new String(c);
		}
		return null;
	}

	static String toUpperCaseLexicographically(CharSequence s) {
		if (s != null) {
			char[] c = new char[s.length()];
			for (int i = 0; i < s.length(); ++i) {
				c[i] = Character.toUpperCase(s.charAt(i));
			}
			return new String(c);
		}
		return null;
	}

	static String emptyIfNull(String s) {
		return s == null ? "" : s;
	}

	static String generateUuid() {
		String s = UUID.randomUUID().toString().replace("-", "");
		return s;
	}

	static int _version = -1;

	public static int getApiLevel() {
		int version = _version;
		if (version < 0) {
			try {
				Field SDK_INT_field = Build.VERSION.class.getField("SDK_INT");
				version = (Integer) SDK_INT_field.get(null);
			} catch (Exception e) {
				version = 3;
			}
			_version = version;
		}
		return version;
	}

}
