package com.zello.sdk;

/**
 * The MessageIn class represents an incoming message to the Zello SDK.
 */
public class MessageIn {

	//region Package Private Variables

	Contact _from = new Contact();
	Contact _author = new Contact();
	boolean _active;

	//endregion

	//region Public Methods

	public MessageIn() {

	}

	/**
	 * The reset() method resets the MessageIn instance back to the default values.
	 */
	public void reset() {
		_from.reset();
		_author.reset();
		_active = false;
	}

	@Override
	public MessageIn clone() {
		MessageIn message = new MessageIn();
		copyTo(message);
		return message;
	}

	/**
	 * The getFrom() method returns an instance of the Contact that is sending the user the message.
	 * If the ContactType is ContactType.CHANNEL or ContactType.GROUP, the returned contact will be the Channel or Group, respectively. To get the original author of the message, use the getAuthor() method.
	 * @return The Contact that is sending the message.
     */
	public Contact getFrom() {
		return _from;
	}

	/**
	 * The getAuthor() method returns an instance of the Contact that authored the message.
	 * This method should only be used to get the author of a message when the ContactType of the getFrom() method is ContactType.CHANNEL or ContactType.GROUP.
	 * @return The Contact that authored the message.
     */
	public Contact getAuthor() {
		return _author;
	}

	/**
	 * The isActive() method determines if the MessageIn is in progress.
	 * @return boolean indicating if the message is currently active.
     */
	public boolean isActive() {
		return _active;
	}

	//endregion

	//region Package Private Methods

	void copyTo(MessageIn message) {
		if (message != null) {
			_from.copyTo(message._from);
			_author.copyTo(message._author);
			message._active = _active;
		}
	}

}
