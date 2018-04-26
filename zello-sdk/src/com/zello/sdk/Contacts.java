package com.zello.sdk;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

/**
 * <p>
 *     The <code>Contacts</code> class represents the contacts of the current user.
 * </p>
 * <p>
 *     To use, get the current snapshot of <code>Contacts</code> using the {@link Zello#getContacts()} method. For specific usage, please see the sample projects.
 * </p>
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Contacts {

	//region Private Properties

	private static final String _authoritySuffix = ".provider";
	private static final String _contactsPath = "/contacts";
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
	private static final String _columnNoDisconnect = "nodisconnect";

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
	private int _indexMuted;
	private int _indexNoDisconnect; // Not available if the client app is old - has to be at least 3.19

	private static Uri _uri;

	//endregion

	//region Package Private Methods

	Contacts(String packageName, Context context, Handler handler) {
		_context = context;
		_observer = ContactsObserver.create(this, handler);
		Uri uri = _uri;
		if (uri == null) {
			uri = Uri.parse("content://" + packageName + _authoritySuffix + _contactsPath);
			_uri = uri;
		}
		query();
	}

	void close() {
		_context = null;
		clean();
		ContactsObserver observer = _observer;
		if (observer != null) {
			observer.close();
		}
		_observer = null;
	}

	void invalidate() {
		_invalid = true;

		for (Events event : Zello.getInstance().events) {
			event.onContactsChanged();
		}
	}

	//endregion

	//region Public Methods

	//region Getters

	/**
	 * <p>
	 *     Returns the number of contacts in the list.
	 * </p>
	 * <p>
	 *     NB: This method may take nontrivial time to execute, so do not call it from the UI thread.
	 * </p>
	 * @return the number of contacts for the user.
     */
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

	/**
	 * <p>
	 *     Returns the <code>Contact</code> at the specified index.
	 * </p>
	 * <p>
	 *     NB: This method may take nontrivial time to execute, so do not call it from the UI thread.
	 * </p>
	 * @param index Index indicating which <code>Contact</code> to retrieve.
	 * @return <code>Contact</code> at the specified index.
     */
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
					case GROUP:
					case CONVERSATION: {
						contact._usersCount = cursor.getInt(_indexUsersCount);
						contact._usersTotal = cursor.getInt(_indexUsersTotal);
						break;
					}
				}
				contact._noDisconnect = (contact._type != ContactType.CHANNEL && contact._type != ContactType.GROUP && contact._type != ContactType.CONVERSATION) ||
						(_indexNoDisconnect >= 0 && cursor.getInt(_indexNoDisconnect) != 0);
				return contact;
			} catch (Throwable t) {
				Log.i("zello sdk", "Error in Contacts.getItem: " + t.toString());
			}
		}
		return null;
	}

	//endregion

	//endregion

	//region Private Methods

	private void query() {
		Context context = _context;
		if (context != null) {
			Cursor cursor = null;
			_indexNoDisconnect = -1;
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
			if (cursor != null) {
				try {
					_indexNoDisconnect = cursor.getColumnIndex(_columnNoDisconnect);
				} catch (Throwable ignore) {
				}
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

	//endregion

}
