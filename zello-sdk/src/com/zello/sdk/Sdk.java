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

/**
 * The Sdk class acts as the implementation of the Zello SDK methods.
 * To use, instantiate an instance of the Sdk class.
 */
class Sdk implements SafeHandlerEvents, ServiceConnection {

	//region Private Variables

	private String _package = "";
	private Context _context;
	private SafeHandler<Sdk> _handler;
	private boolean _resumed;
	private String _activeTabAction = "com.zello.sdk." + Util.generateUuid();
	private Contact _selectedContact = new Contact();
	private MessageIn _messageIn = new MessageIn();
	private MessageOut _messageOut = new MessageOut();
	private Contacts _contacts;
	private Audio _audio;
	private AppState _appState = new AppState();
	private boolean _serviceBound; // Service is bound
	private boolean _serviceConnecting; // Service is bound but is still connecting
	private String _delayedNetwork, _delayedUsername, _delayedPassword;
	private boolean _delayedPerishable;
	private BroadcastReceiver _receiverPackage; // Broadcast receiver for package install broadcasts
	private BroadcastReceiver _receiverAppState; // Broadcast receiver for app state broadcasts
	private BroadcastReceiver _receiverMessageState; // Broadcast receiver for message state broadcasts
	private BroadcastReceiver _receiverContactSelected; // Broadcast receiver for selected contact broadcasts
	private BroadcastReceiver _receiverActiveTab; // Broadcast receiver for last selected contact list tab
	private BroadcastReceiver _receiverPermissionErrors; // Broadcast receiver for permissions errors

	private static final int AWAKE_TIMER = 1;

	private static final String _pttActivityClass = "com.zello.sdk.Activity";
	private static final String _pttPermissionsActivityClass = "com.zello.sdk.PermissionsActivity";
	private static Intent _serviceIntent;

	//endregion

	//region Initializer

	Sdk() {

	}

	//endregion

	//region Lifecycle Methods

	@SuppressLint("InlinedApi")
	@SuppressWarnings("deprecation")
	void onCreate(String packageName, Context context) {
		_package = Util.toLowerCaseLexicographically(Util.emptyIfNull(packageName));
		_context = context;
		_handler = new SafeHandler<>(this);
		_appState._available = isAppAvailable();
		if (context != null) {
			// Spin up the main app
			connect();
			// Register to receive package install broadcasts
			_receiverPackage = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					updateAppAvailable();
					if (intent != null) {
						String action = intent.getAction();
						if (action != null) {
							if (action.equals(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE) || action.equals(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE)) {
								String[] pkgs = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
								if (pkgs != null) {
									for (String pkg : pkgs) {
										if (pkg.equalsIgnoreCase(_package)) {
											reconnect();
											updateSelectedContact(null);
											updateContacts();
											break;
										}
									}
								}
							} else {
								Uri data = intent.getData();
								if (data != null) {
									String pkg = data.getSchemeSpecificPart();
									if (pkg != null && pkg.equalsIgnoreCase(_package)) {
										reconnect();
										updateSelectedContact(null);
										updateContacts();
									}
								}
							}
						}
					}
				}
			};
			IntentFilter filterPackage = new IntentFilter();
			filterPackage.addAction(Intent.ACTION_PACKAGE_ADDED);
			filterPackage.addAction(Intent.ACTION_PACKAGE_INSTALL);
			filterPackage.addAction(Intent.ACTION_PACKAGE_REMOVED);
			filterPackage.addAction(Intent.ACTION_PACKAGE_REPLACED);
			filterPackage.addAction(Intent.ACTION_PACKAGE_CHANGED);
			filterPackage.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED);
			filterPackage.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
			filterPackage.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
			filterPackage.addDataScheme("package");
			context.registerReceiver(_receiverPackage, filterPackage);
			// Register to receive app state broadcasts
			_receiverAppState = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					updateAppState(intent);
				}
			};
			Intent intentStickyAppState = context.registerReceiver(_receiverAppState, new IntentFilter(_package + "." + Constants.ACTION_APP_STATE));
			updateAppState(intentStickyAppState);
			updateContacts();
			// Register to receive app permissions broadcasts
			_receiverPermissionErrors = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					handlePermissionError(intent);
				}
			};
			context.registerReceiver(_receiverPermissionErrors, new IntentFilter(_package + "." + Constants.ACTION_PERMISSION_ERRORS));
			// Register to receive message state broadcasts
			_receiverMessageState = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					updateMessageState(intent);
				}
			};
			Intent intentStickyMessageState = context.registerReceiver(_receiverMessageState, new IntentFilter(_package + "." + Constants.ACTION_MESSAGE_STATE));
			updateMessageState(intentStickyMessageState);
			// Register to receive selected contact broadcasts
			_receiverContactSelected = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					updateSelectedContact(intent);
				}
			};
			Intent intentStickySelectedContact = context.registerReceiver(_receiverContactSelected, new IntentFilter(_package + "." + Constants.ACTION_CONTACT_SELECTED));
			updateSelectedContact(intentStickySelectedContact);
			// Register to receive last selected contact list tab
			_receiverActiveTab = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					updateSelectedTab(intent);
				}
			};
			context.registerReceiver(_receiverActiveTab, new IntentFilter(_activeTabAction));
		}
	}

	void onDestroy() {
		disconnect();
		_resumed = false;
		Context context = _context;
		if (context != null) {
			if (_receiverPackage != null) {
				context.unregisterReceiver(_receiverPackage);
			}
			if (_receiverAppState != null) {
				context.unregisterReceiver(_receiverAppState);
			}
			if (_receiverPermissionErrors != null) {
				context.unregisterReceiver(_receiverPermissionErrors);
			}
			if (_receiverMessageState != null) {
				context.unregisterReceiver(_receiverMessageState);
			}
			if (_receiverContactSelected != null) {
				context.unregisterReceiver(_receiverContactSelected);
			}
			if (_receiverActiveTab != null) {
				context.unregisterReceiver(_receiverActiveTab);
			}
		}
		Contacts contacts = _contacts;
		if (contacts != null) {
			contacts.close();
		}
		Audio audio = _audio;
		if (audio != null) {
			audio.close();
		}
		_receiverPackage = null;
		_receiverAppState = null;
		_receiverPermissionErrors = null;
		_receiverMessageState = null;
		_receiverContactSelected = null;
		_receiverActiveTab = null;
		stopAwakeTimer();
		_handler = null;
		if (!_serviceConnecting) {
			_context = null;
		}
		_package = "";
		_contacts = null;
		_audio = null;
	}

	void onResume() {
		if (!_resumed) {
			_resumed = true;
			sendStayAwake();
			startAwakeTimer();
		}
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
		if (context != null) {
			try {
				Intent intent = new Intent();
				intent.setComponent(new ComponentName(_package, _pttPermissionsActivityClass));
				intent.putExtra(Constants.EXTRA_REQUEST_VITAL_PERMISSIONS, true);
				context.startActivity(intent);
			} catch (Exception ignored) {
				// ActivityNotFoundException
			}
		}
	}

	void showMicrophonePermissionDialog() {
		Context context = _context;
		if (context != null) {
			try {
				Intent intent = new Intent();
				intent.setComponent(new ComponentName(_package, _pttPermissionsActivityClass));
				intent.putExtra(Constants.EXTRA_PERMISSION_DIALOG, true);
				intent.putExtra(Constants.EXTRA_PERMISSION_MICROPHONE, true);
				context.startActivity(intent);
			} catch (Exception ignored) {
				// ActivityNotFoundException
			}
		}
	}

	//endregion

	//region Contact Selection

	void selectContact(String title, Tab[] tabs, Tab activeTab, Theme theme) {
		Context context = _context.getApplicationContext();
		if (context != null) {
			String tabList = tabsToString(tabs);
			if (tabList != null) {
				try {
					Intent intent = new Intent();
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setComponent(new ComponentName(_package, _pttActivityClass));
					intent.setAction(Intent.ACTION_PICK);
					intent.putExtra(Intent.EXTRA_TITLE, title); // Activity title; optional
					intent.putExtra(Constants.EXTRA_TABS, tabList); // Set of displayed tabs; required; any combination of RECENTS, USERS and CHANNELS
					intent.putExtra(Constants.EXTRA_TAB, tabToString(activeTab)); // Initially active tab; optional; can be RECENTS, USERS or CHANNELS
					intent.putExtra(Constants.EXTRA_CALLBACK, _activeTabAction); // Last selected tab callback action; optional
					if (theme == Theme.LIGHT) {
						intent.putExtra(Constants.EXTRA_THEME, Constants.VALUE_LIGHT);
					}
					context.startActivity(intent);
				} catch (Exception ignored) {
					// ActivityNotFoundException
				}
			}
		}
	}

	void selectContact(String title, Tab[] tabs, Tab activeTab, Theme theme, Activity activity) {
		if (activity != null) {
			String tabList = tabsToString(tabs);
			if (tabList != null) {
				try {
					Intent intent = new Intent();
					intent.setComponent(new ComponentName(_package, _pttActivityClass));
					intent.setAction(Intent.ACTION_PICK);
					intent.putExtra(Intent.EXTRA_TITLE, title); // Activity title; optional
					intent.putExtra(Constants.EXTRA_TABS, tabList); // Set of displayed tabs; required; any combination of RECENTS, USERS and CHANNELS
					intent.putExtra(Constants.EXTRA_TAB, tabToString(activeTab)); // Initially active tab; optional; can be RECENTS, USERS or CHANNELS
					intent.putExtra(Constants.EXTRA_CALLBACK, _activeTabAction); // Last selected tab callback action; optional
					if (theme == Theme.LIGHT) {
						intent.putExtra(Constants.EXTRA_THEME, Constants.VALUE_LIGHT);
					}
					activity.startActivity(intent);
				} catch (Exception ignored) {
					// ActivityNotFoundException
				}
			}
		}
	}

	//endregion

	//region Sending Messages

	void beginMessage() {
		Context context = _context;
		if (context != null) {
			Intent intent = new Intent(_package + "." + Constants.ACTION_COMMAND);
			intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_BEGIN_MESSAGE);
			context.sendBroadcast(intent);
		}
	}

	void endMessage() {
		Context context = _context;
		if (context != null) {
			Intent intent = new Intent(_package + "." + Constants.ACTION_COMMAND);
			intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_END_MESSAGE);
			context.sendBroadcast(intent);
		}
	}

	//endregion

	//region Channels

	void connectChannel(String channel) {
		if (channel != null && channel.length() > 0) {
			Context context = _context;
			if (context != null) {
				Intent intent = new Intent(_package + "." + Constants.ACTION_COMMAND);
				intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_CONNECT);
				intent.putExtra(Constants.EXTRA_CONTACT_NAME, channel);
				context.sendBroadcast(intent);
			}
		}
	}

	void disconnectChannel(String channel) {
		if (channel != null && channel.length() > 0) {
			Context context = _context;
			if (context != null) {
				Intent intent = new Intent(_package + "." + Constants.ACTION_COMMAND);
				intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_DISCONNECT);
				intent.putExtra(Constants.EXTRA_CONTACT_NAME, channel);
				context.sendBroadcast(intent);
			}
		}
	}

	//endregion

	//region Contacts

	void muteContact(Contact contact, boolean mute) {
		if (contact != null) {
			Context context = _context;
			if (context != null) {
				ContactType type = contact.getType();
				Intent intent = new Intent(_package + "." + Constants.ACTION_COMMAND);
				intent.putExtra(Constants.EXTRA_COMMAND, mute ? Constants.VALUE_MUTE : Constants.VALUE_UNMUTE);
				intent.putExtra(Constants.EXTRA_CONTACT_NAME, contact.getName());
				intent.putExtra(Constants.EXTRA_CONTACT_TYPE, type == ContactType.CHANNEL || type == ContactType.GROUP ? 1 : 0);
				context.sendBroadcast(intent);
			}
		}
	}

	//endregion

	//region Authentication

	boolean signIn(String network, String username, String password) {
		return signIn(network, username, password, false);
	}

	boolean signIn(String network, String username, String password, boolean perishable) {
		if (network != null && network.length() > 0 && username != null && username.length() > 0 && password != null && password.length() > 0) {
			if (isConnected()) {
				Context context = _context;
				if (context != null) {
					Intent intent = new Intent(_package + "." + Constants.ACTION_COMMAND);
					intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_SIGN_IN);
					intent.putExtra(Constants.EXTRA_NETWORK_URL, network);
					intent.putExtra(Constants.EXTRA_USERNAME, username);
					intent.putExtra(Constants.EXTRA_PASSWORD, md5(password));
					intent.putExtra(Constants.EXTRA_PERISHABLE, perishable);
					context.sendBroadcast(intent);
				}
			} else if (_serviceBound && _serviceConnecting) {
				_delayedNetwork = network;
				_delayedUsername = username;
				_delayedPassword = password;
				_delayedPerishable = perishable;
			}
			return true;
		} else {
			return false;
		}
	}

	void signOut() {
		_delayedNetwork = _delayedUsername = _delayedPassword = null;
		_delayedPerishable = false;
		if (_serviceBound) {
			Context context = _context;
			if (context != null) {
				Intent intent = new Intent(_package + "." + Constants.ACTION_COMMAND);
				intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_SIGN_OUT);
				context.sendBroadcast(intent);
			}
		}
	}

	void cancel() {
		_delayedNetwork = _delayedUsername = _delayedPassword = null;
		_delayedPerishable = false;
		if (_serviceBound) {
			Context context = _context;
			if (context != null) {
				Intent intent = new Intent(_package + "." + Constants.ACTION_COMMAND);
				intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_CANCEL);
				context.sendBroadcast(intent);
			}
		}
	}

	//endregion

	//region Locking

	void lock(String applicationName, String packageName) {
		if (isConnected()) {
			Context context = _context;
			if (context != null && applicationName != null && applicationName.length() > 0) {
				Intent intent = new Intent(_package + "." + Constants.ACTION_COMMAND);
				intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_LOCK);
				intent.putExtra(Constants.EXTRA_APPLICATION, applicationName);
				intent.putExtra(Constants.EXTRA_PACKAGE, packageName);
				context.sendBroadcast(intent);
			}
		}
	}

	void unlock() {
		if (isConnected()) {
			Context context = _context;
			if (context != null) {
				Intent intent = new Intent(_package + "." + Constants.ACTION_COMMAND);
				intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_LOCK);
				context.sendBroadcast(intent);
			}
		}
	}

	//endregion

	//region Status

	void setStatus(Status status) {
		if (_serviceBound) {
			Context context = _context;
			if (context != null) {
				Intent intent = new Intent(_package + "." + Constants.ACTION_COMMAND);
				intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_SET_STATUS);
				intent.putExtra(Constants.EXTRA_STATE_BUSY, status == Status.BUSY);
				intent.putExtra(Constants.EXTRA_STATE_SOLO, status == Status.SOLO);
				context.sendBroadcast(intent);
			}
		}
	}

	void setStatusMessage(String message) {
		if (_serviceBound) {
			Context context = _context;
			if (context != null) {
				Intent intent = new Intent(_package + "." + Constants.ACTION_COMMAND);
				intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_SET_STATUS);
				intent.putExtra(Constants.EXTRA_STATE_STATUS_MESSAGE, Util.emptyIfNull(message));
				context.sendBroadcast(intent);
			}
		}
	}

	//endregion

	//region Opening PTT app

	void openMainScreen() {
		Context context = _context;
		if (context != null) {
			try {
				Intent LaunchIntent = context.getPackageManager().getLaunchIntentForPackage(_package);
				context.startActivity(LaunchIntent);
			} catch (Exception ignored) {
				// PackageManager.NameNotFoundException, ActivityNotFoundException
			}
		}
	}

	//endregion

	//region Getters

	void getMessageIn(MessageIn message) {
		_messageIn.copyTo(message);
	}

	void getMessageOut(MessageOut message) {
		_messageOut.copyTo(message);
	}

	void getAppState(AppState state) {
		_appState.copyTo(state);
	}

	void getSelectedContact(Contact contact) {
		_selectedContact.copyTo(contact);
	}

	Contacts getContacts() {
		return _contacts;
	}

	Audio getAudio() {
		if (_context != null) {
			if (_audio == null) {
				_audio = new Audio(_package, _context);
			}
		}
		return _audio;
	}

	//endregion

	//region Setters

	void setAutoRun(boolean enable) {
		if (isConnected()) {
			Context context = _context;
			if (context != null) {
				Intent intent = new Intent(_package + "." + Constants.ACTION_COMMAND);
				intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_SET_AUTO_RUN);
				intent.putExtra(Constants.EXTRA_STATE_AUTO_RUN, enable);
				context.sendBroadcast(intent);
			}
		}
	}

	void setAutoConnectChannels(boolean connect) {
		if (isConnected()) {
			Context context = _context;
			if (context != null) {
				Intent intent = new Intent(_package + "." + Constants.ACTION_COMMAND);
				intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_SET_AUTO_CHANNELS);
				intent.putExtra(Constants.EXTRA_STATE_AUTO_CHANNELS, connect);
				context.sendBroadcast(intent);
			}
		}
	}

	void setExternalId(String id) {
		if (isConnected()) {
			Context context = _context;
			if (context != null) {
				Intent intent = new Intent(_package + "." + Constants.ACTION_COMMAND);
				intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_SET_EID);
				intent.putExtra(Constants.EXTRA_EID, id == null ? "" : id);
				context.sendBroadcast(intent);
			}
		}
	}

	void setSelectedContact(Contact contact) {
		if (contact != null) {
			ContactType type = contact.getType();
			selectContact(type == ContactType.CHANNEL || type == ContactType.GROUP ? 1 : 0, contact.getName());
		} else {
			selectContact(0, null);
		}
	}

	void setSelectedUserOrGateway(String name) {
		selectContact(0, name);
	}

	void setSelectedChannelOrGroup(String name) {
		selectContact(1, name);
	}

	//endregion

	//endregion

	//region Overridden Methods

	//region SafeHandlerEvents

	@Override
	public void handleMessageFromSafeHandler(Message message) {
		if (message != null) {
			if (message.what == AWAKE_TIMER) {
				if (_resumed) {
					sendStayAwake();
					Handler h = _handler;
					if (h != null) {
						h.sendMessageDelayed(h.obtainMessage(AWAKE_TIMER), Constants.STAY_AWAKE_TIMEOUT);
					}
				}
			}
		}
	}

	//endregion

	//region ServiceConnection

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Context context = _context;
		if (context != null) {
			if (_serviceConnecting) {
				_serviceConnecting = false;
				context.startService(getServiceIntent());
				if (_delayedNetwork != null) {
					signIn(_delayedNetwork, _delayedUsername, _delayedPassword, _delayedPerishable);
				}
				_delayedNetwork = _delayedUsername = _delayedPassword = null;
				_delayedPerishable = false;
				// If service is not bound, the component was destroyed and the service needs to be disconnected
				if (!_serviceBound) {
					Log.i("zello sdk", "disconnecting because sdk was destroyed");
					try {
						context.unbindService(this);
					} catch (Throwable t) {
					}
					_context = null;
					_appState._error = false;
				}
				_appState._initializing = false;
				fireAppStateChanged();
			}
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
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

	//region Private Methods

	private void selectContact(int type, String name) {
		Context context = _context;
		if (context != null) {
			Intent intent = new Intent(_package + "." + Constants.ACTION_COMMAND);
			intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_SELECT_CONTACT);
			if (name != null && name.length() > 0) {
				intent.putExtra(Constants.EXTRA_CONTACT_NAME, name);
				intent.putExtra(Constants.EXTRA_CONTACT_TYPE, type);
			}
			context.sendBroadcast(intent);
		}
	}

	private void sendStayAwake() {
		if (isConnected()) {
			Context context = _context;
			if (context != null) {
				Intent intent = new Intent(_package + "." + Constants.ACTION_COMMAND);
				intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_STAY_AWAKE);
				context.sendBroadcast(intent);
			}
		}
	}

	private void connect() {
		if (!_serviceBound || !_serviceConnecting) {
			Context context = _context;
			if (context != null) {
				_serviceConnecting = true;
				_appState._initializing = true;
				_appState._error = false;
				fireAppStateChanged();

				if (!_serviceBound) {
					try {
						_serviceBound = context.bindService(getServiceIntent(), this, Context.BIND_AUTO_CREATE);
					} catch (Throwable t) {
						_serviceConnecting = false;
						Log.i("zello sdk", "Error in Sdk.connect: " + t.toString());
					}
				}
				if (!_serviceBound) {
					_appState._error = true;
					try {
						context.unbindService(this);
					} catch (Throwable t) {
					}
				}
				if (_serviceConnecting) {
					_appState._initializing = false;
					fireAppStateChanged();
				}
			}
		}
	}

	private void disconnect() {
		_delayedNetwork = _delayedUsername = _delayedPassword = null;
		_delayedPerishable = false;
		if (_serviceBound) {
			_serviceBound = false;
			if (!_serviceConnecting) {
				Context context = _context;
				if (context != null) {
					try {
						context.unbindService(this);
					} catch (Throwable t) {
					}
				}
			} else {
				Log.i("zello sdk", "Early Sdk.disconnect");
			}
		}
	}

	private Intent getServiceIntent() {
		Intent intent = _serviceIntent;
		if (intent == null) {
			intent = new Intent();
			intent.setClassName(_package, "com.loudtalks.client.ui.Svc");
			_serviceIntent = intent;
		}
		return intent;
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

	private void updateAppState(Intent intent) {
		_appState.reset();
		if (intent != null) {
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

	private void updateMessageState(Intent intent) {
		boolean out = false;
		boolean in = false;
		if (intent != null) {
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

	private void updateContacts() {
		Contacts contacts = _contacts;
		_contacts = null;
		if (contacts != null) {
			contacts.close();
		}
		Context context = _context;
		if (context != null) {
			_contacts = new Contacts(_package, context, _handler);
		}
	}

	private void updateSelectedContact(Intent intent) {
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
			_selectedContact._noDisconnect = intent.getIntExtra(Constants.EXTRA_CHANNEL_NO_DISCONNECT, _selectedContact._type != ContactType.CHANNEL ? 1 : 0) != 0;
		} else {
			_selectedContact.reset();
		}

		for (Events event : Zello.getInstance().events) {
			event.onSelectedContactChanged();
		}
	}

	private void updateSelectedTab(Intent intent) {
		if (intent != null) {
			Tab tab = stringToTab(intent.getStringExtra(Constants.EXTRA_TAB));

			for (Events event : Zello.getInstance().events) {
				event.onLastContactsTabChanged(tab);
			}
		}
	}

	private void handlePermissionError(Intent intent) {
		if (intent != null) {
			PermissionError error = intToPermissionError(intent.getIntExtra(Constants.EXTRA_LATEST_PERMISSION_ERROR, PermissionError.NONE.ordinal()));
			if (error == PermissionError.MICROPHONE_NOT_GRANTED) {
				for (Events event : Zello.getInstance().events) {
					event.onMicrophonePermissionNotGranted();
				}
			}
		}
	}

	private boolean isConnected() {
		return _serviceBound && !_serviceConnecting;
	}

	private boolean isAppAvailable() {
		Context context = _context;
		if (context != null) {
			try {
				return null != context.getPackageManager().getLaunchIntentForPackage(_package);
			} catch (Exception e) {
				// PackageManager.NameNotFoundException
			}
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

	static Error intToError(int error) {
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
			} else {
				return Error.UNKNOWN;
			}
		} else {
			return Error.NONE;
		}
	}

	static PermissionError intToPermissionError(int error) {
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

	static ContactType intToContactType(int type) {
		switch (type) {
			case 1:
				return ContactType.CHANNEL;
			case 3:
				return ContactType.GROUP;
			case 2:
				return ContactType.GATEWAY;
			default:
				return ContactType.USER;
		}
	}

	static ContactStatus intToContactStatus(int status) {
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

	static String tabToString(Tab tab) {
		switch (tab) {
			case RECENTS:
				return Constants.VALUE_RECENTS;
			case USERS:
				return Constants.VALUE_USERS;
			case CHANNELS:
				return Constants.VALUE_CHANNELS;
		}
		return null;
	}

	static String tabsToString(Tab[] tabs) {
		String s = null;
		if (tabs != null) {
			for (Tab tab : tabs) {
				String name = tabToString(tab);
				if (name != null) {
					if (s == null) {
						s = name;
					} else {
						s += "," + name;
					}
				}
			}
		}
		return s;
	}

	static Tab stringToTab(String s) {
		if (s.equals(Constants.VALUE_USERS)) {
			return Tab.USERS;
		}
		if (s.equals(Constants.VALUE_CHANNELS)) {
			return Tab.CHANNELS;
		}
		return Tab.RECENTS;
	}

	static String bytesToHex(byte[] data) {
		if (data != null) {
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < data.length; i++) {
				int halfbyte = (data[i] >>> 4) & 0x0F;
				int two_halfs = 0;
				do {
					if ((0 <= halfbyte) && (halfbyte <= 9))
						buf.append((char) ('0' + halfbyte));
					else
						buf.append((char) ('a' + (halfbyte - 10)));
					halfbyte = data[i] & 0x0F;
				} while (two_halfs++ < 1);
			}
			return buf.toString();
		}
		return null;
	}

	static String md5(String s) {
		if (s != null && s.length() > 0) {
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
		}
		return "";
	}

	//endregion

}
