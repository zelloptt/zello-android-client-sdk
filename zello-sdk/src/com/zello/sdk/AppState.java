package com.zello.sdk;

/**
 * <pre>
 * The AppState class is a representation of the current state of the Zello SDK at any given moment.
 * This class is useful to get information about the Sdk and the status of the app.
 * </pre>
 * <pre>
 * To use, retrieve the current AppState instance from the Sdk instance using the getAppState() method. For specific usage, please see the sample projects.
 * </pre>
 */
public class AppState {

	//region Private Variables

	private boolean _available;
	private boolean _error; // Set when the service fails to connect
	private boolean _initializing;
	private boolean _customBuild;
	private boolean _configuring;
	private boolean _locked;
	private boolean _signedIn;
	private boolean _signingIn;
	private boolean _signingOut;
	private boolean _cancelling;
	private int _reconnectTimer = -1;
	private boolean _waitingForNetwork;
	private boolean _showContacts;
	private boolean _busy;
	private boolean _solo;
	private boolean _autoRun;
	private boolean _autoChannels = true;
	private Error _lastError = Error.NONE;
	private String _statusMessage;
	private String _network;
	private String _networkUrl;
	private String _username;
	private String _externalId;

	//endregion

	/**
	 * The reset() method resets the AppState instance back to the default values.
	 */
	public void reset() {
		//_available = false;
		_customBuild = false;
		_configuring = false;
		_locked = false;
		_signedIn = false;
		_signingIn = false;
		_signingOut = false;
		_cancelling = false;
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
		_externalId = null;
	}

	@Override
	public AppState clone() {
		AppState state = new AppState();
		copyTo(state);
		return state;
	}

	//region Public State Methods

	/**
	 * The isAvailable() method determines if the PTT app is available.
	 * @return boolean indicating if the app is available.
     */
	public boolean isAvailable() {
		return _available && !_error;
	}

	/**
	 * The isInitializing() method determines if the PTT app is initializing.
	 * @return boolean indicating if the app is initializing.
	 */
	public boolean isInitializing() {
		return _initializing;
	}

	/**
	 * The isCustomBuild() method determines if the PTT app is a custom build.
	 * @return boolean indicating if the app is a custom build.
	 */
	public boolean isCustomBuild() {
		return _customBuild;
	}

	/**
	 * The isConfiguring() method determines if the PTT app is currently configuring.
	 * @return boolean indicating if the app is currently configuring.
	 */
	public boolean isConfiguring() {
		return _configuring;
	}

	/**
	 * The isLocked() method determines if the PTT app is currently locked.
	 * @return boolean indicating if the app is currently locked.
	 */
	public boolean isLocked() {
		return _locked;
	}

	/**
	 * The isSignedIn() method determines if the user is currently authenticated.
	 * @return boolean indicating if the user is signed in.
	 */
	public boolean isSignedIn() {
		return _signedIn;
	}

	/**
	 * The isSigningIn() method determines if the user is in the process of authenticating.
	 * @return boolean indicating if the user is being authenticated.
     */
	public boolean isSigningIn() {
		return _signingIn;
	}

	/**
	 * The isSigningOut() method determines if the user is in the process of signing out.
	 * @return boolean indicating if the user is signing out.
     */
	public boolean isSigningOut() {
		return _signingOut;
	}

	/**
	 * The isCancellingSignin() method determines if the authentication request for the user is being cancelled.
	 * @return boolean indicating if the authentication request is being cancelled.
     */
	public boolean isCancellingSignin() {
		return _cancelling;
	}

	/**
	 * The isReconnecting() method determines if the PTT app is trying to reconnect the user.
	 * @return boolean indicating if the app is trying to reconnect the user.
     */
	public boolean isReconnecting() {
		return _reconnectTimer >= 0;
	}

	/**
	 * The isWaitingForNetwork() method determines if the PTT app is waiting for the network to respond.
	 * @return boolean indicating if the app is waiting for the network.
     */
	public boolean isWaitingForNetwork() {
		return _waitingForNetwork;
	}

	/**
	 * The isAutoRunEnabled() method determines if the auto run setting is enabled.
	 * @return boolean indicating whether or not auto run is enabled.
	 */
	public boolean isAutoRunEnabled() {
		return _autoRun;
	}

	/**
	 * The isChannelAutoConnectEnabled() method determines if the auto connect channel setting is enabled.
	 * @return boolean indicating whether or not auto connect channels is enabled.
	 */
	public boolean isChannelAutoConnectEnabled() {
		return _autoChannels;
	}

	//endregion

	//region Public Getters

	/**
	 * The getReconnectTimer() method returns the timer for reconnecting to the network.
	 * @return The network reconnect timer.
     */
	public int getReconnectTimer() {
		return _reconnectTimer;
	}

	/**
	 * The getShowContacts() method determines if the contacts for the user should be shown or not.
	 * @return boolean indicating if the contacts should be shown or not.
     */
	public boolean getShowContacts() {
		return _showContacts;
	}

	/**
	 * The getStatus() method returns the Status for the user.
	 * @return The current Status for the user.
     */
	public Status getStatus() {
		return _busy ? Status.BUSY : (_solo ? Status.SOLO : Status.AVAILABLE);
	}

	/**
	 * The getStatusMessage() method returns the custom status message for the user.
	 * @return Nullable; The status message for the user.
     */
	public String getStatusMessage() {
		return _statusMessage;
	}

	/**
	 * The getNetwork() method returns the network String for the PTT app.
	 * @return Nullable; The network String.
	 */
	public String getNetwork() {
		return _network;
	}

	/**
	 * The getNetworkUrl() method returns the network in URL format for the PTT app.
	 * @return Nullable; The network URL.
	 */
	public String getNetworkUrl() {
		return _networkUrl;
	}

	/**
	 * The getUsername() method returns the username of the authenticated user.
	 * @return Nullable; The username for the user.
     */
	public String getUsername() {
		return _username;
	}

	/**
	 * The getLastError() method returns the most recent Error encountered by the PTT app.
	 * @return Error type indicating the latest error.
     */
	public Error getLastError() {
		return _lastError;
	}

	/**
	 * The getExternalId() method returns the external id for the PTT app.
	 * @return Nullable; The external id for the app.
     */
	public String getExternalId() {
		return _externalId;
	}

	//endregion

	//region Private Methods

	private void copyTo(AppState state) {
		if (state != null) {
			state._customBuild = _customBuild;
			state._available = _available;
			state._error = _error;
			state._initializing = _initializing;
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
			state._lastError = _lastError;
			state._externalId = _externalId;
		}
	}

	//endregion

}
