package com.zello.sdk;

public class MessageOut {

	Contact _to = new Contact();
	boolean _active;
	boolean _connecting;

	public MessageOut() {
	}

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

	public void copyTo(MessageOut message) {
		if (message != null) {
			_to.copyTo(message._to);
			message._active = _active;
			message._connecting = _connecting;
		}
	}

	public Contact getTo() {
		return _to;
	}

	public boolean isActive() {
		return _active;
	}

	public boolean isConnecting() {
		return _connecting;
	}

}
