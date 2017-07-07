package com.zello.sdk;

import android.os.Handler;

@SuppressWarnings({"WeakerAccess", "unused"})
class ContactsObserver03 extends ContactsObserver {

	ContactsObserver03(Contacts contacts, Handler handler) {
		super(contacts, handler);
	}

	@Override
	public void onChange(boolean selfChange) {
		invalidate();
	}

}
