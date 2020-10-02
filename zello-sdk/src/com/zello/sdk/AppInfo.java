package com.zello.sdk;

import androidx.annotation.NonNull;

class AppInfo {

	public final @NonNull String packageName;

	public final @NonNull String serviceClassName;

	public AppInfo(@NonNull String packageName, @NonNull String serviceClassName) {
		this.packageName = packageName;
		this.serviceClassName = serviceClassName;
	}

}
