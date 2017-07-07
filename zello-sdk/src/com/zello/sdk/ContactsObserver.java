package com.zello.sdk;

import android.database.ContentObserver;
import android.os.Build;
import android.os.Handler;

@SuppressWarnings({"WeakerAccess", "unused"})
abstract class ContactsObserver extends ContentObserver {

	private Contacts _contacts;

	ContactsObserver(Contacts contacts, Handler handler) {
		super(handler);
		_contacts = contacts;
	}

	void close() {
		_contacts = null;
	}

	protected void invalidate() {
		Contacts contacts = _contacts;
		if (contacts != null) {
			contacts.invalidate();
		}
	}

	static ContactsObserver create(Contacts contacts, Handler handler) {
		int api = Util.getApiLevel();
		if (api >= Build.VERSION_CODES.JELLY_BEAN) {
			return new ContactsObserver16(contacts, handler);
		} else {
			return new ContactsObserver03(contacts, handler);
		}
	}

}
