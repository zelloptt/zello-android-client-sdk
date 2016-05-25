package com.zello.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * <pre>
 * The Audio class acts as an intermediary between the devices audio and the Zello SDK.
 * This class is useful for getting and updating the current output mode of audio from the device (ie. AudioMode).
 * </pre>
 * <pre>
 * To use, retrieve the current Audio instance from the Sdk instance using the getAudio() method. For specific usage, please see the sample projects.
 * </pre>
 */
public class Audio {

	//region Private Properties

	private boolean _sp, _ep, _bt; // Speaker, earpiece, bluetooth
	private int _wearable = -1; // Wearable index
	private int _wearables; // Wearable device count
	private boolean _changing;
	private AudioMode _mode; // Current mode
	private BroadcastReceiver _receiver;
	private Events _events;
	private Context _context;
	private String _package;

	//endregion

	//region Package Private Methods

	/* package */
	Audio(Events events, String packageName, Context context) {
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

	/* package */
	void close() {
		Context context = _context;
		if (context != null && _receiver != null) {
			context.unregisterReceiver(_receiver);
		}
		_receiver = null;
		_events = null;
		_context = null;
	}

	//endregion

	//region Public Audio Methods

	/**
	 * The isModeAvailable() method determines if the passed in AudioMode is available on the device.
	 * @param mode The mode to test availability for.
	 * @return boolean indicating if the mode is available for the device.
     */
	public boolean isModeAvailable(AudioMode mode) {
		switch (mode) {
			case SPEAKER:
				return _sp;
			case EARPIECE:
				return _ep;
			case BLUETOOTH:
				return _bt;
			case WEARABLE:
				return _wearables > 0;
			default:
				return false;
		}
	}

	/**
	 * The isModeChanging() method determines if the set AudioMode is currently in the process of changing to another AudioMode.
	 * @return boolean indicating if the mode is changing.
     */
	public boolean isModeChanging() {
		return _changing;
	}

	/**
	 * The getMode() method returns the current AudioMode type.
	 * @return AudioMode indicating the current output of audio.
     */
	public AudioMode getMode() {
		return _mode;
	}

	/**
	 * The setMode() method sets the current AudioMode to the passed in mode.
	 * @param mode AudioMode indicating the new form of audio output.
     */
	public void setMode(AudioMode mode) {
		doSetMode(mode, 0);
	}

	/**
	 * The setWearableMode() method sets the current AudioMode to the WEARABLE mode using the passed in wearable index.
	 * @param wearable The index of the wearable device to set the output of audio to.
     */
	public void setWearableMode(int wearable) {
		if (wearable >= 0) {
			doSetMode(AudioMode.WEARABLE, wearable);
		}
	}

	//endregion

	//region Private Methods

	/**
	 *
	 * @param mode
	 * @param wearable
     */
	private void doSetMode(AudioMode mode, int wearable) {
		Context context = _context;
		if (context != null) {
			Intent intent = new Intent(_package + "." + Constants.ACTION_COMMAND);
			intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_SET_AUDIO);
			final String command;
			switch (mode) {
				case BLUETOOTH:
					command = Constants.EXTRA_BT;
					break;
				case EARPIECE:
					command = Constants.EXTRA_EP;
					break;
				case WEARABLE:
					command = Constants.EXTRA_WA + wearable;
					break;
				default:
					command = Constants.EXTRA_SP;
			}
			intent.putExtra(Constants.EXTRA_MODE, command);
			context.sendBroadcast(intent);
		}
	}

	private void updateAudioState(Intent intent) {
		if (intent != null) {
			String mode = Util.emptyIfNull(intent.getStringExtra(Constants.EXTRA_MODE));
			if (mode.startsWith(Constants.EXTRA_WA)) {
				_mode = AudioMode.WEARABLE;
				try {
					_wearable = Integer.parseInt(mode.substring(Constants.EXTRA_WA.length()));
				} catch (NumberFormatException ignore) {
					_wearable = 0;
				}
			} else if (mode.equals(Constants.EXTRA_EP)) {
				_mode = AudioMode.EARPIECE;
			} else if (mode.equals(Constants.EXTRA_BT)) {
				_mode = AudioMode.BLUETOOTH;
			} else {
				_mode = AudioMode.SPEAKER;
			}
			_sp = intent.getBooleanExtra(Constants.EXTRA_SP, true);
			_ep = intent.getBooleanExtra(Constants.EXTRA_EP, false);
			_bt = intent.getBooleanExtra(Constants.EXTRA_BT, false);
			_wearables = intent.getIntExtra(Constants.EXTRA_WA, 0);
		}
	}

	//endregion

}
