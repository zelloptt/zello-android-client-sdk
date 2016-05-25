package com.zello.sdk;

/**
 * The Events interface provides a means to intercept key changes within the Zello SDK.
 */
public interface Events {

	/**
	 * The onSelectedContactChanged() method is fired when the selected contact for a user changes.
	 */
	void onSelectedContactChanged();

	/**
	 * The onMessageStateChanged() method is fired when the state of either a MessageOut or MessageIn changes.
	 */
	void onMessageStateChanged();

	/**
	 * The onAppStateChanged() method is fired when the AppState changes.
	 */
	void onAppStateChanged();

	/**
	 * The onLastContactsTabChanged() method is fired when the last Contacts tab changes.
	 * @param tab
     */
	void onLastContactsTabChanged(Tab tab);

	/**
	 * The onContactsChanged() method is fired when the Contacts for a user changes.
	 */
	void onContactsChanged();

	/**
	 * The onAudioStateChanged() method is fired when the the state of the audio changes.
	 */
	void onAudioStateChanged();

}
