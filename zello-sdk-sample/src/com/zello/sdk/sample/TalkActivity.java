package com.zello.sdk.sample;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import com.zello.sdk.Theme;

public class TalkActivity extends Activity implements com.zello.sdk.Events {

	private TextView _txtAppState;
	private View _viewTalkScreen;
	private SquareButton _btnTalk;
	private ImageView _imgContactStatus;
	private TextView _txtContactName;
	private TextView _txtContactStatus;
	private View _viewContactNotSelected;
	private View _viewContactInfo;
	private ImageView _imgMessageStatus;
	private TextView _txtMessageName;
	private TextView _txtMessageStatus;
	private View _viewMessageInfo;

	private com.zello.sdk.Sdk _sdk = new com.zello.sdk.Sdk();
	private com.zello.sdk.AppState _appState = new com.zello.sdk.AppState();
	private com.zello.sdk.MessageIn _messageIn = new com.zello.sdk.MessageIn();
	private com.zello.sdk.MessageOut _messageOut = new com.zello.sdk.MessageOut();
	private com.zello.sdk.Contact _selectedContact = new com.zello.sdk.Contact();
	private com.zello.sdk.Tab _activeTab = com.zello.sdk.Tab.RECENTS;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_talk);
		_txtAppState = (TextView) findViewById(R.id.app_state);
		_viewTalkScreen = findViewById(R.id.talk_screen);
		_btnTalk = (SquareButton) findViewById(R.id.talk);
		_imgContactStatus = (ImageView) findViewById(R.id.contact_image);
		_txtContactName = (TextView) findViewById(R.id.contact_name);
		_txtContactStatus = (TextView) findViewById(R.id.contact_status);
		_viewContactNotSelected = findViewById(R.id.contact_not_selected);
		_viewContactInfo = findViewById(R.id.contact_info);
		_imgMessageStatus = (ImageView) findViewById(R.id.message_image);
		_txtMessageName = (TextView) findViewById(R.id.message_name);
		_txtMessageStatus = (TextView) findViewById(R.id.message_status);
		_viewMessageInfo = findViewById(R.id.message_info);

		_btnTalk.setMaxHeight(getResources().getDimensionPixelSize(R.dimen.talk_button_size));
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

		findViewById(R.id.select_contact).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				chooseActiveContact();
			}
		});

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
	}

	@Override
	protected void onPause() {
		super.onPause();
		_sdk.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (_appState.isAvailable() && _appState.isSignedIn() && !_appState.isSigningIn() && !_appState.isSigningOut()) {
			getMenuInflater().inflate(R.menu.menu, menu);
			if (_appState.isBusy()) {
				menu.findItem(R.id.menu_set_busy).setVisible(false);
			} else {
				menu.findItem(R.id.menu_set_available).setVisible(false);
			}
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_set_busy: {
				_sdk.setStatusBusy(true);
				return true;
			}
			case R.id.menu_set_available: {
				_sdk.setStatusBusy(false);
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

	private void chooseActiveContact() {
		// Activity title; optional
		String title = getResources().getString(R.string.select_contact_title);
		// Set of displayed tabs; required; any combination of RECENTS, USERS and CHANNELS
		com.zello.sdk.Tab[] tabs = new com.zello.sdk.Tab[]{com.zello.sdk.Tab.RECENTS, com.zello.sdk.Tab.USERS, com.zello.sdk.Tab.CHANNELS};
		// Initially active tab; optional; can be RECENTS, USERS or CHANNELS
		com.zello.sdk.Tab tab = _activeTab;
		// Visual theme; optional; can be DARK or LIGHT
		com.zello.sdk.Theme theme = Theme.DARK;

		_sdk.selectContact(title, tabs, tab, theme);
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
			String displayName = _selectedContact.getDisplayName(); // Contact name or a full name if not empty
			com.zello.sdk.ContactType type = _selectedContact.getType();
			com.zello.sdk.ContactStatus status = _selectedContact.getStatus();
			_imgContactStatus.setImageDrawable(statusToDrawable(status, type));
			_txtContactName.setText(displayName);
			switch (type) {
				case USER:
				case GATEWAY: {
					// User or radio gateway
					canTalk = status != com.zello.sdk.ContactStatus.OFFLINE; // Not offline
					String message = _selectedContact.getStatusMessage(); // User-defined status message
					_txtContactStatus.setText(message == null || message.length() == 0 ? statusToText(status) : message);
					break;
				}
				case CHANNEL: {
					if (status == com.zello.sdk.ContactStatus.AVAILABLE) {
						int count = _selectedContact.getUsersCount();
						String countText = Integer.toString(count);
						canTalk = true; // Channel is online
						_txtContactStatus.setText(getResources().getString(R.string.status_channel_users_count).replace("%count%", countText));
					} else {
						_txtContactStatus.setText(statusToText(status));
					}
					break;
				}
				case GROUP: {
					int count = _selectedContact.getUsersCount();
					if (status == com.zello.sdk.ContactStatus.AVAILABLE && count > 0) {
						int total = _selectedContact.getUsersTotal();
						String countText = Integer.toString(count);
						String totalText = Integer.toString(total);
						canTalk = true; // Group is online and there are online contacts in it
						_txtContactStatus.setText(getResources().getString(R.string.status_group_users_count).replace("%count%", countText).replace("%total%", totalText));
					} else {
						_txtContactStatus.setText(statusToText(com.zello.sdk.ContactStatus.OFFLINE));
					}
					break;
				}
			}
		}
		_viewContactNotSelected.setVisibility(selected ? View.INVISIBLE : View.VISIBLE);
		_viewContactInfo.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);
		_btnTalk.setEnabled(canTalk);
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
		boolean available = false;
		if (!_appState.isAvailable()) {
			state = getString(R.string.ptt_app_not_installed);
		} else {
			if (_appState.isSignedIn()) {
				available = true;
			} else if (_appState.isSigningIn()) {
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
		if (available) {
			_txtAppState.setVisibility(View.GONE);
			_viewTalkScreen.setVisibility(View.VISIBLE);
		} else {
			_txtAppState.setText(state);
			_txtAppState.setVisibility(View.VISIBLE);
			_viewTalkScreen.setVisibility(View.GONE);
		}
		Helper.invalidateOptionsMenu(this);
	}

	private String statusToText(com.zello.sdk.ContactStatus status) {
		int id = R.string.status_offline;
		switch (status) {
			case STANDBY:
				id = R.string.status_standby;
				break;
			case AVAILABLE:
				id = R.string.status_online;
				break;
			case BUSY:
				id = R.string.status_busy;
				break;
			case CONNECTING:
				id = R.string.status_connecting;
				break;
			default:
		}
		return getResources().getString(id);
	}

	private Drawable statusToDrawable(com.zello.sdk.ContactStatus status, com.zello.sdk.ContactType type) {
		int id = 0;
		switch (type) {
			case USER: {
				// User
				switch (status) {
					case STANDBY:
						id = R.drawable.user_standby;
						break;
					case AVAILABLE:
						id = R.drawable.user_online;
						break;
					case BUSY:
						id = R.drawable.user_busy;
						break;
					default:
						id = R.drawable.user_offline;
				}
				break;
			}
			case CHANNEL: {
				// Channel
				switch (status) {
					case AVAILABLE:
						id = R.drawable.channel_online;
						break;
					default:
						id = R.drawable.channel_offline;
				}
				break;
			}
			case GATEWAY: {
				// Radio gateway
				id = R.drawable.gateway_online;
				break;
			}
			case GROUP: {
				// Group
				id = R.drawable.group_online;
				break;
			}
		}
		return getResources().getDrawable(id);
	}

}
