package com.zello.sdk.sample.misc;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.zello.sdk.AppState;
import com.zello.sdk.BluetoothAccessoryState;
import com.zello.sdk.BluetoothAccessoryType;
import com.zello.sdk.Status;
import com.zello.sdk.Tab;
import com.zello.sdk.Zello;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MiscActivity extends AppCompatActivity implements com.zello.sdk.Events {

	private final AppState _appState = new AppState();

	private Button _lockButton;
	private TextView _statusTextView;
	private EditText _externalIdEditText;
	private TextView _externalIdTextView;

	//region Lifecycle methods

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_misc);

		_lockButton = findViewById(R.id.lockButton);
		_statusTextView = findViewById(R.id.statusTextView);
		_externalIdEditText = findViewById(R.id.externalIdEditText);
		_externalIdTextView = findViewById(R.id.externalIdTextView);

		_lockButton.setOnClickListener(v -> {
			Zello.getInstance().getAppState(_appState);

			if (_appState.isLocked()) {
				Zello.getInstance().unlock();
			} else {
				Zello.getInstance().lock("Zello SDK Sample Misc", getPackageName());
			}
		});

		_externalIdEditText.setOnEditorActionListener((v, actionId, event) -> {
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				Zello.getInstance().setExternalId(_externalIdEditText.getText().toString());

				hideKeyboard();

				return true;
			}

			return false;
		});

		Zello zello = Zello.getInstance();
		// Automatically choose the app to connect to in the following order of preference: com.loudtalks, net.loudtalks, com.pttsdk
		// Alternatively, connect to a preferred app by supplying a package name, for example: Zello.getInstance().configure("net.loudtalks", this)
		zello.configure(this);
		zello.subscribeToEvents(this);

		onAppStateChanged();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Zello zello = Zello.getInstance();
		zello.unsubscribeFromEvents(this);
		zello.unconfigure();
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
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		menu.clear();

		Zello.getInstance().getAppState(_appState);
		if (_appState.isAvailable() && !_appState.isInitializing()) {
			getMenuInflater().inflate(R.menu.menu, menu);

			boolean available = false, solo = false, busy = false;
			if (!_appState.isConfiguring() && _appState.isSignedIn() && !_appState.isSigningIn() && !_appState.isSigningOut()) {
				Status status = _appState.getStatus();
				available = status == Status.AVAILABLE;
				solo = status == Status.SOLO;
				busy = status == Status.BUSY;
			}

			showMenuItem(menu, R.id.menu_available, available);
			showMenuItem(menu, R.id.menu_solo, solo);
			showMenuItem(menu, R.id.menu_busy, busy);

			showMenuItem(menu, R.id.menu_enable_auto_run, !_appState.isAutoRunEnabled());
			showMenuItem(menu, R.id.menu_disable_auto_run, _appState.isAutoRunEnabled());
			showMenuItem(menu, R.id.menu_enable_auto_connect_channels, _appState.isSignedIn() && !_appState.isChannelAutoConnectEnabled());
			showMenuItem(menu, R.id.menu_disable_auto_connect_channels, _appState.isSignedIn() && _appState.isChannelAutoConnectEnabled());

			showMenuItem(menu, R.id.menu_open_app, true);
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
		if (id == R.id.menu_enable_auto_run) {
			Zello.getInstance().setAutoRun(true);
			return true;
		}
		if (id == R.id.menu_disable_auto_run) {
			Zello.getInstance().setAutoRun(false);
			return true;
		}
		if (id == R.id.menu_enable_auto_connect_channels) {
			Zello.getInstance().setAutoConnectChannels(true);
			return true;
		}
		if (id == R.id.menu_disable_auto_connect_channels) {
			Zello.getInstance().setAutoConnectChannels(false);
			return true;
		}
		if (id == R.id.menu_open_app) {
			Zello.getInstance().openMainScreen();
			return true;
		}
		return false;
	}

	//endregion

	//region Zello SDK Events

	@Override
	public void onMessageStateChanged() {
	}

	@Override
	public void onSelectedContactChanged() {
	}

	@Override
	public void onAudioStateChanged() {
	}

	@Override
	public void onContactsChanged() {
	}

	@Override
	public void onLastContactsTabChanged(@NonNull Tab tab) {
	}

	@Override
	public void onAppStateChanged() {
		Zello.getInstance().getAppState(_appState);

		if (_appState.isSignedIn()) {
			invalidateOptionsMenu();
			updateUIForSignedIn();
		} else if (_appState.isSigningIn()) {
			updateUIForSigningIn();
		} else {
			updateUIForDisconnected();
		}
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

	//endregion

	private void showMenuItem(Menu menu, int itemId, boolean show) {
		MenuItem item = menu.findItem(itemId);
		if (item != null) {
			item.setVisible(show);
		}
	}

	private void chooseStatus() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		Resources res = getResources();
		Zello.getInstance().getAppState(_appState);

		Status status = _appState.getStatus();
		int selection = switch (status) {
			case AVAILABLE -> 0;
			case SOLO -> 1;
			case BUSY -> 2;
		};
		if (_appState.getStatusMessage() != null && !_appState.getStatusMessage().equals("")) {
			selection = 3;
		}

		String[] items = new String[]{res.getString(R.string.menu_available), res.getString(R.string.menu_solo), res.getString(R.string.menu_busy), res.getString(R.string.menu_set_status_message)};
		builder.setSingleChoiceItems(items, selection, (dialog, which) -> {
			switch (which) {
				case 0 -> Zello.getInstance().setStatus(Status.AVAILABLE);
				case 1 -> Zello.getInstance().setStatus(Status.SOLO);
				case 2 -> Zello.getInstance().setStatus(Status.BUSY);
				case 3 -> createCustomStatusMessageDialog();
			}
			dialog.dismiss();
		});
		builder.setCancelable(true).setTitle(res.getString(R.string.menu_set_status));
		final AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
	}

	private void updateUIForLocked() {
		_lockButton.setText(R.string.unlock);
	}

	private void updateUIForUnlocked() {
		_lockButton.setText(R.string.lock);
	}

	private void updateUIForSignedIn() {
		invalidateOptionsMenu();

		_externalIdEditText.setVisibility(View.VISIBLE);
		_externalIdEditText.setText(_appState.getExternalId());

		_externalIdTextView.setVisibility(View.VISIBLE);

		_statusTextView.setVisibility(View.INVISIBLE);

		if (_appState.isLocked()) {
			updateUIForLocked();
		} else {
			updateUIForUnlocked();
		}

		_lockButton.setVisibility(View.VISIBLE);
	}

	private void updateUIForSigningIn() {
		invalidateOptionsMenu();

		_statusTextView.setVisibility(View.VISIBLE);
		_lockButton.setVisibility(View.INVISIBLE);
		_externalIdEditText.setVisibility(View.INVISIBLE);
		_externalIdTextView.setVisibility(View.INVISIBLE);

		_statusTextView.setText(R.string.sign_in_status_signing_in);
	}

	private void updateUIForDisconnected() {
		invalidateOptionsMenu();

		_statusTextView.setVisibility(View.VISIBLE);
		_lockButton.setVisibility(View.INVISIBLE);
		_externalIdEditText.setVisibility(View.INVISIBLE);
		_externalIdTextView.setVisibility(View.INVISIBLE);

		_statusTextView.setText(R.string.sign_in_status_offline);
	}

	@SuppressLint("InflateParams")
	private void createCustomStatusMessageDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		Zello.getInstance().getAppState(_appState);
		String previousStatus = _appState.getStatusMessage();
		View view = getLayoutInflater().inflate(R.layout.status_dialog, null);
		final EditText statusEditText = view.findViewById(R.id.editView);
		statusEditText.setText(previousStatus);
		builder.setPositiveButton(R.string.ok, (dialog, which) -> Zello.getInstance().setStatusMessage(statusEditText.getText().toString()));
		builder.setNegativeButton(R.string.cancel, null);
		builder.setTitle(R.string.menu_set_status_message);
		builder.setView(view);

		builder.show();
	}

	private void hideKeyboard() {
		View view = this.getCurrentFocus();
		if (view == null) {
			return;
		}
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm == null) {
			return;
		}
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

}
