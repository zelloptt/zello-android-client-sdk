package com.zello.sdk.sample.signin;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.zello.sdk.AppState;
import com.zello.sdk.BluetoothAccessoryState;
import com.zello.sdk.BluetoothAccessoryType;
import com.zello.sdk.Error;
import com.zello.sdk.Tab;
import com.zello.sdk.Zello;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SigninActivity extends AppCompatActivity implements com.zello.sdk.Events {

	private EditText _networkEdit;
	private EditText _usernameEdit;
	private EditText _passwordEdit;
	private TextView _textView;
	private CheckBox _perishableCheckBox;
	private View _signingInView;
	private View _signInView;
	private Button _cancelButton;
	private Button _signOutButton;
	private TextView _errorTextView;
	private boolean _signInAttempted = false;

	private final AppState _appState = new AppState();

	//region Lifecycle Methods

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_signin);

		_signInView = findViewById(R.id.signInView);
		_networkEdit = findViewById(R.id.networkEdit);
		_usernameEdit = findViewById(R.id.usernameEdit);
		_passwordEdit = findViewById(R.id.passwordEdit);
		_textView = findViewById(R.id.statusTextView);
		_perishableCheckBox = findViewById(R.id.checkPerishable);
		_signingInView = findViewById(R.id.signingInView);
		_cancelButton = findViewById(R.id.cancelButton);
		_errorTextView = findViewById(R.id.incorrectPasswordTextView);
		_signOutButton = findViewById(R.id.signOutButton);

		Zello zello = Zello.getInstance();
		// Automatically choose the app to connect to in the following order of preference: com.loudtalks, net.loudtalks, com.pttsdk
		// Alternatively, connect to a preferred app by supplying a package name, for example: Zello.getInstance().configure("net.loudtalks", this)
		zello.configure(this);
		zello.subscribeToEvents(this);

		findViewById(R.id.loginButton).setOnClickListener(view -> {
			String network = _networkEdit.getText().toString();
			String username = _usernameEdit.getText().toString();
			String password = _passwordEdit.getText().toString();
			boolean perishable = _perishableCheckBox.isChecked();

			_signInAttempted = true;
			Zello.getInstance().signIn(network, username, password, perishable);

			hideKeyboard();
		});

		_cancelButton.setOnClickListener(view -> Zello.getInstance().cancelSignIn());

		_signOutButton.setOnClickListener(view -> Zello.getInstance().signOut());
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
		updateUI();
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

	@Override
	public void onForegroundServiceStartFailed(@Nullable Throwable throwable) {
	}

	//endregion

	private void updateUI() {
		if (_appState.isSignedIn()) {
			_signInView.setVisibility(View.GONE);
			_signingInView.setVisibility(View.VISIBLE);
			_textView.setText(R.string.signed_in);
			_cancelButton.setVisibility(View.GONE);
			_signOutButton.setVisibility(View.VISIBLE);
		} else if (_appState.isSigningIn()) {
			_textView.setText(R.string.signing_in);
			_cancelButton.setVisibility(View.VISIBLE);
			_signOutButton.setVisibility(View.GONE);
			_signInView.setVisibility(View.GONE);
			_signingInView.setVisibility(View.VISIBLE);
		} else {
			_signInView.setVisibility(View.VISIBLE);
			_signingInView.setVisibility(View.GONE);
			_signOutButton.setVisibility(View.GONE);
		}

		if (_signInAttempted) {
			String error = getErrorText(_appState.getLastError());
			_errorTextView.setText(error);
		}
	}

	private String getErrorText(Error error) {
		return switch (error) {
			case UNKNOWN -> getString(R.string.error_unknown);
			case INVALID_CREDENTIALS -> getString(R.string.error_invalid_credentials);
			case INVALID_NETWORK_NAME -> getString(R.string.error_invalid_network_name);
			case NETWORK_SUSPENDED -> getString(R.string.error_network_suspended);
			case SERVER_SECURE_CONNECT_FAILED -> getString(R.string.error_secure_connect_failed);
			case SERVER_SIGNIN_FAILED -> getString(R.string.error_server_signin_failed);
			case NETWORK_SIGNIN_FAILED -> getString(R.string.error_network_signin_failed);
			case KICKED -> getString(R.string.error_kicked);
			case APP_UPDATE_REQUIRED -> getString(R.string.error_update_required);
			case NO_INTERNET_CONNECTION -> getString(R.string.error_no_internet);
			case INTERNET_CONNECTION_RESTRICTED -> getString(R.string.error_internet_restricted);
			case SERVER_LICENSE_PROBLEM -> getString(R.string.error_server_license);
			case TOO_MANY_SIGNIN_ATTEMPTS -> getString(R.string.error_brute_force_protection);
			case DEVICE_ID_MISMATCH -> getString(R.string.error_device_id_mismatch);
			default -> null;
		};
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
