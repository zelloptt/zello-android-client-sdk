package com.zello.sdk;

/**
 * <p>
 *     The <code>ContactStatus</code> enum represents the status of a {@link Contact}.
 * </p>
 * <p>
 *     For the status of the authenticated user, see the {@link Status} enum.
 * </p>
 * */
public enum ContactStatus {

	/**
	 * <p>
	 *     Contact is offline.
	 * </p>
	 * <p>
	 *     Messages cannot be sent to a channel with this status. If channel is <code>OFFLINE</code> you need
	 *     to connect to it before sending the message.
	 * </p>
	 * <p>
	 *     Messages can be sent to a user with this status and will be saved locally for later delivery, which
	 *     will happen when user comes back online.
	 * </p>
	 */
	OFFLINE,
	/**
	 * <p>
	 *     Contact is online and available to talk.
	 * </p>
	 * <p>
	 *     Messages will be immediately delivered to a contact with this status.
	 * </p>
	 */
	AVAILABLE,
	/**
	 * <p>
	 *     Contact is online but currently busy.
	 * </p>
	 * <p>
	 *     Messages will be delivered to the Zello for Work app of the contact with this status and
	 *     will be saved to the history. However they won't play live and won't be delivered to the Zello SDK.
	 * </p>
	 */
	BUSY,
	/**
	 * <p>
	 *     Contact is offline but was recently online.
	 * </p>
	 * <p>
	 *     Messages sent to a contact with this status will be saved on the server and delivered to
	 *     the contact the next time they sign in. Depending on the plattform and configuration they
	 *     may also get the push notification about the message.
	 * </p>
	 */
	STANDBY,
	/**
	 * <p>
	 *     The channel is in the process of connecting.
	 * </p>
	 * <p>
	 *     This status only applies to channels. You cannot send messages to a channel in this state.
	 * </p>
	 * @see Events#onMessageStateChanged()
	 */
	CONNECTING

}
