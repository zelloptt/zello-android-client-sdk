package com.zello.sdk;

import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * <p>
 * The <code>Zello</code> singleton acts as the primary point of interacting to the Zello SDK.
 * </p>
 * <p>
 * To get an instance of the object call {@link Zello#getInstance()}.
 * Before using this class, call the {@link Zello#configure(Context)} or {@link Zello#configure(String, Context)} method.
 * For specific usage, see the sample projects.
 * </p>
 */
@SuppressWarnings("WeakerAccess")
public class Zello {

	private static volatile Zello _instance;

	final @NonNull ArrayList<Events> events = new ArrayList<>();

	private @Nullable Sdk _sdk;
	// Protect against multiple attempts to configure SDK.
	private boolean _configured;

	//region Instance

	/**
	 * Gets the instance of Zello singleton object.
	 *
	 * @return Zello singleton instance.
	 */
	public static @NonNull Zello getInstance() {
		if (_instance == null) {
			synchronized (Zello.class) {
				if (_instance == null) {
					_instance = new Zello();
				}
			}
		}

		return _instance;
	}

	//endregion

	//region Configuration

	/**
	 * Configures the Zello SDK.
	 * <p>
	 * You must call <code>configure(String, Context)</code> or <code>configure(String, Context, Events)</code> before using any other SDK methods.
	 * In most cases, the <code>Application.onCreate()</code> method is the best place to do this.
	 * </p>
	 * <p>
	 * This method will chose the name of a package to connect to automatically.
	 * </p>
	 *
	 * @param context The context for the app.
	 * @see #configure(String, Context, Events)
	 * @see #unconfigure()
	 */
	public void configure(@Nullable Context context) {
		doConfigure(null, context);
	}

	/**
	 * Configures the Zello SDK.
	 * <p>
	 * You must call <code>configure(String, Context)</code> or <code>configure(String, Context, Events)</code> before using any other SDK methods.
	 * In most cases, the <code>Application.onCreate()</code> method is the best place to do this.
	 * </p>
	 * <p>
	 * If the APK was downloaded from zellowork.com, <code>packageName</code> should be "net.loudtalks".
	 * If the APK was downloaded from Android Client SDK on GitHub, <code>packageName</code> should be "com.pttsdk".
	 * If the APK was downloaded from Google Play, <code>packageName</code> should be "com.loudtalks".
	 * To have a the package name chosen automatically, pass in a <code>null</code> <code>packageName</code>.
	 * </p>
	 *
	 * @param packageName The package name of the Zello Work app.
	 * @param context The context for the app.
	 * @see #configure(String, Context, Events)
	 * @see #unconfigure()
	 */
	public void configure(@Nullable String packageName, @Nullable Context context) {
		doConfigure(packageName, context);
	}

	/**
	 * Configures the Zello SDK and subscribes for Zello SDK <code>Events</code>.
	 * <p>
	 * You must call <code>configure(String, Context)</code> or <code>configure(String, Context, Events)</code> before using any other SDK methods.
	 * In most cases, the <code>Application.onCreate()</code> method is the best place to do this.
	 * </p>
	 * <p>
	 * If the APK was downloaded from zellowork.com, <code>packageName</code> should be "net.loudtalks". If you are using a standalone version of the apk use "com.pttsdk".
	 * </p>
	 *
	 * @param packageName The package name of the Zello Work app.
	 * @param context The context for the app.
	 * @param event Events handler
	 * @see #configure(String, Context)
	 * @see #unconfigure()
	 */
	public void configure(@Nullable String packageName, @Nullable Context context, @Nullable Events event) {
		if (event != null) {
			subscribeToEvents(event);
		}
		doConfigure(packageName, context);
	}

	//endregion

	//region Lifecycle Methods

	/**
	 * Limits communication between the Zello Work app and the server to optimize power and data consumption.
	 * <p>
	 * Call this method when the Zello UI is not visible to minimize app power and data use. When in power saving mode, the app won't receive non-essential status updates.
	 * Call <code>leavePowerSavingMode</code> to resume status updates when the Zello UI is back on the screen.
	 * </p>
	 *
	 * @see #leavePowerSavingMode()
	 */
	public void enterPowerSavingMode() {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.onPause();
		}
	}

	/**
	 * Resumes full communication between the Zello Work app and the server.
	 *
	 * @see #enterPowerSavingMode()
	 */
	public void leavePowerSavingMode() {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.onResume();
		}
	}

	/**
	 * Disconnects the SDK from Zello Work app and unsubscribes all <code>Events</code> handlers.
	 *
	 * @see #configure(String, Context)
	 */
	public void unconfigure() {
		checkConfiguration();
		doUnconfigure();
	}

	//endregion

	//region Events Handlers

	/**
	 * Subscribes for Zello SDK <code>Events</code>.
	 *
	 * @param event Events handler.
	 * @see Events
	 */
	public void subscribeToEvents(@NonNull Events event) {
		if (!events.contains(event)) {
			events.add(event);
		}
	}

	/**
	 * Unsubscribes from Zello SDK <code>Events</code>.
	 *
	 * @param event Events handler.
	 * @see Events
	 */
	public void unsubscribeFromEvents(@NonNull Events event) {
		events.remove(event);
	}

	//endregion

	//region Zello SDK Methods

	//region Permissions

	/**
	 * Opens a dialog that requests all of the vital run time permissions needed by the Zello Work app to function properly.
	 * <p>
	 * This method is only necessary for Android devices running 6.0 (API 23) and above.
	 * This method requests run time permissions for the microphone, phone, and external storage.
	 * </p>
	 * <p>
	 * If these permissions have already been granted, this method has no effect.
	 * In addition, if the device is running Android 5.1 (API 22) or less, this method has no effect.
	 * </p>
	 * <p>
	 * Use this method when you don't have an activity on the screen (ex. from a service).
	 * </p>
	 *
	 * @see Zello#requestVitalPermissions(Activity)
	 * @see Zello#beginMessage()
	 * @see Zello#showMicrophonePermissionDialog()
	 * @see Events#onMicrophonePermissionNotGranted()
	 */
	public void requestVitalPermissions() {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.requestPermissions(null, false, Constants.EXTRA_REQUEST_VITAL_PERMISSIONS);
		}
	}

	/**
	 * Opens a dialog that requests all of the vital run time permissions needed by the Zello Work app to function properly.
	 * <p>
	 * This method is only necessary for Android devices running 6.0 (API 23) and above.
	 * This method requests run time permissions for the microphone, phone, and external storage.
	 * </p>
	 * <p>
	 * If these permissions have already been granted, this method has no effect.
	 * In addition, if the device is running Android 5.1 (API 22) or less, this method has no effect.
	 * </p>
	 * <p>
	 * Use this method to open the permissions UI from an existing activity.
	 * </p>
	 *
	 * @param activity Caller activity.
	 * @see Zello#requestVitalPermissions()
	 * @see Zello#beginMessage()
	 * @see Zello#showMicrophonePermissionDialog()
	 * @see Events#onMicrophonePermissionNotGranted()
	 */
	public void requestVitalPermissions(@Nullable Activity activity) {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.requestPermissions(activity, false, Constants.EXTRA_REQUEST_VITAL_PERMISSIONS);
		}
	}

	/**
	 * Opens a dialog that requests the location permission needed by the Zello Work app to function properly.
	 * <p>
	 * This method is only necessary for Android devices running 6.0 (API 23) and above.
	 * This method requests run time permissions for the microphone, phone, and external storage.
	 * </p>
	 * <p>
	 * If these permissions have already been granted, this method has no effect.
	 * In addition, if the device is running Android 5.1 (API 22) or less, this method has no effect.
	 * </p>
	 * <p>
	 * Use this method when you don't have an activity on the screen (ex. from a service).
	 * </p>
	 *
	 * @see Zello#requestVitalPermissions(Activity)
	 * @see Zello#beginMessage()
	 * @see Zello#showMicrophonePermissionDialog()
	 * @see Events#onMicrophonePermissionNotGranted()
	 */
	public void requestLocationPermission() {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.requestPermissions(null, false, Constants.EXTRA_PERMISSION_LOCATION);
		}
	}

	/**
	 * Opens a dialog that requests the location permission needed by the Zello Work app to function properly.
	 * <p>
	 * This method is only necessary for Android devices running 6.0 (API 23) and above.
	 * This method requests run time permissions for the microphone, phone, and external storage.
	 * </p>
	 * <p>
	 * If these permissions have already been granted, this method has no effect.
	 * In addition, if the device is running Android 5.1 (API 22) or less, this method has no effect.
	 * </p>
	 * <p>
	 * Use this method to open the permissions UI from an existing activity.
	 * </p>
	 *
	 * @param activity Caller activity.
	 * @see Zello#requestVitalPermissions()
	 * @see Zello#beginMessage()
	 * @see Zello#showMicrophonePermissionDialog()
	 * @see Events#onMicrophonePermissionNotGranted()
	 */
	public void requestLocationPermission(@Nullable Activity activity) {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.requestPermissions(activity, false, Constants.EXTRA_PERMISSION_LOCATION);
		}
	}

	/**
	 * Opens a popup dialog that tells the user that they cannot send voice messages until the
	 * permission to record audio has been granted.
	 * <p>
	 * This method is only necessary for Android devices running 6.0 (API 23) and above.
	 * </p>
	 * <p>
	 * The most appropriate place to call this method is when <code>onMicrophonePermissionNotGranted()</code>
	 * is called on the <code>Events</code> interface.
	 * </p>
	 * <p>
	 * If the microphone permission has already been granted, this method has no effect.
	 * In addition, if the device is running Android 5.1 (API 22) or less, this method has no effect.
	 * </p>
	 * <p>
	 * Use this method when you don't have an activity on the screen (ex. from a service).
	 * </p>
	 *
	 * @see Zello#showMicrophonePermissionDialog(Activity)
	 * @see Events#onMicrophonePermissionNotGranted()
	 * @see Zello#requestVitalPermissions()
	 */
	public void showMicrophonePermissionDialog() {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.requestPermissions(null, true, Constants.EXTRA_PERMISSION_MICROPHONE);
		}
	}

	/**
	 * Opens a popup dialog that tells the user that they cannot send voice messages until the
	 * permission to record audio has been granted.
	 * <p>
	 * This method is only necessary for Android devices running 6.0 (API 23) and above.
	 * </p>
	 * <p>
	 * The most appropriate place to call this method is when <code>onMicrophonePermissionNotGranted()</code>
	 * is called on the <code>Events</code> interface.
	 * </p>
	 * <p>
	 * If the microphone permission has already been granted, this method has no effect.
	 * In addition, if the device is running Android 5.1 (API 22) or less, this method has no effect.
	 * </p>
	 * <p>
	 * Use this method to open the permissions UI from an existing activity.
	 * </p>
	 *
	 * @param activity Caller activity.
	 * @see Zello#showMicrophonePermissionDialog()
	 * @see Events#onMicrophonePermissionNotGranted()
	 * @see Zello#requestVitalPermissions()
	 */
	public void showMicrophonePermissionDialog(@Nullable Activity activity) {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.requestPermissions(activity, true, Constants.EXTRA_PERMISSION_MICROPHONE);
		}
	}

	//endregion

	//region Contact Selection

	/**
	 * Opens an activity, which lets the user select a user or channel to talk to.
	 * <p>
	 * The Zello SDK provides a built-in UI for contact selection that is available when the user is signed in.
	 * You can customize the title of the activity (leave <code>null</code> for default), the tabs available,
	 * initial tab, and specify a dark or light theme.
	 * </p>
	 * <p>
	 * Use this method when you don't have an activity on the screen (ex. from a service).
	 * </p>
	 *
	 * @param title Activity title. Can be <code>null</code>.
	 * @param tabs Set of displayed tabs.
	 * @param activeTab Initially active tab.
	 * @param theme Visual theme for activity.
	 * @see #selectContact(String, Tab[], Tab, Theme, Activity)
	 * @see #setSelectedContact(Contact)
	 * @see #getSelectedContact(Contact)
	 * @see Events#onSelectedContactChanged()
	 * @see Events#onLastContactsTabChanged(Tab)
	 */
	public void selectContact(@Nullable String title, @Nullable Tab[] tabs, @Nullable Tab activeTab, @Nullable Theme theme) {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.selectContact(title, tabs, activeTab, theme);
		}
	}

	/**
	 * Opens an activity, which lets the user select a user or channel to talk to, from another activity.
	 * <p>
	 * The Zello SDK provides a built-in UI for contact selection that is available when the user is signed in.
	 * You can customize the title of the activity (leave <code>null</code> for default), the tabs available,
	 * initial tab, and specify a dark or light theme.
	 * </p>
	 * <p>
	 * Use this method to open a contact selection UI from an existing activity.
	 * </p>
	 *
	 * @param title Activity title. Can be <code>null</code>.
	 * @param tabs Set of displayed tabs.
	 * @param activeTab Initially active tab.
	 * @param theme Visual theme for activity.
	 * @param activity Caller activity.
	 * @see #selectContact(String, Tab[], Tab, Theme)
	 * @see #setSelectedContact(Contact)
	 * @see #getSelectedContact(Contact)
	 * @see Events#onSelectedContactChanged()
	 * @see Events#onLastContactsTabChanged(Tab)
	 */
	public void selectContact(@Nullable String title, @Nullable Tab[] tabs, @Nullable Tab activeTab, @Nullable Theme theme, @Nullable Activity activity) {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.selectContact(title, tabs, activeTab, theme, activity);
		}
	}

	//endregion

	//region Sending Messages

	/**
	 * Starts sending a voice message to the currently selected user or channel.
	 * <p>
	 * This method is asynchronous. When the message status changes, <code>onMessageStateChanged()</code>
	 * is called on the <code>Events</code> interface.
	 * </p>
	 * <p>
	 * If the microphone permission is not granted, <code>onMicrophonePermissionNotGranted()</code>
	 * is called on the <code>Events</code> interface.
	 * </p>
	 *
	 * @see #endMessage()
	 * @see #selectContact(String, Tab[], Tab, Theme)
	 * @see Events#onMessageStateChanged()
	 * @see Events#onMicrophonePermissionNotGranted()
	 */
	public void beginMessage() {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.beginMessage();
		}
	}

	/**
	 * Stops sending a voice message.
	 * <p>
	 * This method has no effect if there is no active outgoing message.
	 * </p>
	 * <p>
	 * This method is asynchronous. When the message status changes, <code>onMessageStateChanged()</code>
	 * is called on the <code>Events</code> interface.
	 * </p>
	 *
	 * @see #beginMessage()
	 * @see Events#onMessageStateChanged()
	 */
	public void endMessage() {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.endMessage();
		}
	}

	//endregion

	//region Replaying Messages

	/**
	 * Replays the last played voice message.
	 * <p>
	 * There is no expiration for messages that can be played using this method.
	 * The last played message is only changed by overriding it with a new message, signing out or deleting the contact that sent the message.
	 * </p>
	 * <p>
	 * This method has no effect if there was no last played voice message or if there is an active outgoing message.
	 * </p>
	 * <p>
	 * This method is asynchronous. When the message status changes, <code>onMessageStateChanged()</code>
	 * is called on the <code>Events</code> interface.
	 * </p>
	 *
	 * @see Events#onMessageStateChanged()
	 * @see #isLastMessageReplayAvailable()
	 */
	public void replayLastIncomingMessage() {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.replayLastIncomingMessage();
		}
	}

	/**
	 * Determines if there is a message available to replay.
	 * <p>
	 * Use this method to determine whether or not your UI should display a button to replay the last incoming message.
	 * There are two events that may trigger a change in the last message availability:
	 * <code>Events.onMessageStateChanged()</code> when a live incoming voice message is received or when the contact that sent the last incoming message is deleted.
	 * <code>Events.onAppStateChanged()</code> when the user signs out.
	 * </p>
	 *
	 * @return boolean indicating whether there is a message to replay.
	 * @see Events#onMessageStateChanged()
	 * @see Events#onAppStateChanged()
	 * @see Zello#replayLastIncomingMessage()
	 */
	public boolean isLastMessageReplayAvailable() {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			return sdk.isLastMessageReplayAvailable();
		} else {
			return false;
		}
	}

	//endregion

	//region Channels

	/**
	 * Connects the user to a channel.
	 * <p>
	 * This method is asynchronous. When the channel is connected, <code>onContactsChanged()</code>
	 * is called on the <code>Events</code> interface.
	 * </p>
	 *
	 * @param channel The name of the channel to connect to.
	 * @see #disconnectChannel(String)
	 * @see #setAutoConnectChannels(boolean)
	 * @see Events#onContactsChanged()
	 */
	public void connectChannel(@Nullable String channel) {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.connectChannel(channel);
		}
	}

	/**
	 * Disconnects the user from a channel.
	 * <p>
	 * This method is asynchronous. When the channel is disconnected, <code>onContactsChanged()</code>
	 * is called on the <code>Events</code> interface.
	 * </p>
	 *
	 * @param channel The name of the channel to disconnect from.
	 * @see #connectChannel(String)
	 * @see #setAutoConnectChannels(boolean)
	 * @see Events#onContactsChanged()
	 */
	public void disconnectChannel(@Nullable String channel) {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.disconnectChannel(channel);
		}
	}

	//endregion

	//region Contacts

	/**
	 * Mutes or unmutes a contact.
	 *
	 * @param contact The contact to mute or unmute.
	 * @param mute Whether the contact should be muted or not.
	 */
	public void muteContact(@Nullable Contact contact, boolean mute) {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.muteContact(contact, mute);
		}
	}

	//endregion

	//region Authentication

	/**
	 * Signs the user into the network with the passed in login credentials.
	 * <p>
	 * See <code>signIn(String, String, String, Boolean)</code> for more details.
	 * </p>
	 *
	 * @param network The network name or URL.
	 * @param username The username to authenticate.
	 * @param password The password for the username.
	 * @return Indicates whether a sign in was initiated or not.
	 * @see #signIn(String, String, String, boolean)
	 * @see #cancelSignIn()
	 * @see #signOut()
	 * @see Events#onAppStateChanged()
	 */
	public boolean signIn(@Nullable String network, @Nullable String username, @Nullable String password) {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			return sdk.signIn(network, username, password);
		} else {
			return false;
		}
	}

	/**
	 * Signs is the user into the network with the passed in login credentials.
	 * <p>
	 * When connecting to an account hosted at zellowork.com, you can use the network name for <code>network</code>.
	 * When connecting to a standalone server, use the full server domain name or IP.
	 * </p>
	 * <p>
	 * The standard sign in behavior is to save login credentials so that the next time the app is
	 * started it signs in automatically. Set <code>perishable</code> to <code>true</code> to sign in
	 * without saving login credentials.
	 * </p>
	 * <p>
	 * This method is asynchronous. When sign in fails or succeeds, <code>onAppStateChanged()</code>
	 * is called on the <code>Events</code> interface.
	 * </p>
	 * <p>
	 * To cancel the sign in process, use the <code>cancelSignIn()</code> method.
	 * </p>
	 *
	 * @param network The network name or URL.
	 * @param username The username to authenticate.
	 * @param password The password for the username.
	 * @param perishable Whether or not the authentication information should be saved.
	 * @return boolean indicating whether a sign in was initiated or not.
	 * @see #signIn(String, String, String)
	 * @see #cancelSignIn()
	 * @see #signOut()
	 * @see Events#onAppStateChanged()
	 */
	public boolean signIn(@Nullable String network, @Nullable String username, @Nullable String password, boolean perishable) {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			return sdk.signIn(network, username, password, perishable);
		} else {
			return false;
		}
	}

	/**
	 * Signs out currently signed in user.
	 * <p>
	 * This method does not remove saved user credentials.
	 * </p>
	 * <p>
	 * This method is asynchronous. When the sign out succeeds, <code>onAppStateChanged()</code>
	 * is called on the <code>Events</code> interface.
	 * </p>
	 *
	 * @see #signIn(String, String, String, boolean)
	 * @see Events#onAppStateChanged()
	 */
	public void signOut() {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.signOut();
		}
	}

	/**
	 * Cancels the currently running sign in process.
	 * <p>
	 * This method won't have an effect if the user is already signed in.
	 * </p>
	 *
	 * @see #signIn(String, String, String, boolean)
	 */
	public void cancelSignIn() {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.cancel();
		}
	}

	//endregion

	//region Locking

	/**
	 * Locks the default Zello Work app UI to prevent users from accessing it.
	 * <p>
	 * In this locked state, the Zello Work app, when accessed by user, only displays an
	 * information screen with the name of your app that can be clicked to open the main activity.
	 * This does NOT interfere with the sending and receiving of messages through the SDK.
	 * </p>
	 * <p>
	 * This method is useful when you implement a fully custom UI in your app and want
	 * to prevent users from accidentally accessing the standard Zello Work UI.
	 * </p>
	 *
	 * @param applicationName The name of your application.
	 * @param packageName The package name of your application.
	 * @see #unlock()
	 */
	public void lock(@Nullable String applicationName, @Nullable String packageName) {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.lock(applicationName, packageName);
		}
	}

	/**
	 * Unlocks the default Zello Work app UI.
	 *
	 * @see #lock(String, String)
	 */
	public void unlock() {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.unlock();
		}
	}

	//endregion

	//region Status

	/**
	 * Sets the current user's availability status.
	 *
	 * @param status The status to set.
	 * @see #setStatusMessage(String)
	 * @see AppState#getStatus()
	 * @see Events#onAppStateChanged()
	 */
	public void setStatus(@NonNull Status status) {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.setStatus(status);
		}
	}

	/**
	 * Sets the current user's custom status message text.
	 *
	 * @param message The custom message text to display in user's status.
	 * @see #setStatus(Status)
	 * @see AppState#getStatusMessage()
	 */
	public void setStatusMessage(@Nullable String message) {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.setStatusMessage(message);
		}
	}

	//endregion

	//region Opening Zello Work app

	/**
	 * Opens the main screen of the Zello Work app.
	 */
	public void openMainScreen() {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.openMainScreen();
		}
	}

	//endregion

	//region App settings

	/**
	 * Opens the PTT buttons settings screen.
	 *
	 * @param activity Nullable, Caller activity.
	 */
	public void showPttButtonsScreen(@Nullable Activity activity) {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.showPttButtonsScreen(activity);
		}
	}

	//endregion

	//region Getters

	/**
	 * Gets info about the current active incoming voice message.
	 * <p>
	 * This method updates the provided instance of the <code>MessageIn</code> object with the current data.
	 * The object does not update automatically, so call this method every time you need to get the
	 * current info about an active incoming voice message.
	 * </p>
	 *
	 * @param message The object to copy data to.
	 * @see MessageIn
	 * @see Events#onMessageStateChanged()
	 */
	public void getMessageIn(@Nullable MessageIn message) {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.getMessageIn(message);
		}
	}

	/**
	 * Gets info about the current active outgoing voice message.
	 * <p>
	 * This method updates the provided instance of the <code>MessageOut</code> object with the current data.
	 * The object does not update automatically, so call this method every time you need to get the
	 * current info about an active outgoing voice message.
	 * </p>
	 *
	 * @param message The object to copy data to.
	 * @see MessageOut
	 * @see Events#onMessageStateChanged()
	 */
	public void getMessageOut(@Nullable MessageOut message) {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.getMessageOut(message);
		}
	}

	/**
	 * Gets info about the current application state.
	 * <p>
	 * This method updates the provided instance of the <code>AppState</code> object with the current data.
	 * The object does not update automatically, so call this method every time you need to get the
	 * current info about Zello Work app and Zello SDK state.
	 * </p>
	 *
	 * @param state The object to copy data to.
	 * @see AppState
	 * @see Events#onAppStateChanged()
	 */
	public void getAppState(@Nullable AppState state) {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.getAppState(state);
		}
	}

	/**
	 * Gets info about the currently selected contact.
	 * <p>
	 * This method updates the provided instance of the <code>Contact</code> object with the current data.
	 * The object does not update automatically, so call this method every time you need to get the
	 * info about currently selected contact.
	 * </p>
	 *
	 * @param contact The object to copy data to.
	 * @see Contact
	 * @see #setSelectedContact(Contact)
	 * @see Events#onSelectedContactChanged()
	 */
	public void getSelectedContact(@Nullable Contact contact) {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.getSelectedContact(contact);
		}
	}

	/**
	 * Gets the list of users and channels in the contact list of the current user.
	 * <p>
	 * The returned list includes users, channels and their statuses. It does not update
	 * automatically, so use this method to get a fresh copy of the list when needed.
	 * </p>
	 * <p>
	 * When the list is updated in any way, <code>onContactsChanged()</code> is called on
	 * the <code>Events</code> interface. Possible reasons for the contact list being updated include
	 * changes of online status of users, channels connecting and disconnecting and modifications
	 * made to the contact list through web console or API.
	 * </p>
	 *
	 * @return The contact list for the currently signed in user.
	 * @see Events#onContactsChanged()
	 */
	public @Nullable Contacts getContacts() {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			return sdk.getContacts();
		} else {
			return null;
		}
	}

	/**
	 * Gets an instance of the <code>Audio</code> class that is used to control Zello audio settings.
	 *
	 * @return The audio instance.
	 * @see Audio
	 * @see Events#onAudioStateChanged()
	 */
	public @Nullable Audio getAudio() {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			return sdk.getAudio();
		} else {
			return null;
		}
	}

	//endregion

	//region Setters

	/**
	 * Specifies whether the Zello Work app should be launched on the start of the OS.
	 * <p>
	 * Configured autorun behavior persists between app launches.
	 * </p>
	 *
	 * @param enable The boolean to enable this feature or not. By default, this value is true.
	 * @see #getAppState(AppState)
	 * @see AppState#isAutoRunEnabled()
	 */
	public void setAutoRun(boolean enable) {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.setAutoRun(enable);
		}
	}

	/**
	 * Specifies whether newly added channels should connect automatically.
	 * <p>
	 * Zello remembers whether the channel was connected by the user. The default behavior when
	 * user signs in for the first time or added to a new channel via web console or the API is
	 * to connect to that channel. Set <code>connect</code> to <code>false</code> to prevent this
	 * behavior. Call this method before <code>signIn</code> to make sure no channels are connected
	 * automatically on first sign in.
	 * </p>
	 * <p>
	 * This method doesn't affect the channels previously connected or disconnected by the user or
	 * the Zello SDK.
	 * </p>
	 *
	 * @param connect Enables autoconnect.
	 * @see #connectChannel(String)
	 * @see #disconnectChannel(String)
	 * @see AppState#isChannelAutoConnectEnabled()
	 */
	public void setAutoConnectChannels(boolean connect) {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.setAutoConnectChannels(connect);
		}
	}

	/**
	 * Sets an optional external id tag used by Zello Work Server Recording.
	 * <p>
	 * When set, the provided <code>id</code> is included in the metadata of all messages sent by
	 * Zello and can be accessed using the Zello Work server API to query recordings metadata.
	 * </p>
	 * <p>
	 * This method has no effect unless the Zello Work Server Recording feature is turned on for the
	 * current network.
	 * </p>
	 *
	 * @param id Nullable; String indicating the external id.
	 * @see AppState#getExternalId()
	 */
	public void setExternalId(@Nullable String id) {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.setExternalId(id);
		}
	}

	/**
	 * Selects a contact (user or channel) to send messages to.
	 * <p>
	 * Use a <code>Contact</code> object from the list returned by
	 * <code>Zello.getContacts()</code> method as an argument.
	 * </p>
	 *
	 * @param contact Nullable; a contact to select. A null value deselects the current contact.
	 * @see #getSelectedContact(Contact)
	 * @see #setSelectedUserOrGateway(String)
	 * @see #setSelectedChannelOrGroup(String)
	 * @see #selectContact(String, Tab[], Tab, Theme, Activity)
	 * @see Events#onSelectedContactChanged()
	 * @see #getContacts()
	 */
	public void setSelectedContact(@Nullable Contact contact) {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.setSelectedContact(contact);
		}
	}

	/**
	 * Selects a contact (user or gateway) to send messages to.
	 * <p>
	 * Use a <code>Contact</code> object from the list returned by
	 * <code>Zello.getContacts()</code> method as an argument.
	 * </p>
	 *
	 * @param name Nullable; The name of the user or gateway to select. A null value deselects the current contact.
	 * @see #setSelectedContact(Contact)
	 * @see #setSelectedChannelOrGroup(String)
	 * @see #selectContact(String, Tab[], Tab, Theme, Activity)
	 * @see Events#onSelectedContactChanged()
	 * @see #getContacts()
	 */
	public void setSelectedUserOrGateway(@Nullable String name) {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.setSelectedUserOrGateway(name);
		}
	}

	/**
	 * Selects a contact (channel or group) to send messages to.
	 * <p>
	 * Use a <code>Contact</code> object from the list returned by
	 * <code>Zello.getContacts()</code> method as an argument.
	 * </p>
	 *
	 * @param name Nullable; The name of the channel or group or contact to select. A null value deselects the current contact.
	 * @see #setSelectedContact(Contact)
	 * @see #setSelectedUserOrGateway(String)
	 * @see #selectContact(String, Tab[], Tab, Theme, Activity)
	 * @see Events#onSelectedContactChanged()
	 */
	public void setSelectedChannelOrGroup(@Nullable String name) {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.setSelectedChannelOrGroup(name);
		}
	}

	/**
	 * Asks the host app to suppress UI notifications when Bluetooth PTT buttons connect or disconnect.
	 * <p>
	 * When set, the application will not show popup notifications every time a button connects or disconnects.
	 * </p>
	 *
	 * @param show Show Bluetooth PTT buttons' related notifications.
	 */
	public void setShowBluetoothAccessoriesNotifications(boolean show) {
		checkConfiguration();
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.setShowBluetoothAccessoriesNotifications(show);
		}
	}

	//endregion

	//endregion

	//region Private Methods

	private Zello() {
	}

	private synchronized void doConfigure(@Nullable String packageName, @Nullable Context context) {
		if (_configured) {
			return;
		}
		_configured = true;

		Sdk sdk = new Sdk();
		sdk.onCreate(packageName, context);
		_sdk = sdk;

		// Updates should be on by default
		leavePowerSavingMode();
	}

	private synchronized void doUnconfigure() {
		Sdk sdk = _sdk;
		if (sdk != null) {
			sdk.onDestroy();
		}
		_sdk = null;
		events.clear();

		_configured = false;
	}

	private void checkConfiguration() {
		if (!_configured) {
			throw new RuntimeException("The SDK has not yet been configured. Call Zello.configure() first.");
		}
	}

	//endregion

}
