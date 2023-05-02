package com.zello.sdk

import android.content.Intent
import android.os.Build
import android.os.Parcelable

object BundleUtils {

	inline fun <reified T : Parcelable?> getParcelableExtra(intent: Intent?, name: String): T? =
		getParcelableExtra(intent, name, T::class.java)

	/**
	 * Wrapper method for getParcelable that will call the proper implementation
	 * based on the current sdk version
	 */
	@Suppress("DEPRECATION")
	@JvmStatic
	fun <T : Parcelable?> getParcelableExtra(intent: Intent?, name: String, clazz: Class<T>): T? =
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
			intent?.getParcelableExtra(name, clazz)
		else
			intent?.getParcelableExtra(name) as? T

}
