package com.zello.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * <p>
 *     Manages {@link AudioMode} used by Zello for Work app.
 * </p>
 * <p>
 *     To use, retrieve the current <code>Audio</code> instance using the {@link Zello#getAudio()} method. For specific usage, please see the sample projects.
 * </p>
 */
public class Audio {

	//region Private Properties

	private boolean _sp, _ep, _bt; // Speaker, earpiece, bluetooth
	private int _wearable = -1; // Wearable index
	private int _wearables; // Wearable device count
	private boolean _changing;
	private AudioMode _mode; // Current mode
	private BroadcastReceiver _receiver;
	private Context _context;
	private String _package;

	//endregion

	//region Package Private Methods

	/* package */
	Audio(String packageName, Context context) {
		_package = packageName;
		_context = context;
		if (context != null && packageName != null) {
			_receiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					updateAudioState(intent);

					for (Events event : Zello.getInstance().events) {
						event.onAudioStateChanged();
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
		_context = null;
	}

	//endregion

	//region Public Audio Methods

	/**
	 * Determines if the passed in <code>AudioMode</code> is available on the device.
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
	 * Determines if the old <code>AudioMode</code> is currently in the process of changing to a new <code>AudioMode</code>.
	 * @return boolean indicating if the mode is changing.
	 * @see #setMode(AudioMode)
     */
	public boolean isModeChanging() {
		return _changing;
	}

	/**
	 * Returns the current <code>AudioMode</code> type.
	 * @return <code>AudioMode</code> indicating the current output of audio.
	 * @see #setMode(AudioMode)
     */
	public AudioMode getMode() {
		return _mode;
	}

	/**
	 * <p>
	 *     Sets the current <code>AudioMode</code> to <code>mode</code>.
	 * </p>
	 * <p>
	 *     The method is asynchronous so using <code>Audio.getMode()</code> immediatelly after calling
	 *     it may return the previous audio mode. {@link Events#onAudioStateChanged()} is called when
	 *     audio mode changes.
	 * </p>
	 * @param mode <code>AudioMode</code> indicating the new form of audio output.
	 * @see #getMode()
	 * @see Events#onAudioStateChanged()
     */
	public void setMode(AudioMode mode) {
		doSetMode(mode, 0);
	}

	/**
	 * <p>
	 *     Sets the current <code>AudioMode</code> to the {@link AudioMode#WEARABLE} mode using the passed in wearable index.
	 * </p>
	 * <p>
	 *     The <code>wearableIndex</code> can be between <code>0</code> and {@link #getWearableCount()}
	 * </p>
	 * @param wearableIndex The index of the wearable device to set the output of audio to.
     */
	public void setWearableMode(int wearableIndex) {
		if (wearableIndex >= 0) {
			doSetMode(AudioMode.WEARABLE, wearableIndex);
		}
	}

	/**
	 * Gets the number of wearable devices that can play audio.
	 * @return The number of wearables connected to the device.
	 */
	public int getWearableCount() {
		return _wearables;
	}


	//endregion

	//region Private Methods

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
