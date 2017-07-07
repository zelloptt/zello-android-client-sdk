package com.zello.sdk.sample.signin;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.app.Activity;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zello.sdk.Tab;
import com.zello.sdk.Zello;

public class SigninActivity extends Activity implements com.zello.sdk.Events {

    private EditText _networkEdit;
    private EditText _usernameEdit;
    private EditText _passwordEdit;
    private Button _loginButton;
    private TextView _textView;
    private CheckBox _perishableCheckBox;
    private RelativeLayout _signingInView;
    private RelativeLayout _signInView;
    private Button _cancelButton;
    private Button _signOutButton;
    private TextView _errorTextView;
    private boolean _signInAttempted = false;

    private com.zello.sdk.AppState _appState = new com.zello.sdk.AppState();

	//region Lifecycle Methods

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signin);

		_signInView = (RelativeLayout)findViewById(R.id.signInView);
		_networkEdit = (EditText)findViewById(R.id.networkEdit);
		_usernameEdit = (EditText)findViewById(R.id.usernameEdit);
		_passwordEdit = (EditText)findViewById(R.id.passwordEdit);
		_loginButton = (Button)findViewById(R.id.loginButton);
		_textView = (TextView)findViewById(R.id.statusTextView);
		_perishableCheckBox = (CheckBox)findViewById(R.id.checkBox);
		_signingInView = (RelativeLayout)findViewById(R.id.signingInView);
		_cancelButton = (Button)findViewById(R.id.cancelButton);
		_errorTextView = (TextView)findViewById(R.id.incorrectPasswordTextView);
		_signOutButton = (Button)findViewById(R.id.signOutButton);

        Zello.getInstance().configure("com.pttsdk", this, this);

        _loginButton.setOnClickListener(new View.OnClickListener() {
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
	public void onLastMessageReplayAvailableChanged() {

	}

	//endregion

    private void updateUI() {
        if (_appState.isSignedIn()) {
            _signInView.setVisibility(View.INVISIBLE);
            _signingInView.setVisibility(View.VISIBLE);

            _textView.setText(R.string.signed_in);

            _cancelButton.setVisibility(View.INVISIBLE);
            _signOutButton.setVisibility(View.VISIBLE);
        } else if (_appState.isSigningIn()) {
            _textView.setText(R.string.signing_in);
            _cancelButton.setVisibility(View.VISIBLE);
            _signOutButton.setVisibility(View.INVISIBLE);

            _signInView.setVisibility(View.INVISIBLE);
            _signingInView.setVisibility(View.VISIBLE);
        } else {
            _signInView.setVisibility(View.VISIBLE);
            _signingInView.setVisibility(View.INVISIBLE);

            _signOutButton.setVisibility(View.INVISIBLE);
        }

        if (_signInAttempted) {
            String error = getErrorText(_appState.getLastError());
            if (error == null) {
                _errorTextView.setText("");
            } else {
                _errorTextView.setText(error);
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

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
