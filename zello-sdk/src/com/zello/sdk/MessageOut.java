package com.zello.sdk;

/**
 * The MessageOut class represents an outgoing message through the Zello for Work app.
 */
public class MessageOut {

	//region Package Private Variables

	Contact _to = new Contact();
	boolean _active;
	boolean _connecting;

	//endregion

	//region Public Methods

	public MessageOut() {

	}

	/**
	 * The reset() method resets the MessageOut instance back to the default values.
	 */
	public void reset() {
		_to.reset();
		_active = false;
		_connecting = false;
	}

	@Override
	public MessageOut clone() {
		MessageOut message = new MessageOut();
		copyTo(message);
		return message;
	}

	/**
	 * The getTo() method returns the instance of the Contact that the outgoing message is being sent to.
	 * @return The Contact recieving the message.
     */
	public Contact getTo() {
		return _to;
	}

	/**
	 * The isActive() method determines if the MessageOut is in progress.
	 * @return boolean indicating if the message is currently active.
	 */
	public boolean isActive() {
		return _active;
	}

	/**
	 * The isConnecting() method determines if a connection is currently being established between the user and the recipient.
	 * @return boolean indicating if the Contacts are connecting.
     */
	public boolean isConnecting() {
		return _connecting;
	}

	//endregion

	//region Package Private Methods

	void copyTo(MessageOut message) {
		if (message != null) {
			_to.copyTo(message._to);
			message._active = _active;
			message._connecting = _connecting;
		}
	}

	//endregion

}
