package com.zello.sdk;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Build;

import java.util.Locale;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class Util {

	// Known compatible Zello app package names sorted in the order or preference
	private final static CompatibleAppInfo[] knownCompatiblePackages = {
			new CompatibleAppInfo("com.loudtalks", true),
			new CompatibleAppInfo("net.loudtalks", false),
			new CompatibleAppInfo("com.pttsdk", false)
	};

	// Known Zello app service names sorted in the order or preference
	private final static String[] knownServiceNames = {
			"com.zello.ui.Svc",
			"com.loudtalks.client.ui.Svc"
	};

	private final static String sdkMetaDataName = "com.zello.SDK";

	public static @NonNull String emptyIfNull(@Nullable String s) {
		return s == null ? "" : s;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
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
		return emptyIfNull(packageName1).toLowerCase(Locale.ROOT).equals(emptyIfNull(packageName2).toLowerCase(Locale.ROOT));
	}

	/**
	 * Obtain information about a Zello package.
	 *
	 * @param context App context
	 * @param packageName Optional package name; null to find the most suitable package
	 */
	public static @Nullable AppInfo findAppInfo(@Nullable Context context, @Nullable String packageName) {
		if (context == null) {
			return null;
		}
		// If a preferred package name is supplied, try to look it up
		if (!Util.isNullOrEmpty(packageName)) {
			return tryPackageInfo(context, packageName, false);
		}
		// If a preferred package name is not supplied, find the most suitable package
		for (CompatibleAppInfo info : knownCompatiblePackages) {
			AppInfo packageInfo = tryPackageInfo(context, info.packageName, info.requireMetaData);
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
	private static @Nullable AppInfo tryPackageInfo(@NonNull Context context, @NonNull String packageName, boolean requireMetaData) {
		PackageInfo pi;
		try {
			pi = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SERVICES);
		} catch (PackageManager.NameNotFoundException ignored) {
			return null;
		}
		if (pi == null) {
			return null;
		}
		if (requireMetaData) {
			ApplicationInfo ai;
			try {
				ai = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
			} catch (PackageManager.NameNotFoundException ignored) {
				return null;
			}
			if (ai.metaData == null) {
				return null;
			}
			Object metaData = ai.metaData.get(sdkMetaDataName);
			if (!(metaData instanceof Boolean) || !((Boolean) metaData)) {
				return null;
			}
		}
		String serviceClassName = findServiceClassName(pi);
		if (serviceClassName == null) {
			return null;
		}
		return new AppInfo(packageName, serviceClassName);
	}

	/**
	 * Look up a Zello service class name.
	 *
	 * @param pi Package info that includes information about services
	 * @return Service class name or null
	 */
	private static @Nullable String findServiceClassName(@NonNull PackageInfo pi) {
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
