package com.zello.sdk;

/**
 * The <code>Error</code> enum represents some of the authentication errors that can be encountered in the Zello SDK.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public enum Error {

	/**
	 * No error.
	 */
	NONE,
	/**
	 * An unknown error occurred.
	 */
	UNKNOWN,
	/**
	 * An invalid username or password was input.
	 */
	INVALID_CREDENTIALS,
	/**
	 * An invalid network name was input.
	 */
	INVALID_NETWORK_NAME,
	/**
	 * The network was suspended.
	 */
	NETWORK_SUSPENDED,
	/**
	 * A secure connection with the server could not be established.
	 */
	SERVER_SECURE_CONNECT_FAILED,
	/**
	 * Can't connect to the server.
	 */
	SERVER_SIGNIN_FAILED,
	/**
	 * Can't join the network.
	 */
	NETWORK_SIGNIN_FAILED,
	/**
	 * User signed in on another device, which kicked the user off the network on the previous device.
	 */
	KICKED,
	/**
	 * An update is required.
	 */
	APP_UPDATE_REQUIRED,
	/**
	 * There is no internet connection.
	 */
	NO_INTERNET_CONNECTION,
	/**
	 * Internet access is currently restricted.
	 */
	INTERNET_CONNECTION_RESTRICTED,
	/**
	 * There is a server license problem.
	 */
	SERVER_LICENSE_PROBLEM,
	/**
	 * Incorrect credentials were input too many times.
	 */
	TOO_MANY_SIGNIN_ATTEMPTS,
	/**
	 * Connection is unreliable and is causing the app to repeatedly sign in.
	 */
	UNRELIABLE_CONNECTION

}
