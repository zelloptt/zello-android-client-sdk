package com.zello.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * The <code>MessageOut</code> class represents an outgoing voice message.
 * @see Zello#getMessageOut(MessageOut)
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class MessageOut {

	//region Package Private Variables

	final @NonNull Contact _to = new Contact();
	boolean _active;
	boolean _connecting;

	//endregion

	//region Public Methods

	/**
	 * <p>
	 *     Resets the <code>MessageOut</code> instance back to the default values.
	 * </p>
	 * <p>
	 *     This method does not affect the state of the outgoing message to the Zello Work app.
	 *     This method only resets the values for this copied instance of the <code>MessageOut</code>.
	 * </p>
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
	 * <p>
	 *     Returns the instance of the <code>Contact</code> that the outgoing message is being sent to.
	 * </p>
	 * @return The <code>Contact</code> receiving the message.
     */
	public @NonNull Contact getTo() {
		return _to;
	}

	/**
	 * <p>
	 *     Determines if the <code>MessageOut</code> is currently in progress or not.
	 * </p>
	 * @return boolean indicating if the outgoing message is currently active.
	 */
	public boolean isActive() {
		return _active;
	}

	/**
	 * <p>
	 *     Determines if a connection is currently being established between the user and the recipient.
	 * </p>
	 * @return boolean indicating if the Contacts are connecting.
     */
	public boolean isConnecting() {
		return _connecting;
	}

	//endregion

	//region Package Private Methods

	void copyTo(@Nullable MessageOut message) {
		if (message == null) {
			return;
		}
		_to.copyTo(message._to);
		message._active = _active;
		message._connecting = _connecting;
	}

	//endregion

}
