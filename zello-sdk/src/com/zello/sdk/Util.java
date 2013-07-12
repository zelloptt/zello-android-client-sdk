package com.zello.sdk;

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

}
