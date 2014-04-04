package com.zello.sdk;

import android.database.ContentObserver;
import android.os.Handler;

public class ContactsObserver03 extends ContentObserver {

	private Events _events;

	public ContactsObserver03(Events events, Handler handler) {
		super(handler);
		_events = events;
	}

	@Override
	public void onChange(boolean selfChange) {
		Events events = _events;
		if (events != null) {
			events.onContactsChanged();
		}
	}

}
