package com.zello.sdk;

import android.app.Activity;

/**
 * <p>
 *     The <code>Status</code> enum represents the status of the current user.
 * </p>
 * <p>
 *     For the status of a {@link Contact}, see the {@link ContactStatus} enum.
 * </p>
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public enum Status {

	/**
	 * <p>
	 *     User is online and available to talk.
	 * </p>
	 * <p>
	 *     Messages are immediately delivered to the Zello SDK and play live.
	 *     Delivered messages call the <code>onMessageStateChanged()</code>
	 *     method on the <code>Events</code> interface.
	 * </p>
	 * @see Events#onMessageStateChanged()
	 */
	AVAILABLE,
	/**
	 * <p>
	 *      User is online but currently busy.
	 * </p>
	 * <p>
	 *      Messages are not delivered to the Zello SDK and do not play live.
	 *      However, they are delivered to the ZelloWork app and save in history.
	 * </p>
	 * */
	BUSY,
	/**
	 * <p>
	 *     User is online but only available to talk with the selected <code>Contact</code>.
	 * </p>
	 * <p>
	 *     Messages from the selected <code>Contact</code> are immediately delivered to the Zello SDK and play live.
	 *     Delivered messages call the <code>onMessageStateChanged()</code>
	 *     method on the <code>Events</code> interface.
	 * </p>
	 * <p>
	 *     Messages from a non-selected <code>Contact</code> are not delivered to the Zello SDK and do not play live.
	 *     However, they are delivered to the ZelloWork app and save in history.
	 * </p>
	 * @see Zello#getSelectedContact(Contact)
	 * @see Zello#selectContact(String, Tab[], Tab, Theme)
	 * @see Zello#selectContact(String, Tab[], Tab, Theme, Activity)
	 * @see Zello#setSelectedContact(Contact)
	 * @see Zello#setSelectedUserOrGateway(String)
	 * @see Zello#setSelectedChannelOrGroup(String)
	 * @see Events#onMessageStateChanged()
	 */
	SOLO

}
