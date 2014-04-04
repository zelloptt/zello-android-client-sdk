package com.zello.sdk;

import android.os.Handler;

public class ContactsObserver03 extends ContactsObserver {

	public ContactsObserver03(Contacts contacts, Handler handler) {
		super(contacts, handler);
	}

	@Override
	public void onChange(boolean selfChange) {
		invalidate();
	}

}
