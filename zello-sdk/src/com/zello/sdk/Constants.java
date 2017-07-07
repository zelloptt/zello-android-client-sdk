package com.zello.sdk;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Constants {

	public static final int STAY_AWAKE_TIMEOUT = 3000;

	public static final String EXTRA_TABS = "TABS";
	public static final String EXTRA_TAB = "TAB";
	public static final String EXTRA_CALLBACK = "CALLBACK";
	public static final String EXTRA_THEME = "THEME";
	public static final String EXTRA_COMMAND = "COMMAND";
	public static final String EXTRA_STATE_CUSTOM_BUILD = "STATE_CUSTOM_BUILD";
	public static final String EXTRA_STATE_CONFIGURING = "STATE_CONFIGURING";
	public static final String EXTRA_STATE_LOCKED = "STATE_LOCKED";
	public static final String EXTRA_STATE_SIGNED_IN = "STATE_SIGNED_IN";
	public static final String EXTRA_STATE_SIGNING_IN = "STATE_SIGNING_IN";
	public static final String EXTRA_STATE_SIGNING_OUT = "STATE_SIGNING_OUT";
	public static final String EXTRA_STATE_CANCELLING_SIGNIN = "STATE_CANCELLING_SIGNIN";
	public static final String EXTRA_STATE_RECONNECT_TIMER = "STATE_RECONNECT_TIMER";
	public static final String EXTRA_STATE_WAITING_FOR_NETWORK = "STATE_WAITING_FOR_NETWORK";
	public static final String EXTRA_STATE_SHOW_CONTACTS = "STATE_SHOW_CONTACTS";
	public static final String EXTRA_STATE_BUSY = "STATE_BUSY";
	public static final String EXTRA_STATE_SOLO = "STATE_SOLO";
	public static final String EXTRA_STATE_AUTO_RUN = "STATE_AUTO_RUN";
	public static final String EXTRA_STATE_AUTO_CHANNELS = "STATE_AUTO_CHANNELS";
	public static final String EXTRA_STATE_STATUS_MESSAGE = "STATE_STATUS_MESSAGE";
	public static final String EXTRA_STATE_NETWORK = "STATE_NETWORK";
	public static final String EXTRA_STATE_NETWORK_URL = "STATE_NETWORK_URL";
	public static final String EXTRA_STATE_USERNAME = "STATE_USERNAME";
	public static final String EXTRA_STATE_LAST_ERROR = "STATE_LAST_ERROR";
	public static final String EXTRA_CONTACT_NAME = "CONTACT_NAME";
	public static final String EXTRA_CONTACT_FULL_NAME = "CONTACT_FULL_NAME";
	public static final String EXTRA_CONTACT_DISPLAY_NAME = "CONTACT_DISPLAY_NAME";
	public static final String EXTRA_CONTACT_TYPE = "CONTACT_TYPE";
	public static final String EXTRA_CONTACT_STATUS = "CONTACT_STATUS";
	public static final String EXTRA_CONTACT_STATUS_MESSAGE = "CONTACT_STATUS_MESSAGE";
	public static final String EXTRA_CHANNEL_USERS_COUNT = "CHANNEL_USERS_COUNT";
	public static final String EXTRA_CHANNEL_USERS_TOTAL = "CHANNEL_USERS_TOTAL";
	public static final String EXTRA_CONTACT_TITLE = "CONTACT_TITLE";
	public static final String EXTRA_CONTACT_MUTED = "CONTACT_MUTED";
	public static final String EXTRA_CHANNEL_NO_DISCONNECT = "CHANNEL_NO_DISCONNECT";
	public static final String EXTRA_CHANNEL_SUBCHANNEL = "CHANNEL_SUNCHANNEL";
	public static final String EXTRA_CHANNEL_USER_NAME = "CHANNEL_USER_NAME";
	public static final String EXTRA_CHANNEL_USER_FULL_NAME = "CHANNEL_USER_FULL_NAME";
	public static final String EXTRA_CHANNEL_USER_DISPLAY_NAME = "CHANNEL_USER_DISPLAY_NAME";
	public static final String EXTRA_CHANNEL_USER_ROLES = "CHANNEL_USER_ROLES";
	public static final String EXTRA_CHANNEL_AUTHOR_NAME = "CHANNEL_AUTHOR_NAME";
	public static final String EXTRA_CHANNEL_AUTHOR_FULL_NAME = "CHANNEL_AUTHOR_FULL_NAME";
	public static final String EXTRA_CHANNEL_AUTHOR_DISPLAY_NAME = "CHANNEL_AUTHOR_DISPLAY_NAME";
	public static final String EXTRA_CHANNEL_AUTHOR_STATUS = "CHANNEL_AUTHOR_STATUS";
	public static final String EXTRA_CHANNEL_AUTHOR_STATUS_MESSAGE = "CHANNEL_AUTHOR_STATUS_MESSAGE";
	public static final String EXTRA_CHANNEL_CROSSLINK = "CHANNEL_CROSSLINK";
	public static final String EXTRA_CHANNEL_CROSSLINK_DISPLAY_NAME = "CHANNEL_CROSSLINK_DISPLAY_NAME";
	public static final String EXTRA_CHANNEL_CROSSLINK_AUTHOR_NAME = "CHANNEL_CROSSLINK_AUTHOR_NAME";
	public static final String EXTRA_MESSAGE_OUT = "MESSAGE_OUT";
	public static final String EXTRA_MESSAGE_IN = "MESSAGE_IN";
	public static final String EXTRA_MESSAGE_CONNECTING = "MESSAGE_CONNECTING";
	public static final String EXTRA_NETWORK_URL = "N";
	public static final String EXTRA_USERNAME = "U";
	public static final String EXTRA_PASSWORD = "P";
	public static final String EXTRA_PERISHABLE = "TMP";
	public static final String EXTRA_APPLICATION = "APP";
	public static final String EXTRA_PACKAGE = "PACKAGE";
	public static final String EXTRA_EID = "EID";
	public static final String EXTRA_MODE = "MODE";
	public static final String EXTRA_CHANGING = "CHANGING";
	public static final String EXTRA_BT = "BT";
	public static final String EXTRA_EP = "EP";
	public static final String EXTRA_SP = "SP";
	public static final String EXTRA_WA = "WA"; // When used as a value of EXTRA_MODE, should include an index of the wearable, i.e. WA0, WA2 etc.
	public static final String EXTRA_REQUEST_VITAL_PERMISSIONS = "REQUEST_VITAL_PERMISSIONS";
	public static final String EXTRA_LATEST_PERMISSION_ERROR = "LATEST_PERMISSION_ERROR";
	public static final String EXTRA_PERMISSION_DIALOG = "PERMISSION_DIALOG";
	public static final String EXTRA_PERMISSION_MICROPHONE = "PERMISSION_MICROPHONE";
	public static final String EXTRA_LAST_MESSAGE_REPLAY_AVAILABLE = "STATE_LAST_MESSAGE_REPLAY_AVAILABLE";

	public static final String VALUE_BEGIN_MESSAGE = "BEGIN_MESSAGE";
	public static final String VALUE_END_MESSAGE = "END_MESSAGE";
	public static final String VALUE_REPLAY_MESSAGE = "REPLAY_MESSAGE";
	public static final String VALUE_CONNECT = "CONNECT";
	public static final String VALUE_DISCONNECT = "DISCONNECT";
	public static final String VALUE_MUTE = "MUTE";
	public static final String VALUE_UNMUTE = "UNMUTE";
	public static final String VALUE_SET_STATUS = "SET_STATUS";
	public static final String VALUE_STAY_AWAKE = "STAY_AWAKE";
	public static final String VALUE_SELECT_CONTACT = "SELECT_CONTACT";
	public static final String VALUE_SIGN_IN = "SIGN_IN";
	public static final String VALUE_SIGN_OUT = "SIGN_OUT";
	public static final String VALUE_CANCEL = "CANCEL";
	public static final String VALUE_SET_AUTO_RUN = "SET_AUTO_RUN";
	public static final String VALUE_SET_AUTO_CHANNELS = "SET_AUTO_CHANNELS";
	public static final String VALUE_SET_EID = "SET_EID";
	public static final String VALUE_LOCK = "LOCK";
	public static final String VALUE_LIGHT = "LIGHT";
	public static final String VALUE_USERS = "USERS";
	public static final String VALUE_CHANNELS = "CHANNELS";
	public static final String VALUE_RECENTS = "RECENTS";
	public static final String VALUE_SET_AUDIO = "SET_AUDIO";

	public static final String ACTION_COMMAND = "COMMAND";
	public static final String ACTION_APP_STATE = "APP_STATE";
	public static final String ACTION_MESSAGE_STATE = "MESSAGE_STATE";
	public static final String ACTION_CONTACT_SELECTED = "CONTACT_SELECTED";
	public static final String ACTION_AUDIO_STATE = "AUDIO_STATE";
	public static final String ACTION_PERMISSION_ERRORS = "PERMISSION_ERRORS";

}
