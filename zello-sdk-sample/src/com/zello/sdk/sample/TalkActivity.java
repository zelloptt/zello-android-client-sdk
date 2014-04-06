package com.zello.sdk.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.*;
import android.widget.*;

public class TalkActivity extends Activity implements com.zello.sdk.Events {

	private TextView _txtState;
	private View _viewLogin;
	private EditText _editUsername;
	private EditText _editPassword;
	private EditText _editNetwork;
	private View _viewContent;
	private ListView _listContacts;
	private View _viewTalkScreen;
	private SquareButton _btnTalk;
	private View _viewContactNotSelected;
	private View _viewContactInfo;
	private ImageView _imgMessageStatus;
	private TextView _txtMessageName;
	private TextView _txtMessageStatus;
	private View _viewMessageInfo;

	private boolean _active; // Activity is resumed and visible to the user
	private boolean _dirtyContacts; // Contact list needs to be refreshed next time before it's presented to the user

	private com.zello.sdk.Sdk _sdk = new com.zello.sdk.Sdk();
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
		_editUsername = (EditText) _viewLogin.findViewById(R.id.username);
		_editPassword = (EditText) _viewLogin.findViewById(R.id.password);
		_editNetwork = (EditText) _viewLogin.findViewById(R.id.network);
		_viewContent = findViewById(R.id.content);
		_listContacts = (ListView) _viewContent.findViewById(R.id.contact_list);
		_viewTalkScreen = _viewContent.findViewById(R.id.talk_screen);
		_viewContactInfo = _viewTalkScreen.findViewById(R.id.contact_info);
		_btnTalk = (SquareButton) _viewTalkScreen.findViewById(R.id.talk);
		_viewContactNotSelected = _viewTalkScreen.findViewById(R.id.contact_not_selected);
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

		_dirtyContacts = true;
		_sdk.onCreate("com.pttsdk", this, this);
		updateAppState();
		updateMessageState();
		updateSelectedContact();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		_sdk.onDestroy();
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
		if (_appState.isAvailable() && !_appState.isConfiguring() && _appState.isSignedIn() && !_appState.isSigningIn() && !_appState.isSigningOut()) {
			getMenuInflater().inflate(R.menu.menu, menu);
			com.zello.sdk.Status status = _appState.getStatus();
			MenuItem itemAvailable = menu.findItem(R.id.menu_available);
			if (itemAvailable != null) {
				itemAvailable.setVisible(status == com.zello.sdk.Status.AVAILABLE);
			}
			MenuItem itemSolo = menu.findItem(R.id.menu_solo);
			if (itemSolo != null) {
				itemSolo.setVisible(status == com.zello.sdk.Status.SOLO);
			}
			MenuItem itemBusy = menu.findItem(R.id.menu_busy);
			if (itemBusy != null) {
				itemBusy.setVisible(status == com.zello.sdk.Status.BUSY);
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
		}
		return false;
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
		boolean canTalk = false;
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
					if (status == com.zello.sdk.ContactStatus.AVAILABLE) {
						canTalk = true; // Channel is online
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

	private void updateActiveScreen() {
		int stateFlag = View.GONE;
		int loginFlag = View.GONE;
		int contentFlag = View.GONE;
		int listFlag = View.GONE;
		int talkFlag = View.GONE;
		if (!_appState.isAvailable() || _appState.isConfiguring()) {
			stateFlag = View.VISIBLE;
		} else if (!_appState.isSignedIn()) {
			if (_appState.isSigningIn() || _appState.isSigningOut() || _appState.isWaitingForNetwork() || _appState.isReconnecting()) {
				stateFlag = View.VISIBLE;
			} else {
				loginFlag = View.VISIBLE;
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

}
