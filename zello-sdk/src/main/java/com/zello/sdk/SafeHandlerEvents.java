package com.zello.sdk;

import android.os.Message;

import androidx.annotation.NonNull;

/**
 * <p>
 *     Callback events used with {@link SafeHandler}.
 * </p>
 */
public interface SafeHandlerEvents {

	/**
	 * <p>
	 *     Method that's implements handling of a {@link Message} routed through an associated {@link SafeHandler}.
	 * </p>
	 * @param message Message object
	 */
	void handleMessageFromSafeHandler(@NonNull Message message);

}
