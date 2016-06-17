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
	 *     <code>Contact</code> is offline.
	 * </p>
	 * <p>
	 *     Messages can potentially be delivered to a <code>Contact</code> with this status.
	 *     If the <code>Contact</code> signs in before the user signs out, the message will be delivered to the <code>Contact</code> the next time they sign in.
	 * </p>
	 */
	OFFLINE,
	/**
	 * <p>
	 *     <code>Contact</code> is online and available to talk.
	 * </p>
	 * <p>
	 *     Messages will be immediately delivered to a <code>Contact</code> with this status.
	 *     Delivered messages will fire the {@link Events#onMessageStateChanged()} method.
	 * </p>
	 * @see Events#onMessageStateChanged()
	 */
	AVAILABLE,
	/**
	 * <p>
	 *     <code>Contact</code> is online but currently busy.
	 * </p>
	 * <p>
	 *     Messages will be delivered to the Zello for Work app of the <code>Contact</code> with this status, but they will not be delivered to the Zello SDK.
	 * </p>
	 */
	BUSY,
	/**
	 * <p>
	 *     <code>Contact</code> is offline but was recently online.
	 * </p>
	 * <p>
	 *     Messages sent to a <code>Contact</code> with this status will be delivered to the Zello SDK for the <code>Contact</code> the next time they sign in.
	 * </p>
	 */
	STANDBY,
	/**
	 * <p>
	 *     <code>Contact</code> is currently connecting to the network.
	 * </p>
	 * <p>
	 *     Messages sent to a <code>Contact</code> with this status will be delivered to the Zello SDK when they finish signing in.
	 *     Delivered messages will fire the {@link Events#onMessageStateChanged()} method.
	 * </p>
	 * @see Events#onMessageStateChanged()
	 */
	CONNECTING

}
