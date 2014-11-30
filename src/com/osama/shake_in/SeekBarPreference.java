package com.osama.shake_in;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SeekBarPreference extends Preference implements
		OnSeekBarChangeListener {

	private final int MAX = 500;
	private final int MIN = 100;
	private final int DEFAULT_CURRENT_VALUE = 193;

	private int currentValue;
	private TextView txtCurrentValue;
	private TextView txtMaxValue;
	private TextView txtMinValue;

	public SeekBarPreference(Context context) {
		super(context);
	}

	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getContext());
		currentValue = sharedPreferences
				.getInt(getKey(), DEFAULT_CURRENT_VALUE);

		// TODO: outcoded use xml instead of programming.

		// Inflate layout
		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_slider, null);

		SeekBar bar = (SeekBar) view.findViewById(R.id.seek_bar);
		bar.setMax(MAX - MIN);
		bar.setProgress(currentValue);
		bar.setOnSeekBarChangeListener(this);

		this.txtCurrentValue = (TextView) view.findViewById(R.id.current_value);
		txtCurrentValue.setText("Sensitivity: " + String.valueOf(currentValue + MIN));

		this.txtMaxValue = (TextView) view.findViewById(R.id.max_value);
		this.txtMaxValue.setText(String.valueOf(MAX));

		this.txtMinValue = (TextView) view.findViewById(R.id.min_value);
		txtMinValue.setText(String.valueOf(MIN));

		return view;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {

		if (!callChangeListener(progress)) {
			seekBar.setProgress((int) this.currentValue);
			return;
		}

		seekBar.setProgress(progress);
		this.txtCurrentValue.setText((progress + MIN) + "");
		updatePreference(progress);

		notifyChanged();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	@Override
	protected Object onGetDefaultValue(TypedArray ta, int index) {

		int dValue = (int) ta.getInt(index, DEFAULT_CURRENT_VALUE);

		return validateValue(dValue);
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

		int temp = restoreValue ? getPersistedInt(DEFAULT_CURRENT_VALUE)
				: (Integer) defaultValue;

		if (!restoreValue)
			persistInt(temp);

		this.currentValue = temp;
	}

	private int validateValue(int value) {

		if (value > MAX - MIN)
			value = MAX - MIN;
		else if (value < 0)
			value = 0;

		return value;
	}

	private void updatePreference(int newValue) {

		SharedPreferences.Editor editor = getEditor();
		editor.putInt(getKey(), newValue);
		editor.commit();
	}

}