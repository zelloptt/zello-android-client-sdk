package com.zello.sdk;

import androidx.annotation.NonNull;

/**
 * A helper class that stores information about a compatible package name
 * and an optional meta data.
 */
class CompatibleAppInfo {

	public final @NonNull String packageName;

	public final boolean requireMetaData;

	public CompatibleAppInfo(@NonNull String packageName, boolean requireMetaData) {
		this.packageName = packageName;
		this.requireMetaData = requireMetaData;
	}

}
