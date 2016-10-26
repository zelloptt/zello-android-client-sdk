package com.zello.sdk;

import android.app.Activity;

/**
 * The <code>Tab</code> enum represents the contact selection UI tabs of the ZelloWork app.
 * @see Zello#selectContact(String, Tab[], Tab, Theme, Activity)
 */
public enum Tab {

	/**
	 * Tab populated with recent users and channels.
	 */
	RECENTS,
	/**
	 * Tab populated with the list of users.
	 */
	USERS,
	/**
	 * Tab populated with the list of channels.
	 */
	CHANNELS

}
