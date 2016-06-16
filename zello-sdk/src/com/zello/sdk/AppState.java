package com.zello.sdk;

/**
 * <p>
 *     The <code>AppState</code> class is a representation of the current state of the Zello for Work app at any given moment.
 *     This class is useful for getting the status of the Zello for Work app.
 * </p>
 * <p>
 *     To use, retrieve the current <code>AppState</code> instance using the {@link Zello#getAppState(AppState)} method. For specific usage, please see the sample projects.
 * </p>
 * @see Zello#getAppState(AppState)
 */
public class AppState {

	//region Package Private Variables

	boolean _available;
	boolean _error; // Set when the service fails to connect
	boolean _initializing;
	boolean _customBuild;
	boolean _configuring;
	boolean _locked;
	boolean _signedIn;
	boolean _signingIn;
	boolean _signingOut;
	boolean _cancelling;
	int _reconnectTimer = -1;
	boolean _waitingForNetwork;
	boolean _showContacts;
	boolean _busy;
	boolean _solo;
	boolean _autoRun;
	boolean _autoChannels = true;
	Error _lastError = Error.NONE;
	String _statusMessage;
	String _network;
	String _networkUrl;
	String _username;
	String _externalId;

	//endregion

	/**
	 * <p>
	 *     Resets the <code>Appstate</code> instance back to the default values.
	 * </p>
	 * <p>
	 *     This method will not affect the state of the Zello SDK.
	 *     This will only reset the values for this copied instance of the <code>Appstate</code>.
	 * </p>
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
	 * <p>
	 *     Determines if the Zello for Work app is available on the device.
	 * </p>
	 * <p>
	 *     This method will return false if the Zello for Work app is not installed on the device
	 *     or if the app is in a state of error.
	 * </p>
	 * @return boolean indicating if the app is available to communicate with.
     */
	public boolean isAvailable() {
		return _available && !_error;
	}

	/**
	 * Determines if the Zello for Work app is initializing.
	 * @return boolean indicating if the app is initializing.
	 */
	public boolean isInitializing() {
		return _initializing;
	}

	/**
	 * <p>
	 *     Determines if the Zello for Work app is a custom build.
	 * </p>
	 * <p>
	 *     This method will return true if the PTT .apk was downloaded from zellowork.com.
	 * </p>
	 * @return boolean indicating if the app is a custom build.
	 */
	public boolean isCustomBuild() {
		return _customBuild;
	}

	/**
	 * Determines if the Zello for Work app is currently configuring.
	 * @return boolean indicating if the app is currently configuring.
	 */
	public boolean isConfiguring() {
		return _configuring;
	}

	/**
	 * <p>
	 *     Determines if the Zello for Work app is currently locked.
	 * </p>
	 * <p>
	 *     If the Zello for Work app is locked, the UI will only display an information screen with the name of your app that can be clicked to open the main activity.
	 *     Being locked does NOT interfere with the sending and receiving of messages through the Zello for Work app.
	 * </p>
	 * @return boolean indicating if the app is currently locked.
	 * @see Zello#lock(String, String)
	 */
	public boolean isLocked() {
		return _locked;
	}

	/**
	 * Determines if the user is currently authenticated with the network.
	 * @return boolean indicating if the user is signed in.
	 * @see Zello#signIn(String, String, String)
	 * @see Zello#signIn(String, String, String, boolean)
	 */
	public boolean isSignedIn() {
		return _signedIn;
	}

	/**
	 * Determines if the user is in the process of authenticating with the network.
	 * @return boolean indicating if the user is being authenticated.
	 * @see Zello#signIn(String, String, String)
	 * @see Zello#signIn(String, String, String, boolean)
	 */
	public boolean isSigningIn() {
		return _signingIn;
	}

	/**
	 * Determines if the user is in the process of signing out.
	 * @return boolean indicating if the user is signing out.
	 * @see Zello#signOut()
     */
	public boolean isSigningOut() {
		return _signingOut;
	}

	/**
	 *  <p>
	 *      Determines if the authentication request for the user is being cancelled.
	 *  </p>
	 *  <p>
	 *      This method will return true if the {@link Zello#cancelSignIn()} method is called.
	 *  </p>
	 * @return boolean indicating if the authentication request is being cancelled.
	 * @see Zello#cancelSignIn()
     */
	public boolean isCancellingSignin() {
		return _cancelling;
	}

	/**
	 * Determines if the Zello for Work app is trying to reconnect the user to the network.
	 * @return boolean indicating if the app is trying to reconnect the user.
     */
	public boolean isReconnecting() {
		return _reconnectTimer >= 0;
	}

	/**
	 * Determines if the Zello for Work app is waiting for the network to respond.
	 * @return boolean indicating if the app is waiting for the network.
     */
	public boolean isWaitingForNetwork() {
		return _waitingForNetwork;
	}

	/**
	 * <p>
	 *     Determines if the auto run setting is enabled.
	 * </p>
	 * <p>
	 *     The auto run enabled feature determines if the app should be launched on the start of the OS or not.
	 *     This feature can be enabled or disabled using the {@link Zello#setAutoRun(boolean)} method.
	 * </p>
	 * @return boolean indicating whether or not auto run is enabled.
	 * @see Zello#setAutoRun(boolean)
	 */
	public boolean isAutoRunEnabled() {
		return _autoRun;
	}

	/**
	 * <p>
	 *     Determines if the auto connect channel setting is enabled.
	 * </p>
	 * <p>
	 *     The auto connect channel feature determines whether or not any new channel that the authenticated user is added to will be automatically connected to.
	 * </p>
	 * @return boolean indicating whether or not auto connect channels is enabled.
	 * @see Zello#setAutoConnectChannels(boolean)
	 */
	public boolean isChannelAutoConnectEnabled() {
		return _autoChannels;
	}

	//endregion

	//region Public Getters

	/**
	 * Returns the timer for reconnecting to the network.
	 * @return The network reconnect timer in seconds.
     */
	public int getReconnectTimer() {
		return _reconnectTimer;
	}

	/**
	 * Returns if the contacts for the user should be shown or not.
	 * @return boolean indicating if the contacts should be shown or not.
     */
	public boolean getShowContacts() {
		return _showContacts;
	}

	/**
	 * Returns the <code>Status</code> for the authenticated user.
	 * @return The current <code>Status</code> for the user.
	 * @see Zello#setStatus(Status)
     */
	public Status getStatus() {
		return _busy ? Status.BUSY : (_solo ? Status.SOLO : Status.AVAILABLE);
	}

	/**
	 * Returns the custom status message for the authenticated user.
	 * @return Nullable; The status message for the user.
	 * @see Zello#setStatusMessage(String)
     */
	public String getStatusMessage() {
		return _statusMessage;
	}

	/**
	 * Returns the network String for the Zello for Work app.
	 * @return Nullable; The network String.
	 * @see Zello#signIn(String, String, String)
	 * @see Zello#signIn(String, String, String, boolean)
	 */
	public String getNetwork() {
		return _network;
	}

	/**
	 * Returns the network in URL format for the Zello for Work app.
	 * @return Nullable; The network URL.
	 * @see Zello#signIn(String, String, String)
	 * @see Zello#signIn(String, String, String, boolean)
	 */
	public String getNetworkUrl() {
		return _networkUrl;
	}

	/**
	 * Returns the username of the authenticated user.
	 * @return Nullable; The username for the user.
	 * @see Zello#signIn(String, String, String)
	 * @see Zello#signIn(String, String, String, boolean)
	 */
	public String getUsername() {
		return _username;
	}

	/**
	 * Returns the most recent authentication <code>Error</code> encountered by the Zello for Work app.
	 * @return <code>Error</code> type indicating the latest error.
     */
	public Error getLastError() {
		return _lastError;
	}

	/**
	 * <p>
	 * 	   Returns an optional external id tag used by Zello Server Recording.
	 * </p>
	 * <p>
	 *     The method will return null unless the Zello Server Recording feature is turned on for the
	 *     current network.
	 * </p>
	 * @return Nullable; The external id for the app.
	 * @see Zello#setExternalId(String)
     */
	public String getExternalId() {
		return _externalId;
	}

	//endregion

	//region Package Private Methods

	void copyTo(AppState state) {
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
