package com.zello.sdk;

/**
 * The Contact class acts as a representation of a single contact for the user. A contact has a type attached to it (ie. ContactType)
 */
public class Contact {

	//region Package Private Properties

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
	boolean _noDisconnect;

	//endregion

	/**
	 * The reset() method resets the Contact instance back to the default values.
	 */
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
		_noDisconnect = false;
	}

	@Override
	public Contact clone() {
		Contact contact = new Contact();
		copyTo(contact);
		return contact;
	}

	/**
	 * The getName() method returns the name of the Contact.
	 * @return Nullable; Name of the Contact.
     */
	public String getName() {
		return _name;
	}

	/**
	 * The getFullName() method returns the full name of the Contact.
	 * @return Nullable; Full name of the Contact.
     */
	public String getFullName() {
		return _fullName;
	}

	/**
	 * The getDisplayName() method returns the display name for the Contact.
	 * @return Nullable; Display Name for the Contact.
     */
	public String getDisplayName() {
		return _displayName;
	}

	/**
	 * The getType() method returns the ContactType for the Contact.
	 * @return ContactType for the Contact.
     */
	public ContactType getType() {
		return _type;
	}

	/**
	 * The getStatus() method returns the ContactStatus for the Contact.
	 * @return ContactStatus for the Contact.
     */
	public ContactStatus getStatus() {
		return _status;
	}

	/**
	 * The getStatusMessage() method returns the custom status message for the Contact.
	 * @return Nullable; The status message for the Contact.
     */
	public String getStatusMessage() {
		return _statusMessage;
	}

	/**
	 * <pre>
	 * The getUsersCount() method returns the number of online users under the Contact.
	 * </pre>
	 * <pre>
	 * For ContactType.USER and ContactType.GATEWAY, this value will be 0.
	 * For ContactType.CHANNEL and ContactType.GROUP, this value will be the number of users online in the channel or group, respectively.
	 * </pre>
	 * @return number of online users under the Contact for the ContactType.
     */
	public int getUsersCount() {
		return _usersCount;
	}

	/**
	 * <pre>
	 * The getUsersTotal() method returns the number of total users under the Contact.
	 * </pre>
	 * <pre>
	 * For ContactType.USER and ContactType.GATEWAY, this value will be 0.
	 * </pre>
	 * @return number of total users under the Contact for the ContactType.
	 */
	public int getUsersTotal() {
		return _usersTotal;
	}

	/**
	 * The getTitle() method returns the title for the Contact.
	 * @return Nullable; The title for the contact.
     */
	public String getTitle() {
		return _title;
	}

	/**
	 * The getMuted() method determines whether the Contact is muted or not.
	 * @return boolean indicating if the Contact is muted.
     */
	public boolean getMuted() {
		return _muted;
	}

	/**
	 * The getNoDisconnect() method determines whether the Contact has the no disconnect setting enabled or not.
	 * @return boolean indicating if the Contact has the no disconnect setting enabled.
     */
	public boolean getNoDisconnect() {
		return _noDisconnect;
	}

	//region Package Private Methods

	void copyTo(Contact contact) {
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
			contact._noDisconnect = _noDisconnect;
		}
	}

	//endregion

}
