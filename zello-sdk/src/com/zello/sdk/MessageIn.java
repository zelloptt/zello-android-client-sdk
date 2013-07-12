package com.zello.sdk;

public class MessageIn {

	Contact _from = new Contact();
	Contact _author = new Contact();
	boolean _active;

	public MessageIn() {
	}

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

	public void copyTo(MessageIn message) {
		if (message != null) {
			_from.copyTo(message._from);
			_author.copyTo(message._author);
			message._active = _active;
		}
	}

	public Contact getFrom() {
		return _from;
	}

	public Contact getAuthor() {
		return _author;
	}

	public boolean isActive() {
		return _active;
	}

}
