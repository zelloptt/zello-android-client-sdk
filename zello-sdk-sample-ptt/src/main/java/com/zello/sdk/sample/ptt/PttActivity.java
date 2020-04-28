package com.zello.sdk.sample.ptt;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.zello.sdk.AppState;
import com.zello.sdk.Audio;
import com.zello.sdk.AudioMode;
import com.zello.sdk.BluetoothAccessoryState;
import com.zello.sdk.BluetoothAccessoryType;
import com.zello.sdk.Contact;
import com.zello.sdk.ContactStatus;
import com.zello.sdk.ContactType;
import com.zello.sdk.MessageIn;
import com.zello.sdk.MessageOut;
import com.zello.sdk.Tab;
import com.zello.sdk.Theme;
import com.zello.sdk.Zello;

public class PttActivity extends AppCompatActivity implements com.zello.sdk.Events {

	private boolean _active;

	private TextView _statusTextView;
	private ToggleButton _connectChannelButton;
	private Button _pttButton;
	private NoToggleButton _speakerButton;
	private NoToggleButton _earpieceButton;
	private NoToggleButton _bluetoothButton;
	private View _audioModeView;
	private TextView _messageStateTextView;
	private TextView _selectedContactTextView;

	private AppState _appState = new AppState();
	private Audio _audio;
	private Contact _selectedContact = new Contact();
	private MessageIn _messageIn = new MessageIn();
	private MessageOut _messageOut = new MessageOut();
	private Tab _activeTab = Tab.RECENTS;

	//region Lifecycle Methods

	@SuppressLint("ClickableViewAccessibility")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_ptt);

		_statusTextView = findViewById(R.id.statusTextView);
		_connectChannelButton = findViewById(R.id.connectChannelButton);
		_pttButton = (SquareButton) findViewById(R.id.pushToTalkButton);
		_speakerButton = findViewById(R.id.audio_speaker);
		_earpieceButton = findViewById(R.id.audio_earpiece);
		_bluetoothButton = findViewById(R.id.audio_bluetooth);
		_audioModeView = findViewById(R.id.audio_mode);
		_messageStateTextView = findViewById(R.id.messageStateTextView);
		_selectedContactTextView = findViewById(R.id.selectedContactTextView);

		// Use to connect to an app installed from an apk obtained from https://www.zellowork.com
		//Zello.getInstance().configure("net.loudtalks", this, this);

		// Use with an app installed from a generic PTT SDK apk obtained from https://github.com/zelloptt/zello-android-client-sdk/releases
		Zello.getInstance().configure("com.pttsdk", this, this);

		Zello.getInstance().requestVitalPermissions(this);
		_audio = Zello.getInstance().getAudio();

		_connectChannelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onConnectChannelButtonPressed();
			}
		});

		// PTT button push/release handler
		_pttButton.setOnTouchListener(new View.OnTouchListener() {
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

		// Audio modes
		_speakerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (_audio != null) {
					_audio.setMode(AudioMode.SPEAKER);
				}
			}
		});

		_earpieceButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (_audio != null) {
					_audio.setMode(AudioMode.EARPIECE);
				}
			}
		});

		_bluetoothButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (_audio != null) {
					_audio.setMode(AudioMode.BLUETOOTH);
				}
			}
		});

		updateAudioMode();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		Zello.getInstance().unsubscribeFromEvents(this);
		Zello.getInstance().unconfigure();
	}

	@Override
	protected void onResume() {
		super.onResume();

		_active = true;
		Zello.getInstance().leavePowerSavingMode();
	}

	@Override
	protected void onPause() {
		super.onPause();

		_active = false;
		Zello.getInstance().enterPowerSavingMode();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		menu.clear();

		Zello.getInstance().getAppState(_appState);

		if (_appState.isSignedIn()) {
			Zello.getInstance().getSelectedContact(_selectedContact);

			getMenuInflater().inflate(R.menu.menu, menu);
			showMenuItem(menu, R.id.menu_mute_contact, _selectedContact.getDisplayName() != null);
			showMenuItem(menu, R.id.menu_replay, Zello.getInstance().isLastMessageReplayAvailable());
			showMenuItem(menu, R.id.menu_select_contact, !_appState.isSigningIn() && !_appState.isSigningOut());
			setMenuItemText(menu, R.id.menu_mute_contact, getResources().getString(_selectedContact.getMuted() ? R.string.unmute_contact : R.string.mute_contact));
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_mute_contact:
				Zello.getInstance().getSelectedContact(_selectedContact);
				Zello.getInstance().muteContact(_selectedContact, !_selectedContact.getMuted());
				return true;
			case R.id.menu_replay:
				Zello.getInstance().replayLastIncomingMessage();
				return true;
			case R.id.menu_select_contact:
				chooseActiveContact();
				return true;
		}
		return false;
	}

	//endregion

	//region Zello SDK Events

	@Override
	public void onAppStateChanged() {
		updateUI();
	}

	@Override
	public void onMessageStateChanged() {
		updateUIForAvailableContact();

		Zello.getInstance().getMessageIn(_messageIn);
		Zello.getInstance().getMessageOut(_messageOut);

		boolean incoming = _messageIn.isActive(); // Is the incoming message active?
		boolean outgoing = _messageOut.isActive(); // Is the outgoing message active?

		String text = "";
		if (incoming) {
			String author = _messageIn.getAuthor().getDisplayName(); // Is the message from a channel?

			if (author != null && author.length() > 0) {
				text = getResources().getString(R.string.receiving_message_channel_author, _messageIn.getFrom().getDisplayName(), author); // Show channel and author names
			} else {
				text = getResources().getString(R.string.receiving_message_sender, _messageIn.getFrom().getDisplayName()); // Show sender name
			}
		} else if (outgoing) {
			if (_messageOut.isConnecting()) {
				text = getResources().getString(R.string.connecting_to_contact, _selectedContact.getDisplayName());
			} else {
				text = getResources().getString(R.string.outgoing_message_to_contact, _selectedContact.getDisplayName());
			}
		}
		_messageStateTextView.setText(text);
	}

	@Override
	public void onSelectedContactChanged() {
		updateUI();
	}

	@Override
	public void onAudioStateChanged() {
		updateAudioMode();
	}

	@Override
	public void onContactsChanged() {
	}

	@Override
	public void onLastContactsTabChanged(Tab tab) {
		_activeTab = tab;
	}

	@Override
	public void onMicrophonePermissionNotGranted() {
		if (_active) {
			Zello.getInstance().showMicrophonePermissionDialog(this);
		}
	}

	@Override
	public void onBluetoothAccessoryStateChanged(BluetoothAccessoryType bluetoothAccessoryType, BluetoothAccessoryState bluetoothAccessoryState, String s, String s1) {
	}

	//endregion

	private void updateUI() {
		Zello.getInstance().getAppState(_appState);

		if (_appState.isSignedIn()) {
			Zello.getInstance().getSelectedContact(_selectedContact);

			if (_selectedContact.getDisplayName() != null) {
				updateUIForAvailableContact();
			} else {
				updateUIForNoContact();
			}
		} else if (_appState.isSigningIn()) {
			updateUIForSigningIn();
		} else {
			updateUIForDisconnected();
		}
	}

	private void updateUIForAvailableContact() {
		invalidateOptionsMenu();

		_statusTextView.setVisibility(View.GONE);

		_pttButton.setVisibility(View.VISIBLE);
		_pttButton.setEnabled(getCanTalk());
		_audioModeView.setVisibility(View.VISIBLE);
		_selectedContactTextView.setVisibility(View.VISIBLE);
		_selectedContactTextView.setText(getResources().getString(R.string.selected_contact, _selectedContact.getDisplayName()));

		updateConnectChannelButton();
		updateAudioMode();
	}

	private void updateUIForNoContact() {
		invalidateOptionsMenu();

		_statusTextView.setVisibility(View.VISIBLE);

		_pttButton.setVisibility(View.INVISIBLE);
		_audioModeView.setVisibility(View.INVISIBLE);
		_speakerButton.setVisibility(View.INVISIBLE);
		_bluetoothButton.setVisibility(View.INVISIBLE);
		_earpieceButton.setVisibility(View.INVISIBLE);
		_connectChannelButton.setVisibility(View.INVISIBLE);
		_selectedContactTextView.setVisibility(View.INVISIBLE);

		_statusTextView.setText(R.string.no_selected_contact);
	}

	private void updateUIForSigningIn() {
		invalidateOptionsMenu();

		_statusTextView.setVisibility(View.VISIBLE);

		_pttButton.setVisibility(View.INVISIBLE);
		_audioModeView.setVisibility(View.INVISIBLE);
		_speakerButton.setVisibility(View.INVISIBLE);
		_bluetoothButton.setVisibility(View.INVISIBLE);
		_earpieceButton.setVisibility(View.INVISIBLE);
		_connectChannelButton.setVisibility(View.INVISIBLE);
		_selectedContactTextView.setVisibility(View.INVISIBLE);

		_statusTextView.setText(R.string.sign_in_status_signing_in);
	}

	private void updateUIForDisconnected() {
		invalidateOptionsMenu();

		_statusTextView.setVisibility(View.VISIBLE);

		_pttButton.setVisibility(View.INVISIBLE);
		_audioModeView.setVisibility(View.INVISIBLE);
		_connectChannelButton.setVisibility(View.INVISIBLE);
		_selectedContactTextView.setVisibility(View.INVISIBLE);

		_statusTextView.setText(R.string.sign_in_status_offline);
	}

	private void showMenuItem(Menu menu, int itemId, boolean show) {
		MenuItem item = menu.findItem(itemId);
		if (item != null) {
			item.setVisible(show);
		}
	}

	@SuppressWarnings("SameParameterValue")
	private void setMenuItemText(Menu menu, int itemId, String text) {
		MenuItem item = menu.findItem(itemId);
		if (item != null) {
			item.setTitle(text);
		}
	}

	private void updateAudioMode() {
		boolean speaker = false, earpiece = false, bluetooth = false;
		AudioMode mode = AudioMode.SPEAKER;

		// Can't set new mode while the mode is being changed
		boolean currentlyChangingMode = false;
		if (_audio != null) {
			speaker = _audio.isModeAvailable(AudioMode.SPEAKER);
			earpiece = _audio.isModeAvailable(AudioMode.EARPIECE);
			bluetooth = _audio.isModeAvailable(AudioMode.BLUETOOTH);
			mode = _audio.getMode();
			currentlyChangingMode = _audio.isModeChanging();
		}

		// If none of the modes is available, the client app is old and needs to be updated
		if (bluetooth || earpiece || speaker) {
			_speakerButton.setVisibility(speaker ? View.VISIBLE : View.GONE);
			if (speaker) {
				_speakerButton.setChecked(mode == AudioMode.SPEAKER);
				_speakerButton.setEnabled(!currentlyChangingMode && (earpiece || bluetooth));
			}

			_earpieceButton.setVisibility(earpiece ? View.VISIBLE : View.GONE);
			if (earpiece) {
				_earpieceButton.setChecked(mode == AudioMode.EARPIECE);
				_earpieceButton.setEnabled(!currentlyChangingMode && (speaker || bluetooth));
			}

			_bluetoothButton.setVisibility(bluetooth ? View.VISIBLE : View.GONE);
			if (bluetooth) {
				_bluetoothButton.setChecked(mode == AudioMode.BLUETOOTH);
				_bluetoothButton.setEnabled(!currentlyChangingMode && (speaker || earpiece));
			}
		}

		_audioModeView.setVisibility(speaker || earpiece || bluetooth ? View.VISIBLE : View.GONE);
	}

	private void onConnectChannelButtonPressed() {
		ContactType type = _selectedContact.getType();
		if (type == ContactType.CHANNEL || type == ContactType.GROUP) {
			ContactStatus status = _selectedContact.getStatus();
			if (status == ContactStatus.OFFLINE) {
				Zello.getInstance().connectChannel(_selectedContact.getName());
			} else if (status == ContactStatus.AVAILABLE) {
				Zello.getInstance().disconnectChannel(_selectedContact.getName());
			}
		}
	}

	private void updateConnectChannelButton() {
		boolean showConnect = false, connected = false, canConnect = false;

		ContactType type = _selectedContact.getType();
		ContactStatus status = _selectedContact.getStatus();
		switch (type) {
			case USER:
			case GATEWAY:
			case CHANNEL:
			case GROUP:
				showConnect = !_selectedContact.getNoDisconnect();
				if (status == ContactStatus.AVAILABLE) {
					canConnect = true;
					connected = true;
				} else if (status == ContactStatus.OFFLINE) {
					canConnect = true;
				} else if (status == ContactStatus.CONNECTING) {
					connected = true;
				}
				break;
		}

		_connectChannelButton.setEnabled(canConnect);
		_connectChannelButton.setChecked(connected);
		_connectChannelButton.setVisibility(showConnect ? View.VISIBLE : View.GONE);
	}

	private void chooseActiveContact() {
		// Activity title; optional
		String title = getResources().getString(R.string.select_contact_title);
		// Set of displayed tabs; required; any combination of RECENTS, USERS and CHANNELS
		Tab[] tabs = new Tab[]{Tab.RECENTS, Tab.USERS, Tab.CHANNELS};
		// Initially active tab; optional; can be RECENTS, USERS or CHANNELS
		Tab tab = _activeTab;
		// Visual theme; optional; can be DARK or LIGHT
		Theme theme = Theme.DARK;

		// Since Zello was initialized in the Activity, pass in this as Activity parameter
		Zello.getInstance().selectContact(title, tabs, tab, theme, this);
	}

	private boolean getCanTalk() {
		ContactType type = _selectedContact.getType();
		ContactStatus status = _selectedContact.getStatus();
		switch (type) {
			case USER:
			case GATEWAY:
				// User or radio gateway
				return status != ContactStatus.OFFLINE; // Not offline
			case CHANNEL:
			case GROUP:
				// Channel or group
				return _appState.isSignedIn() && status == ContactStatus.AVAILABLE; // Channel is online
		}
		return false;
	}

}
