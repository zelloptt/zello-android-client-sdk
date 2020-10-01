package com.zello.sdk;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Build;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@SuppressWarnings("WeakerAccess")
class Util {

	// Known compatible Zello app package names sorted in the order or preference
	private final static CompatiblePackageInfo[] knownCompatiblePackages = {
			new CompatiblePackageInfo("com.loudtalks", 2600519),
			new CompatiblePackageInfo("net.loudtalks", 0),
			new CompatiblePackageInfo("com.pttsdk", 0)
	};

	// Known Zello app service names sorted in the order or preference
	private final static String[] knownServiceNames = {
			"com.zello.ui.Svc",
			"com.loudtalks.client.ui.Svc"
	};

	public static @Nullable String toLowerCaseLexicographically(@Nullable CharSequence s) {
		if (s == null) {
			return null;
		}
		char[] c = new char[s.length()];
		for (int i = 0; i < s.length(); ++i) {
			c[i] = Character.toLowerCase(Character.toUpperCase(s.charAt(i)));
		}
		return new String(c);
	}

	public static @Nullable String toUpperCaseLexicographically(@Nullable CharSequence s) {
		if (s == null) {
			return null;
		}
		char[] c = new char[s.length()];
		for (int i = 0; i < s.length(); ++i) {
			c[i] = Character.toUpperCase(s.charAt(i));
		}
		return new String(c);
	}

	public static @NonNull String emptyIfNull(@Nullable String s) {
		return s == null ? "" : s;
	}

	public static @Nullable String nullIfEmpty(@Nullable String s) {
		return s == null || s.isEmpty() ? null : s;
	}

	public static boolean isNullOrEmpty(@Nullable String s) {
		return s == null || s.isEmpty();
	}

	public static @NonNull String generateUuid() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	public static int getApiLevel() {
		return Build.VERSION.SDK_INT;
	}

	/**
	 * Check if two package names are the same.
	 *
	 * @param packageName1 Package name #1
	 * @param packageName2 Package name #2
	 * @return True if package names are the same
	 */
	public static boolean samePackageNames(@Nullable String packageName1, @Nullable String packageName2) {
		return emptyIfNull(toLowerCaseLexicographically(packageName1)).equals(emptyIfNull(toLowerCaseLexicographically(packageName2)));
	}

	/**
	 * Obtain information about a Zello package.
	 *
	 * @param context App context
	 * @param packageName Optional package name; null to find the most suitable package
	 */
	public static @Nullable PackageInfo findPackageInfo(@Nullable Context context, @Nullable String packageName) {
		if (context == null) {
			return null;
		}
		// If a preferred package name is supplied, try to look it up
		if (!Util.isNullOrEmpty(packageName)) {
			return tryPackageInfo(context, packageName, 0);
		}
		// If a preferred package name is not supplied, find the most suitable package
		for (CompatiblePackageInfo info : knownCompatiblePackages) {
			PackageInfo packageInfo = tryPackageInfo(context, info.packageName, info.minVersion);
			if (packageInfo != null) {
				return packageInfo;
			}
		}
		return null;
	}

	/**
	 * Attempt to obtain package info.
	 *
	 * @param context App context
	 * @param packageName Package name
	 * @return Package info or null if unavailable or incompatible
	 */
	private static @Nullable PackageInfo tryPackageInfo(@NonNull Context context, @NonNull String packageName, int minVersion) {
		android.content.pm.PackageInfo pi;
		try {
			pi = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SERVICES);
		} catch (PackageManager.NameNotFoundException ignored) {
			return null;
		}
		if (pi == null || pi.versionCode < minVersion) {
			return null;
		}
		String serviceClassName = findServiceClassName(pi);
		if (serviceClassName == null) {
			return null;
		}
		return new PackageInfo(packageName, serviceClassName);
	}

	/**
	 * Look up a Zello service class name.
	 *
	 * @param pi Package info that includes information about services
	 * @return Service class name or null
	 */
	private static @Nullable String findServiceClassName(@NonNull android.content.pm.PackageInfo pi) {
		if (pi.services == null) {
			return null;
		}
		// Start by looking for the most preferred service name
		for (String name : knownServiceNames) {
			// Cycle through all exported services
			for (ServiceInfo info : pi.services) {
				if (info.name.equals(name)) {
					return name;
				}
			}
		}
		return null;
	}

}
