package com.zello.sdk;

public class AppState {

	boolean _available;
	boolean _customBuild;
	boolean _configuring;
	boolean _locked;
	boolean _signedIn;
	boolean _signingIn;
	boolean _signingOut;
	int _reconnectTimer = -1;
	boolean _waitingForNetwork;
	boolean _showContacts;
	boolean _busy;
	boolean _solo;
	boolean _autoRun;
	boolean _autoChannels = true;
	String _statusMessage;
	String _network;
	String _networkUrl;
	String _username;

	public void reset() {
		//_available = false;
		_customBuild = false;
		_configuring = false;
		_locked = false;
		_signedIn = false;
		_signingIn = false;
		_signingOut = false;
		_reconnectTimer = -1;
		_waitingForNetwork = false;
		_showContacts = false;
		_busy = false;
		_solo = false;
		_autoRun = false;
		_autoChannels = true;
		_statusMessage = null;
		_network = null;
		_networkUrl = null;
		_username = null;
	}

	@Override
	public AppState clone() {
		AppState state = new AppState();
		copyTo(state);
		return state;
	}

	public void copyTo(AppState state) {
		if (state != null) {
			state._customBuild = _customBuild;
			state._available = _available;
			state._configuring = _configuring;
			state._locked = _locked;
			state._signedIn = _signedIn;
			state._signingIn = _signingIn;
			state._signingOut = _signingOut;
			state._reconnectTimer = _reconnectTimer;
			state._waitingForNetwork = _waitingForNetwork;
			state._showContacts = _showContacts;
			state._busy = _busy;
			state._solo = _solo;
			state._autoRun = _autoRun;
			state._autoChannels = _autoChannels;
			state._statusMessage = _statusMessage;
			state._network = _network;
			state._networkUrl = _networkUrl;
			state._username = _username;
		}
	}

	public boolean isAvailable() {
		return _available;
	}

	public boolean isCustomBuild() {
		return _customBuild;
	}

	public boolean isConfiguring() {
		return _configuring;
	}

	public boolean isLocked() {
		return _locked;
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

	public boolean isAutoRunEnabled() {
		return _autoRun;
	}

	public boolean isChannelAutoConnectEnabled() {
		return _autoChannels;
	}

	public String getStatusMessage() {
		return _statusMessage;
	}

	public String getNetwork() {
		return _network;
	}

	public String getNetworkUrl() {
		return _networkUrl;
	}

	public String getUsername() {
		return _username;
	}

}
