package com.zello.sdk;

import android.database.ContentObserver;
import android.os.Build;
import android.os.Handler;

public abstract class ContactsObserver {

	public static ContentObserver create(Events events, Handler handler) {
		int api = Util.getApiLevel();
		if (api >= Build.VERSION_CODES.JELLY_BEAN) {
			return new ContactsObserver16(events, handler);
		} else {
			return new ContactsObserver03(events, handler);
		}
	}

}
