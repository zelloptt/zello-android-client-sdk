package com.zello.sdk;

import android.app.Activity;

/**
 * The <code>Events</code> interface provides a means to intercept key changes within the Zello SDK.
 */
public interface Events {

	/**
	 * <p>
	 *     This method is fired when the selected contact for a user changes.
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
	 *     This method is fired when the state of either the {@link MessageOut} or {@link MessageIn} changes.
	 * </p>
	 * <p>
	 *     This method is invoked on the UI thread.
	 *     To retrieve the message state changes, call the {@link Zello#getMessageIn(MessageIn)} and {@link Zello#getMessageOut(MessageOut)} methods.
	 * </p>
	 * @see Zello#getMessageIn(MessageIn)
	 * @see Zello#getMessageOut(MessageOut)
	 * @see Zello#beginMessage()
	 * @see Zello#endMessage()
	 */
	void onMessageStateChanged();

	/**
	 * <p>
	 *     This method is fired when the {@link AppState} changes.
	 * </p>
	 * <p>
	 *     This method is invoked on the UI thread.
	 *     To retrieve the <code>AppState</code> changes, call the {@link Zello#getAppState(AppState)} method.
	 * </p>
	 * @see Zello#getAppState(AppState)
	 */
	void onAppStateChanged();

	/**
	 * <p>
	 *     This method is fired when the last {@link Contacts} {@link Tab} changes.
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
	 *     This method is fired when the {@link Contacts} for the user changes.
	 * </p>
	 * <p>
	 *     This method is invoked on the UI thread.
	 *     To retrieve the <code>Contacts</code> changes, call the {@link Zello#getContacts()} method.
	 * </p>
	 * @see Zello#getContacts()
	 */
	void onContactsChanged();

	/**
	 * <p>
	 *     This method is fired when the the state of the {@link Audio} changes.
	 * </p>
	 * <p>
	 *     This method is invoked on the UI thread.
	 *     To retrieve the <code>Audio</code> changes, call the {@link Zello#getAudio()} method.
	 * </p>
	 * @see Zello#getAudio()
	 */
	void onAudioStateChanged();

}
