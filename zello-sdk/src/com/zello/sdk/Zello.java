package com.zello.sdk;

import android.content.Context;

import java.util.ArrayList;

/**
 * <pre>
 * The Zello class acts as the primary means of interacting to the Zello SDK.
 * </pre>
 * <pre>
 * For specific usage, please see the sample projects.
 * </pre>
 */
public final class Zello {

    static ArrayList<Events> events = new ArrayList<Events>();

    static Sdk sdk = new Sdk();

    // Protect against multiple attempts to initialize SDK.
    static boolean initialized = false;

    //region Initialization

    /**
     * The initialize() method initializes the Zello SDK without immediately subscribing to the Events.
     * @param packageName The package name of the PTT app.
     * @param context The context for the app.
     */
    public static void initialize(String packageName, Context context) {
        doInitialization(packageName, context);
    }

    /**
     * The initialize() method initializes the Zello SDK and subscribes the passed in Events to receive updates.
     * @param packageName The package name of the PTT app.
     * @param context The context for the app.
     * @param event The Events implementor to subscribe.
     */
    public static void initialize(String packageName, Context context, Events event) {
        subscribeToEvents(event);
        Zello.doInitialization(packageName, context);
    }

    //endregion

    //region Lifecycle Methods

    /**
     * <pre>
     * The enterPowerSavingMode() method limits communication with the Zello for Work app to optimize power and data consumption.
     * </pre>
     * <pre>
     * The Zello SDK communicates with the Zello for Work app to send and receive updates.
     * By invoking this method, this communication will be limited.
     * </pre>
     */
    public static void enterPowerSavingMode() {
        sdk.onPause();
    }

    /**
     * <pre>
     * The leavePowerSavingMode() method resumes normal communication with the Zello for Work app.
     * </pre>
     * <pre>
     * The Zello SDK communicates with the Zello for Work app to send and receive updates.
     * By invoking this method, this limited communication will return to normal.
     * </pre>
     */
    public static void leavePowerSavingMode() {
        sdk.onResume();
    }

    /**
     * <pre>
     * The uninitialize() method terminates communication with the Zello for Work app.
     * </pre>
     * <pre>
     * The Zello SDK communicates with the Zello for Work app to send and receive updates.
     * By invoking this method, this communication will end.
     * </pre>
     */
    public static void uninitialize() {
        sdk.onDestroy();
    }

    //endregion

    //region Events Handlers

    /**
     * The subscribeToEvents() method ensures the passed in Events implementor will receive Events method invocations.
     * @param event The Events instance to subscribe.
     */
    public static void subscribeToEvents(Events event) {
        if (!events.contains(event)) {
            events.add(event);
        }
    }

    /**
     * The unsubscribeFromEvents() method ensures the passed in Events implementor will no longer receive Events method invocations.
     * @param event The Events instance to unsubscribe.
     */
    public static void unsubscribeFromEvents(Events event) {
        if (events.contains(event)) {
            events.remove(event);
        }
    }

    //endregion

    //region Zello SDK Methods

    /**
     * The selectContact() opens an Activity that displays the authenticated users Contacts to select.
     * @param title     Nullable; Activity Title.
     * @param tabs		Set of displayed Tabs.
     * @param activeTab Initially active Tab.
     * @param theme     Visual Theme for Activity.
     */
    public static void selectContact(String title, Tab[] tabs, Tab activeTab, Theme theme) {
        sdk.selectContact(title, tabs, activeTab, theme);
    }

    //region Sending Messages

    /**
     * <pre>
     * The beginMessage() method is the starting point for sending a message through the Zello SDK.
     * </pre>
     * <pre>
     * Once called, a message will be recorded until endMessage() method is called.
     * </pre>
     */
    public static void beginMessage() {
        sdk.beginMessage();
    }

    /**
     * <pre>
     * The endMessage() method is the ending point for sending a message through the Zello SDK.
     * </pre>
     * <pre>
     * Prerequisites: There must be an invocation of the beginMessage() method.
     * </pre>
     */
    public static void endMessage() {
        sdk.endMessage();
    }

    //endregion

    //region Channels

    /**
     * The connectChannel() method connects the authenticated user to a channel for users to communicate through.
     * @param channel The name of the channel to connect to.
     */
    public static void connectChannel(String channel) {
        sdk.connectChannel(channel);
    }

    /**
     * The disconnectChannel() method disconnects the user from the channel.
     * @param channel The name of the channel to disconnect from.
     */
    public static void disconnectChannel(String channel) {
        sdk.disconnectChannel(channel);
    }

    //endregion

    //region Contacts

    /**
     * The muteContact() method either mutes or unmutes a contact.
     * @param contact The contact to mute or unmute.
     * @param mute    Whether the contact should be muted or not.
     */
    public static void muteContact(Contact contact, boolean mute) {
        sdk.muteContact(contact, mute);
    }

    //endregion

    //region Authentication

    /**
     * The signIn() method authenticates the user on the network with the passed in login credentials.
     * @param network  The network to authenticate against.
     * @param username The username to authenticate.
     * @param password The password for the username.
     * @return 		   boolean indicating whether a sign in was initiated or not.
     */
    public static boolean signIn(String network, String username, String password) {
        return sdk.signIn(network, username, password);
    }

    /**
     * The signIn() method authenticates the user on the network with the passed in login credentials with an option for the authentication to perish.
     * @param network    The network to authenticate against.
     * @param username   The username to authenticate.
     * @param password   The password for the username.
     * @param perishable Whether or not the authentication information should be saved.
     * @return 			 boolean indicating whether a sign in was initiated or not.
     */
    public static boolean signIn(String network, String username, String password, boolean perishable) {
        return sdk.signIn(network, username, password, perishable);
    }

    /**
     * The signOut() method unauthenticates the user from the network.
     */
    public static void signOut() {
        sdk.signOut();
    }

    /**
     * The cancelSignIn() method cancels the ongoing authentication request from the signIn() method.
     */
    public static void cancelSignIn() {
        sdk.cancel();
    }

    //endregion

    //region Locking

    /**
     * <pre>
     * The lock() method puts the UI for the Zello for Work app into a locked state.
     * </pre>
     * <pre>
     * In this locked state, the Zello for Work app will only display an information screen with the name of your app that can be clicked to open the main activity.
     * This does NOT interfere with the sending and receiving of messages through the Zello for Work app.
     * </pre>
     * @param applicationName The name of the application.
     * @param packageName	  The package name of the application.
     */
    public static void lock(String applicationName, String packageName) {
        sdk.lock(applicationName, packageName);
    }

    /**
     * The unlock() method unlocks the UI of the Zello for Work app.
     */
    public static void unlock() {
        sdk.unlock();
    }

    //endregion

    //region Status

    /**
     * The setStatus() method sets the status of the authenticated user to a Status message.
     * @param status The state to set the users status to.
     */
    public static void setStatus(Status status) {
        sdk.setStatus(status);
    }

    /**
     * The setStatusMessage() method sets the status of the authenticated user to a custom message.
     * @param message The custom message to set the users status to.
     */
    public static void setStatusMessage(String message) {
        sdk.setStatusMessage(message);
    }

    //endregion

    /**
     * The openMainScreen() method will open the Zello for Work app upon invocation.
     */
    public static void openMainScreen() {
        sdk.openMainScreen();
    }

    //region Getters

    /**
     * <pre>
     * The getMessageIn() method returns a copy of the current incoming message from the Zello for Work app.
     * </pre>
     * <pre>
     * This copy is a snapshot of the state of the MessageIn at the time of invocation.
     * It is static in the sense that the Zello SDK will not update it.
     * </pre>
     * @param message MessageIn to copy into.
     */
    public static void getMessageIn(MessageIn message) {
        sdk.getMessageIn(message);
    }

    /**
     * <pre>
     * The getMessageOut() method returns a copy of the current outgoing message from the Zello for Work app.
     * </pre>
     * <pre>
     * This copy is a snapshot of the state of the MessageOut at the time of invocation.
     * It is static in the sense that the Zello SDK will not update it.
     * </pre>
     * @param message MessageOut to copy into.
     */
    public static void getMessageOut(MessageOut message) {
        sdk.getMessageOut(message);
    }

    /**
     * <pre>
     * The getAppState() method returns a copy of the current AppState from the Zello for Work app.
     * </pre>
     * <pre>
     * This copy is a snapshot of the state of the AppState at the time of invocation.
     * It is static in the sense that the Zello SDK will not update it.
     * </pre>
     * * @param state AppState to copy into.
     */
    public static void getAppState(AppState state) {
        sdk.getAppState(state);
    }

    /**
     * <pre>
     * The getSelectedContact() method returns a copy of the current selected Contact from the Zello for Work app.
     * </pre>
     * <pre>
     * This copy is a snapshot of the state of the Contact at the time of invocation.
     * It is static in the sense that the Zello SDK will not update it.
     * </pre>
     * @param contact Contact to copy into.
     */
    public static void getSelectedContact(Contact contact) {
        sdk.getSelectedContact(contact);
    }

    /**
     * <pre>
     * The getContacts() method returns the Contacts for the authenticated user.
     * </pre>
     * <pre>
     * This copy is a snapshot of the state of the Contacts at the time of invocation.
     * It is static in the sense that the Zello SDK will not update it.
     * </pre>
     * @return The Contacts object for the user.
     */
    public static Contacts getContacts() {
        return sdk.getContacts();
    }

    /**
     * <pre>
     * The getAudio() method returns the current Audio instance for the Zello for Work app.
     * </pre>
     * <pre>
     * This copy is a snapshot of the state of the Audio at the time of invocation.
     * It is static in the sense that the Zello SDK will not update it.
     * </pre>
     * @return The Audio instance.
     */
    public static Audio getAudio() {
        return sdk.getAudio();
    }

    //endregion

    //region Setters

    /**
     * The setAutoRun() method determines if the app should be launched on the start of the OS or not.
     * @param enable The boolean to enable this feature or not. By default, this value is false.
     */
    public static void setAutoRun(boolean enable) {
        sdk.setAutoRun(enable);
    }

    /**
     * The setAutoConnectChannels() method determines if new channels should be automatically connected to.
     * @param connect The boolean to enable this feature or not.
     */
    public static void setAutoConnectChannels(boolean connect) {
        sdk.setAutoConnectChannels(connect);
    }

    /**
     * The setExternalId() method sets an external id tag onto messages recorded on the server.
     * This tag is only recorded if the server history feature is enabled on the Zello for Work console.
     * @param id Nullable; String indicating the external id.
     */
    public static void setExternalId(String id) {
        sdk.setExternalId(id);
    }

    /**
     * <pre>
     * The setSelectedContact() method sets the selected contact to a specified Contact.
     * </pre>
     * <pre>
     * The selected Contact will be the contact that a voice message will be sent to upon a Zello.beginMessage() invocation.
     * </pre>
     * @param contact Nullable; Contact to select. A null value will deselect the current Contact.
     */
    public static void setSelectedContact(Contact contact) {
        sdk.setSelectedContact(contact);
    }

    /**
     * The setSelectedUserOrGateway() method sets the selected contact to a specified User or Gateway.
     * @param name Nullable; The name of the User or Gateway to select. A null value will deselect the current Contact.
     */
    public static void setSelectedUserOrGateway(String name) {
        sdk.setSelectedUserOrGateway(name);
    }

    /**
     * The setSelectedChannelOrGroup() method sets the selected contact to a specified Channel or Group.
     * @param name Nullable; The name of the Channel or Group to select. A null value will deselect the current Contact.
     */
    public static void setSelectedChannelOrGroup(String name) {
        sdk.setSelectedChannelOrGroup(name);
    }

    //endregion

    //endregion

    //region Private Methods

    private Zello() {

    }

    private static void doInitialization(String packageName, Context context) {
        if (!initialized) {
            initialized = true;

            sdk.onCreate(packageName, context);

            // Updates should be on by default
            Zello.leavePowerSavingMode();
        }
    }

    //endregion

}
