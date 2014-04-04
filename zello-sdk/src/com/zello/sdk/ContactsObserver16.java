package com.zello.sdk;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

public class ContactsObserver16 extends ContentObserver {

	private Events _events;

	public ContactsObserver16(Events events, Handler handler) {
		super(handler);
		_events = events;
	}

	@Override
	public void onChange(boolean selfChange, Uri uri) {
		Events events = _events;
		if (events != null) {
			events.onContactsChanged();
		}
	}

}
