package com.zello.sdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.security.MessageDigest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * The Sdk class acts as the implementation of the Zello SDK methods.
 * To use, instantiate an instance of the Sdk class.
 */
@SuppressWarnings("WeakerAccess")
class Sdk implements SafeHandlerEvents, ServiceConnection {

	//region Private variables

	// Package name of the preferred Zello app, null for auto-select
	private @Nullable String _preferredPackage;
	// Package name of the app that we are currently connected to, null when not connected
	private @Nullable String _connectedPackage;

	private @Nullable Context _context;
	private @Nullable SafeHandler<Sdk> _handler;
	private boolean _resumed;
	private final @NonNull String _activeTabAction = "com.zello.sdk." + Util.generateUuid();
	private final @NonNull Contact _selectedContact = new Contact();
	private final @NonNull MessageIn _messageIn = new MessageIn();
	private final @NonNull MessageOut _messageOut = new MessageOut();
	private @Nullable Contacts _contacts;
	private @Nullable Audio _audio;
	private final @NonNull AppState _appState = new AppState();
	private boolean _serviceBound; // Service is bound
	private @Nullable Intent _serviceIntent; // Service connect/disconnect intent
	private boolean _serviceConnecting; // Service is bound but is still connecting
	private @Nullable String _delayedNetwork, _delayedUsername, _delayedPassword;
	private boolean _delayedPerishable;
	private @Nullable Boolean _delayedShowBtAcceccoriesNotifications;
	private boolean _lastMessageReplayAvailable;
	private @Nullable BroadcastReceiver _receiverPackage; // Broadcast receiver for package install broadcasts
	private @Nullable BroadcastReceiver _receiverAppState; // Broadcast receiver for app state broadcasts
	private @Nullable BroadcastReceiver _receiverMessageState; // Broadcast receiver for message state broadcasts
	private @Nullable BroadcastReceiver _receiverContactSelected; // Broadcast receiver for selected contact broadcasts
	private @Nullable BroadcastReceiver _receiverActiveTab; // Broadcast receiver for last selected contact list tab
	private @Nullable BroadcastReceiver _receiverPermissionErrors; // Broadcast receiver for permissions errors
	private @Nullable BroadcastReceiver _receiverBtAccessoryState; // Broadcast receiver for bluetooth accessory state broadcasts

	private static final int AWAKE_TIMER = 1;

	private static final String _pttActivityClass = "com.zello.sdk.Activity";
	private static final String _pttPermissionsActivityClass = "com.zello.sdk.PermissionsActivity";
	private static final String _pttPttButtonsActivityClass = "com.zello.sdk.PttButtonsActivity";

	//endregion

	//region Initializer

	Sdk() {
	}

	//endregion

	//region Lifecycle methods

	@SuppressLint("InlinedApi")
	void onCreate(@Nullable String packageName, @Nullable Context context) {
		if (context == null) {
			return;
		}
		_context = context.getApplicationContext();
		_preferredPackage = Util.nullIfEmpty(Util.toLowerCaseLexicographically(packageName));
		_handler = new SafeHandler<>(this);
		_appState._available = isAppAvailable();
		// Spin up the main app
		connect();
		registerReceivers();
	}

	void onDestroy() {
		if (_context == null) {
			return;
		}
		disconnect();
		_resumed = false;
		unregisterReceivers();
		Contacts contacts = _contacts;
		if (contacts != null) {
			contacts.close();
		}
		Audio audio = _audio;
		if (audio != null) {
			audio.close();
		}
		stopAwakeTimer();
		_handler = null;
		if (!_serviceConnecting) {
			_context = null;
		}
		_preferredPackage = null;
		_contacts = null;
		_audio = null;
	}

	void onResume() {
		if (_resumed) {
			return;
		}
		_resumed = true;
		sendStayAwake();
		startAwakeTimer();
	}

	void onPause() {
		_resumed = false;
		stopAwakeTimer();
	}

	//endregion

	//region Zello SDK Methods

	//region Permissions

	void requestVitalPermissions() {
		Context context = _context;
		String connectedPackage = _connectedPackage;
		if (context == null || connectedPackage == null) {
			return;
		}
		try {
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setComponent(new ComponentName(connectedPackage, _pttPermissionsActivityClass));
			intent.putExtra(Constants.EXTRA_REQUEST_VITAL_PERMISSIONS, true);
			context.startActivity(intent);
		} catch (Throwable ignored) {
		}
	}

	void requestVitalPermissions(@Nullable Activity activity) {
		String connectedPackage = _connectedPackage;
		if (activity == null || connectedPackage == null) {
			return;
		}
		try {
			Intent intent = new Intent();
			intent.setComponent(new ComponentName(connectedPackage, _pttPermissionsActivityClass));
			intent.putExtra(Constants.EXTRA_REQUEST_VITAL_PERMISSIONS, true);
			activity.startActivity(intent);
		} catch (Throwable ignored) {
		}
	}

	void showMicrophonePermissionDialog() {
		Context context = _context;
		String connectedPackage = _connectedPackage;
		if (context == null || connectedPackage == null) {
			return;
		}
		try {
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setComponent(new ComponentName(connectedPackage, _pttPermissionsActivityClass));
			intent.putExtra(Constants.EXTRA_PERMISSION_DIALOG, true);
			intent.putExtra(Constants.EXTRA_PERMISSION_MICROPHONE, true);
			context.startActivity(intent);
		} catch (Throwable ignored) {
		}
	}

	void showMicrophonePermissionDialog(@Nullable Activity activity) {
		String connectedPackage = _connectedPackage;
		if (activity == null || connectedPackage == null) {
			return;
		}
		try {
			Intent intent = new Intent();
			intent.setComponent(new ComponentName(connectedPackage, _pttPermissionsActivityClass));
			intent.putExtra(Constants.EXTRA_PERMISSION_DIALOG, true);
			intent.putExtra(Constants.EXTRA_PERMISSION_MICROPHONE, true);
			activity.startActivity(intent);
		} catch (Throwable ignored) {
		}
	}

	//endregion

	//region Contact selection

	void selectContact(@Nullable String title, @Nullable Tab[] tabs, @Nullable Tab activeTab, @Nullable Theme theme) {
		Context context = _context;
		String connectedPackage = _connectedPackage;
		if (context == null || connectedPackage == null) {
			return;
		}
		String tabList = tabsToString(tabs);
		if (tabList == null) {
			return;
		}
		try {
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setComponent(new ComponentName(connectedPackage, _pttActivityClass));
			intent.setAction(Intent.ACTION_PICK);
			intent.putExtra(Intent.EXTRA_TITLE, title); // Activity title; optional
			intent.putExtra(Constants.EXTRA_TABS, tabList); // Set of displayed tabs; required; any combination of RECENTS, USERS and CHANNELS
			intent.putExtra(Constants.EXTRA_TAB, tabToString(activeTab)); // Initially active tab; optional; can be RECENTS, USERS or CHANNELS
			intent.putExtra(Constants.EXTRA_CALLBACK, _activeTabAction); // Last selected tab callback action; optional
			if (theme == Theme.LIGHT) {
				intent.putExtra(Constants.EXTRA_THEME, Constants.VALUE_LIGHT);
			}
			context.getApplicationContext().startActivity(intent);
		} catch (Throwable ignored) {
		}
	}

	void selectContact(@Nullable String title, @Nullable Tab[] tabs, @Nullable Tab activeTab, @Nullable Theme theme, @Nullable Activity activity) {
		String connectedPackage = _connectedPackage;
		if (activity == null || connectedPackage == null) {
			return;
		}
		String tabList = tabsToString(tabs);
		if (tabList == null) {
			return;
		}
		try {
			Intent intent = new Intent();
			intent.setComponent(new ComponentName(connectedPackage, _pttActivityClass));
			intent.setAction(Intent.ACTION_PICK);
			intent.putExtra(Intent.EXTRA_TITLE, title); // Activity title; optional
			intent.putExtra(Constants.EXTRA_TABS, tabList); // Set of displayed tabs; required; any combination of RECENTS, USERS and CHANNELS
			intent.putExtra(Constants.EXTRA_TAB, tabToString(activeTab)); // Initially active tab; optional; can be RECENTS, USERS or CHANNELS
			intent.putExtra(Constants.EXTRA_CALLBACK, _activeTabAction); // Last selected tab callback action; optional
			if (theme == Theme.LIGHT) {
				intent.putExtra(Constants.EXTRA_THEME, Constants.VALUE_LIGHT);
			}
			activity.startActivity(intent);
		} catch (Throwable ignored) {
		}
	}

	//endregion

	//region Sending messages

	void beginMessage() {
		Context context = _context;
		String connectedPackage = _connectedPackage;
		if (context == null || connectedPackage == null) {
			return;
		}
		Intent intent = new Intent(connectedPackage + "." + Constants.ACTION_COMMAND);
		intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_BEGIN_MESSAGE);
		context.sendBroadcast(intent);
	}

	void endMessage() {
		Context context = _context;
		String connectedPackage = _connectedPackage;
		if (context == null || connectedPackage == null) {
			return;
		}
		Intent intent = new Intent(connectedPackage + "." + Constants.ACTION_COMMAND);
		intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_END_MESSAGE);
		context.sendBroadcast(intent);
	}

	//endregion

	//region Replaying messages

	void replayLastIncomingMessage() {
		Context context = _context;
		String connectedPackage = _connectedPackage;
		if (context == null || connectedPackage == null) {
			return;
		}
		Intent intent = new Intent(connectedPackage + "." + Constants.ACTION_COMMAND);
		intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_REPLAY_MESSAGE);
		context.sendBroadcast(intent);
	}

	public boolean isLastMessageReplayAvailable() {
		return _lastMessageReplayAvailable;
	}

	//endregion

	//region Channels

	void connectChannel(@Nullable String channel) {
		Context context = _context;
		String connectedPackage = _connectedPackage;
		if (context == null || connectedPackage == null) {
			return;
		}
		if (channel == null || channel.isEmpty()) {
			return;
		}
		Intent intent = new Intent(connectedPackage + "." + Constants.ACTION_COMMAND);
		intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_CONNECT);
		intent.putExtra(Constants.EXTRA_CONTACT_NAME, channel);
		context.sendBroadcast(intent);
	}

	void disconnectChannel(@Nullable String channel) {
		Context context = _context;
		String connectedPackage = _connectedPackage;
		if (context == null || connectedPackage == null) {
			return;
		}
		if (channel == null || channel.isEmpty()) {
			return;
		}
		Intent intent = new Intent(connectedPackage + "." + Constants.ACTION_COMMAND);
		intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_DISCONNECT);
		intent.putExtra(Constants.EXTRA_CONTACT_NAME, channel);
		context.sendBroadcast(intent);
	}

	//endregion

	//region Contacts

	void muteContact(@Nullable Contact contact, boolean mute) {
		Context context = _context;
		String connectedPackage = _connectedPackage;
		if (context == null || connectedPackage == null) {
			return;
		}
		if (contact == null) {
			return;
		}
		ContactType type = contact.getType();
		Intent intent = new Intent(connectedPackage + "." + Constants.ACTION_COMMAND);
		intent.putExtra(Constants.EXTRA_COMMAND, mute ? Constants.VALUE_MUTE : Constants.VALUE_UNMUTE);
		intent.putExtra(Constants.EXTRA_CONTACT_NAME, contact.getName());
		intent.putExtra(Constants.EXTRA_CONTACT_TYPE, type == ContactType.CHANNEL || type == ContactType.GROUP || type == ContactType.CONVERSATION ? 1 : 0);
		context.sendBroadcast(intent);
	}

	//endregion

	//region Authentication

	boolean signIn(@Nullable String network, @Nullable String username, @Nullable String password) {
		return signIn(network, username, password, false);
	}

	boolean signIn(@Nullable String network, @Nullable String username, @Nullable String password, boolean perishable) {
		if (network == null || network.isEmpty() || username == null || username.isEmpty() || password == null || password.isEmpty()) {
			return false;
		}
		if (isConnected()) {
			Context context = _context;
			String connectedPackage = _connectedPackage;
			if (context == null || connectedPackage == null) {
				return false;
			}
			Intent intent = new Intent(connectedPackage + "." + Constants.ACTION_COMMAND);
			intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_SIGN_IN);
			intent.putExtra(Constants.EXTRA_NETWORK_URL, network);
			intent.putExtra(Constants.EXTRA_USERNAME, username);
			intent.putExtra(Constants.EXTRA_PASSWORD, md5(password));
			intent.putExtra(Constants.EXTRA_PERISHABLE, perishable);
			context.sendBroadcast(intent);
			context.startService(_serviceIntent);
		} else if (_serviceBound) {
			_delayedNetwork = network;
			_delayedUsername = username;
			_delayedPassword = password;
			_delayedPerishable = perishable;
		}
		return true;
	}

	void signOut() {
		_delayedNetwork = _delayedUsername = _delayedPassword = null;
		_delayedPerishable = false;
		if (!_serviceBound) {
			return;
		}
		Context context = _context;
		String connectedPackage = _connectedPackage;
		if (context == null || connectedPackage == null) {
			return;
		}
		Intent intent = new Intent(connectedPackage + "." + Constants.ACTION_COMMAND);
		intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_SIGN_OUT);
		context.sendBroadcast(intent);
	}

	void cancel() {
		_delayedNetwork = _delayedUsername = _delayedPassword = null;
		_delayedPerishable = false;
		if (!_serviceBound) {
			return;
		}
		Context context = _context;
		String connectedPackage = _connectedPackage;
		if (context == null || connectedPackage == null) {
			return;
		}
		Intent intent = new Intent(connectedPackage + "." + Constants.ACTION_COMMAND);
		intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_CANCEL);
		context.sendBroadcast(intent);
	}

	//endregion

	//region Locking

	void lock(@Nullable String applicationName, @Nullable String packageName) {
		if (applicationName == null || applicationName.isEmpty()) {
			return;
		}
		if (!isConnected()) {
			return;
		}
		Context context = _context;
		String connectedPackage = _connectedPackage;
		if (context == null || connectedPackage == null) {
			return;
		}
		Intent intent = new Intent(connectedPackage + "." + Constants.ACTION_COMMAND);
		intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_LOCK);
		intent.putExtra(Constants.EXTRA_APPLICATION, applicationName);
		intent.putExtra(Constants.EXTRA_PACKAGE, packageName);
		context.sendBroadcast(intent);
	}

	void unlock() {
		if (!isConnected()) {
			return;
		}
		Context context = _context;
		String connectedPackage = _connectedPackage;
		if (context == null || connectedPackage == null) {
			return;
		}
		Intent intent = new Intent(connectedPackage + "." + Constants.ACTION_COMMAND);
		intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_LOCK);
		context.sendBroadcast(intent);
	}

	//endregion

	//region Status

	void setStatus(@NonNull Status status) {
		if (!_serviceBound) {
			return;
		}
		Context context = _context;
		String connectedPackage = _connectedPackage;
		if (context == null || connectedPackage == null) {
			return;
		}
		Intent intent = new Intent(connectedPackage + "." + Constants.ACTION_COMMAND);
		intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_SET_STATUS);
		intent.putExtra(Constants.EXTRA_STATE_BUSY, status == Status.BUSY);
		intent.putExtra(Constants.EXTRA_STATE_SOLO, status == Status.SOLO);
		context.sendBroadcast(intent);
	}

	void setStatusMessage(@Nullable String message) {
		if (!_serviceBound) {
			return;
		}
		Context context = _context;
		String connectedPackage = _connectedPackage;
		if (context == null || connectedPackage == null) {
			return;
		}
		Intent intent = new Intent(connectedPackage + "." + Constants.ACTION_COMMAND);
		intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_SET_STATUS);
		intent.putExtra(Constants.EXTRA_STATE_STATUS_MESSAGE, Util.emptyIfNull(message));
		context.sendBroadcast(intent);
	}

	//endregion

	//region Opening PTT app

	void openMainScreen() {
		Context context = _context;
		String connectedPackage = _connectedPackage;
		if (context == null || connectedPackage == null) {
			return;
		}
		Intent LaunchIntent = context.getPackageManager().getLaunchIntentForPackage(connectedPackage);
		try {
			context.startActivity(LaunchIntent);
		} catch (Throwable ignored) {
		}
	}

	//endregion

	//region App settings

	void showPttButtonsScreen(@Nullable Activity activity) {
		Context context = activity != null ? activity : _context;
		String connectedPackage = _connectedPackage;
		if (context == null || connectedPackage == null) {
			return;
		}
		Intent intent = new Intent();
		if (activity == null) {
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		intent.setComponent(new ComponentName(connectedPackage, _pttPttButtonsActivityClass));
		try {
			context.startActivity(intent);
		} catch (Throwable ignored) {
		}
	}

	//endregion

	//region Getters

	void getMessageIn(@Nullable MessageIn message) {
		_messageIn.copyTo(message);
	}

	void getMessageOut(@Nullable MessageOut message) {
		_messageOut.copyTo(message);
	}

	void getAppState(@Nullable AppState state) {
		_appState.copyTo(state);
	}

	void getSelectedContact(@Nullable Contact contact) {
		_selectedContact.copyTo(contact);
	}

	@Nullable Contacts getContacts() {
		return _contacts;
	}

	@Nullable Audio getAudio() {
		Context context = _context;
		String connectedPackage = _connectedPackage;
		if (context == null || connectedPackage == null) {
			return _audio;
		}
		if (_audio == null) {
			_audio = new Audio(connectedPackage, _context);
		}
		return _audio;
	}

	//endregion

	//region Setters

	void setAutoRun(boolean enable) {
		Context context = _context;
		String connectedPackage = _connectedPackage;
		if (context == null || connectedPackage == null) {
			return;
		}
		if (!isConnected()) {
			return;
		}
		Intent intent = new Intent(connectedPackage + "." + Constants.ACTION_COMMAND);
		intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_SET_AUTO_RUN);
		intent.putExtra(Constants.EXTRA_STATE_AUTO_RUN, enable);
		context.sendBroadcast(intent);
	}

	void setAutoConnectChannels(boolean connect) {
		Context context = _context;
		String connectedPackage = _connectedPackage;
		if (context == null || connectedPackage == null) {
			return;
		}
		if (!isConnected()) {
			return;
		}
		Intent intent = new Intent(connectedPackage + "." + Constants.ACTION_COMMAND);
		intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_SET_AUTO_CHANNELS);
		intent.putExtra(Constants.EXTRA_STATE_AUTO_CHANNELS, connect);
		context.sendBroadcast(intent);
	}

	void setExternalId(@Nullable String id) {
		Context context = _context;
		String connectedPackage = _connectedPackage;
		if (context == null || connectedPackage == null) {
			return;
		}
		if (!isConnected()) {
			return;
		}
		Intent intent = new Intent(connectedPackage + "." + Constants.ACTION_COMMAND);
		intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_SET_EID);
		intent.putExtra(Constants.EXTRA_EID, id == null ? "" : id);
		context.sendBroadcast(intent);
	}

	void setSelectedContact(@Nullable Contact contact) {
		if (contact != null) {
			ContactType type = contact.getType();
			selectContact(type == ContactType.CHANNEL || type == ContactType.GROUP || type == ContactType.CONVERSATION ? 1 : 0, contact.getName());
		} else {
			selectContact(0, null);
		}
	}

	void setSelectedUserOrGateway(@Nullable String name) {
		selectContact(0, name);
	}

	void setSelectedChannelOrGroup(@Nullable String name) {
		selectContact(1, name);
	}

	void setShowBluetoothAccessoriesNotifications(boolean show) {
		if (!isConnected()) {
			_delayedShowBtAcceccoriesNotifications = show;
			return;
		}
		Context context = _context;
		String connectedPackage = _connectedPackage;
		if (context == null || connectedPackage == null) {
			return;
		}
		Intent intent = new Intent(connectedPackage + "." + Constants.ACTION_COMMAND);
		intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_SET_SHOW_BT_ACCESSORIES_NOTIFICATIONS);
		intent.putExtra(Constants.EXTRA_VALUE, show);
		context.sendBroadcast(intent);
	}

	//endregion

	//endregion

	//region Overridden methods

	//region SafeHandlerEvents

	@Override
	public void handleMessageFromSafeHandler(@NonNull Message message) {
		if (message.what != AWAKE_TIMER) {
			return;
		}
		if (_resumed) {
			sendStayAwake();
			Handler h = _handler;
			if (h != null) {
				h.sendMessageDelayed(h.obtainMessage(AWAKE_TIMER), Constants.STAY_AWAKE_TIMEOUT);
			}
		}
	}

	//endregion

	//region ServiceConnection

	@Override
	public void onServiceConnected(@Nullable ComponentName name, @Nullable IBinder service) {
		if (!_serviceConnecting) {
			return;
		}
		Context context = _context;
		if (context == null) {
			return;
		}
		_serviceConnecting = false;
		context.startService(_serviceIntent);
		if (_delayedShowBtAcceccoriesNotifications != null) {
			setShowBluetoothAccessoriesNotifications(_delayedShowBtAcceccoriesNotifications);
		}
		if (_delayedNetwork != null) {
			signIn(_delayedNetwork, _delayedUsername, _delayedPassword, _delayedPerishable);
		}
		_delayedNetwork = _delayedUsername = _delayedPassword = null;
		_delayedPerishable = false;
		_delayedShowBtAcceccoriesNotifications = null;
		// If service is not bound, the component was destroyed and the service needs to be disconnected
		if (!_serviceBound) {
			Log.i("zello sdk", "disconnecting because sdk was destroyed");
			try {
				context.unbindService(this);
			} catch (Throwable ignored) {
			}
			_context = null;
			_appState._error = false;
		}
		_appState._initializing = false;
		fireAppStateChanged();
	}

	@Override
	public void onServiceDisconnected(@Nullable ComponentName name) {
		_serviceBound = false;
		if (_serviceConnecting) {
			_serviceConnecting = false;
			_appState._initializing = false;
			_appState._error = false;
			fireAppStateChanged();
		}
	}

	//endregion

	//endregion

	//region Private methods

	private void registerReceivers() {
		if (_context == null) {
			return;
		}
		// Register to receive package install broadcasts
		_receiverPackage = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				updateAppAvailable();
				if (intent == null || _handler == null) {
					return;
				}
				String action = intent.getAction();
				if (action == null) {
					return;
				}
				if (action.equals(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE) || action.equals(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE)) {
					String[] pkgs = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
					if (pkgs == null) {
						return;
					}
					for (String pkg : pkgs) {
						// Reconnect any time when currently connected package is affected or
						// when the best package is automatically selected and a package with a higher preference is affected
						if (pkg.equalsIgnoreCase(_connectedPackage) || checkPreferredAppChanged()) {
							reconnect();
							updateSelectedContact(null);
							updateContacts();
							return;
						}
					}
				} else {
					Uri data = intent.getData();
					if (data == null) {
						return;
					}
					String pkg = data.getSchemeSpecificPart();
					if (pkg == null || !pkg.equalsIgnoreCase(_connectedPackage)) {
						return;
					}
					reconnect();
					updateSelectedContact(null);
					updateContacts();
				}
			}
		};
		IntentFilter filterPackage = new IntentFilter();
		filterPackage.addAction(Intent.ACTION_PACKAGE_ADDED);
		filterPackage.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filterPackage.addAction(Intent.ACTION_PACKAGE_REPLACED);
		filterPackage.addAction(Intent.ACTION_PACKAGE_CHANGED);
		filterPackage.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED);
		filterPackage.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
		filterPackage.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
		filterPackage.addDataScheme("package");
		_context.registerReceiver(_receiverPackage, filterPackage);
		// Register to receive app state broadcasts
		_receiverAppState = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				updateAppState(intent);
			}
		};
		Intent intentStickyAppState = _context.registerReceiver(_receiverAppState, new IntentFilter(_connectedPackage + "." + Constants.ACTION_APP_STATE));
		updateAppState(intentStickyAppState);
		updateContacts();
		// Register to receive app permissions broadcasts
		_receiverPermissionErrors = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				handlePermissionError(intent);
			}
		};
		_context.registerReceiver(_receiverPermissionErrors, new IntentFilter(_connectedPackage + "." + Constants.ACTION_PERMISSION_ERRORS));
		// Register to receive message state broadcasts
		_receiverMessageState = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				updateMessageState(intent);
			}
		};
		Intent intentStickyMessageState = _context.registerReceiver(_receiverMessageState, new IntentFilter(_connectedPackage + "." + Constants.ACTION_MESSAGE_STATE));
		updateMessageState(intentStickyMessageState);
		// Register to receive selected contact broadcasts
		_receiverContactSelected = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				updateSelectedContact(intent);
			}
		};
		Intent intentStickySelectedContact = _context.registerReceiver(_receiverContactSelected, new IntentFilter(_connectedPackage + "." + Constants.ACTION_CONTACT_SELECTED));
		updateSelectedContact(intentStickySelectedContact);
		// Register to receive last selected contact list tab
		_receiverActiveTab = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				updateSelectedTab(intent);
			}
		};
		_context.registerReceiver(_receiverActiveTab, new IntentFilter(_activeTabAction));
		// Register to receive bluetooth accessory state broadcasts
		_receiverBtAccessoryState = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				handleBtAccessoryState(intent);
			}
		};
		_context.registerReceiver(_receiverBtAccessoryState, new IntentFilter(_connectedPackage + "." + Constants.ACTION_BT_ACCESSORY_STATE));
	}

	private void unregisterReceivers() {
		if (_context == null) {
			return;
		}
		if (_receiverPackage != null) {
			_context.unregisterReceiver(_receiverPackage);
		}
		if (_receiverAppState != null) {
			_context.unregisterReceiver(_receiverAppState);
		}
		if (_receiverPermissionErrors != null) {
			_context.unregisterReceiver(_receiverPermissionErrors);
		}
		if (_receiverMessageState != null) {
			_context.unregisterReceiver(_receiverMessageState);
		}
		if (_receiverContactSelected != null) {
			_context.unregisterReceiver(_receiverContactSelected);
		}
		if (_receiverActiveTab != null) {
			_context.unregisterReceiver(_receiverActiveTab);
		}
		if (_receiverBtAccessoryState != null) {
			_context.unregisterReceiver(_receiverBtAccessoryState);
		}
		_receiverPackage = null;
		_receiverAppState = null;
		_receiverPermissionErrors = null;
		_receiverMessageState = null;
		_receiverContactSelected = null;
		_receiverActiveTab = null;
		_receiverBtAccessoryState = null;
	}

	/**
	 * Check if a preferred package has changed.
	 * Happens when a package of interest or of potential interest is installed or removed.
	 */
	private boolean checkPreferredAppChanged() {
		if (_preferredPackage != null) {
			// Don't care - package auto-select is not enabled
			return false;
		}
		// Find preferred package
		PackageInfo preferredApp = Util.findPackageInfo(_context, null);
		// Check if currently connected package is different from the auto-preferred package
		return !Util.samePackageNames(_connectedPackage, preferredApp != null ? preferredApp.packageName : null);
	}

	private void selectContact(int type, @Nullable String name) {
		Context context = _context;
		if (context == null) {
			return;
		}
		String connectedPackage = _connectedPackage;
		if (connectedPackage == null) {
			return;
		}
		Intent intent = new Intent(_connectedPackage + "." + Constants.ACTION_COMMAND);
		intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_SELECT_CONTACT);
		if (name != null && name.length() > 0) {
			intent.putExtra(Constants.EXTRA_CONTACT_NAME, name);
			intent.putExtra(Constants.EXTRA_CONTACT_TYPE, type);
		}
		context.sendBroadcast(intent);
	}

	private void sendStayAwake() {
		if (!isConnected()) {
			return;
		}
		Context context = _context;
		if (context == null) {
			return;
		}
		String connectedPackage = _connectedPackage;
		if (connectedPackage == null) {
			return;
		}
		Intent intent = new Intent(_connectedPackage + "." + Constants.ACTION_COMMAND);
		intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_STAY_AWAKE);
		context.sendBroadcast(intent);
	}

	private void connect() {
		if (_serviceBound && _serviceConnecting) {
			return;
		}
		Context context = _context;
		if (context == null) {
			return;
		}
		PackageInfo packageInfo = Util.findPackageInfo(_context, _preferredPackage);
		if (packageInfo == null) {
			// A compatible package wasn't found
			_appState._initializing = false;
			_appState._error = true;
			fireAppStateChanged();
			return;
		}

		_serviceConnecting = true;
		_appState._initializing = true;
		_appState._error = false;
		_connectedPackage = packageInfo.packageName;
		fireAppStateChanged();

		_serviceIntent = new Intent();
		_serviceIntent.setClassName(packageInfo.packageName, packageInfo.serviceClassName);

		try {
			_serviceBound = context.bindService(_serviceIntent, this, Context.BIND_AUTO_CREATE);
		} catch (Throwable t) {
			_serviceConnecting = false;
			Log.i("zello sdk", "Error in Sdk.connect: " + t.toString());
		}

		if (!_serviceBound) {
			_appState._error = true;
			_connectedPackage = null;
			try {
				context.unbindService(this);
			} catch (Throwable ignored) {
			}
		}
		if (_serviceConnecting) {
			_appState._initializing = false;
			fireAppStateChanged();
		}
	}

	private void disconnect() {
		unregisterReceivers();
		_delayedNetwork = _delayedUsername = _delayedPassword = null;
		_delayedPerishable = false;
		if (!_serviceBound) {
			return;
		}
		_serviceBound = false;
		if (!_serviceConnecting) {
			Context context = _context;
			if (context != null) {
				try {
					context.unbindService(this);
				} catch (Throwable ignored) {
				}
			}
		} else {
			Log.i("zello sdk", "Early Sdk.disconnect");
		}
	}

	private void reconnect() {
		disconnect();
		connect();
		Contacts contacts = _contacts;
		if (contacts != null) {
			contacts.invalidate();
		}
	}

	private void startAwakeTimer() {
		if (_resumed) {
			Handler h = _handler;
			if (h != null) {
				h.sendMessageDelayed(h.obtainMessage(AWAKE_TIMER), Constants.STAY_AWAKE_TIMEOUT);
			}
		}
	}

	private void stopAwakeTimer() {
		Handler h = _handler;
		if (h != null) {
			h.removeMessages(AWAKE_TIMER);
		}
	}

	private void updateAppAvailable() {
		boolean available = isAppAvailable();
		if (available != _appState._available) {
			_appState._available = available;
			fireAppStateChanged();
		}
	}

	private void updateAppState(@Nullable Intent intent) {
		_appState.reset();
		if (intent != null) {
			updateLastMessageReplayAvailable(intent);

			_appState._customBuild = intent.getBooleanExtra(Constants.EXTRA_STATE_CUSTOM_BUILD, false);
			_appState._configuring = intent.getBooleanExtra(Constants.EXTRA_STATE_CONFIGURING, false);
			_appState._locked = intent.getBooleanExtra(Constants.EXTRA_STATE_LOCKED, false);
			_appState._signedIn = intent.getBooleanExtra(Constants.EXTRA_STATE_SIGNED_IN, false);
			_appState._signingIn = intent.getBooleanExtra(Constants.EXTRA_STATE_SIGNING_IN, false);
			_appState._signingOut = intent.getBooleanExtra(Constants.EXTRA_STATE_SIGNING_OUT, false);
			_appState._cancelling = intent.getBooleanExtra(Constants.EXTRA_STATE_CANCELLING_SIGNIN, false);
			_appState._reconnectTimer = intent.getIntExtra(Constants.EXTRA_STATE_RECONNECT_TIMER, -1);
			_appState._waitingForNetwork = intent.getBooleanExtra(Constants.EXTRA_STATE_WAITING_FOR_NETWORK, false);
			_appState._showContacts = intent.getBooleanExtra(Constants.EXTRA_STATE_SHOW_CONTACTS, false);
			_appState._busy = intent.getBooleanExtra(Constants.EXTRA_STATE_BUSY, false);
			_appState._solo = intent.getBooleanExtra(Constants.EXTRA_STATE_SOLO, false);
			_appState._autoRun = intent.getBooleanExtra(Constants.EXTRA_STATE_AUTO_RUN, false);
			_appState._autoChannels = intent.getBooleanExtra(Constants.EXTRA_STATE_AUTO_CHANNELS, true);
			_appState._statusMessage = intent.getStringExtra(Constants.EXTRA_STATE_STATUS_MESSAGE);
			_appState._network = intent.getStringExtra(Constants.EXTRA_STATE_NETWORK);
			_appState._networkUrl = intent.getStringExtra(Constants.EXTRA_STATE_NETWORK_URL);
			_appState._username = intent.getStringExtra(Constants.EXTRA_STATE_USERNAME);
			_appState._lastError = intToError(intent.getIntExtra(Constants.EXTRA_STATE_LAST_ERROR, Error.NONE.ordinal()));
			_appState._externalId = intent.getStringExtra(Constants.EXTRA_EID);
		}
		fireAppStateChanged();
	}

	private void updateMessageState(@Nullable Intent intent) {
		boolean out = false;
		boolean in = false;
		if (intent != null) {
			updateLastMessageReplayAvailable(intent);

			out = intent.getBooleanExtra(Constants.EXTRA_MESSAGE_OUT, false);
			in = !out && intent.getBooleanExtra(Constants.EXTRA_MESSAGE_IN, false);
			if (out) {
				_messageOut._to._name = intent.getStringExtra(Constants.EXTRA_CONTACT_NAME);
				_messageOut._to._fullName = intent.getStringExtra(Constants.EXTRA_CONTACT_FULL_NAME);
				_messageOut._to._displayName = intent.getStringExtra(Constants.EXTRA_CONTACT_DISPLAY_NAME);
				_messageOut._to._type = intToContactType(intent.getIntExtra(Constants.EXTRA_CONTACT_TYPE, -1));
				_messageOut._to._status = intToContactStatus(intent.getIntExtra(Constants.EXTRA_CONTACT_STATUS, 0));
				_messageOut._to._statusMessage = intent.getStringExtra(Constants.EXTRA_CONTACT_STATUS_MESSAGE);
				_messageOut._to._usersCount = intent.getIntExtra(Constants.EXTRA_CHANNEL_USERS_COUNT, 0);
				_messageOut._to._usersTotal = intent.getIntExtra(Constants.EXTRA_CHANNEL_USERS_TOTAL, 0);
				_messageOut._active = true;
				_messageOut._connecting = intent.getBooleanExtra(Constants.EXTRA_MESSAGE_CONNECTING, false);
			}
			if (in) {
				_messageIn._from._name = intent.getStringExtra(Constants.EXTRA_CONTACT_NAME);
				_messageIn._from._fullName = intent.getStringExtra(Constants.EXTRA_CONTACT_FULL_NAME);
				_messageIn._from._displayName = intent.getStringExtra(Constants.EXTRA_CONTACT_DISPLAY_NAME);
				_messageIn._from._type = intToContactType(intent.getIntExtra(Constants.EXTRA_CONTACT_TYPE, -1));
				_messageIn._from._status = intToContactStatus(intent.getIntExtra(Constants.EXTRA_CONTACT_STATUS, 0));
				_messageIn._from._statusMessage = intent.getStringExtra(Constants.EXTRA_CONTACT_STATUS_MESSAGE);
				_messageIn._from._usersCount = intent.getIntExtra(Constants.EXTRA_CHANNEL_USERS_COUNT, 0);
				_messageIn._from._usersTotal = intent.getIntExtra(Constants.EXTRA_CHANNEL_USERS_TOTAL, 0);
				_messageIn._author._name = intent.getStringExtra(Constants.EXTRA_CHANNEL_AUTHOR_NAME);
				_messageIn._author._fullName = intent.getStringExtra(Constants.EXTRA_CHANNEL_AUTHOR_FULL_NAME);
				_messageIn._author._displayName = intent.getStringExtra(Constants.EXTRA_CHANNEL_AUTHOR_DISPLAY_NAME);
				_messageIn._author._status = intToContactStatus(intent.getIntExtra(Constants.EXTRA_CHANNEL_AUTHOR_STATUS, 0));
				_messageIn._author._statusMessage = intent.getStringExtra(Constants.EXTRA_CHANNEL_AUTHOR_STATUS_MESSAGE);
				_messageIn._active = true;
			}
		}
		if (!in) {
			_messageIn.reset();
		}
		if (!out) {
			_messageOut.reset();
		}

		for (Events event : Zello.getInstance().events) {
			event.onMessageStateChanged();
		}
	}

	private void updateLastMessageReplayAvailable(@Nullable Intent intent) {
		if (intent == null) {
			return;
		}
		_lastMessageReplayAvailable = intent.getBooleanExtra(Constants.EXTRA_LAST_MESSAGE_REPLAY_AVAILABLE, false);
	}

	private void updateContacts() {
		Contacts contacts = _contacts;
		_contacts = null;
		if (contacts != null) {
			contacts.close();
		}
		Context context = _context;
		if (context == null) {
			return;
		}
		String connectedPackage = _connectedPackage;
		if (connectedPackage == null) {
			return;
		}
		_contacts = new Contacts(connectedPackage, context, _handler);
	}

	private void updateSelectedContact(@Nullable Intent intent) {
		String name = intent != null ? intent.getStringExtra(Constants.EXTRA_CONTACT_NAME) : null; // Contact name
		boolean selected = name != null && name.length() > 0;
		if (selected) {
			// Update info
			_selectedContact._name = name;
			_selectedContact._fullName = intent.getStringExtra(Constants.EXTRA_CONTACT_FULL_NAME);
			_selectedContact._displayName = intent.getStringExtra(Constants.EXTRA_CONTACT_DISPLAY_NAME);
			_selectedContact._type = intToContactType(intent.getIntExtra(Constants.EXTRA_CONTACT_TYPE, -1));
			_selectedContact._status = intToContactStatus(intent.getIntExtra(Constants.EXTRA_CONTACT_STATUS, 0));
			_selectedContact._statusMessage = intent.getStringExtra(Constants.EXTRA_CONTACT_STATUS_MESSAGE);
			_selectedContact._usersCount = intent.getIntExtra(Constants.EXTRA_CHANNEL_USERS_COUNT, 0);
			_selectedContact._usersTotal = intent.getIntExtra(Constants.EXTRA_CHANNEL_USERS_TOTAL, 0);
			_selectedContact._title = intent.getStringExtra(Constants.EXTRA_CONTACT_TITLE);
			_selectedContact._muted = intent.getIntExtra(Constants.EXTRA_CONTACT_MUTED, 0) != 0;
			_selectedContact._noDisconnect = intent.getIntExtra(Constants.EXTRA_CHANNEL_NO_DISCONNECT,
					_selectedContact._type != ContactType.CHANNEL && _selectedContact._type != ContactType.GROUP && _selectedContact._type != ContactType.CONVERSATION ? 1 : 0) != 0;
		} else {
			_selectedContact.reset();
		}

		for (Events event : Zello.getInstance().events) {
			event.onSelectedContactChanged();
		}
	}

	private void updateSelectedTab(@Nullable Intent intent) {
		if (intent == null) {
			return;
		}
		Tab tab = stringToTab(intent.getStringExtra(Constants.EXTRA_TAB));
		for (Events event : Zello.getInstance().events) {
			event.onLastContactsTabChanged(tab);
		}
	}

	private void handlePermissionError(@Nullable Intent intent) {
		if (intent == null) {
			return;
		}
		PermissionError error = intToPermissionError(intent.getIntExtra(Constants.EXTRA_LATEST_PERMISSION_ERROR, PermissionError.NONE.ordinal()));
		if (error == PermissionError.MICROPHONE_NOT_GRANTED) {
			for (Events event : Zello.getInstance().events) {
				event.onMicrophonePermissionNotGranted();
			}
		}
	}

	private void handleBtAccessoryState(@Nullable Intent intent) {
		if (intent == null) {
			return;
		}
		BluetoothAccessoryType type = intToBtAccessoryType(intent.getIntExtra(Constants.EXTRA_TYPE, BluetoothAccessoryType.SPP.ordinal()));
		BluetoothAccessoryState state = intToBtAccessoryState(intent.getIntExtra(Constants.EXTRA_STATE, BluetoothAccessoryState.ERROR.ordinal()));
		String name = intent.getStringExtra(Constants.EXTRA_NAME);
		String description = intent.getStringExtra(Constants.EXTRA_DESCRIPTION);
		for (Events event : Zello.getInstance().events) {
			event.onBluetoothAccessoryStateChanged(type, state, name, description);
		}
	}

	private boolean isConnected() {
		return _serviceBound && !_serviceConnecting;
	}

	private boolean isAppAvailable() {
		Context context = _context;
		String connectedPackage = _connectedPackage;
		if (connectedPackage == null) {
			PackageInfo packageInfo = Util.findPackageInfo(context, _preferredPackage);
			connectedPackage = packageInfo != null ? packageInfo.packageName : null;
		}
		if (context == null || connectedPackage == null) {
			return false;
		}
		try {
			return null != context.getPackageManager().getLaunchIntentForPackage(connectedPackage);
		} catch (Throwable ignored) {
			// PackageManager.NameNotFoundException
		}
		return false;
	}

	private void fireAppStateChanged() {
		for (Events event : Zello.getInstance().events) {
			event.onAppStateChanged();
		}
	}

	//endregion

	//region Static Methods

	private static @NonNull Error intToError(int error) {
		if (error > Error.NONE.ordinal()) {
			if (error == Error.INVALID_CREDENTIALS.ordinal()) {
				return Error.INVALID_CREDENTIALS;
			} else if (error == Error.INVALID_NETWORK_NAME.ordinal()) {
				return Error.INVALID_NETWORK_NAME;
			} else if (error == Error.NETWORK_SUSPENDED.ordinal()) {
				return Error.NETWORK_SUSPENDED;
			} else if (error == Error.SERVER_SECURE_CONNECT_FAILED.ordinal()) {
				return Error.SERVER_SECURE_CONNECT_FAILED;
			} else if (error == Error.SERVER_SIGNIN_FAILED.ordinal()) {
				return Error.SERVER_SIGNIN_FAILED;
			} else if (error == Error.NETWORK_SIGNIN_FAILED.ordinal()) {
				return Error.NETWORK_SIGNIN_FAILED;
			} else if (error == Error.KICKED.ordinal()) {
				return Error.KICKED;
			} else if (error == Error.APP_UPDATE_REQUIRED.ordinal()) {
				return Error.APP_UPDATE_REQUIRED;
			} else if (error == Error.NO_INTERNET_CONNECTION.ordinal()) {
				return Error.NO_INTERNET_CONNECTION;
			} else if (error == Error.INTERNET_CONNECTION_RESTRICTED.ordinal()) {
				return Error.INTERNET_CONNECTION_RESTRICTED;
			} else if (error == Error.SERVER_LICENSE_PROBLEM.ordinal()) {
				return Error.SERVER_LICENSE_PROBLEM;
			} else if (error == Error.TOO_MANY_SIGNIN_ATTEMPTS.ordinal()) {
				return Error.TOO_MANY_SIGNIN_ATTEMPTS;
			} else if (error == Error.UNRELIABLE_CONNECTION.ordinal()) {
				return Error.UNRELIABLE_CONNECTION;
			} else if (error == Error.DEVICE_ID_MISMATCH.ordinal()) {
				return Error.DEVICE_ID_MISMATCH;
			} else {
				return Error.UNKNOWN;
			}
		} else {
			return Error.NONE;
		}
	}

	private static @NonNull PermissionError intToPermissionError(int error) {
		if (error > PermissionError.NONE.ordinal()) {
			if (error == PermissionError.MICROPHONE_NOT_GRANTED.ordinal()) {
				return PermissionError.MICROPHONE_NOT_GRANTED;
			} else {
				return PermissionError.UNKNOWN;
			}
		} else {
			return PermissionError.NONE;
		}
	}

	static @NonNull ContactType intToContactType(int type) {
		switch (type) {
			case 1:
				return ContactType.CHANNEL;
			case 3:
				return ContactType.GROUP;
			case 2:
				return ContactType.GATEWAY;
			case 4:
				return ContactType.CONVERSATION;
			default:
				return ContactType.USER;
		}
	}

	static @NonNull ContactStatus intToContactStatus(int status) {
		switch (status) {
			case 1:
				return ContactStatus.STANDBY;
			case 2:
			case 4:
			case 5:
				return ContactStatus.AVAILABLE;
			case 3:
				return ContactStatus.BUSY;
			case 6:
				return ContactStatus.CONNECTING;
			default:
				return ContactStatus.OFFLINE;
		}
	}

	private static @Nullable String tabToString(@Nullable Tab tab) {
		if (tab != null) {
			switch (tab) {
				case RECENTS:
					return Constants.VALUE_RECENTS;
				case USERS:
					return Constants.VALUE_USERS;
				case CHANNELS:
					return Constants.VALUE_CHANNELS;
			}
		}
		return null;
	}

	private static @Nullable String tabsToString(@Nullable Tab[] tabs) {
		if (tabs == null) {
			return null;
		}
		StringBuilder s = null;
		for (Tab tab : tabs) {
			String name = tabToString(tab);
			if (name == null) {
				continue;
			}
			if (s == null) {
				s = new StringBuilder(name);
			} else {
				s.append(",").append(name);
			}
		}
		return s != null ? s.toString() : null;
	}

	private static @NonNull Tab stringToTab(@Nullable String s) {
		if (Constants.VALUE_USERS.equals(s)) {
			return Tab.USERS;
		}
		if (Constants.VALUE_CHANNELS.equals(s)) {
			return Tab.CHANNELS;
		}
		return Tab.RECENTS;
	}

	private static @NonNull BluetoothAccessoryType intToBtAccessoryType(int type) {
		if (type == BluetoothAccessoryType.LE.ordinal()) {
			return BluetoothAccessoryType.LE;
		}
		return BluetoothAccessoryType.SPP;
	}

	private static @NonNull BluetoothAccessoryState intToBtAccessoryState(int state) {
		if (state == BluetoothAccessoryState.CONNECTED.ordinal()) {
			return BluetoothAccessoryState.CONNECTED;
		}
		if (state == BluetoothAccessoryState.DISCONNECTED.ordinal()) {
			return BluetoothAccessoryState.DISCONNECTED;
		}
		return BluetoothAccessoryState.ERROR;
	}

	private static @Nullable String bytesToHex(@Nullable byte[] data) {
		if (data == null) {
			return null;
		}
		StringBuilder buf = new StringBuilder();
		for (byte c : data) {
			int halfbyte = (c >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if (halfbyte <= 9) {
					buf.append((char) ('0' + halfbyte));
				} else {
					buf.append((char) ('a' + (halfbyte - 10)));
				}
				halfbyte = c & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	@SuppressWarnings("CharsetObjectCanBeUsed")
	private static @NonNull String md5(String s) {
		if (s == null || s.isEmpty()) {
			return "";
		}
		try {
			MessageDigest digester = MessageDigest.getInstance("MD5");
			byte[] bytes = s.getBytes("UTF-8");
			digester.update(bytes, 0, bytes.length);
			byte[] digest = digester.digest();
			String hex = bytesToHex(digest);
			if (hex != null) {
				return hex;
			}
		} catch (Throwable t) {
			Log.i("zello sdk", "Error in Sdk.md5: " + t.toString());
		}
		return "";
	}

	//endregion

}
