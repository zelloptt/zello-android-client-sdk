package com.zello.sdk;

/**
 * The <code>MessageIn</code> class represents an incoming voice message.
 * @see Zello#getMessageIn(MessageIn)
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
	 * <p>
	 *     Resets the <code>MessageIn</code> instance back to the default values.
	 * </p>
	 * <p>
	 *     This method does not affect the state of the incoming message to the ZelloWork app.
	 *     This method only resets the values for this copied instance of the <code>MessageIn</code>.
	 * </p>
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
	 * <p>
	 *     Returns an instance of the <code>Contact</code> that is sending the user the message.
	 * </p>
	 * <p>
	 *     If the <code>ContactType</code> is {@link ContactType#CHANNEL} or {@link ContactType#GROUP}, the returned <code>Contact</code> is the channel or group, respectively.
	 *     For channels and groups, use the <code>getAuthor()</code> method to get the original author of the message.
	 * </p>
	 * @return The <code>Contact</code> that is sending the message.
	 * @see #getAuthor()
     */
	public Contact getFrom() {
		return _from;
	}

	/**
	 * <p>
	 *     Returns an instance of the <code>Contact</code> that authored the message.
	 * </p>
	 * <p>
	 *     This method should only be used to get the author of a message when the <code>ContactType</code> of the {@link #getFrom()} method is {@link ContactType#CHANNEL} or {@link ContactType#GROUP}.
	 * </p>
	 * @return The <code>Contact</code> that authored the message.
     */
	public Contact getAuthor() {
		return _author;
	}

	/**
	 * <p>
	 *     Determines if the <code>MessageIn</code> is currently in progress or not.
	 * </p>
	 * @return boolean indicating if the incoming message is currently active.
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
