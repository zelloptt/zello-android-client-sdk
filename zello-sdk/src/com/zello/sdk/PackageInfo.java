package com.zello.sdk;

import androidx.annotation.NonNull;

class PackageInfo {

	public final @NonNull String packageName;

	public final @NonNull String serviceClassName;

	public PackageInfo(@NonNull String packageName, @NonNull String serviceClassName) {
		this.packageName = packageName;
		this.serviceClassName = serviceClassName;
	}

}
