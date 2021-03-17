package com.zello.sdk;

import android.database.ContentObserver;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

abstract class ContactsObserver extends ContentObserver {

	private @Nullable Contacts _contacts;

	ContactsObserver(@Nullable Contacts contacts, @Nullable Handler handler) {
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

	static @NonNull ContactsObserver create(Contacts contacts, Handler handler) {
		int api = Util.getApiLevel();
		if (api < Build.VERSION_CODES.R) {
			return new ContactsObserver16(contacts, handler);
		} else {
			return new ContactsObserver30(contacts, handler);
		}
	}

}
