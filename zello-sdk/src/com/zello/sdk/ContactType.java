package com.zello.sdk;

/**
 * The <code>ContactType</code> enum represents the type of {@link Contact}.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public enum ContactType {

	/**
	 * <code>Contact</code> is a single user.
	 */
	USER,
	/**
	 * <code>Contact</code> is a channel.
	 */
	CHANNEL,
	/**
	 * <code>Contact</code> is a group.
	 */
	GROUP,
	/**
	 * <code>Contact</code> is a gateway.
	 */
	GATEWAY,

	/**
	 * <code>Contact</code> is a conversation.
	 */
	CONVERSATION

}
