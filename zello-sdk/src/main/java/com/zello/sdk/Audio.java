package com.zello.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * <p>
 *     Manages {@link AudioMode} used by Zello Work app.
 * </p>
 * <p>
 *     To use, retrieve the current <code>Audio</code> instance using the {@link Zello#getAudio()} method. For specific usage, please see the sample projects.
 * </p>
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Audio {

	//region Private Properties

	private boolean _sp, _ep, _bt; // Speaker, earpiece, bluetooth
	private int _wearables; // Wearable device count
	private boolean _changing;
	private @NonNull AudioMode _mode = AudioMode.SPEAKER; // Current mode
	private @Nullable BroadcastReceiver _receiver;
	private @Nullable Context _context;
	private final @Nullable String _package;

	//endregion

	//region Package Private Methods

	Audio(@Nullable String packageName, @Nullable Context context) {
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
			IntentFilter filter = new IntentFilter(packageName + "." + Constants.ACTION_AUDIO_STATE);
			Intent intent = ContextCompat.registerReceiver(context, _receiver, filter, ContextCompat.RECEIVER_EXPORTED);
			updateAudioState(intent);
		}
	}

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
	public boolean isModeAvailable(@NonNull AudioMode mode) {
		return switch (mode) {
			case SPEAKER -> _sp;
			case EARPIECE -> _ep;
			case BLUETOOTH -> _bt;
			case WEARABLE -> _wearables > 0;
		};
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
	public @NonNull AudioMode getMode() {
		return _mode;
	}

	/**
	 * <p>
	 *     Sets the current <code>AudioMode</code> to <code>mode</code>.
	 * </p>
	 * <p>
	 *     This method is asynchronous. When the mode has changed, <code>onAudioStateChanged()</code>
	 *     is called on the <code>Events</code> interface.
	 * </p>
	 * @param mode <code>AudioMode</code> indicating the new form of audio output.
	 * @see #getMode()
	 * @see Events#onAudioStateChanged()
     */
	public void setMode(@NonNull AudioMode mode) {
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

	private void doSetMode(@NonNull AudioMode mode, int wearable) {
		Context context = _context;
		if (context == null) {
			return;
		}
		Intent intent = new Intent(_package + "." + Constants.ACTION_COMMAND);
		intent.putExtra(Constants.EXTRA_COMMAND, Constants.VALUE_SET_AUDIO);
		String command = switch (mode) {
			case BLUETOOTH -> Constants.EXTRA_BT;
			case EARPIECE -> Constants.EXTRA_EP;
			case WEARABLE -> Constants.EXTRA_WA + wearable;
			default -> Constants.EXTRA_SP;
		};
		intent.putExtra(Constants.EXTRA_MODE, command);
		context.sendBroadcast(intent);
	}

	private void updateAudioState(@Nullable Intent intent) {
		if (intent == null) {
			return;
		}
		String mode = Util.emptyIfNull(intent.getStringExtra(Constants.EXTRA_MODE));
		if (mode.startsWith(Constants.EXTRA_WA)) {
			_mode = AudioMode.WEARABLE;
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

	//endregion

}
