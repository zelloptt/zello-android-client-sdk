package com.zello.sdk;

import android.app.Activity;

/**
 * The <code>Events</code> interface enables monitoring of Zello SDK state and property changes
 */
public interface Events {

	/**
	 * <p>
	 *     Called when the selected contact changes.
	 * </p>
	 * <p>
	 *     This method is invoked on the UI thread.
	 *     To retrieve the newly selected contact, call the {@link Zello#getSelectedContact(Contact)} method.
	 * </p>
	 * @see Zello#getSelectedContact(Contact)
	 * @see Zello#selectContact(String, Tab[], Tab, Theme)
	 * @see Zello#selectContact(String, Tab[], Tab, Theme, Activity)
	 * @see Zello#setSelectedContact(Contact)
	 * @see Zello#setSelectedUserOrGateway(String)
	 * @see Zello#setSelectedChannelOrGroup(String)
	 */
	void onSelectedContactChanged();

	/**
	 * <p>
	 *     Called when the state of either the {@link MessageOut} or {@link MessageIn} changes.
	 * </p>
	 * <p>
	 *     This method is invoked on the UI thread.
	 *     To retrieve the current message state, call the {@link Zello#getMessageIn(MessageIn)} and {@link Zello#getMessageOut(MessageOut)} methods.
	 * </p>
	 * @see Zello#getMessageIn(MessageIn)
	 * @see Zello#getMessageOut(MessageOut)
	 * @see Zello#beginMessage()
	 * @see Zello#endMessage()
	 */
	void onMessageStateChanged();

	/**
	 * <p>
	 *     Called when the {@link AppState} changes.
	 * </p>
	 * <p>
	 *     This method is invoked on the UI thread.
	 *     To retrieve the current <code>AppState</code>, call the {@link Zello#getAppState(AppState)} method.
	 * </p>
	 * @see Zello#getAppState(AppState)
	 */
	void onAppStateChanged();

	/**
	 * <p>
	 *     Called when the last {@link Contacts} {@link Tab} changes.
	 * </p>
	 * <p>
	 *     This method is invoked on the UI thread.
	 * </p>
	 * @param tab The tab that changed.
	 * @see Zello#selectContact(String, Tab[], Tab, Theme)
	 * @see Zello#selectContact(String, Tab[], Tab, Theme, Activity)
     */
	void onLastContactsTabChanged(Tab tab);

	/**
	 * <p>
	 *     Called when the {@link Contacts} for the user changes.
	 * </p>
	 * <p>
	 *     This method is invoked on the UI thread.
	 *     To retrieve the current <code>Contacts</code> snapshot, call the {@link Zello#getContacts()} method.
	 *     The best approach, when dealing with large contact lists (in 1000s) is to run both <code>getContacts()</code>
	 *     and any contact processing in a background thread, then post the result to UI thread for display.
	 * </p>
	 * @see Zello#getContacts()
	 */
	void onContactsChanged();

	/**
	 * <p>
	 *     Called when the the state of the {@link Audio} changes.
	 * </p>
	 * <p>
	 *     This method is invoked on the UI thread.
	 *     To retrieve the current <code>Audio</code>, call the {@link Zello#getAudio()} method.
	 * </p>
	 * @see Zello#getAudio()
	 */
	void onAudioStateChanged();

	/**
	 * <p>
	 *     Called when an invocation of the {@link Zello#beginMessage()} method failed because the microphone permission hasn't been granted.
	 * </p>
	 * <p>
	 *     This method is invoked on the UI thread.
	 * </p>
	 * <p>
	 *     The normal use case for handling this error would be to call {@link Zello#showMicrophonePermissionDialog()}.
	 *     However, it is the responsibility of the app using the SDK to determine if they should handle this error or not.
	 *     For example, if there are multiple apps using the SDK, they will all receive this this callback. The {@link Zello#showMicrophonePermissionDialog()}
	 *     method should likely only be on one of these apps (the one in the foreground).
	 * </p>
	 * @see Zello#showMicrophonePermissionDialog()
	 * @see Zello#beginMessage()
	 */
	void onMicrophonePermissionNotGranted();

}
