package com.zello.sdk;

public class Contact {

	String _name;
	String _fullName;
	String _displayName;
	ContactType _type = ContactType.USER;
	ContactStatus _status = ContactStatus.OFFLINE;
	String _statusMessage;
	int _usersCount;
	int _usersTotal;
	String _title;
	boolean _muted;

	public void reset() {
		_name = null;
		_fullName = null;
		_displayName = null;
		_type = ContactType.USER;
		_status = ContactStatus.OFFLINE;
		_statusMessage = null;
		_usersCount = 0;
		_usersTotal = 0;
		_title = null;
		_muted = false;
	}

	@Override
	public Contact clone() {
		Contact contact = new Contact();
		copyTo(contact);
		return contact;
	}

	public void copyTo(Contact contact) {
		if (contact != null) {
			contact._name = _name;
			contact._fullName = _fullName;
			contact._displayName = _displayName;
			contact._type = _type;
			contact._status = _status;
			contact._statusMessage = _statusMessage;
			contact._usersCount = _usersCount;
			contact._usersTotal = _usersTotal;
			contact._title = _title;
			contact._muted = _muted;
		}
	}

	public String getName() {
		return _name;
	}

	public String getFullName() {
		return _fullName;
	}

	public String getDisplayName() {
		return _displayName;
	}

	public ContactType getType() {
		return _type;
	}

	public ContactStatus getStatus() {
		return _status;
	}

	public String getStatusMessage() {
		return _statusMessage;
	}

	public int getUsersCount() {
		return _usersCount;
	}

	public int getUsersTotal() {
		return _usersTotal;
	}

	public String getTitle() {
		return _title;
	}

	public boolean getMuted() {
		return _muted;
	}

}
