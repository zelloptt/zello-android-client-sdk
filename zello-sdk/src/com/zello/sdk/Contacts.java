package com.zello.sdk;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
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
	private static final String _columnMuted = "muted";

	private Events _events;
	private ContactsObserver _observer;
	private Cursor _cursor;
	private Context _context;
	private SafeHandler<Sdk> _handler;
	private boolean _valid;
	private int _indexName;
	private int _indexFullName;
	private int _indexDisplayName;
	private int _indexStatusMessage;
	private int _indexType;
	private int _indexStatus;
	private int _indexUsersCount;
	private int _indexUsersTotal;
	private int _indexTitle;
	private int _indexMuted;

	private static Uri _uri;

	/* package */ Contacts(String packageName, Context context, SafeHandler<Sdk> handler, Events events) {
		synchronized (this) {
			_events = events;
			_context = context;
			_handler = handler;
			_observer = ContactsObserver.create(this, handler);
		}
		Uri uri = _uri;
		if (uri == null) {
			uri = Uri.parse("content://" + packageName + _authoritySuffix + _contactsPath);
			_uri = uri;
		}
	}

	/* package */ void close() {
		ContactsObserver observer;
		Cursor cursor;
		synchronized (this) {
			cursor = _cursor;
			observer = _observer;
			_cursor = null;
			_observer = null;
			_context = null;
			_handler = null;
			_events = null;
		}
		closeCursor(cursor, observer);
		if (observer != null) {
			observer.close();
		}
	}

	// Have to requery
	/* package */ void invalidate() {
		Events events;
		synchronized (this) {
			events = _events;
			if (events != null) {
				_valid = false;
			}
		}
		if (events != null) {
			events.onContactsChanged();
		}
	}

	// Don't need to requery
	/* package */ void update() {
		Events events;
		synchronized (this) {
			events = _events;
		}
		if (events != null) {
			events.onContactsChanged();
		}
	}

	private void query() {
		final Context context;
		final ContactsObserver observer;
		boolean valid;
		synchronized (this) {
			context = _context;
			observer = _observer;
			valid = _valid;
			_valid = true;
		}
		if (context != null && !valid) {
			new Thread() {
				@Override
				public void run() {
					if (context == _context) {
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
							_indexMuted = cursor.getColumnIndex(_columnMuted);
							cursor.registerContentObserver(observer);
							final Cursor cursorNew = cursor;
							synchronized (Contacts.this) {
								if (_handler != null && _handler.post(new Runnable() {
									@Override
									public void run() {
										Cursor cursorOld = null;
										Events events = null;
										synchronized (Contacts.this) {
											if (_handler != null) {
												cursorOld = _cursor;
												events = _events;
												if (events != null) {
													_cursor = cursorNew;
												} else {
													_cursor = null;
												}
											}
										}
										if (events != null) {
											events.onContactsChanged();
										}
										closeCursor(cursorOld, observer);
									}
								})) {
									return;
								}
							}
							closeCursor(cursor, observer);
						} catch (Throwable t) {
							closeCursor(cursor, null);
							Log.i("zello sdk", "Error in Contacts.Contacts: " + t.toString());
						}
					}
				}
			}.start();
		}
	}

	public int getCount() {
		query();
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
		query();
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
				contact._muted = cursor.getInt(_indexMuted) != 0;
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

	private static void closeCursor(Cursor cursor, ContactsObserver unregisterObserver) {
		if (cursor != null) {
			if (unregisterObserver != null) {
				try {
					cursor.unregisterContentObserver(unregisterObserver);
				} catch (Throwable t) {
					Log.i("zello sdk", "Error in Contacts.close: " + t.toString());
				}
			}
			try {
				cursor.close();
			} catch (Throwable t) {
				Log.i("zello sdk", "Error in Contacts.close: " + t.toString());
			}
		}
	}

}
