package com.zello.sdk;

import android.os.Message;

import androidx.annotation.NonNull;

@SuppressWarnings("unused")
interface SafeHandlerEvents {

	void handleMessageFromSafeHandler(@NonNull Message message);

}
