package com.zello.sdk;

import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;

/**
 * <p>
 *     The Zello singleton acts as the primary point of interacting to the Zello SDK.
 * </p>
 * <p>
 *     To get an instance of the object call {@link Zello#getInstance()}.
 *     Before using this class, call the {@link Zello#configure(String, Context)} method.
 *     For specific usage, see the sample projects.
 * </p>
 */
public class Zello {

    private static Zello _instance = null;

    ArrayList<Events> events = new ArrayList<Events>();

    Sdk sdk;

    // Protect against multiple attempts to configure SDK.
    boolean configured = false;

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
     * You must call <code>configure(String, Context)</code> or <code>configure(String, Context, Events)</code> before using any other SDK methods.
     * In most cases, the <code>Application.onCreate()</code> method is the best place to do this.
     * </p>
     * <p>
     * If the apk was downloaded from zellowork.com, <code>packageName</code> should be "net.loudtalks". If you are using a standalone version of the apk use "com.pttsdk".
     * </p>
     * @param packageName The package name of the Zello for Work app.
     * @param context The context for the app.
     * @see #configure(String, Context, Events)
     * @see #unconfigure()
     */
    public void configure(String packageName, Context context) {
        doConfiguration(packageName, context);
    }

    /**
     * Configures the Zello SDK and subscribes for Zello SDK <code>Events</code>.
     * <p>
     * You must call <code>configure(String, Context)</code> or <code>configure(String, Context, Events)</code> before using any other SDK methods.
     * In most cases, the <code>Application.onCreate()</code> method is the best place to do this.
     * </p>
     * <p>
     * If the apk was downloaded from zellowork.com, <code>packageName</code> should be "net.loudtalks". If you are using a standalone version of the apk use "com.pttsdk".
     * </p>
     * @param packageName The package name of the Zello for Work app.
     * @param context The context for the app.
     * @param event Events handler
     * @see #configure(String, Context)
     * @see #unconfigure()
     */
    public void configure(String packageName, Context context, Events event) {
        subscribeToEvents(event);
        doConfiguration(packageName, context);
    }

    //endregion

    //region Lifecycle Methods

    /**
     * Limits communication between the Zello for Work app and the server to optimize power and data consumption.
     * <p>
     * Call this method when Zello UI is not visible to minimize app power and data use. When in power saving mode the app won't receive non-essential status updates.
     * Call <code>leavePowerSavingMode</code> to resume status updates when Zello UI is back on the screen.
     * </p>
     * @see #leavePowerSavingMode()
     */
    public void enterPowerSavingMode() {
        sdk.onPause();
    }

    /**
     * Resumes full communication between Zello for Work app and the server.
     * @see #enterPowerSavingMode()
     */
    public void leavePowerSavingMode() {
        sdk.onResume();
    }

    /**
     * Disconnects the SDK from Zello for Work app and unsubscribes all event handlers
     * @see #configure(String, Context)
     */
    public synchronized void unconfigure() {
        if (configured) {
            sdk.onDestroy();
            events.clear();
            sdk = null;
            configured = false;
        }
    }

    //endregion

    //region Events Handlers

    /**
     * Subscribes for Zello SDK events.
     * @param event Events handler.
     * @see Events
     */
    public void subscribeToEvents(Events event) {
        if (!events.contains(event)) {
            events.add(event);
        }
    }

    /**
     * Unsubscribes from Zello SDK events.
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

    /**
     * Opens an activity, which lets the user select a user or channel to talk to.
     * <p>
     * Zello SDK provides built-in UI for contact selection, available when user is signed in.
     * You can customize the title of the activity (leave <code>null</code> for default), the tabs available,
     * initial tab, and specify dark or light theme.
     * </p>
     * <p>
     * Use this method when you don't have an activity on the screen (i.e. from service).
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
        sdk.selectContact(title, tabs, activeTab, theme);
    }

    /**
     * Opens an activity, which lets user select user or channel to talk to, from another activity.
     * <p>
     * Use this method to open contact selector UI from existing activity.
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
        sdk.selectContact(title, tabs, activeTab, theme, activity);
    }

    //region Sending Messages

    /**
     * Starts sending a voice message to currently selected user or channel.
     * <p>
     *     The method is asynchronous. When message status changes <code>onMessageStateChanged()</code>
     *     is called on <code>Events</code> interface.
     * </p>
     * @see #endMessage()
     * @see #selectContact(String, Tab[], Tab, Theme)
     * @see Events#onMessageStateChanged()
     */
    public void beginMessage() {
        sdk.beginMessage();
    }

    /**
     * Stops sending a voice message.
     * <p>
     *     The method has no effect if there is no active outgoing message.
     *     The method is asynchronous. When message status changes <code>onMessageStateChanged()</code>
     *     is called on <code>Events</code> interface.
     * </p>
     * @see #beginMessage()
     * @see Events#onMessageStateChanged()
     */
    public void endMessage() {
        sdk.endMessage();
    }

    //endregion

    //region Channels

    /**
     * Connects the user to a channel.
     * <p>
     *     The method is asynchronous. When the channel is connected <code>onContactsChanged()</code>
     *     is called on <code>Events</code> interface.
     * </p>
     * @param channel The name of the channel to connect to.
     * @see #disconnectChannel(String)
     * @see #setAutoConnectChannels(boolean)
     * @see Events#onContactsChanged()
     */
    public void connectChannel(String channel) {
        sdk.connectChannel(channel);
    }

    /**
     * Disconnects the user from a channel.
     * <p>
     *     The method is asynchronous. When the channel is disconnected <code>onContactsChanged()</code>
     *     is called on <code>Events</code> interface.
     * </p>
     * @param channel The name of the channel to disconnect from.
     * @see #connectChannel(String)
     * @see #setAutoConnectChannels(boolean)
     * @see Events#onContactsChanged()
     */
    public void disconnectChannel(String channel) {
        sdk.disconnectChannel(channel);
    }

    //endregion

    //region Contacts

    /**
     * Mutes or unmutes a contact.
     * @param contact The contact to mute or unmute.
     * @param mute    Whether the contact should be muted or not.
     */
    public void muteContact(Contact contact, boolean mute) {
        sdk.muteContact(contact, mute);
    }

    //endregion

    //region Authentication

    /**
     * Signs is the user into the network with the passed in login credentials.
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
        return sdk.signIn(network, username, password);
    }

    /**
     * Signs is the user into the network with the passed in login credentials.
     * <p>
     *     When connecting to an account hosted at zellowork.com you can use network name for <code>network</code>.
     *     When connecting to a standalone server use the full server damina name or IP.
     * </p>
     * <p>
     *     The standard sign in behavior is to save logon credentials so that the next time the app is
     *     started it signs in automatically. Set <code>perishable</code> to <code>true</code> to sign in
     *     without saving logon credentials.
     * </p>
     * <p>
     *     The method is asynchronous. When sign in fails or succeeds <code>onAppStateChanged()</code>
     *     is called on <code>Events</code> interface. To cancel sign in in progress use <code>cancelSignIn()</code>
     *     method.
     * </p>
     * @param network  The network name or URL.
     * @param username The username to authenticate.
     * @param password The password for the username.
     * @param perishable Whether or not the authentication information should be saved.
     * @return 			 boolean indicating whether a sign in was initiated or not.
     * @see #signIn(String, String, String)
     * @see #cancelSignIn()
     * @see #signOut()
     * @see Events#onAppStateChanged()
     */
    public boolean signIn(String network, String username, String password, boolean perishable) {
        return sdk.signIn(network, username, password, perishable);
    }

    /**
     * Signs out currently signed in user.
     * <p>
     *     The method does not remove saved user credentials.
     * </p>
     * <p>
     *     The method is asynchronous. When sign out succeeds <code>onAppStateChanged()</code>
     *     is called on <code>Events</code> interface.
     * </p>
     * @see #signIn(String, String, String, boolean)
     * @see Events#onAppStateChanged()
     */
    public void signOut() {
        sdk.signOut();
    }

    /**
     * Cancels currently running sign in process.
     *
     * <p>
     *     This method won't have an effect if user already signed in.
     * </p>
     * @see #signIn(String, String, String, boolean)
     */
    public void cancelSignIn() {
        sdk.cancel();
    }

    //endregion

    //region Locking

    /**
     * Locks the default Zello for Work app UI to prevent users from accessing it.
     * <p>
     *     In this locked state, the Zello for Work app when accessed by user will only display an
     *     information screen with the name of your app that can be clicked to open the main activity.
     *     This does NOT interfere with the sending and receiving of messages through the SDK.
     * </p>
     * <p>
     *     This method is useful when you implement a fully custom UI in your app and want
     *     to prevent users from accidentally accessing the standard Zello UI.
     * </p>
     * @param applicationName The name of your application.
     * @param packageName	  The package name of your application.
     * @see #unlock()
     */
    public void lock(String applicationName, String packageName) {
        sdk.lock(applicationName, packageName);
    }

    /**
     * Unlocks the default Zello for Work app UI.
     * @see #lock(String, String)
     */
    public void unlock() {
        sdk.unlock();
    }

    //endregion

    //region Status

    /**
     * Sets current user availability status.
     * @param status The status to set.
     * @see #setStatusMessage(String)
     * @see AppState#getStatus()
     * @see Events#onAppStateChanged()
     */
    public void setStatus(Status status) {
        sdk.setStatus(status);
    }

    /**
     * Sets current user custom status message text.
     * @param message The custom message text to display in user's status.
     * @see #setStatus(Status)
     * @see AppState#getStatusMessage()
     */
    public void setStatusMessage(String message) {
        sdk.setStatusMessage(message);
    }

    //endregion

    /**
     * Opens the main screen of the standard Zello for Work app.
     */
    public void openMainScreen() {
        sdk.openMainScreen();
    }

    //region Getters

    /**
     * Gets info about the current active incoming voice message.
     * <p>
     * The method updates provided instance of <code>MessageIn</code> object with the current data.
     * The object will not update automatically so call this method every time you need to get the
     * current info about an active incoming voice message.
     * </p>
     * @param message The object to copy data to.
     * @see MessageIn
     * @see Events#onMessageStateChanged()
     */
    public void getMessageIn(MessageIn message) {
        sdk.getMessageIn(message);
    }

    /**
     * Gets info about the current active outgoing voice message.
     * <p>
     * The method updates provided instance of <code>MessageOut</code> object with the current data.
     * The object will not update automatically so call this method every time you need to get the
     * current info about an active outgoing voice message.
     * </p>
     * @param message The object to copy data to.
     * @see MessageOut
     * @see Events#onMessageStateChanged()
     */
    public void getMessageOut(MessageOut message) {
        sdk.getMessageOut(message);
    }

    /**
     * Gets info about the current application state.
     * <p>
     * The method updates provided instance of <code>AppState</code> object with the current data.
     * The object will not update automatically so call this method every time you need to get the
     * current info about Zello app and SDK state.
     * </p>
     * @param state The object to copy data to.
     * @see AppState
     * @see Events#onAppStateChanged()
     */
    public void getAppState(AppState state) {
        sdk.getAppState(state);
    }

    /**
     * Gets info about currently selected contact.
     * <p>
     * The method updates provided instance of <code>Contact</code> object with the current data.
     * The object will not update automatically so call this method every time you need to get the
     * info about currently selected contact.
     * </p>
     * @param contact The object to copy data to.
     * @see Contact
     * @see #setSelectedContact(Contact)
     * @see Events#onSelectedContactChanged()
     */
    public void getSelectedContact(Contact contact) {
        sdk.getSelectedContact(contact);
    }

    /**
     * Gets the list of users and channels in the contact list of the current user
     * <p>
     *     The list includes users, chanenls and their statuses. The object returned will not update
     *     automatically so use this method to get a fresh copy of the list when needed.
     * </p>
     * <p>
     *     When the list is updated in any way <code>onContactsChanged()</code> is called on
     *     <code>Events</code> interface. Possible reasons for contact list being updated include
     *     changes of online status of users, channels connecting and disconnecting, and modifications
     *     made to the contact list through web console or API.
     * </p>
     * @return The contact list for the currently signed in user
     * @see Events#onContactsChanged()
     */
    public Contacts getContacts() {
        return sdk.getContacts();
    }

    /**
     * Gets an instance of Audio class used to control Zello audio settings
     * @return The Audio
     * @see Audio
     * @see Events#onAudioStateChanged()
     */
    public Audio getAudio() {
        return sdk.getAudio();
    }

    //endregion

    //region Setters

    /**
     * Specifies whether the Zello for Work app should be launched on the start of the OS.
     * <p>
     *     Configured autorun behavior persists between app launches.
     * </p>
     * @param enable The boolean to enable this feature or not. By default, this value is true.
     * @see #getAppState(AppState)
     * @see AppState#isAutoRunEnabled()
     */
    public void setAutoRun(boolean enable) {
        sdk.setAutoRun(enable);
    }

    /**
     * Specifies whether newly added channels should connect automatically.
     * <p>
     *     Zello remembers whether the channel was connected by the user. The default behavior when
     *     user signs in for the first time or added to a bew channel via web console or the API is
     *     to connect to that channel. Set <code>connect</code> to <code>false</code> to prevent this
     *     behavior. Call this method before <code>signIn</code> to make sure no channels are connected
     *     automatically on first sign in.
     * </p>
     * <p>
     *     The method doesn't affect the channels previously connected or disconnected by the user or
     *     the SDK.
     * </p>
     *
     * @param connect Enables autoconnect.
     * @see #connectChannel(String)
     * @see #disconnectChannel(String)
     * @see AppState#isChannelAutoConnectEnabled()
     */
    public void setAutoConnectChannels(boolean connect) {
        sdk.setAutoConnectChannels(connect);
    }

    /**
     * Sets an optional external id tag used by Zello Server Recording.
     * <p>
     *     When set the provided <code>id</code> is included in the metadata of all messages sent by
     *     Zello, and could be accessed using Zello server API to query recordings metadata.
     * </p>
     * <p>
     *     The method has no effect unless Zello Server Recording feature is turned on for the
     *     current network.
     * </p>.
     * @param id Nullable; String indicating the external id.
     * @see AppState#getExternalId()
     */
    public void setExternalId(String id) {
        sdk.setExternalId(id);
    }

    /**
     * Selects a contact (user or channel) to send the messages to
     * <p>
     *     Use the <code>Contact</code> object from the list returned by
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
        sdk.setSelectedContact(contact);
    }

    /**
     * Selects a user or radio gateway to send the messages to by name.
     *
     * @param name Nullable; The name of the user or gateway to select. A null value will deselect the current contact.
     * @see #setSelectedContact(Contact)
     * @see #setSelectedChannelOrGroup(String)
     * @see #selectContact(String, Tab[], Tab, Theme, Activity)
     * @see Events#onSelectedContactChanged()
     */
    public void setSelectedUserOrGateway(String name) {
        sdk.setSelectedUserOrGateway(name);
    }

    /**
     * Selects a channel or group to send the messages to by name.
     * @param name Nullable; The name of the channel or group to select. A null value will deselect the current contact.
     * @see #setSelectedContact(Contact)
     * @see #setSelectedUserOrGateway(String)
     * @see #selectContact(String, Tab[], Tab, Theme, Activity)
     * @see Events#onSelectedContactChanged()
     */
    public void setSelectedChannelOrGroup(String name) {
        sdk.setSelectedChannelOrGroup(name);
    }

    //endregion

    //endregion

    //region Private Methods

    private Zello() {

    }

    private synchronized void doConfiguration(String packageName, Context context) {
        if (!configured) {
            configured = true;

            sdk = new Sdk();
            sdk.onCreate(packageName, context);

            // Updates should be on by default
            leavePowerSavingMode();
        }
    }

    //endregion

}
