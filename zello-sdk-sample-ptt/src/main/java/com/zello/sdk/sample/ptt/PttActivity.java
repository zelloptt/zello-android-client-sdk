package com.zello.sdk.sample.ptt;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.zello.sdk.Contact;
import com.zello.sdk.ContactStatus;
import com.zello.sdk.MessageIn;
import com.zello.sdk.MessageOut;
import com.zello.sdk.Tab;
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

	private com.zello.sdk.AppState _appState = new com.zello.sdk.AppState();
	private com.zello.sdk.Audio _audio;
	private Contact _selectedContact = new Contact();
	private MessageIn _messageIn = new MessageIn();
	private MessageOut _messageOut = new MessageOut();

	//region Lifecycle Methods

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
					_audio.setMode(com.zello.sdk.AudioMode.SPEAKER);
				}
			}
		});

		_earpieceButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (_audio != null) {
					_audio.setMode(com.zello.sdk.AudioMode.EARPIECE);
				}
			}
		});

		_bluetoothButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (_audio != null) {
					_audio.setMode(com.zello.sdk.AudioMode.BLUETOOTH);
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

			menu.getItem(0).setTitle(_selectedContact.getMuted() ? R.string.unmute_contact : R.string.mute_contact);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_mute_contact) {
			Zello.getInstance().getSelectedContact(_selectedContact);

			Zello.getInstance().muteContact(_selectedContact, !_selectedContact.getMuted());
		} else if (item.getItemId() == R.id.menu_replay) {
			Zello.getInstance().replayLastIncomingMessage();
		}

		return true;
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
	}

	@Override
	public void onMicrophonePermissionNotGranted() {
		if (_active) {
			Zello.getInstance().showMicrophonePermissionDialog(this);
		}
	}

	//endregion

	private void updateUI() {
		Zello.getInstance().getAppState(_appState);

		if (_appState.isSignedIn()) {
			Zello.getInstance().getSelectedContact(_selectedContact);

			if (_selectedContact.getDisplayName() != null) {
				if (_selectedContact.getStatus() == ContactStatus.AVAILABLE) {
					updateUIForAvailableContact();
				} else {
					updateUIForUnavailableContact();
				}
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
		_audioModeView.setVisibility(View.VISIBLE);
		_selectedContactTextView.setVisibility(View.VISIBLE);
		_selectedContactTextView.setText(getResources().getString(R.string.selected_contact, _selectedContact.getDisplayName()));

		updateConnectChannelButton();
		updateAudioMode();
	}

	private void updateUIForUnavailableContact() {
		invalidateOptionsMenu();

		_statusTextView.setVisibility(View.VISIBLE);

		_pttButton.setVisibility(View.INVISIBLE);
		_audioModeView.setVisibility(View.INVISIBLE);
		_speakerButton.setVisibility(View.INVISIBLE);
		_bluetoothButton.setVisibility(View.INVISIBLE);
		_earpieceButton.setVisibility(View.INVISIBLE);
		_connectChannelButton.setVisibility(View.INVISIBLE);
		_selectedContactTextView.setVisibility(View.INVISIBLE);

		_statusTextView.setText(R.string.unavailable_selected_contact);
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

	private void updateAudioMode() {
		boolean speaker = false, earpiece = false, bluetooth = false;
		com.zello.sdk.AudioMode mode = com.zello.sdk.AudioMode.SPEAKER;

		// Can't set new mode while the mode is being changed
		boolean currentlyChangingMode = false;
		if (_audio != null) {
			speaker = _audio.isModeAvailable(com.zello.sdk.AudioMode.SPEAKER);
			earpiece = _audio.isModeAvailable(com.zello.sdk.AudioMode.EARPIECE);
			bluetooth = _audio.isModeAvailable(com.zello.sdk.AudioMode.BLUETOOTH);
			mode = _audio.getMode();
			currentlyChangingMode = _audio.isModeChanging();
		}

		// If none of the modes is available, the client app is old and needs to be updated
		if (bluetooth || earpiece || speaker) {
			_speakerButton.setVisibility(speaker ? View.VISIBLE : View.GONE);
			if (speaker) {
				_speakerButton.setChecked(mode == com.zello.sdk.AudioMode.SPEAKER);
				_speakerButton.setEnabled(!currentlyChangingMode && (earpiece || bluetooth));
			}

			_earpieceButton.setVisibility(earpiece ? View.VISIBLE : View.GONE);
			if (earpiece) {
				_earpieceButton.setChecked(mode == com.zello.sdk.AudioMode.EARPIECE);
				_earpieceButton.setEnabled(!currentlyChangingMode && (speaker || bluetooth));
			}

			_bluetoothButton.setVisibility(bluetooth ? View.VISIBLE : View.GONE);
			if (bluetooth) {
				_bluetoothButton.setChecked(mode == com.zello.sdk.AudioMode.BLUETOOTH);
				_bluetoothButton.setEnabled(!currentlyChangingMode && (speaker || earpiece));
			}
		}

		_audioModeView.setVisibility(speaker || earpiece || bluetooth ? View.VISIBLE : View.GONE);
	}

	private void onConnectChannelButtonPressed() {
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

	private void updateConnectChannelButton() {
		boolean showConnect = false, connected = false, canConnect = false;

		com.zello.sdk.ContactType type = _selectedContact.getType();
		com.zello.sdk.ContactStatus status = _selectedContact.getStatus();
		switch (type) {
			case USER:
			case GATEWAY:
			case CHANNEL:
			case GROUP: {
				showConnect = !_selectedContact.getNoDisconnect();
				if (status == com.zello.sdk.ContactStatus.AVAILABLE) {
					canConnect = true;
					connected = true;
				} else if (status == com.zello.sdk.ContactStatus.OFFLINE) {
					canConnect = true;
				} else if (status == com.zello.sdk.ContactStatus.CONNECTING) {
					connected = true;
				}
				break;
			}
		}

		_connectChannelButton.setEnabled(canConnect);
		_connectChannelButton.setChecked(connected);
		_connectChannelButton.setVisibility(showConnect ? View.VISIBLE : View.GONE);
	}

}
