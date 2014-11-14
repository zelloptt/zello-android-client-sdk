package com.zello.sdk;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class Contacts {

	private static final String _authoritySuffix = ".provider";
	private static final String _contactsPath = "/contacts";
	//private static final String _contactPath = "/contacts/#";
	private static final String _columnName = "name";
	private static final String _columnFullName = "fullname";
	private static final String _columnDisplayName = "displayname";
	private static final String _columnStatusMessage = "statusmessage";
	private static final String _columnType = "type";
	private static final String _columnStatus = "status";
	private static final String _columnUsersCount = "userscount";
	private static final String _columnUsersTotal = "userstotal";
	private static final String _columnTitle = "title";

	private Events _events;
	private ContactsObserver _observer;
	private Cursor _cursor;
	private Context _context;
	private boolean _invalid;
	private int _indexName;
	private int _indexFullName;
	private int _indexDisplayName;
	private int _indexStatusMessage;
	private int _indexType;
	private int _indexStatus;
	private int _indexUsersCount;
	private int _indexUsersTotal;
	private int _indexTitle;

	private static Uri _uri;

	/* package */ Contacts(String packageName, Context context, Handler handler, Events events) {
		_events = events;
		_context = context;
		_observer = ContactsObserver.create(this, handler);
		Uri uri = _uri;
		if (uri == null) {
			uri = Uri.parse("content://" + packageName + _authoritySuffix + _contactsPath);
			_uri = uri;
		}
		query();
	}

	/* package */ void close() {
		_context = null;
		clean();
		ContactsObserver observer = _observer;
		if (observer != null) {
			observer.close();
		}
		_observer = null;
		_events = null;
	}

	/* package */ void invalidate() {
		_invalid = true;
		Events events = _events;
		if (events != null) {
			events.onContactsChanged();
		}
	}

	private void query() {
		Context context = _context;
		if (context != null) {
			Cursor cursor = null;
			try {
				cursor = context.getContentResolver().query(_uri, null, null, null, null);
				_indexName = cursor.getColumnIndex(_columnName);
				_indexFullName = cursor.getColumnIndex(_columnFullName);
				_indexDisplayName = cursor.getColumnIndex(_columnDisplayName);
				_indexStatusMessage = cursor.getColumnIndex(_columnStatusMessage);
				_indexType = cursor.getColumnIndex(_columnType);
				_indexStatus = cursor.getColumnIndex(_columnStatus);
				_indexUsersCount = cursor.getColumnIndex(_columnUsersCount);
				_indexUsersTotal = cursor.getColumnIndex(_columnUsersTotal);
				_indexTitle = cursor.getColumnIndex(_columnTitle);
				cursor.registerContentObserver(_observer);
			} catch (Throwable t) {
				if (cursor != null) {
					try {
						cursor.close();
					} catch (Throwable ignored) {
					}
					cursor = null;
				}
				Log.i("zello sdk", "Error in Contacts.Contacts: " + t.toString());
			}
			_cursor = cursor;
		}
	}

	private void clean() {
		Cursor cursor = _cursor;
		_cursor = null;
		if (cursor != null) {
			try {
				cursor.unregisterContentObserver(_observer);
			} catch (Throwable t) {
				Log.i("zello sdk", "Error in Contacts.close: " + t.toString());
			}
			try {
				cursor.close();
			} catch (Throwable t) {
				Log.i("zello sdk", "Error in Contacts.close: " + t.toString());
			}
		}
	}

	private void check() {
		if (_invalid) {
			_invalid = false;
			clean();
			query();
		}
	}

	public int getCount() {
		check();
		Cursor cursor = _cursor;
		if (cursor != null) {
			try {
				return cursor.getCount();
			} catch (Throwable t) {
				Log.i("zello sdk", "Error in Contacts.getCount: " + t.toString());
			}
		}
		return 0;
	}

	public Contact getItem(int index) {
		check();
		Cursor cursor = _cursor;
		if (cursor != null) {
			cursor.moveToPosition(index);
			Contact contact = new Contact();
			try {
				contact._name = cursor.getString(_indexName);
				contact._fullName = cursor.getString(_indexFullName);
				contact._displayName = cursor.getString(_indexDisplayName);
				contact._type = Sdk.intToContactType(cursor.getInt(_indexType));
				contact._status = Sdk.intToContactStatus(cursor.getInt(_indexStatus));
				contact._title = cursor.getString(_indexTitle);
				switch (contact._type) {
					case USER:
					case GATEWAY: {
						contact._statusMessage = cursor.getString(_indexStatusMessage);
						break;
					}
					case CHANNEL: {
						contact._usersCount = cursor.getInt(_indexUsersCount);
						break;
					}
					case GROUP: {
						contact._usersCount = cursor.getInt(_indexUsersCount);
						contact._usersTotal = cursor.getInt(_indexUsersTotal);
						break;
					}
				}
				return contact;
			} catch (Throwable t) {
				Log.i("zello sdk", "Error in Contacts.getItem: " + t.toString());
			}
		}
		return null;
	}

}
