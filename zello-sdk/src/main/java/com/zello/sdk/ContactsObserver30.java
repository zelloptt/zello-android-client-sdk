package com.zello.sdk;

import android.net.Uri;
import android.os.Handler;

import java.util.Collection;

class ContactsObserver30 extends ContactsObserver16 {

	ContactsObserver30(Contacts contacts, Handler handler) {
		super(contacts, handler);
	}

	@Override
	public void onChange(boolean selfChange, Uri uri, int flags) {
		invalidate();
	}

	@Override
	public void onChange(boolean selfChange, Collection<Uri> uris, int flags) {
		invalidate();
	}

}
