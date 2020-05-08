package com.zello.sdk;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

@SuppressWarnings({"WeakerAccess", "unused"})
class Util {

	static @Nullable String toLowerCaseLexicographically(@Nullable CharSequence s) {
		if (s != null) {
			char[] c = new char[s.length()];
			for (int i = 0; i < s.length(); ++i) {
				c[i] = Character.toLowerCase(Character.toUpperCase(s.charAt(i)));
			}
			return new String(c);
		}
		return null;
	}

	static @Nullable String toUpperCaseLexicographically(@Nullable CharSequence s) {
		if (s != null) {
			char[] c = new char[s.length()];
			for (int i = 0; i < s.length(); ++i) {
				c[i] = Character.toUpperCase(s.charAt(i));
			}
			return new String(c);
		}
		return null;
	}

	static @NonNull String emptyIfNull(@Nullable String s) {
		return s == null ? "" : s;
	}

	static @NonNull String generateUuid() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	public static int getApiLevel() {
		return Build.VERSION.SDK_INT;
	}

}
