package com.zello.sdk.sample.contacts;

import android.os.Parcelable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.zello.sdk.Tab;
import com.zello.sdk.Zello;

public class ContactsActivity extends AppCompatActivity implements com.zello.sdk.Events {

	private ListView _contactsListView;
	private TextView _statusTextView;
	private TextView _selectedContactTextView;

	private com.zello.sdk.AppState _appState = new com.zello.sdk.AppState();

	//region Lifecycle Methods

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_contacts);

		_contactsListView = findViewById(R.id.contactsListView);
		_statusTextView = findViewById(R.id.statusTextView);
		_selectedContactTextView = findViewById(R.id.selectedContactTextView);

		// Use to connect to an app installed from an apk obtained from https://www.zellowork.com
		//Zello.getInstance().configure("net.loudtalks", this, this);

		// Use with an app installed from a generic PTT SDK apk obtained from https://github.com/zelloptt/zello-android-client-sdk/releases
		Zello.getInstance().configure("com.pttsdk", this, this);

		// Contact list pick handler
		_contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ListAdapter adapter = (ListAdapter) _contactsListView.getAdapter();
				if (adapter != null) {
					com.zello.sdk.Contact contact = (com.zello.sdk.Contact) adapter.getItem(position);
					if (contact != null) {
						Zello.getInstance().setSelectedContact(contact);
					}
				}
			}
		});

		onAppStateChanged();
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

			showMenuItem(menu, R.id.menu_select_contact, true);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_select_contact: {
				// Activity title; optional
				String title = getResources().getString(R.string.select_contact_title);
				// Set of displayed tabs; required; any combination of RECENTS, USERS and CHANNELS
				com.zello.sdk.Tab[] tabs = new com.zello.sdk.Tab[]{com.zello.sdk.Tab.RECENTS, com.zello.sdk.Tab.USERS, com.zello.sdk.Tab.CHANNELS};
				// Initially active tab; optional; can be RECENTS, USERS or CHANNELS
				com.zello.sdk.Tab tab = Tab.RECENTS;
				// Visual theme; optional; can be DARK or LIGHT
				com.zello.sdk.Theme theme = com.zello.sdk.Theme.DARK;

				// Since Zello was initialized in the Activity, pass in this as Activity parameter
				Zello.getInstance().selectContact(title, tabs, tab, theme, this);
				break;
			}
		}
		return true;
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
			_selectedContactTextView.setText(getResources().getString(R.string.selected_contact, selectedContact.getDisplayName()));
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
	public void onLastContactsTabChanged(Tab tab) {
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

	//endregion

	private void showMenuItem(Menu menu, int itemId, boolean show) {
		MenuItem item = menu.findItem(itemId);
		if (item != null) {
			item.setVisible(show);
		}
	}

}
