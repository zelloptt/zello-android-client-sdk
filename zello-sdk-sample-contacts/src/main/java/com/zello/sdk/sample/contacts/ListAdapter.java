package com.zello.sdk.sample.contacts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zello.sdk.Contact;
import com.zello.sdk.ContactStatus;
import com.zello.sdk.ContactType;
import com.zello.sdk.Contacts;

import java.text.NumberFormat;
import java.util.ArrayList;

@SuppressWarnings("WeakerAccess")
public class ListAdapter extends BaseAdapter {

	private final ArrayList<Contact> _contacts = new ArrayList<>();

	public ListAdapter() {
		super();
	}

	public void setContacts(Contacts contacts) {
		_contacts.clear();
		if (contacts != null) {
			int n = contacts.getCount();
			_contacts.ensureCapacity(n);
			for (int i = 0; i < n; ++i) {
				_contacts.add(contacts.getItem(i));
			}
		}
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return _contacts.isEmpty();
	}

	@Override
	public Object getItem(int position) {
		if (position >= 0 && position < _contacts.size()) {
			return _contacts.get(position);
		} else {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getCount() {
		return _contacts.size();
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		if (convertView != null) {
			view = convertView;
		} else {
			view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact, null);
		}
		Contact contact = null;
		if (position >= 0 && position < _contacts.size()) {
			contact = _contacts.get(position);
		}
		configureView(view, contact);
		return view;
	}

	public static void configureView(View view, Contact contact) {
		ImageView imgContactStatus = view.findViewById(R.id.contact_image);
		View viewContactMute = view.findViewById(R.id.contact_mute);
		TextView txtContactName = view.findViewById(R.id.contact_name);
		TextView txtContactStatus = view.findViewById(R.id.contact_status);
		if (contact != null) {
			String displayName = contact.getDisplayName(); // Contact name or a full name if not empty
			String title = contact.getTitle();
			if (title != null && title.length() > 0) {
				displayName += " (" + title + ")";
			}
			String statusText = "";
			ContactType type = contact.getType();
			ContactStatus status = contact.getStatus();
			Context context = view.getContext();

			switch (type) {
				case USER, GATEWAY -> {
					// User or radio gateway
					String message = contact.getStatusMessage(); // User-defined status message
					statusText = message == null || message.length() == 0 ? statusToText(context, status) : message;
				}
				case CHANNEL -> {
					if (status == ContactStatus.AVAILABLE) {
						String countText = NumberFormat.getInstance().format(contact.getUsersCount());
						statusText = context.getString(R.string.status_channel_users_count).replace("%count%", countText);
					} else {
						statusText = statusToText(context, status);
					}
				}
				case GROUP -> {
					String countText = NumberFormat.getInstance().format(contact.getUsersTotal());
					String totalText = NumberFormat.getInstance().format(contact.getUsersTotal());
					statusText = view.getContext().getString(R.string.status_group_users_count).replace("%count%", countText).replace("%total%", totalText);
				}
				case CONVERSATION -> {
					if (status == ContactStatus.AVAILABLE) {
						String countText = NumberFormat.getInstance().format(contact.getUsersCount());
						String totalText = NumberFormat.getInstance().format(contact.getUsersTotal());
						statusText = view.getContext().getString(R.string.status_group_users_count).replace("%count%", countText).replace("%total%", totalText);
					} else {
						statusText = statusToText(context, status);
					}
				}
			}

			imgContactStatus.setImageResource(statusToDrawableId(status, type));
			viewContactMute.setVisibility(contact.getMuted() ? View.VISIBLE : View.GONE);
			txtContactName.setText(displayName);
			txtContactStatus.setText(statusText);
		} else {
			imgContactStatus.setVisibility(View.INVISIBLE);
			viewContactMute.setVisibility(View.GONE);
			txtContactName.setVisibility(View.INVISIBLE);
			txtContactStatus.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	private static String statusToText(Context context, ContactStatus status) {
		int id;
		switch (status) {
			case STANDBY -> id = R.string.status_standby;
			case AVAILABLE -> id = R.string.status_online;
			case BUSY -> id = R.string.status_busy;
			case CONNECTING -> id = R.string.status_connecting;
			default -> id = R.string.status_offline;
		}
		return context.getString(id);
	}

	@SuppressWarnings("SwitchStatementWithTooFewBranches")
	private static int statusToDrawableId(ContactStatus status, ContactType type) {
		switch (type) {
			case USER -> {
				// User
				return switch (status) {
					case STANDBY -> R.drawable.user_standby;
					case AVAILABLE -> R.drawable.user_online;
					case BUSY -> R.drawable.user_busy;
					default -> R.drawable.user_offline;
				};
			}
			case CHANNEL -> {
				// Channel
				return switch (status) {
					case AVAILABLE -> R.drawable.channel_online;
					default -> R.drawable.channel_offline;
				};
			}
			case GATEWAY -> {
				// Radio gateway
				return R.drawable.gateway_online;
			}
			case GROUP -> {
				// Group
				return switch (status) {
					case AVAILABLE -> R.drawable.group_online;
					default -> R.drawable.channel_offline;
				};
			}
			case CONVERSATION -> {
				// Channel
				return switch (status) {
					case AVAILABLE -> R.drawable.conversation_online;
					default -> R.drawable.conversation_offline;
				};
			}
		}
		return R.drawable.user_offline;
	}

}
