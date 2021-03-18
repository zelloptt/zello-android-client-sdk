package com.zello.sdk;

import androidx.annotation.NonNull;

/**
 * A helper class that holds information about an app that was found
 * to be capable of accepting an SDK connection.
 */
class AppInfo {

	public final @NonNull String packageName;

	public final @NonNull String serviceClassName;

	public AppInfo(@NonNull String packageName, @NonNull String serviceClassName) {
		this.packageName = packageName;
		this.serviceClassName = serviceClassName;
	}

}
