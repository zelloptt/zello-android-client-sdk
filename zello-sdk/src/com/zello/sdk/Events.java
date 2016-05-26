package com.zello.sdk;

/**
 * The Events interface provides a means to intercept key changes within the Zello SDK.
 */
public interface Events {

	/**
	 * The onSelectedContactChanged() method is fired when the selected contact for a user changes.
	 * This method is invoked on the UI thread.
	 */
	void onSelectedContactChanged();

	/**
	 * The onMessageStateChanged() method is fired when the state of either a MessageOut or MessageIn changes.
	 * This method is invoked on the UI thread.
	 */
	void onMessageStateChanged();

	/**
	 * The onAppStateChanged() method is fired when the AppState changes.
	 * This method is invoked on the UI thread.
	 */
	void onAppStateChanged();

	/**
	 * The onLastContactsTabChanged() method is fired when the last Contacts tab changes.
	 * This method is invoked on the UI thread.
	 * @param tab
     */
	void onLastContactsTabChanged(Tab tab);

	/**
	 * The onContactsChanged() method is fired when the Contacts for a user changes.
	 * This method is invoked on the UI thread.
	 */
	void onContactsChanged();

	/**
	 * The onAudioStateChanged() method is fired when the the state of the audio changes.
	 * This method is invoked on the UI thread.
	 */
	void onAudioStateChanged();

}
