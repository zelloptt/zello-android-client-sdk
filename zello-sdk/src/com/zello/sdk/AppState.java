package com.zello.sdk;

public class AppState {

	boolean _available;
	boolean _configuring;
	boolean _signedIn;
	boolean _signingIn;
	boolean _signingOut;
	int _reconnectTimer = -1;
	boolean _waitingForNetwork;
	boolean _showContacts;
	boolean _busy;
	boolean _solo;
	String _statusMessage;

	public void reset() {
		//_available = false;
		_configuring = false;
		_signedIn = false;
		_signingIn = false;
		_signingOut = false;
		_reconnectTimer = -1;
		_waitingForNetwork = false;
		_showContacts = false;
		_busy = false;
		_solo = false;
		_statusMessage = null;
	}

	@Override
	public AppState clone() {
		AppState state = new AppState();
		copyTo(state);
		return state;
	}

	public void copyTo(AppState state) {
		if (state != null) {
			state._available = _available;
			state._configuring = _configuring;
			state._signedIn = _signedIn;
			state._signingIn = _signingIn;
			state._signingOut = _signingOut;
			state._reconnectTimer = _reconnectTimer;
			state._waitingForNetwork = _waitingForNetwork;
			state._showContacts = _showContacts;
			state._busy = _busy;
			state._solo = _solo;
			state._statusMessage = _statusMessage;
		}
	}

	public boolean isAvailable() {
		return _available;
	}

	public boolean isConfiguring() {
		return _configuring;
	}

	public boolean isSignedIn() {
		return _signedIn;
	}

	public boolean isSigningIn() {
		return _signingIn;
	}

	public boolean isSigningOut() {
		return _signingOut;
	}

	public boolean isReconnecting() {
		return _reconnectTimer > 0;
	}

	public boolean isWaitingForNetwork() {
		return _waitingForNetwork;
	}

	public int getReconnectTimer() {
		return _reconnectTimer;
	}

	public boolean getShowContacts() {
		return _showContacts;
	}

	public Status getStatus() {
		return _busy ? Status.BUSY : (_solo ? Status.SOLO : Status.AVAILABLE);
	}

	public String getStatusMessage() {
		return _statusMessage;
	}

}
