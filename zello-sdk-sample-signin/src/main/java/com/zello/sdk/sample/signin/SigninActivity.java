package com.zello.sdk.sample.signin;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.zello.sdk.AppState;
import com.zello.sdk.BluetoothAccessoryState;
import com.zello.sdk.BluetoothAccessoryType;
import com.zello.sdk.Error;
import com.zello.sdk.Tab;
import com.zello.sdk.Zello;

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

	private AppState _appState = new AppState();

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

		// Pass net.loudtalks to connect to an app installed from an apk obtained from https://www.zellowork.com
		// Pass com.pttsdk to connect to a PTT SDK app obtained from https://github.com/zelloptt/zello-android-client-sdk/releases
		// Pass com.loudtalks to connect to Zello app installed from the Google Play Store
		// Or, Pass null to automatically choose the app to connect to in the following order of preference: com.loudtalks, net.loudtalks, com.pttsdk
		Zello.getInstance().configure(null, this);

		findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String network = _networkEdit.getText().toString();
				String username = _usernameEdit.getText().toString();
				String password = _passwordEdit.getText().toString();
				boolean perishable = _perishableCheckBox.isChecked();

				_signInAttempted = true;
				Zello.getInstance().signIn(network, username, password, perishable);

				hideKeyboard();
			}
		});

		_cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Zello.getInstance().cancelSignIn();
			}
		});

		_signOutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Zello.getInstance().signOut();
			}
		});
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
	public void onLastContactsTabChanged(Tab tab) {
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
	public void onBluetoothAccessoryStateChanged(BluetoothAccessoryType bluetoothAccessoryType, BluetoothAccessoryState bluetoothAccessoryState, String s, String s1) {
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
		switch (error) {
			case UNKNOWN:
				return getString(R.string.error_unknown);
			case INVALID_CREDENTIALS:
				return getString(R.string.error_invalid_credentials);
			case INVALID_NETWORK_NAME:
				return getString(R.string.error_invalid_network_name);
			case NETWORK_SUSPENDED:
				return getString(R.string.error_network_suspended);
			case SERVER_SECURE_CONNECT_FAILED:
				return getString(R.string.error_secure_connect_failed);
			case SERVER_SIGNIN_FAILED:
				return getString(R.string.error_server_signin_failed);
			case NETWORK_SIGNIN_FAILED:
				return getString(R.string.error_network_signin_failed);
			case KICKED:
				return getString(R.string.error_kicked);
			case APP_UPDATE_REQUIRED:
				return getString(R.string.error_update_required);
			case NO_INTERNET_CONNECTION:
				return getString(R.string.error_no_internet);
			case INTERNET_CONNECTION_RESTRICTED:
				return getString(R.string.error_internet_restricted);
			case SERVER_LICENSE_PROBLEM:
				return getString(R.string.error_server_license);
			case TOO_MANY_SIGNIN_ATTEMPTS:
				return getString(R.string.error_brute_force_protection);
			case DEVICE_ID_MISMATCH:
				return getString(R.string.error_device_id_mismatch);
			default:
				return null;
		}
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
