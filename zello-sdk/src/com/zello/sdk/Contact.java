package com.zello.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * The <code>Contact</code> class represents a contact of the current user.
 * A Contact can be another user, a radio gateway, a group channel or a dynamic channel (see {@link ContactType}).
 */
@SuppressWarnings({"WeakerAccess", "unused"})
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
	 * <p>
	 *     Resets the <code>Contact</code> instance back to the default values.
	 * </p>
	 * <p>
	 *     This method only resets the values for this copied instance of the <code>Contact</code>.
	 * </p>
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
	public @NonNull Contact clone() {
		Contact contact = new Contact();
		copyTo(contact);
		return contact;
	}

	/**
	 * Check if the object is empty.
	 * @return true if the object represents a valid contact.
	 */
	public boolean isValid() {
		return _name != null && !_name.isEmpty();
	}

	/**
	 * <p>
	 *     Returns the identifying name of the <code>Contact</code>.
	 * </p>
	 * <p>
	 *     For {@link ContactType#CHANNEL and @link ContactType#GROUP} the value is the channel name.
	 *     For {@link ContactType#USER} and {@link ContactType#GATEWAY} the value is the username.
	 *     For {@link ContactType#CONVERSATION} the value is a unique conversation ID.
	 * </p>
	 * @return Name of the <code>Contact</code>.
     */
	public @Nullable String getName() {
		return _name;
	}

	/**
	 * Returns the full name of the <code>Contact</code>.
	 * @return Nullable; Full name of the <code>Contact</code>.
     */
	public @Nullable String getFullName() {
		return _fullName;
	}

	/**
	 * <p>
	 *     Returns the display name for the <code>Contact</code>.
	 * </p>
	 * <p>
	 *     Use this function if you need to show the contact on the screen as it uses the best
	 *     available data to build the human-readable name.
	 * </p>
	 * @return Nullable; Display Name for the <code>Contact</code>.
     */
	public @Nullable String getDisplayName() {
		return _displayName;
	}

	/**
	 * Returns the <code>ContactType</code> for the <code>Contact</code>.
	 * @return <code>ContactType</code> for the <code>Contact</code>.
     */
	public @NonNull ContactType getType() {
		return _type;
	}

	/**
	 * Returns the <code>ContactStatus</code> for the <code>Contact</code>.
	 * @return <code>ContactStatus</code> for the <code>Contact</code>.
     */
	public @NonNull ContactStatus getStatus() {
		return _status;
	}

	/**
	 * Returns the custom status message for the <code>Contact</code>.
	 * @return Nullable; The status message for the <code>Contact</code>.
     */
	public @Nullable String getStatusMessage() {
		return _statusMessage;
	}

	/**
	 * <p>
	 * Returns the number of online users in the channel.
	 * </p>
	 * <p>
	 *     For {@link ContactType#CHANNEL}. {@link ContactType#GROUP} and  and {@link ContactType#CONVERSATION} this value is the number of users online in the channel or group, respectively.
	 *     For {@link ContactType#USER} and {@link ContactType#GATEWAY} this value is not defined (<code>0</code> is returned).
	 * </p>
	 * @return number of online users under the <code>Contact</code> for the <code>ContactType</code>.
     */
	public int getUsersCount() {
		return _usersCount;
	}

	/**
	 * <p>
	 * Returns the number of total users in the channel.
	 * </p>
	 * <p>
	 *     For {@link ContactType#GROUP} and {@link ContactType#CONVERSATION} this value is the number of total users in the channel or group, respectively.
	 *     For {@link ContactType#USER}, {@link ContactType#GATEWAY} and {@link ContactType#CHANNEL} this value is not defined (<code>0</code> is returned).
	 * </p>
	 * @return number of total users under the <code>Contact</code> for the <code>ContactType</code>.
	 */
	public int getUsersTotal() {
		return _usersTotal;
	}

	/**
	 * <p>
	 *     Returns the title for the <code>Contact</code>.
	 * </p>
	 * <p>
	 *     For {@link ContactType#CHANNEL} and {@link ContactType#GROUP} this value is not defined (<code>null</code> is returned)
	 * </p>
	 *
	 * @return Nullable; The title for the <code>Contact</code>.
     */
	public @Nullable String getTitle() {
		return _title;
	}

	/**
	 * Returns whether the <code>Contact</code> is muted or not.
	 * @return boolean indicating if the <code>Contact</code> is muted.
     */
	public boolean getMuted() {
		return _muted;
	}

	/**
	 * Returns whether the channel has the no disconnect setting enabled or not.
	 * <p>
	 *     For {@link ContactType#USER}, {@link ContactType#GATEWAY} and {@link ContactType#CONVERSATION} this value is not defined (<code>false</code> is returned).
	 * </p>
	 * @return boolean indicating if the <code>Contact</code> has the no disconnect setting enabled.
     */
	public boolean getNoDisconnect() {
		return _noDisconnect;
	}

	//region Package Private Methods

	void copyTo(Contact contact) {
		if (contact == null) {
			return;
		}
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

	//endregion

}
