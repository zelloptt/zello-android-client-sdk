package com.zello.sdk;

import androidx.annotation.NonNull;

/**
 * A helper class that stores information about a compatible package name
 * and an optional minimum app version.
 */
class CompatiblePackageInfo {

	public final @NonNull String packageName;

	public final int minVersion;

	public CompatiblePackageInfo(@NonNull String packageName, int minVersion) {
		this.packageName = packageName;
		this.minVersion = minVersion;
	}

}
