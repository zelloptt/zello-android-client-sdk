package com.zello.sdk.sample;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.zello.sdk.BluetoothAccessoryState;
import com.zello.sdk.BluetoothAccessoryType;
import com.zello.sdk.Zello;

import java.text.NumberFormat;

public class TalkActivity extends AppCompatActivity implements com.zello.sdk.Events {

	private View _viewState;
	private TextView _txtState;
	private Button _btnCancel;
	private View _viewLogin;
	private TextView _txtNetwork;
	private EditText _editUsername;
	private EditText _editPassword;
	private EditText _editNetwork;
	private CheckBox _checkPerishable;
	private View _viewContent;
	private ListView _listContacts;
	private View _viewTalkScreen;
	private SquareButton _btnTalk;
	private ToggleButton _btnConnect;
	private ImageButton _btnReplay;
	private View _viewContactNotSelected;
	private View _viewContactInfo;
	private ImageView _imgMessageStatus;
	private TextView _txtMessageName;
	private TextView _txtMessageStatus;
	private View _viewAudioMode;
	private ToggleButton _btnSpeaker, _btnEarpiece, _btnBluetooth;
	private View _viewMessageInfo;
	private Button _btnLogin;
	private TextView _txtError;

	private boolean _active; // Activity is resumed and visible to the user
	private boolean _dirtyContacts; // Contact list needs to be refreshed next time before it's presented to the user
	private com.zello.sdk.Contact _contextContact; // Contact for which currently active context menu is being displayed

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

		final Object event = new Object();

		new Thread() {
			@Override
			public void run() {
				// Do lengthy operation
				event.notify();
			}
		}.run();

		new Thread() {
			@Override
			public void run() {
				// Wait for the lengthy operation above to complete
				event.wait();
				Toast.makeText(TalkActivity.this, "Job complete", Toast.LENGTH_SHORT).show();
			}
		}.start();



		setContentView(R.layout.activity_talk);
		_viewState = findViewById(R.id.state_screen);
		_txtState = _viewState.findViewById(R.id.state);
		_btnCancel = _viewState.findViewById(R.id.cancel);
		_viewLogin = findViewById(R.id.login_screen);
		_txtNetwork = _viewLogin.findViewById(R.id.network_label);
		_editUsername = _viewLogin.findViewById(R.id.username);
		_editPassword = _viewLogin.findViewById(R.id.password);
		_editNetwork = _viewLogin.findViewById(R.id.network);
		_checkPerishable = _viewLogin.findViewById(R.id.perishable);
		_viewContent = findViewById(R.id.content_screen);
		_listContacts = _viewContent.findViewById(R.id.contact_list);
		_viewTalkScreen = _viewContent.findViewById(R.id.talk_screen);
		_viewContactInfo = _viewTalkScreen.findViewById(R.id.contact_info);
		_btnTalk = _viewTalkScreen.findViewById(R.id.talk);
		_btnReplay = findViewById(R.id.button_replay);
		_btnConnect = findViewById(R.id.button_connect);
		_viewContactNotSelected = _viewTalkScreen.findViewById(R.id.contact_not_selected);
		_viewAudioMode = findViewById(R.id.audio_mode);
		_btnSpeaker = _viewAudioMode.findViewById(R.id.audio_speaker);
		_btnEarpiece = _viewAudioMode.findViewById(R.id.audio_earpiece);
		_btnBluetooth = _viewAudioMode.findViewById(R.id.audio_bluetooth);
		_viewMessageInfo = findViewById(R.id.message_info);
		_imgMessageStatus = _viewMessageInfo.findViewById(R.id.message_image);
		_txtMessageName = _viewMessageInfo.findViewById(R.id.message_name);
		_txtMessageStatus = _viewMessageInfo.findViewById(R.id.message_status);
		_btnLogin = _viewLogin.findViewById(R.id.login);
		_txtError = _viewLogin.findViewById(R.id.error);

		// PTT button push/release handler
		_btnTalk.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				if (action == MotionEvent.ACTION_DOWN) {
					Zello.getInstance().beginMessage();
				} else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
					Zello.getInstance().endMessage();
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
						Zello.getInstance().setSelectedContact(contact);
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
				Zello.getInstance().setSelectedContact(null);
			}
		});

		// Login credentials
		_editUsername.setText(Helper.loadValue(this, _keyUsername));
		_editPassword.setText(Helper.loadValue(this, _keyPassword));
		_editNetwork.setText(Helper.loadValue(this, _keyNetwork));
		_editPassword = _viewLogin.findViewById(R.id.password);
		_editNetwork = _viewLogin.findViewById(R.id.network);
		_btnLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String username = _editUsername.getText().toString();
				String password = _editPassword.getText().toString();
				String network = _editNetwork.getText().toString();
				boolean perishable = _checkPerishable.isChecked();
				Helper.saveValue(TalkActivity.this, _keyUsername, username);
				Helper.saveValue(TalkActivity.this, _keyPassword, password);
				Helper.saveValue(TalkActivity.this, _keyNetwork, network);
				Zello.getInstance().signIn(network, username, password, perishable);
			}
		});
		_btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (_appState.isReconnecting() || _appState.isWaitingForNetwork() || (_appState.isSigningIn() && !_appState.isCancellingSignin())) {
					Zello.getInstance().cancelSignIn();
				}
			}
		});
		_btnReplay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Zello.getInstance().replayLastIncomingMessage();
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

		// Use to connect to an app installed from an apk obtained from https://www.zellowork.com
		//Zello.getInstance().configure("net.loudtalks", this);

		// Use with an app installed from a generic PTT SDK apk obtained from https://github.com/zelloptt/zello-android-client-sdk/releases
		Zello.getInstance().configure("com.pttsdk", this);

		Zello zello = Zello.getInstance();
		zello.requestVitalPermissions(this);
		zello.subscribeToEvents(this);
		zello.setShowBluetoothAccessoriesNotifications(false);
		_audio = zello.getAudio();

		updateAppState();
		updateAudioMode();
		updateMessageState();
		updateSelectedContact();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Zello zello = Zello.getInstance();
		zello.unsubscribeFromEvents(this);
		zello.unconfigure();
		_audio = null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		Zello.getInstance().leavePowerSavingMode();
		_active = true;
		updateContactList();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Zello.getInstance().enterPowerSavingMode();
		_active = false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (_appState.isAvailable() && !_appState.isInitializing()) {
			getMenuInflater().inflate(R.menu.menu, menu);
			boolean select = false, available = false, solo = false, busy = false;
			if (!_appState.isConfiguring() && _appState.isSignedIn() && !_appState.isSigningIn() && !_appState.isSigningOut()) {
				com.zello.sdk.Status status = _appState.getStatus();
				select = true;
				available = status == com.zello.sdk.Status.AVAILABLE;
				solo = status == com.zello.sdk.Status.SOLO;
				busy = status == com.zello.sdk.Status.BUSY;
			}
			showMenuItem(menu, R.id.menu_select_contact, select);
			showMenuItem(menu, R.id.menu_lock_ptt_app, !_appState.isLocked());
			showMenuItem(menu, R.id.menu_unlock_ptt_app, _appState.isLocked());
			showMenuItem(menu, R.id.menu_enable_auto_run, !_appState.isAutoRunEnabled());
			showMenuItem(menu, R.id.menu_disable_auto_run, _appState.isAutoRunEnabled());
			showMenuItem(menu, R.id.menu_enable_auto_connect_channels, !_appState.isChannelAutoConnectEnabled());
			showMenuItem(menu, R.id.menu_disable_auto_connect_channels, _appState.isChannelAutoConnectEnabled());
			showMenuItem(menu, R.id.menu_about, true);
			showMenuItem(menu, R.id.menu_available, available);
			showMenuItem(menu, R.id.menu_solo, solo);
			showMenuItem(menu, R.id.menu_busy, busy);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
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
				Zello.getInstance().beginMessage();
				return true;
			}
			case R.id.menu_stop_message: {
				Zello.getInstance().endMessage();
				return true;
			}
			case R.id.menu_ptt_buttons: {
				openPttButtonsScreen();
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
							Zello.getInstance().setSelectedContact(_contextContact);
							break;
						}
						case R.id.menu_mute: {
							Zello.getInstance().muteContact(_contextContact, true);
							break;
						}
						case R.id.menu_unmute: {
							Zello.getInstance().muteContact(_contextContact, false);
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
		updateAppState();
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

	@Override
	public void onMicrophonePermissionNotGranted() {
		if (_active) {
			Zello.getInstance().showMicrophonePermissionDialog(this);
		}
	}

	@Override
	public void onBluetoothAccessoryStateChanged(BluetoothAccessoryType type, BluetoothAccessoryState state, String name, String description) {
		Toast.makeText(this, description, Toast.LENGTH_SHORT).show();
	}

	private void showMenuItem(Menu menu, int itemId, boolean show) {
		MenuItem item = menu.findItem(itemId);
		if (item != null) {
			item.setVisible(show);
		}
	}

	private void openPttButtonsScreen() {
		Zello.getInstance().showPttButtonsScreen(this);
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

		// Since Zello was initialized in the Activity, pass in this as Activity parameter
		Zello.getInstance().selectContact(title, tabs, tab, theme, this);
	}

	private void lockPttApp() {
		// Configure PTT app to display information screen with the name of this app that can be clicked to open main activity
		Zello.getInstance().lock(getString(R.string.app_name), getPackageName());
	}

	private void unlockPttApp() {
		// Switch PTT app back to normal UI mode
		Zello.getInstance().unlock();
	}

	private void enableAutoRun() {
		// Enable client auto-run option
		Zello.getInstance().setAutoRun(true);
	}

	private void disableAutoRun() {
		// Disable client auto-run option
		Zello.getInstance().setAutoRun(false);
	}

	private void enableAutoConnectChannels() {
		// Enable auto-connecting to new channels
		Zello.getInstance().setAutoConnectChannels(true);
	}

	private void disableAutoConnectChannels() {
		// Disable auto-connecting to new channels
		Zello.getInstance().setAutoConnectChannels(false);
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
						Zello.getInstance().setStatus(com.zello.sdk.Status.AVAILABLE);
						break;
					}
					case 1: {
						Zello.getInstance().setStatus(com.zello.sdk.Status.SOLO);
						break;
					}
					case 2: {
						Zello.getInstance().setStatus(com.zello.sdk.Status.BUSY);
						break;
					}
					case 3: {
						Zello.getInstance().signOut();
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
		Zello.getInstance().getSelectedContact(_selectedContact);
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
				case CHANNEL:
				case GROUP: {
					showConnect = !_selectedContact.getNoDisconnect();
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
			}
		}
		_viewContactNotSelected.setVisibility(selected ? View.INVISIBLE : View.VISIBLE);
		_viewContactInfo.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);
		_btnTalk.setEnabled(canTalk);
		_btnConnect.setEnabled(canConnect);
		_btnConnect.setChecked(connected);
		_btnConnect.setVisibility(showConnect ? View.VISIBLE : View.GONE);
		updateAppState();
	}

	private void updateMessageState() {
		Zello.getInstance().getMessageIn(_messageIn);
		Zello.getInstance().getMessageOut(_messageOut);
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

	private void updateAppState() {
		Zello.getInstance().getAppState(_appState);
		int stateVisibility = View.GONE;
		int loginVisibility = View.GONE;
		int contentVisibility = View.GONE;
		if (!_appState.isAvailable() || _appState.isInitializing() || _appState.isConfiguring()) {
			stateVisibility = View.VISIBLE;
		} else if (!_appState.isSignedIn()) {
			if (_appState.isSigningIn() || _appState.isSigningOut() || _appState.isWaitingForNetwork() || _appState.isReconnecting()) {
				stateVisibility = View.VISIBLE;
			} else {
				loginVisibility = View.VISIBLE;
			}
		} else {
			contentVisibility = View.VISIBLE;
		}

		if (stateVisibility == View.VISIBLE) {
			updateStateScreen();
		} else if (loginVisibility == View.VISIBLE) {
			updateLoginScreen();
		} else {
			updateContentScreen();
		}
		_viewState.setVisibility(stateVisibility);
		_viewLogin.setVisibility(loginVisibility);
		_viewContent.setVisibility(contentVisibility);
		invalidateOptionsMenu();
	}

	private void updateStateScreen() {
		String error = null, state = "";
		boolean cancelShow = false, cancelEnable = true;
		if (!_appState.isAvailable()) {
			state = getString(R.string.ptt_app_not_installed);
		} else if (_appState.isInitializing()) {
			state = getString(R.string.ptt_app_initializing);
		} else if (_appState.isConfiguring()) {
			state = getString(R.string.ptt_app_configuring);
		} else if (!_appState.isSignedIn()) {
			if (_appState.isSigningIn()) {
				state = getString(R.string.ptt_app_is_signing_in);
				cancelShow = true;
				cancelEnable = !_appState.isCancellingSignin();
			} else if (_appState.isSigningOut()) {
				state = getString(R.string.ptt_app_is_signing_out);
			} else if (_appState.isWaitingForNetwork()) {
				error = getErrorText(_appState.getLastError());
				state = getString(R.string.ptt_app_is_waiting_to_reconnect);
				cancelShow = true;
			} else if (_appState.isReconnecting()) {
				error = getErrorText(_appState.getLastError());
				state = getString(R.string.ptt_app_is_reconnecting).replace("%seconds%", NumberFormat.getInstance().format(_appState.getReconnectTimer()));
				cancelShow = true;
			} else {
				state = getString(R.string.ptt_app_is_signed_out);
			}
		}
		_txtState.setText(error != null ? (error + "\n" + state) : state);
		_btnCancel.setEnabled(cancelEnable);
		_btnCancel.setVisibility(cancelShow ? View.VISIBLE : View.GONE);
	}

	private void updateLoginScreen() {
		// Network URL controls are only used when PTT app is not pre-configured to work with a particular network
		int networkFlag = _appState.isCustomBuild() ? View.GONE : View.VISIBLE;
		String error = getErrorText(_appState.getLastError());
		_txtNetwork.setVisibility(networkFlag);
		_editNetwork.setVisibility(networkFlag);
		_txtError.setVisibility(error == null ? View.GONE : View.VISIBLE);
		_txtError.setText(error);
	}

	private void updateContentScreen() {
		int listVisibility = View.GONE;
		int talkVisibility = View.GONE;
		String name = _selectedContact.getName();
		if (name != null && name.length() != 0) {
			talkVisibility = View.VISIBLE;
		} else {
			listVisibility = View.VISIBLE;
		}
		_listContacts.setVisibility(listVisibility);
		_viewTalkScreen.setVisibility(talkVisibility);
		if (listVisibility == View.VISIBLE) {
			updateContactList();
		}
		if (talkVisibility == View.VISIBLE) {
			updateTalkScreen();
		}
	}

	private void updateTalkScreen() {
		_btnReplay.setVisibility(Zello.getInstance().isLastMessageReplayAvailable() ? View.VISIBLE : View.GONE);
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
			adapter.setContacts(Zello.getInstance().getContacts());
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
		com.zello.sdk.ContactType type = _selectedContact.getType();
		if (type == com.zello.sdk.ContactType.CHANNEL || type == com.zello.sdk.ContactType.GROUP) {
			com.zello.sdk.ContactStatus status = _selectedContact.getStatus();
			if (status == com.zello.sdk.ContactStatus.OFFLINE) {
				Zello.getInstance().connectChannel(_selectedContact.getName());
			} else if (status == com.zello.sdk.ContactStatus.AVAILABLE) {
				Zello.getInstance().disconnectChannel(_selectedContact.getName());
			}
		}
	}

	private String getErrorText(com.zello.sdk.Error error) {
		switch (error) {
			case UNKNOWN:
				return getResources().getString(R.string.error_unknown);
			case INVALID_CREDENTIALS:
				return getResources().getString(R.string.error_invalid_credentials);
			case INVALID_NETWORK_NAME:
				return getResources().getString(R.string.error_invalid_network_name);
			case NETWORK_SUSPENDED:
				return getResources().getString(R.string.error_network_suspended);
			case SERVER_SECURE_CONNECT_FAILED:
				return getResources().getString(R.string.error_secure_connect_failed);
			case SERVER_SIGNIN_FAILED:
				return getResources().getString(R.string.error_server_signin_failed);
			case NETWORK_SIGNIN_FAILED:
				return getResources().getString(R.string.error_network_signin_failed);
			case KICKED:
				return getResources().getString(R.string.error_kicked);
			case APP_UPDATE_REQUIRED:
				return getResources().getString(R.string.error_update_required);
			case NO_INTERNET_CONNECTION:
				return getResources().getString(R.string.error_no_internet);
			case INTERNET_CONNECTION_RESTRICTED:
				return getResources().getString(R.string.error_internet_restricted);
			case SERVER_LICENSE_PROBLEM:
				return getResources().getString(R.string.error_server_license);
			case TOO_MANY_SIGNIN_ATTEMPTS:
				return getResources().getString(R.string.error_brute_force_protection);
			default:
				return null;
		}
	}

}
