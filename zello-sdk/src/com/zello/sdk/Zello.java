package com.zello.sdk;

import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;

/**
 * <p>
 *     The <code>Zello</code> singleton acts as the primary point of interacting to the Zello SDK.
 * </p>
 * <p>
 *     To get an instance of the object call {@link Zello#getInstance()}.
 *     Before using this class, call the {@link Zello#configure(String, Context)} method.
 *     For specific usage, see the sample projects.
 * </p>
 */
public class Zello {

    private static volatile Zello _instance;

    ArrayList<Events> events = new ArrayList<>();

    private Sdk _sdk;
    // Protect against multiple attempts to configure SDK.
    private boolean _configured;

    //region Instance

    /**
     * Gets the instance of Zello singleton object.
     *
     * @return Zello singleton instance.
     */
    public static Zello getInstance() {
        if (_instance == null) {
            synchronized(Zello.class) {
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
     * 		You must call <code>configure(String, Context)</code> or <code>configure(String, Context, Events)</code> before using any other SDK methods.
     * 		In most cases, the <code>Application.onCreate()</code> method is the best place to do this.
     * </p>
     * <p>
     * 		If the apk was downloaded from zellowork.com, <code>packageName</code> should be "net.loudtalks". If you are using a standalone version of the apk use "com.pttsdk".
     * </p>
     * @param packageName The package name of the ZelloWork app.
     * @param context The context for the app.
     * @see #configure(String, Context, Events)
     * @see #unconfigure()
     */
    public void configure(String packageName, Context context) {
        doConfigure(packageName, context);
    }

    /**
     * Configures the Zello SDK and subscribes for Zello SDK <code>Events</code>.
     * <p>
     * 		You must call <code>configure(String, Context)</code> or <code>configure(String, Context, Events)</code> before using any other SDK methods.
     * 		In most cases, the <code>Application.onCreate()</code> method is the best place to do this.
     * </p>
     * <p>
     * 		If the apk was downloaded from zellowork.com, <code>packageName</code> should be "net.loudtalks". If you are using a standalone version of the apk use "com.pttsdk".
     * </p>
     * @param packageName The package name of the ZelloWork app.
     * @param context The context for the app.
     * @param event Events handler
     * @see #configure(String, Context)
     * @see #unconfigure()
     */
    public void configure(String packageName, Context context, Events event) {
        subscribeToEvents(event);
        doConfigure(packageName, context);
    }

    //endregion

    //region Lifecycle Methods

    /**
     * Limits communication between the ZelloWork app and the server to optimize power and data consumption.
     * <p>
     * 		Call this method when the Zello UI is not visible to minimize app power and data use. When in power saving mode the app won't receive non-essential status updates.
     * 		Call <code>leavePowerSavingMode</code> to resume status updates when the Zello UI is back on the screen.
     * </p>
     * @see #leavePowerSavingMode()
     */
    public void enterPowerSavingMode() {
		checkConfiguration();
		_sdk.onPause();
    }

    /**
     * Resumes full communication between the ZelloWork app and the server.
     * @see #enterPowerSavingMode()
     */
    public void leavePowerSavingMode() {
		checkConfiguration();
		_sdk.onResume();
    }

    /**
     * Disconnects the SDK from ZelloWork app and unsubscribes all <code>Events</code> handlers.
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
     * @param event Events handler.
     * @see Events
     */
    public void subscribeToEvents(Events event) {
        if (!events.contains(event)) {
			events.add(event);
        }
    }

    /**
     * Unsubscribes from Zello SDK <code>Events</code>.
     * @param event Events handler.
     * @see Events
     */
    public void unsubscribeFromEvents(Events event) {
        if (events.contains(event)) {
			events.remove(event);
        }
    }

    //endregion

    //region Zello SDK Methods

	//region Permissions

	/**
	 * Opens a dialog that requests all of the vital run time permissions needed by the ZelloWork app to function properly.
	 * <p>
	 * 		This method is only necessary for Android devices running 6.0 (API 23) and above.
	 * 		This method requests run time permissions for the microphone, phone, and external storage.
	 * </p>
	 * <p>
	 *		If these permissions have already been granted, this method will have no effect.
	 *		In addition, if the device is running Android 5.1 (API 22) or less, this method will have no effect.
	 * </p>
	 * <p>
	 * 		Use this method when you don't have an activity on the screen (ex. from a service).
	 * </p>
	 * @see Zello#requestVitalPermissions(Activity)
	 * @see Zello#beginMessage()
	 * @see Zello#showMicrophonePermissionDialog()
	 * @see Events#onMicrophonePermissionNotGranted()
	 */
	public void requestVitalPermissions() {
		checkConfiguration();
		_sdk.requestVitalPermissions();
	}

	/**
	 * Opens a dialog that requests all of the vital run time permissions needed by the ZelloWork app to function properly.
	 * <p>
	 * 		This method is only necessary for Android devices running 6.0 (API 23) and above.
	 * 		This method requests run time permissions for the microphone, phone, and external storage.
	 * </p>
	 * <p>
	 *		If these permissions have already been granted, this method will have no effect.
	 *		In addition, if the device is running Android 5.1 (API 22) or less, this method will have no effect.
	 * </p>
	 * <p>
	 * 		Use this method to open the permissions UI from an existing activity.
	 * </p>
	 * @param activity
	 * @see Zello#requestVitalPermissions()
	 * @see Zello#beginMessage()
	 * @see Zello#showMicrophonePermissionDialog()
	 * @see Events#onMicrophonePermissionNotGranted()
	 */
	public void requestVitalPermissions(Activity activity) {
		checkConfiguration();
		_sdk.requestVitalPermissions(activity);
	}

	/**
	 * Opens a popup dialog that tells the user that they cannot send voice messages until the
	 * permission to record audio has been granted.
	 * <p>
	 * 		This method is only necessary for Android devices running 6.0 (API 23) and above.
	 * </p>
	 * <p>
	 * 		The most appropriate place to call this method is when <code>onMicrophonePermissionNotGranted()</code>
	 * 		is called on the <code>Events</code> interface.
	 * </p>
	 * <p>
	 *     	If the microphone permission has already been granted, this method will have no effect.
	 * 		In addition, if the device is running Android 5.1 (API 22) or less, this method will have no effect.
	 * </p>
	 * <p>
	 * 		Use this method when you don't have an activity on the screen (ex. from a service).
	 * </p>
	 * @see Zello#showMicrophonePermissionDialog(Activity)
	 * @see Events#onMicrophonePermissionNotGranted()
	 * @see Zello#requestVitalPermissions()
	 */
	public void showMicrophonePermissionDialog() {
		checkConfiguration();
		_sdk.showMicrophonePermissionDialog();
	}

	/**
	 * Opens a popup dialog that tells the user that they cannot send voice messages until the
	 * permission to record audio has been granted.
	 * <p>
	 * 		This method is only necessary for Android devices running 6.0 (API 23) and above.
	 * </p>
	 * <p>
	 * 		The most appropriate place to call this method is when <code>onMicrophonePermissionNotGranted()</code>
	 * 		is called on the <code>Events</code> interface.
	 * </p>
	 * <p>
	 *     	If the microphone permission has already been granted, this method will have no effect.
	 * 		In addition, if the device is running Android 5.1 (API 22) or less, this method will have no effect.
	 * </p>
	 * <p>
	 * 		Use this method to open the permissions UI from an existing activity.
	 * </p>
	 * @see Zello#showMicrophonePermissionDialog()
	 * @see Events#onMicrophonePermissionNotGranted()
	 * @see Zello#requestVitalPermissions()
	 */
	public void showMicrophonePermissionDialog(Activity activity) {
		checkConfiguration();
		_sdk.showMicrophonePermissionDialog(activity);
	}

	//endregion

	//region Contact Selection

    /**
     * Opens an activity, which lets the user select a user or channel to talk to.
     * <p>
     * 		The Zello SDK provides a built-in UI for contact selection that is available when the user is signed in.
     * 		You can customize the title of the activity (leave <code>null</code> for default), the tabs available,
     * 		initial tab, and specify a dark or light theme.
     * </p>
     * <p>
     * 		Use this method when you don't have an activity on the screen (ex. from a service).
     * </p>
     * @param title     Activity title. Can be <code>null</code>
     * @param tabs		Set of displayed tabs.
     * @param activeTab Initially active tab.
     * @param theme     Visual theme for activity.
     * @see #selectContact(String, Tab[], Tab, Theme, Activity)
     * @see #setSelectedContact(Contact)
     * @see #getSelectedContact(Contact)
     * @see Events#onSelectedContactChanged()
     * @see Events#onLastContactsTabChanged(Tab)
     */
    public void selectContact(String title, Tab[] tabs, Tab activeTab, Theme theme) {
		checkConfiguration();
		_sdk.selectContact(title, tabs, activeTab, theme);
    }

    /**
     * Opens an activity, which lets the user select a user or channel to talk to, from another activity.
     * <p>
     * 		The Zello SDK provides a built-in UI for contact selection that is available when the user is signed in.
     * 		You can customize the title of the activity (leave <code>null</code> for default), the tabs available,
     * 		initial tab, and specify a dark or light theme.
     * </p>
     * <p>
     * 		Use this method to open a contact selection UI from an existing activity.
     * </p>
     * @param title     Activity title. Can be <code>null</code>
     * @param tabs		Set of displayed tabs.
     * @param activeTab Initially active tab.
     * @param theme     Visual theme for activity.
     * @param activity  Caller activity.
     * @see #selectContact(String, Tab[], Tab, Theme)
     * @see #setSelectedContact(Contact)
     * @see #getSelectedContact(Contact)
     * @see Events#onSelectedContactChanged()
     * @see Events#onLastContactsTabChanged(Tab)
     */
    public void selectContact(String title, Tab[] tabs, Tab activeTab, Theme theme, Activity activity) {
		checkConfiguration();
		_sdk.selectContact(title, tabs, activeTab, theme, activity);
    }

	//endregion

    //region Sending Messages

    /**
     * Starts sending a voice message to the currently selected user or channel.
     * <p>
     *     The method is asynchronous. When the message status changes, <code>onMessageStateChanged()</code>
     *     is called on the <code>Events</code> interface.
     * </p>
	 * <p>
	 *     If the microphone permission is not granted, <code>onMicrophonePermissionNotGranted()</code>
	 *     is called on the <code>Events</code> interface.
	 * </p>
     * @see #endMessage()
     * @see #selectContact(String, Tab[], Tab, Theme)
     * @see Events#onMessageStateChanged()
	 * @see Events#onMicrophonePermissionNotGranted()
     */
    public void beginMessage() {
		checkConfiguration();
		_sdk.beginMessage();
    }

    /**
     * Stops sending a voice message.
     * <p>
     *     The method has no effect if there is no active outgoing message.
     *     The method is asynchronous. When the message status changes, <code>onMessageStateChanged()</code>
     *     is called on the <code>Events</code> interface.
     * </p>
     * @see #beginMessage()
     * @see Events#onMessageStateChanged()
     */
    public void endMessage() {
		checkConfiguration();
		_sdk.endMessage();
    }

    //endregion

    //region Channels

    /**
     * Connects the user to a channel.
     * <p>
     *     The method is asynchronous. When the channel is connected, <code>onContactsChanged()</code>
     *     is called on the <code>Events</code> interface.
     * </p>
     * @param channel The name of the channel to connect to.
     * @see #disconnectChannel(String)
     * @see #setAutoConnectChannels(boolean)
     * @see Events#onContactsChanged()
     */
    public void connectChannel(String channel) {
		checkConfiguration();
		_sdk.connectChannel(channel);
    }

    /**
     * Disconnects the user from a channel.
     * <p>
     *     The method is asynchronous. When the channel is disconnected, <code>onContactsChanged()</code>
     *     is called on the <code>Events</code> interface.
     * </p>
     * @param channel The name of the channel to disconnect from.
     * @see #connectChannel(String)
     * @see #setAutoConnectChannels(boolean)
     * @see Events#onContactsChanged()
     */
    public void disconnectChannel(String channel) {
		checkConfiguration();
		_sdk.disconnectChannel(channel);
    }

    //endregion

    //region Contacts

    /**
     * Mutes or unmutes a contact.
     * @param contact The contact to mute or unmute.
     * @param mute    Whether the contact should be muted or not.
     */
    public void muteContact(Contact contact, boolean mute) {
		checkConfiguration();
		_sdk.muteContact(contact, mute);
    }

    //endregion

    //region Authentication

    /**
     * Signs the user into the network with the passed in login credentials.
     * <p>
     *     See <code>signIn(String, String, String, Boolean)</code>
     * </p>
     * @param network  The network name or URL.
     * @param username The username to authenticate.
     * @param password The password for the username.
     * @return 		   Indicates whether a sign in was initiated or not.
     * @see #signIn(String, String, String, boolean)
     * @see #cancelSignIn()
     * @see #signOut()
     * @see Events#onAppStateChanged()
     */
    public boolean signIn(String network, String username, String password) {
		checkConfiguration();
		return _sdk.signIn(network, username, password);
    }

    /**
     * Signs is the user into the network with the passed in login credentials.
     * <p>
     *     When connecting to an account hosted at zellowork.com, you can use the network name for <code>network</code>.
     *     When connecting to a standalone server, use the full server domain name or IP.
     * </p>
     * <p>
     *     The standard sign in behavior is to save logon credentials so that the next time the app is
     *     started it signs in automatically. Set <code>perishable</code> to <code>true</code> to sign in
     *     without saving logon credentials.
     * </p>
     * <p>
     *     The method is asynchronous. When sign in fails or succeeds, <code>onAppStateChanged()</code>
     *     is called on the <code>Events</code> interface. To cancel the sign in process, use <code>cancelSignIn()</code>.
     *     method.
     * </p>
     * @param network    The network name or URL.
     * @param username   The username to authenticate.
     * @param password   The password for the username.
     * @param perishable Whether or not the authentication information should be saved.
     * @return 			 boolean indicating whether a sign in was initiated or not.
     * @see #signIn(String, String, String)
     * @see #cancelSignIn()
     * @see #signOut()
     * @see Events#onAppStateChanged()
     */
    public boolean signIn(String network, String username, String password, boolean perishable) {
		checkConfiguration();
		return _sdk.signIn(network, username, password, perishable);
    }

    /**
     * Signs out currently signed in user.
     * <p>
     *     The method does not remove saved user credentials.
     * </p>
     * <p>
     *     The method is asynchronous. When the sign out succeeds, <code>onAppStateChanged()</code>
     *     is called on the <code>Events</code> interface.
     * </p>
     * @see #signIn(String, String, String, boolean)
     * @see Events#onAppStateChanged()
     */
    public void signOut() {
		checkConfiguration();
		_sdk.signOut();
    }

    /**
     * Cancels the currently running sign in process.
     *
     * <p>
     *     This method won't have an effect if the user is already signed in.
     * </p>
     * @see #signIn(String, String, String, boolean)
     */
    public void cancelSignIn() {
		checkConfiguration();
		_sdk.cancel();
    }

    //endregion

    //region Locking

    /**
     * Locks the default ZelloWork app UI to prevent users from accessing it.
     * <p>
     *     In this locked state, the ZelloWork app, when accessed by user, will only display an
     *     information screen with the name of your app that can be clicked to open the main activity.
     *     This does NOT interfere with the sending and receiving of messages through the SDK.
     * </p>
     * <p>
     *     This method is useful when you implement a fully custom UI in your app and want
     *     to prevent users from accidentally accessing the standard ZelloWork UI.
     * </p>
     * @param applicationName The name of your application.
     * @param packageName	  The package name of your application.
     * @see #unlock()
     */
    public void lock(String applicationName, String packageName) {
		checkConfiguration();
		_sdk.lock(applicationName, packageName);
    }

    /**
     * Unlocks the default ZelloWork app UI.
     * @see #lock(String, String)
     */
    public void unlock() {
		checkConfiguration();
		_sdk.unlock();
    }

    //endregion

    //region Status

    /**
     * Sets the current user's availability status.
     * @param status The status to set.
     * @see #setStatusMessage(String)
     * @see AppState#getStatus()
     * @see Events#onAppStateChanged()
     */
    public void setStatus(Status status) {
		checkConfiguration();
		_sdk.setStatus(status);
    }

    /**
     * Sets the current user's custom status message text.
     * @param message The custom message text to display in user's status.
     * @see #setStatus(Status)
     * @see AppState#getStatusMessage()
     */
    public void setStatusMessage(String message) {
		checkConfiguration();
		_sdk.setStatusMessage(message);
    }

    //endregion

	//region Opening ZelloWork app

	/**
     * Opens the main screen of the ZelloWork app.
     */
    public void openMainScreen() {
		checkConfiguration();
		_sdk.openMainScreen();
    }

	//endregion

    //region Getters

    /**
     * Gets info about the current active incoming voice message.
     * <p>
     * 		The method updates the provided instance of the <code>MessageIn</code> object with the current data.
     * 		The object will not update automatically, so call this method every time you need to get the
     * 		current info about an active incoming voice message.
     * </p>
     * @param message The object to copy data to.
     * @see MessageIn
     * @see Events#onMessageStateChanged()
     */
    public void getMessageIn(MessageIn message) {
		checkConfiguration();
		_sdk.getMessageIn(message);
    }

    /**
     * Gets info about the current active outgoing voice message.
     * <p>
     * 		The method updates the provided instance of the <code>MessageOut</code> object with the current data.
     * 		The object will not update automatically, so call this method every time you need to get the
     * 		current info about an active outgoing voice message.
     * </p>
     * @param message The object to copy data to.
     * @see MessageOut
     * @see Events#onMessageStateChanged()
     */
    public void getMessageOut(MessageOut message) {
		checkConfiguration();
		_sdk.getMessageOut(message);
    }

    /**
     * Gets info about the current application state.
     * <p>
     * 		The method updates the provided instance of the <code>AppState</code> object with the current data.
     * 		The object will not update automatically, so call this method every time you need to get the
     * 		current info about ZelloWork app and SDK state.
     * </p>
     * @param state The object to copy data to.
     * @see AppState
     * @see Events#onAppStateChanged()
     */
    public void getAppState(AppState state) {
		checkConfiguration();
		_sdk.getAppState(state);
    }

    /**
     * Gets info about currently selected contact.
     * <p>
     * 		The method updates the provided instance of the <code>Contact</code> object with the current data.
     * 		The object will not update automatically, so call this method every time you need to get the
     * 		info about currently selected contact.
     * </p>
     * @param contact The object to copy data to.
     * @see Contact
     * @see #setSelectedContact(Contact)
     * @see Events#onSelectedContactChanged()
     */
    public void getSelectedContact(Contact contact) {
		checkConfiguration();
		_sdk.getSelectedContact(contact);
    }

    /**
     * Gets the list of users and channels in the contact list of the current user.
     * <p>
     *     The list includes users, channels and their statuses. The object returned will not update
     *     automatically, so use this method to get a fresh copy of the list when needed.
     * </p>
     * <p>
     *     When the list is updated in any way <code>onContactsChanged()</code> is called on
     *     the <code>Events</code> interface. Possible reasons for the contact list being updated include
     *     changes of online status of users, channels connecting and disconnecting, and modifications
     *     made to the contact list through web console or API.
     * </p>
     * @return The contact list for the currently signed in user
     * @see Events#onContactsChanged()
     */
    public Contacts getContacts() {
		checkConfiguration();
		return _sdk.getContacts();
    }

    /**
     * Gets an instance of the <code>Audio</code> class that is used to control Zello audio settings.
     * @return The Audio
     * @see Audio
     * @see Events#onAudioStateChanged()
     */
    public Audio getAudio() {
		checkConfiguration();
		return _sdk.getAudio();
    }

    //endregion

    //region Setters

    /**
     * Specifies whether the ZelloWork app should be launched on the start of the OS.
     * <p>
     *     Configured autorun behavior persists between app launches.
     * </p>
     * @param enable The boolean to enable this feature or not. By default, this value is true.
     * @see #getAppState(AppState)
     * @see AppState#isAutoRunEnabled()
     */
    public void setAutoRun(boolean enable) {
		checkConfiguration();
		_sdk.setAutoRun(enable);
    }

    /**
     * Specifies whether newly added channels should connect automatically.
     * <p>
     *     Zello remembers whether the channel was connected by the user. The default behavior when
     *     user signs in for the first time or added to a new channel via web console or the API is
     *     to connect to that channel. Set <code>connect</code> to <code>false</code> to prevent this
     *     behavior. Call this method before <code>signIn</code> to make sure no channels are connected
     *     automatically on first sign in.
     * </p>
     * <p>
     *     The method doesn't affect the channels previously connected or disconnected by the user or
     *     the SDK.
     * </p>
     * @param connect Enables autoconnect.
     * @see #connectChannel(String)
     * @see #disconnectChannel(String)
     * @see AppState#isChannelAutoConnectEnabled()
     */
    public void setAutoConnectChannels(boolean connect) {
		checkConfiguration();
		_sdk.setAutoConnectChannels(connect);
    }

    /**
     * Sets an optional external id tag used by ZelloWork Server Recording.
     * <p>
     *     When set, the provided <code>id</code> is included in the metadata of all messages sent by
     *     Zello and can be accessed using the ZelloWork server API to query recordings metadata.
     * </p>
     * <p>
     *     The method has no effect unless the ZelloWork Server Recording feature is turned on for the
     *     current network.
     * </p>
     * @param id Nullable; String indicating the external id.
     * @see AppState#getExternalId()
     */
    public void setExternalId(String id) {
		checkConfiguration();
		_sdk.setExternalId(id);
    }

    /**
     * Selects a contact (user or channel) to send the messages to.
     * <p>
     *     Use a <code>Contact</code> object from the list returned by
     *     <code>Zello.getContacts()</code> method as an argument.
     * </p>
     * @param contact Nullable; a contact to select. A null value will deselect the current contact.
     * @see #getSelectedContact(Contact)
     * @see #setSelectedUserOrGateway(String)
     * @see #setSelectedChannelOrGroup(String)
     * @see #selectContact(String, Tab[], Tab, Theme, Activity)
     * @see Events#onSelectedContactChanged()
     */
    public void setSelectedContact(Contact contact) {
		checkConfiguration();
		_sdk.setSelectedContact(contact);
    }

    /**
     * Selects a contact (user or gateway) to send the messages to.
     * <p>
     *     Use a <code>Contact</code> object from the list returned by
     *     <code>Zello.getContacts()</code> method as an argument.
     * </p>
     *
     * @param name Nullable; The name of the user or gateway to select. A null value will deselect the current contact.
     * @see #setSelectedContact(Contact)
     * @see #setSelectedChannelOrGroup(String)
     * @see #selectContact(String, Tab[], Tab, Theme, Activity)
     * @see Events#onSelectedContactChanged()
     */
    public void setSelectedUserOrGateway(String name) {
		checkConfiguration();
		_sdk.setSelectedUserOrGateway(name);
    }

     /**
     * Selects a contact (channel or group) to send the messages to.
     * <p>
     *     Use a <code>Contact</code> object from the list returned by
     *     <code>Zello.getContacts()</code> method as an argument.
     * </p>
     * @param name Nullable; The name of the channel or group to select. A null value will deselect the current contact.
     * @see #setSelectedContact(Contact)
     * @see #setSelectedUserOrGateway(String)
     * @see #selectContact(String, Tab[], Tab, Theme, Activity)
     * @see Events#onSelectedContactChanged()
     */
    public void setSelectedChannelOrGroup(String name) {
		checkConfiguration();
		_sdk.setSelectedChannelOrGroup(name);
    }

    //endregion

    //endregion

    //region Private Methods

    private Zello() {

    }

    private synchronized void doConfigure(String packageName, Context context) {
        if (!_configured) {
            _configured = true;

			_sdk = new Sdk();
			_sdk.onCreate(packageName, context);

            // Updates should be on by default
            leavePowerSavingMode();
        }
    }

	private synchronized void doUnconfigure() {
		_sdk.onDestroy();
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
