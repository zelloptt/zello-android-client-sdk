package com.zello.sdk.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.*;
import android.widget.*;

public class TalkActivity extends Activity implements com.zello.sdk.Events {

	private TextView _txtState;
	private View _viewLogin;
	private TextView _txtNetwork;
	private EditText _editUsername;
	private EditText _editPassword;
	private EditText _editNetwork;
	private View _viewContent;
	private ListView _listContacts;
	private View _viewTalkScreen;
	private SquareButton _btnTalk;
	private ToggleButton _btnConnect;
	private View _viewContactNotSelected;
	private View _viewContactInfo;
	private ImageView _imgMessageStatus;
	private TextView _txtMessageName;
	private TextView _txtMessageStatus;
	private View _viewAudioMode;
	private ToggleButton _btnSpeaker, _btnEarpiece, _btnBluetooth;
	private View _viewMessageInfo;

	private boolean _active; // Activity is resumed and visible to the user
	private boolean _dirtyContacts; // Contact list needs to be refreshed next time before it's presented to the user
	private com.zello.sdk.Contact _contextContact; // Contact for which currently active context menu is being displayed

	private com.zello.sdk.Sdk _sdk = new com.zello.sdk.Sdk();
	private com.zello.sdk.Audio _audio;
	private com.zello.sdk.AppState _appState = new com.zello.sdk.AppState();
	private com.zello.sdk.MessageIn _messageIn = new com.zello.sdk.MessageIn();
	private com.zello.sdk.MessageOut _messageOut = new com.zello.sdk.MessageOut();
	private com.zello.sdk.Contact _selectedContact = new com.zello.sdk.Contact();
	private com.zello.sdk.Tab _activeTab = com.zello.sdk.Tab.RECENTS;

	private static String _keyUsername = "username";
	private static String _keyPassword = "password";
	private static String _keyNetwork = "network";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_talk);
		_txtState = (TextView) findViewById(R.id.state);
		_viewLogin = findViewById(R.id.login);
		_txtNetwork = (TextView) _viewLogin.findViewById(R.id.network_label);
		_editUsername = (EditText) _viewLogin.findViewById(R.id.username);
		_editPassword = (EditText) _viewLogin.findViewById(R.id.password);
		_editNetwork = (EditText) _viewLogin.findViewById(R.id.network);
		_viewContent = findViewById(R.id.content);
		_listContacts = (ListView) _viewContent.findViewById(R.id.contact_list);
		_viewTalkScreen = _viewContent.findViewById(R.id.talk_screen);
		_viewContactInfo = _viewTalkScreen.findViewById(R.id.contact_info);
		_btnTalk = (SquareButton) _viewTalkScreen.findViewById(R.id.talk);
		_btnConnect = (ToggleButton) findViewById(R.id.button_connect);
		_viewContactNotSelected = _viewTalkScreen.findViewById(R.id.contact_not_selected);
		_viewAudioMode = findViewById(R.id.audio_mode);
		_btnSpeaker = (ToggleButton) _viewAudioMode.findViewById(R.id.audio_speaker);
		_btnEarpiece = (ToggleButton) _viewAudioMode.findViewById(R.id.audio_earpiece);
		_btnBluetooth = (ToggleButton) _viewAudioMode.findViewById(R.id.audio_bluetooth);
		_viewMessageInfo = findViewById(R.id.message_info);
		_imgMessageStatus = (ImageView) _viewMessageInfo.findViewById(R.id.message_image);
		_txtMessageName = (TextView) _viewMessageInfo.findViewById(R.id.message_name);
		_txtMessageStatus = (TextView) _viewMessageInfo.findViewById(R.id.message_status);

		// Constrain PTT button size
		_btnTalk.setMaxHeight(getResources().getDimensionPixelSize(R.dimen.talk_button_size));

		// PTT button push/release handler
		_btnTalk.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				if (action == MotionEvent.ACTION_DOWN) {
					//_sdk.setExternalId(java.util.UUID.randomUUID().toString());
					_sdk.beginMessage();
				} else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
					_sdk.endMessage();
				}
				return false;
			}
		});

		// Contact list pick handler
		_listContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ListAdapter adapter = (ListAdapter) _listContacts.getAdapter();
				if (adapter != null) {
					com.zello.sdk.Contact contact = (com.zello.sdk.Contact) adapter.getItem(position);
					if (contact != null) {
						_sdk.setSelectedContact(contact);
					}
				}
			}
		});
		_listContacts.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
				if (menuInfo != null && menuInfo instanceof AdapterView.AdapterContextMenuInfo) {
					AdapterView.AdapterContextMenuInfo listInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
					if (listInfo.targetView != null && listInfo.targetView.getParent() == _listContacts) {
						int position = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
						android.widget.ListAdapter adapter = _listContacts.getAdapter();
						if (adapter != null && adapter instanceof ListAdapter) {
							if (position >= 0 && adapter.getCount() > position) {
								_contextContact = (com.zello.sdk.Contact) adapter.getItem(position);
								if (_contextContact != null) {
									menu.add(0, R.id.menu_talk, 0, getResources().getString(R.string.menu_talk));
									if (!_contextContact.getMuted()) {
										menu.add(0, R.id.menu_mute, 1, getResources().getString(R.string.menu_mute));
									} else {
										menu.add(0, R.id.menu_unmute, 1, getResources().getString(R.string.menu_unmute));
									}
								}
							}
						}
					}
				}
			}
		});

		// Connect channel
		_btnConnect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				connectChannel();
			}
		});

		// Deselect contact
		findViewById(R.id.button_close).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				_sdk.setSelectedContact(null);
			}
		});

		// Login credentials
		_editUsername.setText(Helper.loadValue(this, _keyUsername));
		_editPassword.setText(Helper.loadValue(this, _keyPassword));
		_editNetwork.setText(Helper.loadValue(this, _keyNetwork));
		_editPassword = (EditText) _viewLogin.findViewById(R.id.password);
		_editNetwork = (EditText) _viewLogin.findViewById(R.id.network);
		findViewById(R.id.button_login).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String username = _editUsername.getText().toString();
				String password = _editPassword.getText().toString();
				String network = _editNetwork.getText().toString();
				Helper.saveValue(TalkActivity.this, _keyUsername, username);
				Helper.saveValue(TalkActivity.this, _keyPassword, password);
				Helper.saveValue(TalkActivity.this, _keyNetwork, network);
				_sdk.signIn(network, username, password);
			}
		});

		// Audio modes
		_btnSpeaker.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (_audio != null) {
					_audio.setMode(com.zello.sdk.AudioMode.SPEAKER);
				}
			}
		});
		_btnEarpiece.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (_audio != null) {
					_audio.setMode(com.zello.sdk.AudioMode.EARPIECE);
				}
			}
		});
		_btnBluetooth.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (_audio != null) {
					_audio.setMode(com.zello.sdk.AudioMode.BLUETOOTH);
				}
			}
		});

		_dirtyContacts = true;
		_sdk.onCreate("com.pttsdk", this, this); // Use with generic apk
		_audio = _sdk.getAudio();
		//_sdk.onCreate("net.loudtalks", this, this); // Use to connect to apk from zellowork.com
		updateAppState();
		updateAudioMode();
		updateMessageState();
		updateSelectedContact();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		_sdk.onDestroy();
		_audio = null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		_sdk.onResume();
		_active = true;
		updateContactList();
	}

	@Override
	protected void onPause() {
		super.onPause();
		_sdk.onPause();
		_active = false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (_appState.isAvailable()) {
			getMenuInflater().inflate(R.menu.menu, menu);
			boolean select = false, available = false, solo = false, busy = false;
			if (!_appState.isConfiguring() && _appState.isSignedIn() && !_appState.isSigningIn() && !_appState.isSigningOut()) {
				com.zello.sdk.Status status = _appState.getStatus();
				select = true;
				available = status == com.zello.sdk.Status.AVAILABLE;
				solo = status == com.zello.sdk.Status.SOLO;
				busy = status == com.zello.sdk.Status.BUSY;
			}
			menu.findItem(R.id.menu_select_contact).setVisible(select);
			menu.findItem(R.id.menu_lock_ptt_app).setVisible(!_appState.isLocked());
			menu.findItem(R.id.menu_unlock_ptt_app).setVisible(_appState.isLocked());
			menu.findItem(R.id.menu_enable_auto_run).setVisible(!_appState.isAutoRunEnabled());
			menu.findItem(R.id.menu_disable_auto_run).setVisible(_appState.isAutoRunEnabled());
			menu.findItem(R.id.menu_enable_auto_connect_channels).setVisible(!_appState.isChannelAutoConnectEnabled());
			menu.findItem(R.id.menu_disable_auto_connect_channels).setVisible(_appState.isChannelAutoConnectEnabled());
			menu.findItem(R.id.menu_about).setVisible(true);
			MenuItem itemAvailable = menu.findItem(R.id.menu_available);
			if (itemAvailable != null) {
				itemAvailable.setVisible(available);
			}
			MenuItem itemSolo = menu.findItem(R.id.menu_solo);
			if (itemSolo != null) {
				itemSolo.setVisible(solo);
			}
			MenuItem itemBusy = menu.findItem(R.id.menu_busy);
			if (itemBusy != null) {
				itemBusy.setVisible(busy);
			}
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_set_status:
			case R.id.menu_available:
			case R.id.menu_solo:
			case R.id.menu_busy: {
				chooseStatus();
				return true;
			}
			case R.id.menu_select_contact: {
				chooseActiveContact();
				return true;
			}
			case R.id.menu_lock_ptt_app: {
				lockPttApp();
				return true;
			}
			case R.id.menu_unlock_ptt_app: {
				unlockPttApp();
				return true;
			}
			case R.id.menu_enable_auto_run: {
				enableAutoRun();
				return true;
			}
			case R.id.menu_disable_auto_run: {
				disableAutoRun();
				return true;
			}
			case R.id.menu_enable_auto_connect_channels: {
				enableAutoConnectChannels();
				return true;
			}
			case R.id.menu_disable_auto_connect_channels: {
				disableAutoConnectChannels();
				return true;
			}
			case R.id.menu_start_message: {
				_sdk.beginMessage();
				return true;
			}
			case R.id.menu_stop_message: {
				_sdk.endMessage();
				return true;
			}
			case R.id.menu_about: {
				showAbout();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item != null) {
			ContextMenu.ContextMenuInfo menuInfo = item.getMenuInfo();
			if (menuInfo != null && menuInfo instanceof AdapterView.AdapterContextMenuInfo) {
				AdapterView.AdapterContextMenuInfo listInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
				if (listInfo.targetView != null && listInfo.targetView.getParent() == _listContacts && _contextContact != null) {
					switch (item.getItemId()) {
						case R.id.menu_talk: {
							_sdk.setSelectedContact(_contextContact);
							break;
						}
						case R.id.menu_mute: {
							_sdk.muteContact(_contextContact, true);
							break;
						}
						case R.id.menu_unmute: {
							_sdk.muteContact(_contextContact, false);
							break;
						}
					}
					return true;
				}
			}
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onSelectedContactChanged() {
		updateSelectedContact();
	}

	@Override
	public void onMessageStateChanged() {
		updateMessageState();
	}

	@Override
	public void onAppStateChanged() {
		updateAppState();
	}

	@Override
	public void onLastContactsTabChanged(com.zello.sdk.Tab tab) {
		_activeTab = tab;
	}

	@Override
	public void onContactsChanged() {
		_dirtyContacts = true;
		updateContactList();
	}

	@Override
	public void onAudioStateChanged() {
		updateAudioMode();
	}

	private void showAbout() {
		System.out.println("showAbout");
		Intent intent = new Intent(this, AnotherActivity.class);
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException ignored) {
			System.out.println("Exception: " + ignored);
		}
	}

	private void chooseActiveContact() {
		// Activity title; optional
		String title = getResources().getString(R.string.select_contact_title);
		// Set of displayed tabs; required; any combination of RECENTS, USERS and CHANNELS
		com.zello.sdk.Tab[] tabs = new com.zello.sdk.Tab[]{com.zello.sdk.Tab.RECENTS, com.zello.sdk.Tab.USERS, com.zello.sdk.Tab.CHANNELS};
		// Initially active tab; optional; can be RECENTS, USERS or CHANNELS
		com.zello.sdk.Tab tab = _activeTab;
		// Visual theme; optional; can be DARK or LIGHT
		com.zello.sdk.Theme theme = com.zello.sdk.Theme.DARK;

		_sdk.selectContact(title, tabs, tab, theme);
	}

	private void lockPttApp() {
		// Configure PTT app to display information screen with the name of this app that can be clicked to open main activity
		_sdk.lock(getString(R.string.app_name), getPackageName());
	}

	private void unlockPttApp() {
		// Switch PTT app back to normal UI mode
		_sdk.unlock();
	}

	private void enableAutoRun() {
		// Enable client auto-run option
		_sdk.setAutoRun(true);
	}

	private void disableAutoRun() {
		// Disable client auto-run option
		_sdk.setAutoRun(false);
	}

	private void enableAutoConnectChannels() {
		// Enable auto-connecting to new channels
		_sdk.setAutoConnectChannels(true);
	}

	private void disableAutoConnectChannels() {
		// Disable auto-connecting to new channels
		_sdk.setAutoConnectChannels(false);
	}

	private void chooseStatus() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		Resources res = getResources();
		com.zello.sdk.Status status = _appState.getStatus();
		int selection = status == com.zello.sdk.Status.BUSY ? 2 : (status == com.zello.sdk.Status.SOLO ? 1 : 0);
		String[] items = new String[]{res.getString(R.string.menu_available), res.getString(R.string.menu_solo), res.getString(R.string.menu_busy), res.getString(R.string.menu_sign_out)};
		builder.setSingleChoiceItems(items, selection, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
					case 0: {
						_sdk.setStatus(com.zello.sdk.Status.AVAILABLE);
						break;
					}
					case 1: {
						_sdk.setStatus(com.zello.sdk.Status.SOLO);
						break;
					}
					case 2: {
						_sdk.setStatus(com.zello.sdk.Status.BUSY);
						break;
					}
					case 3: {
						_sdk.signOut();
						break;
					}
				}
				dialog.dismiss();
			}
		});
		builder.setCancelable(true).setTitle(res.getString(R.string.menu_set_status));
		final AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
	}

	private void cancelMessageOut() {
		_btnTalk.setPressed(false);
		_btnTalk.cancelLongPress();
	}

	private void updateSelectedContact() {
		_sdk.getSelectedContact(_selectedContact);
		String name = _selectedContact.getName(); // Contact name
		boolean selected = name != null && name.length() > 0;
		boolean canTalk = false, showConnect = false, connected = false, canConnect = false;
		if (selected) {
			// Update info
			ListAdapter.configureView(_viewContactInfo, _selectedContact);
			com.zello.sdk.ContactType type = _selectedContact.getType();
			com.zello.sdk.ContactStatus status = _selectedContact.getStatus();
			switch (type) {
				case USER:
				case GATEWAY: {
					// User or radio gateway
					canTalk = status != com.zello.sdk.ContactStatus.OFFLINE; // Not offline
					break;
				}
				case CHANNEL: {
					showConnect = true;
					if (_appState.isSignedIn()) {
						if (status == com.zello.sdk.ContactStatus.AVAILABLE) {
							canTalk = true; // Channel is online
							canConnect = true;
							connected = true;
						} else if (status == com.zello.sdk.ContactStatus.OFFLINE) {
							canConnect = true;
						} else if (status == com.zello.sdk.ContactStatus.CONNECTING) {
							connected = true;
						}
					}
					break;
				}
				case GROUP: {
					int count = _selectedContact.getUsersCount();
					if (status == com.zello.sdk.ContactStatus.AVAILABLE && count > 0) {
						canTalk = true; // Group is online and there are online contacts in it
					}
					break;
				}
			}
		}
		_viewContactNotSelected.setVisibility(selected ? View.INVISIBLE : View.VISIBLE);
		_viewContactInfo.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);
		_btnTalk.setEnabled(canTalk);
		_btnConnect.setEnabled(canConnect);
		_btnConnect.setChecked(connected);
		_btnConnect.setVisibility(showConnect ? View.VISIBLE : View.GONE);
		updateActiveScreen();
	}

	private void updateMessageState() {
		_sdk.getMessageIn(_messageIn);
		_sdk.getMessageOut(_messageOut);
		boolean incoming = _messageIn.isActive(); // Is incoming message active?
		boolean outgoing = _messageOut.isActive(); // Is outgoing message active?
		if (outgoing) {
			_txtMessageName.setText(_messageOut.getTo().getDisplayName()); // Show recipient name
			_txtMessageStatus.setText(_messageOut.isConnecting() ? R.string.message_connecting : R.string.message_sending); // Is outgoing message in a process of connecting?
			_imgMessageStatus.setImageResource(R.drawable.message_out);
		} else {
			cancelMessageOut(); // Let user know that outgoing message has failed
			if (incoming) {
				String author = _messageIn.getAuthor().getDisplayName(); // Is message from channel?
				if (author != null && author.length() > 0) {
					_txtMessageName.setText(_messageIn.getFrom().getDisplayName() + " \\ " + author); // Show channel and author names
				} else {
					_txtMessageName.setText(_messageIn.getFrom().getDisplayName()); // Show sender name
				}
				_txtMessageStatus.setText(R.string.message_receiving);
				_imgMessageStatus.setImageResource(R.drawable.message_in);
			}
		}
		_viewMessageInfo.setVisibility(incoming || outgoing ? View.VISIBLE : View.INVISIBLE);
	}

	private void updateAppState() {
		_sdk.getAppState(_appState);
		String state = "";
		if (!_appState.isAvailable()) {
			state = getString(R.string.ptt_app_not_installed);
		} else if (_appState.isConfiguring()) {
			state = getString(R.string.ptt_app_configuring);
		} else if (!_appState.isSignedIn()) {
			if (_appState.isSigningIn()) {
				state = getString(R.string.ptt_app_is_signing_in);
			} else if (_appState.isSigningOut()) {
				state = getString(R.string.ptt_app_is_signing_out);
			} else if (_appState.isWaitingForNetwork()) {
				state = getString(R.string.ptt_app_is_waiting_to_reconnect);
			} else if (_appState.isReconnecting()) {
				state = getString(R.string.ptt_app_is_reconnecting).replace("%seconds%", Integer.toString(_appState.getReconnectTimer()));
			} else {
				state = getString(R.string.ptt_app_is_signed_out);
			}
		}
		_txtState.setText(state);
		updateActiveScreen();
		Helper.invalidateOptionsMenu(this);
	}

	private void updateAudioMode() {
		boolean speaker = false, earpiece = false, bluetooth = false;
		com.zello.sdk.AudioMode mode = com.zello.sdk.AudioMode.SPEAKER;
		// Can't set new mode while the mode is being changed
		boolean changindMode = false;
		if (_audio != null) {
			speaker = _audio.isModeAvailable(com.zello.sdk.AudioMode.SPEAKER);
			earpiece = _audio.isModeAvailable(com.zello.sdk.AudioMode.EARPIECE);
			bluetooth = _audio.isModeAvailable(com.zello.sdk.AudioMode.BLUETOOTH);
			mode = _audio.getMode();
			changindMode = _audio.isModeChanging();
		}
		// If none of the modes is available, the client app is old and needs to be updated
		if (bluetooth || earpiece || speaker) {
			_btnSpeaker.setVisibility(speaker ? View.VISIBLE : View.GONE);
			if (speaker) {
				_btnSpeaker.setChecked(mode == com.zello.sdk.AudioMode.SPEAKER);
				_btnSpeaker.setEnabled(!changindMode && (earpiece || bluetooth));
			}
			_btnEarpiece.setVisibility(earpiece ? View.VISIBLE : View.GONE);
			if (earpiece) {
				_btnEarpiece.setChecked(mode == com.zello.sdk.AudioMode.EARPIECE);
				_btnEarpiece.setEnabled(!changindMode && (speaker || bluetooth));
			}
			_btnBluetooth.setVisibility(bluetooth ? View.VISIBLE : View.GONE);
			if (bluetooth) {
				_btnBluetooth.setChecked(mode == com.zello.sdk.AudioMode.BLUETOOTH);
				_btnBluetooth.setEnabled(!changindMode && (speaker || earpiece));
			}
		}
		_viewAudioMode.setVisibility(speaker || earpiece || bluetooth ? View.VISIBLE : View.GONE);
	}

	private void updateActiveScreen() {
		int stateFlag = View.GONE;
		int loginFlag = View.GONE;
		int contentFlag = View.GONE;
		int listFlag = View.GONE;
		int talkFlag = View.GONE;
		int networkFlag = View.GONE;
		if (!_appState.isAvailable() || _appState.isConfiguring()) {
			stateFlag = View.VISIBLE;
		} else if (!_appState.isSignedIn()) {
			if (_appState.isSigningIn() || _appState.isSigningOut() || _appState.isWaitingForNetwork() || _appState.isReconnecting()) {
				stateFlag = View.VISIBLE;
			} else {
				loginFlag = View.VISIBLE;
				// Network URL controls are only used when PTT app is not hardcoded to work only with particular network
				networkFlag = _appState.isCustomBuild() ? View.GONE : View.VISIBLE;
			}
		} else {
			contentFlag = View.VISIBLE;
			String name = _selectedContact.getName();
			if (name != null && name.length() != 0) {
				talkFlag = View.VISIBLE;
			} else {
				listFlag = View.VISIBLE;
			}
		}

		_txtState.setVisibility(stateFlag);
		_viewLogin.setVisibility(loginFlag);
		_viewContent.setVisibility(contentFlag);
		_listContacts.setVisibility(listFlag);
		_viewTalkScreen.setVisibility(talkFlag);
		_txtNetwork.setVisibility(networkFlag);
		_editNetwork.setVisibility(networkFlag);

		if (listFlag == View.VISIBLE) {
			updateContactList();
		}
	}

	private void updateContactList() {
		// Avoid updating contact list when it's not visible
		if (_active && _dirtyContacts && _listContacts != null && _listContacts.getVisibility() == View.VISIBLE) {
			_dirtyContacts = false;
			ListAdapter adapter = (ListAdapter) _listContacts.getAdapter();
			boolean newAdapter = false;
			if (adapter == null) {
				newAdapter = true;
				adapter = new ListAdapter();
			}
			adapter.setContacts(_sdk.getContacts());
			Parcelable state = _listContacts.onSaveInstanceState();
			if (newAdapter) {
				_listContacts.setAdapter(adapter);
			} else {
				adapter.notifyDataSetChanged();
			}
			if (state != null) {
				_listContacts.onRestoreInstanceState(state);
			}
			_listContacts.setFocusable(adapter.getCount() > 0);
		}
	}

	private void connectChannel() {
		if (_selectedContact.getType() == com.zello.sdk.ContactType.CHANNEL) {
			com.zello.sdk.ContactStatus status = _selectedContact.getStatus();
			if (status == com.zello.sdk.ContactStatus.OFFLINE) {
				_sdk.connectChannel(_selectedContact.getName());
			} else if (status == com.zello.sdk.ContactStatus.AVAILABLE) {
				_sdk.disconnectChannel(_selectedContact.getName());
			}
		}
	}

}
