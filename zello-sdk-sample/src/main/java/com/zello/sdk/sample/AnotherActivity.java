package com.zello.sdk.sample;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zello.sdk.AppState;
import com.zello.sdk.BluetoothAccessoryState;
import com.zello.sdk.BluetoothAccessoryType;
import com.zello.sdk.MessageIn;
import com.zello.sdk.MessageOut;
import com.zello.sdk.Status;
import com.zello.sdk.Tab;
import com.zello.sdk.Theme;
import com.zello.sdk.Zello;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AnotherActivity extends AppCompatActivity implements com.zello.sdk.Events {

	private final AppState _appState = new AppState();
	private final MessageIn _messageIn = new MessageIn();
	private final MessageOut _messageOut = new MessageOut();
	private Tab _activeTab = Tab.RECENTS;

	private TextView _textMessageInfo;
	private ImageView _imgMessageStatus;
	private TextView _txtMessageName;
	private TextView _txtMessageStatus;
	private View _viewMessageInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("AnotherActivity.onCreate");

		setContentView(R.layout.activity_another);
		_textMessageInfo = findViewById(R.id.network);
		_viewMessageInfo = findViewById(R.id.message_info);
		_imgMessageStatus = _viewMessageInfo.findViewById(R.id.message_image);
		_txtMessageName = _viewMessageInfo.findViewById(R.id.message_name);
		_txtMessageStatus = _viewMessageInfo.findViewById(R.id.message_status);

		Zello.getInstance().subscribeToEvents(this);
		updateAppState();
		updateMessageState();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Zello.getInstance().unsubscribeFromEvents(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Zello.getInstance().leavePowerSavingMode();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Zello.getInstance().enterPowerSavingMode();
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
				Status status = _appState.getStatus();
				select = true;
				available = status == Status.AVAILABLE;
				solo = status == Status.SOLO;
				busy = status == Status.BUSY;
			}
			menu.findItem(R.id.menu_select_contact).setVisible(select);
			menu.findItem(R.id.menu_lock_ptt_app).setVisible(!_appState.isLocked());
			menu.findItem(R.id.menu_unlock_ptt_app).setVisible(_appState.isLocked());
			menu.findItem(R.id.menu_enable_auto_run).setVisible(!_appState.isAutoRunEnabled());
			menu.findItem(R.id.menu_disable_auto_run).setVisible(_appState.isAutoRunEnabled());
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
		int id = item.getItemId();
		if (id == R.id.menu_available || id == R.id.menu_solo || id == R.id.menu_busy) {
			chooseStatus();
			return true;
		}
		if (id == R.id.menu_select_contact) {
			chooseActiveContact();
			return true;
		}
		if (id == R.id.menu_lock_ptt_app) {
			lockPttApp();
			return true;
		}
		if (id == R.id.menu_unlock_ptt_app) {
			unlockPttApp();
			return true;
		}
		if (id == R.id.menu_enable_auto_run) {
			enableAutoRun();
			return true;
		}
		if (id == R.id.menu_disable_auto_run) {
			disableAutoRun();
			return true;
		}
		if (id == R.id.menu_start_message) {
			Zello.getInstance().beginMessage();
			return true;
		}
		if (id == R.id.menu_stop_message) {
			Zello.getInstance().endMessage();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSelectedContactChanged() {

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
	public void onLastContactsTabChanged(@NonNull Tab tab) {
		_activeTab = tab;
	}

	@Override
	public void onContactsChanged() {
	}

	@Override
	public void onAudioStateChanged() {
	}

	@Override
	public void onMicrophonePermissionNotGranted() {
	}

	@Override
	public void onBluetoothAccessoryStateChanged(
			@NonNull BluetoothAccessoryType bluetoothAccessoryType,
			@NonNull BluetoothAccessoryState bluetoothAccessoryState,
			@Nullable String name,
			@Nullable String description
	) {
	}

	private void chooseActiveContact() {
		// Activity title; optional
		String title = getString(R.string.select_contact_title);
		// Set of displayed tabs; required; any combination of RECENTS, USERS and CHANNELS
		Tab[] tabs = new Tab[]{Tab.RECENTS, Tab.USERS, Tab.CHANNELS};
		// Initially active tab; optional; can be RECENTS, USERS or CHANNELS
		Tab tab = _activeTab;
		// Visual theme; optional; can be DARK or LIGHT
		Theme theme = Theme.DARK;

		Zello.getInstance().selectContact(title, tabs, tab, theme);
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

	private void chooseStatus() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		Resources res = getResources();
		Status status = _appState.getStatus();
		int selection = status == Status.BUSY ? 2 : (status == Status.SOLO ? 1 : 0);
		String[] items = new String[]{res.getString(R.string.menu_available), res.getString(R.string.menu_solo), res.getString(R.string.menu_busy), res.getString(R.string.menu_sign_out)};
		builder.setSingleChoiceItems(items, selection, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
					case 0 -> Zello.getInstance().setStatus(Status.AVAILABLE);
					case 1 -> Zello.getInstance().setStatus(Status.SOLO);
					case 2 -> Zello.getInstance().setStatus(Status.BUSY);
					case 3 -> Zello.getInstance().signOut();
				}
				dialog.dismiss();
			}
		});
		builder.setCancelable(true).setTitle(res.getString(R.string.menu_set_status));
		final AlertDialog dialog = builder.create();
		dialog.show();
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
		} else if (incoming) {
			String author = _messageIn.getAuthor().getDisplayName(); // Is message from channel?
			if (author != null && author.length() > 0) {
				// Show channel and author names
				_txtMessageName.setText(getString(R.string.message_in_from_channel, _messageIn.getFrom().getDisplayName(), author));
			} else {
				_txtMessageName.setText(_messageIn.getFrom().getDisplayName()); // Show sender name
			}
			_txtMessageStatus.setText(R.string.message_receiving);
			_imgMessageStatus.setImageResource(R.drawable.message_in);
		}
		_viewMessageInfo.setVisibility(incoming || outgoing ? View.VISIBLE : View.INVISIBLE);
	}


	private void updateAppState() {
		Zello.getInstance().getAppState(_appState);
		String state = "";

		if (!_appState.isAvailable()) {
			state = getString(R.string.ptt_app_not_installed);
		} else if (_appState.isInitializing()) {
			state = getString(R.string.ptt_app_initializing);
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
		_textMessageInfo.setText(_appState.getNetwork());
		System.out.println(state);
		invalidateOptionsMenu();
	}

}
