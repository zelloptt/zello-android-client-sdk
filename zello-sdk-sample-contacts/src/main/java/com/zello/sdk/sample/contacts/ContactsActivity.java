package com.zello.sdk.sample.contacts;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.zello.sdk.AppState;
import com.zello.sdk.BluetoothAccessoryState;
import com.zello.sdk.BluetoothAccessoryType;
import com.zello.sdk.Tab;
import com.zello.sdk.Zello;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ContactsActivity extends AppCompatActivity implements com.zello.sdk.Events {

	private ListView _contactsListView;
	private TextView _statusTextView;
	private TextView _selectedContactTextView;

	private final AppState _appState = new AppState();

	//region Lifecycle Methods

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_contacts);

		_contactsListView = findViewById(R.id.contactsListView);
		_statusTextView = findViewById(R.id.statusTextView);
		_selectedContactTextView = findViewById(R.id.selectedContactTextView);

		Zello zello = Zello.getInstance();
		// Automatically choose the app to connect to in the following order of preference: com.loudtalks, net.loudtalks, com.pttsdk
		// Alternatively, connect to a preferred app by supplying a package name, for example: Zello.getInstance().configure("net.loudtalks", this)
		zello.configure(this);
		zello.subscribeToEvents(this);

		// Contact list pick handler
		_contactsListView.setOnItemClickListener((parent, view, position, id) -> {
			ListAdapter adapter = (ListAdapter) _contactsListView.getAdapter();
			if (adapter == null) {
				return;
			}
			com.zello.sdk.Contact contact = (com.zello.sdk.Contact) adapter.getItem(position);
			if (contact != null) {
				Zello.getInstance().setSelectedContact(contact);
			}
		});

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
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.clear();
		Zello.getInstance().getAppState(_appState);
		if (_appState.isAvailable() && !_appState.isInitializing()) {
			getMenuInflater().inflate(R.menu.menu, menu);

			showMenuItem(menu, R.id.menu_select_contact, true);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.menu_select_contact) {
			// Activity title; optional
			String title = getString(R.string.select_contact_title);
			// Set of displayed tabs; required; any combination of RECENTS, USERS and CHANNELS
			com.zello.sdk.Tab[] tabs = new com.zello.sdk.Tab[]{com.zello.sdk.Tab.RECENTS, com.zello.sdk.Tab.USERS, com.zello.sdk.Tab.CHANNELS};
			// Initially active tab; optional; can be RECENTS, USERS or CHANNELS
			com.zello.sdk.Tab tab = Tab.RECENTS;
			// Visual theme; optional; can be DARK or LIGHT
			com.zello.sdk.Theme theme = com.zello.sdk.Theme.DARK;

			// Since Zello was initialized in the Activity, pass in this as Activity parameter
			Zello.getInstance().selectContact(title, tabs, tab, theme, this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	//endregion

	private void updateContactList() {
		ListAdapter adapter = (ListAdapter) _contactsListView.getAdapter();
		boolean newAdapter = false;
		if (adapter == null) {
			newAdapter = true;
			adapter = new ListAdapter();
		}
		adapter.setContacts(Zello.getInstance().getContacts());
		Parcelable state = _contactsListView.onSaveInstanceState();
		if (newAdapter) {
			_contactsListView.setAdapter(adapter);
		} else {
			adapter.notifyDataSetChanged();
		}
		if (state != null) {
			_contactsListView.onRestoreInstanceState(state);
		}
		_contactsListView.setFocusable(adapter.getCount() > 0);
	}

	//region Zello SDK Events

	@Override
	public void onMessageStateChanged() {
	}

	@Override
	public void onSelectedContactChanged() {
		com.zello.sdk.Contact selectedContact = new com.zello.sdk.Contact();
		Zello.getInstance().getSelectedContact(selectedContact);

		String name = selectedContact.getDisplayName();
		if (name != null) {
			_selectedContactTextView.setText(getString(R.string.selected_contact, selectedContact.getDisplayName()));
		}
	}

	@Override
	public void onAudioStateChanged() {
	}

	@Override
	public void onContactsChanged() {
		updateContactList();
	}

	@Override
	public void onLastContactsTabChanged(@NonNull Tab tab) {
	}

	@Override
	public void onAppStateChanged() {
		Zello.getInstance().getAppState(_appState);

		if (_appState.isSignedIn()) {
			invalidateOptionsMenu();
			_statusTextView.setVisibility(View.INVISIBLE);
			_contactsListView.setVisibility(View.VISIBLE);
			_selectedContactTextView.setVisibility(View.VISIBLE);
			updateContactList();
		} else if (_appState.isSigningIn()) {
			_statusTextView.setVisibility(View.VISIBLE);
			_statusTextView.setText(R.string.sign_in_status_signing_in);
			_contactsListView.setVisibility(View.INVISIBLE);
			_selectedContactTextView.setVisibility(View.INVISIBLE);
		} else {
			_statusTextView.setVisibility(View.VISIBLE);
			_statusTextView.setText(R.string.sign_in_status_offline);
			_contactsListView.setVisibility(View.INVISIBLE);
			_selectedContactTextView.setVisibility(View.INVISIBLE);
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

	@Override
	public void onForegroundServiceStartFailed(@Nullable Throwable throwable) {
	}

	//endregion

	@SuppressWarnings("SameParameterValue")
	private void showMenuItem(Menu menu, int itemId, boolean show) {
		MenuItem item = menu.findItem(itemId);
		if (item != null) {
			item.setVisible(show);
		}
	}

}
