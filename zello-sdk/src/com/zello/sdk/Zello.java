package com.zello.sdk;

import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;

/**
 * <pre>
 * The Zello singleton acts as the primary means of interacting to the Zello SDK.
 * </pre>
 * <pre>
 * To use, call the Zello.getInstance().configure() method and pass in the necessary parameters.
 * For specific usage, please see the sample projects.
 * </pre>
 */
public class Zello {

    private static Zello _instance = null;

    ArrayList<Events> events = new ArrayList<Events>();

    Sdk sdk;

    // Protect against multiple attempts to configure SDK.
    boolean configured = false;

    //region Instance

    public static Zello getInstance() {
        if (_instance == null) {
            _instance = new Zello();
        }
        
        return _instance;
    }

    //endregion

    //region Configuration

    /**
     * The configure() method configures the Zello SDK without immediately subscribing to the Events.
     * @param packageName The package name of the Zello for Work app. If the apk was downloaded off zellowork.com, this parameter should be "net.loudtalks"
     * @param context The context for the app.
     */
    public void configure(String packageName, Context context) {
        doConfiguration(packageName, context);
    }

    /**
     * The configure() method configures the Zello SDK and subscribes the passed in Events to receive updates.
     * @param packageName The package name of the Zello for Work app. If the apk was downloaded off zellowork.com, this parameter should be "net.loudtalks"
     * @param context The context for the app.
     * @param event The Events implementor to subscribe.
     */
    public void configure(String packageName, Context context, Events event) {
        subscribeToEvents(event);
        doConfiguration(packageName, context);
    }

    //endregion

    //region Lifecycle Methods

    /**
     * <pre>
     * The enterPowerSavingMode() method limits communication between the Zello for Work app and the server to optimize power and data consumption.
     * </pre>
     * <pre>
     * The Zello SDK communicates with the Zello for Work app to send and receive updates.
     * By invoking this method, this communication will be limited.
     * </pre>
     */
    public void enterPowerSavingMode() {
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
    public void leavePowerSavingMode() {
        sdk.onResume();
    }

    /**
     * <pre>
     * The unconfigure() method terminates communication with the Zello for Work app.
     * </pre>
     * <pre>
     * The Zello SDK communicates with the Zello for Work app to send and receive updates.
     * By invoking this method, this communication will end.
     * </pre>
     */
    public void unconfigure() {
        sdk.onDestroy();

        sdk = null;
        events = null;
        configured = false;

        _instance = null;
    }

    //endregion

    //region Events Handlers

    /**
     * The subscribeToEvents() method ensures the passed in Events implementor will receive Events method invocations.
     * @param event The Events instance to subscribe.
     */
    public void subscribeToEvents(Events event) {
        if (!events.contains(event)) {
            events.add(event);
        }
    }

    /**
     * The unsubscribeFromEvents() method ensures the passed in Events implementor will no longer receive Events method invocations.
     * @param event The Events instance to unsubscribe.
     */
    public void unsubscribeFromEvents(Events event) {
        if (events.contains(event)) {
            events.remove(event);
        }
    }

    //endregion

    //region Zello SDK Methods

    /**
     * <pre>
     * The selectContact() method opens an Activity that displays the authenticated user's Contacts to select.
     * </pre>
     * <pre>
     * This method should be used if the Zello SDK was initialized from the Application Context.
     * </pre>
     * @param title     Nullable; Activity Title.
     * @param tabs		Set of displayed Tabs.
     * @param activeTab Initially active Tab.
     * @param theme     Visual Theme for Activity.
     */
    public void selectContact(String title, Tab[] tabs, Tab activeTab, Theme theme) {
        sdk.selectContact(title, tabs, activeTab, theme);
    }

    /**
     * <pre>
     * The selectContact() opens an Activity that displays the authenticated user's Contacts to select.
     * </pre>
     * <pre>
     * This method should be used if the Zello SDK was initialized from an Activity Context.
     * </pre>
     * @param title     Nullable; Activity Title.
     * @param tabs		Set of displayed Tabs.
     * @param activeTab Initially active Tab.
     * @param theme     Visual Theme for Activity.
     * @param activity  Activity that is calling this method (ie. this).
     */
    public void selectContact(String title, Tab[] tabs, Tab activeTab, Theme theme, Activity activity) {
        sdk.selectContact(title, tabs, activeTab, theme, activity);
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
    public void beginMessage() {
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
    public void endMessage() {
        sdk.endMessage();
    }

    //endregion

    //region Channels

    /**
     * The connectChannel() method connects the authenticated user to a channel for users to communicate through.
     * @param channel The name of the channel to connect to.
     */
    public void connectChannel(String channel) {
        sdk.connectChannel(channel);
    }

    /**
     * The disconnectChannel() method disconnects the user from the channel.
     * @param channel The name of the channel to disconnect from.
     */
    public void disconnectChannel(String channel) {
        sdk.disconnectChannel(channel);
    }

    //endregion

    //region Contacts

    /**
     * The muteContact() method either mutes or unmutes a contact.
     * @param contact The contact to mute or unmute.
     * @param mute    Whether the contact should be muted or not.
     */
    public void muteContact(Contact contact, boolean mute) {
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
    public boolean signIn(String network, String username, String password) {
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
    public boolean signIn(String network, String username, String password, boolean perishable) {
        return sdk.signIn(network, username, password, perishable);
    }

    /**
     * The signOut() method unauthenticates the user from the network.
     */
    public void signOut() {
        sdk.signOut();
    }

    /**
     * The cancelSignIn() method cancels the ongoing authentication request from the signIn() method.
     */
    public void cancelSignIn() {
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
    public void lock(String applicationName, String packageName) {
        sdk.lock(applicationName, packageName);
    }

    /**
     * The unlock() method unlocks the UI of the Zello for Work app.
     */
    public void unlock() {
        sdk.unlock();
    }

    //endregion

    //region Status

    /**
     * The setStatus() method sets the status of the authenticated user to a Status message.
     * @param status The state to set the user's status to.
     */
    public void setStatus(Status status) {
        sdk.setStatus(status);
    }

    /**
     * The setStatusMessage() method sets the status of the authenticated user to a custom message.
     * @param message The custom message to set the user's status to.
     */
    public void setStatusMessage(String message) {
        sdk.setStatusMessage(message);
    }

    //endregion

    /**
     * The openMainScreen() method will open the Zello for Work app upon invocation.
     */
    public void openMainScreen() {
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
    public void getMessageIn(MessageIn message) {
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
    public void getMessageOut(MessageOut message) {
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
    public void getAppState(AppState state) {
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
    public void getSelectedContact(Contact contact) {
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
    public Contacts getContacts() {
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
     * @return The Audio 
     */
    public Audio getAudio() {
        return sdk.getAudio();
    }

    //endregion

    //region Setters

    /**
     * The setAutoRun() method determines if the app should be launched on the start of the OS or not.
     * @param enable The boolean to enable this feature or not. By default, this value is false.
     */
    public void setAutoRun(boolean enable) {
        sdk.setAutoRun(enable);
    }

    /**
     * <pre>
     * The setAutoConnectChannels() method determines if new channels should be automatically connected to.
     * </pre>
     * <pre>
     * By enabling this feature, any new channel that the authenticated user is added to will be automatically connected to.
     * </pre>
     * @param connect The boolean to enable this feature or not.
     */
    public void setAutoConnectChannels(boolean connect) {
        sdk.setAutoConnectChannels(connect);
    }

    /**
     * The setExternalId() method sets an external id tag onto messages recorded on the server.
     * This tag is only recorded if the server recording feature is enabled on the Zello for Work console.
     * @param id Nullable; String indicating the external id.
     */
    public void setExternalId(String id) {
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
    public void setSelectedContact(Contact contact) {
        sdk.setSelectedContact(contact);
    }

    /**
     * The setSelectedUserOrGateway() method sets the selected contact to a specified User or Gateway.
     * @param name Nullable; The name of the User or Gateway to select. A null value will deselect the current Contact.
     */
    public void setSelectedUserOrGateway(String name) {
        sdk.setSelectedUserOrGateway(name);
    }

    /**
     * The setSelectedChannelOrGroup() method sets the selected contact to a specified Channel or Group.
     * @param name Nullable; The name of the Channel or Group to select. A null value will deselect the current Contact.
     */
    public void setSelectedChannelOrGroup(String name) {
        sdk.setSelectedChannelOrGroup(name);
    }

    //endregion

    //endregion

    //region Private Methods

    private Zello() {

    }

    private void doConfiguration(String packageName, Context context) {
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
