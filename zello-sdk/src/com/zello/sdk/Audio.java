package com.zello.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class Audio {

	private boolean _sp, _ep, _bt; // Speaker, earpiece, bluetooth
	private boolean _changing;
	private AudioMode _mode; // Current mode
	private BroadcastReceiver _receiver;
	private Events _events;
	private Context _context;
	private String _package;

	/* package */ Audio(Events events, String packageName, Context context) {
		_events = events;
		_package = packageName;
		_context = context;
		if (context != null && packageName != null) {
			_receiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					updateAudioState(intent);
					Events events = _events;
					if (events != null) {
						events.onAudioStateChanged();
					}
				}
			};
			Intent intentStickyAudioState = context.registerReceiver(_receiver, new IntentFilter(packageName + "." + Constants.ACTION_AUDIO_STATE));
			updateAudioState(intentStickyAudioState);
		}
	}

	/* package */ void close() {
		Context context = _context;
		if (context != null && _receiver != null) {
			context.unregisterReceiver(_receiver);
		}
		_receiver = null;
		_events = null;
		_context = null;
	}

	public boolean isModeAvailable(AudioMode mode) {
		switch (mode) {
			case SPEAKER:
				return _sp;
			case EARPIECE:
				return _ep;
			case BLUETOOTH:
				return _bt;
			default:
				return false;
		}
	}

	public boolean isModeChanging() {
		return _changing;
	}

	public AudioMode getMode() {
		return _mode;
	}

	public void setMode(AudioMode mode) {
		Context context = _context;
		if (context != null) {
			Intent intent = new Intent(_package + "." + Constants.ACTION_COMMAND);
			intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_SET_AUDIO);
			intent.putExtra(Constants.EXTRA_MODE, mode == AudioMode.BLUETOOTH ? Constants.EXTRA_BT : (mode == AudioMode.EARPIECE ? Constants.EXTRA_EP : Constants.EXTRA_SP));
			context.sendBroadcast(intent);
		}
	}

	private void updateAudioState(Intent intent) {
		if (intent != null) {
			String mode = Util.emptyIfNull(intent.getStringExtra(Constants.EXTRA_MODE));
			if (mode.equals(Constants.EXTRA_EP)) {
				_mode = AudioMode.EARPIECE;
			} else if (mode.equals(Constants.EXTRA_BT)) {
				_mode = AudioMode.BLUETOOTH;
			} else {
				_mode = AudioMode.SPEAKER;
			}
			_sp = intent.getBooleanExtra(Constants.EXTRA_SP, true);
			_ep = intent.getBooleanExtra(Constants.EXTRA_EP, false);
			_bt = intent.getBooleanExtra(Constants.EXTRA_BT, false);
		}
	}

}
