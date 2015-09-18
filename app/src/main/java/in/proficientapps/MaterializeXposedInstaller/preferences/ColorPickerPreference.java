package in.proficientapps.MaterializeXposedInstaller.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import in.proficientapps.MaterializeXposedInstaller.R;

public class ColorPickerPreference extends Preference implements Preference.OnPreferenceClickListener, 
			 ColorPickerDialog.OnColorChangedListener, CompoundButton.OnCheckedChangeListener {

	View mView;
	ColorPickerDialog mDialog;
	boolean mShowCheckBox = false;
	boolean mPickerEnabled = false;
	private int mValue = Color.BLACK;
	private float mDensity = 0;
	private boolean mAlphaSliderEnabled = false;
	private boolean mHexValueEnabled = false;
	public ColorPickerPreference(Context context) {
		super(context);
		init(context, null);
	}
	public ColorPickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}
	public ColorPickerPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getColor(index, Color.BLACK);
	}
	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		if (restoreValue)
			mPickerEnabled = !mShowCheckBox || getSharedPreferences().getBoolean(getKey() + "_checkbox", mPickerEnabled);
		onColorChanged(restoreValue ? getPersistedInt(mValue) : (Integer) defaultValue);
	}
	private void init(Context context, AttributeSet attrs) {
		mDensity = getContext().getResources().getDisplayMetrics().density;
		setOnPreferenceClickListener(this);
		if (attrs != null) {
			mAlphaSliderEnabled = attrs.getAttributeBooleanValue(null, "alphaSlider", false);
			mShowCheckBox = attrs.getAttributeBooleanValue(null, "showCheckBox", false);
			mPickerEnabled = !mShowCheckBox || attrs.getAttributeBooleanValue(null, "enabledByDefault", false);
			mHexValueEnabled = attrs.getAttributeBooleanValue(null, "hexValue", false);
		}
	}
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		mView = view;
		setPreviewColor();
	}
	private void setPreviewColor() {
		if (mView == null) return;
		CheckBox cbPickerEnabled = null;
		if (mShowCheckBox) {
			cbPickerEnabled = new CheckBox(getContext());
			Drawable checkbg = getContext().getResources().getDrawable(R.drawable.checks);
			int colorAccent = getContext().getResources().getColor(R.color.colorAccent0);
			checkbg.setColorFilter(colorAccent, PorterDuff.Mode.MULTIPLY );
			cbPickerEnabled.setButtonDrawable(checkbg);
			cbPickerEnabled.setFocusable(false);
			cbPickerEnabled.setEnabled(super.isEnabled());
			cbPickerEnabled.setChecked(mPickerEnabled);
			cbPickerEnabled.setOnCheckedChangeListener(this);
		}
		ImageView iView = new ImageView(getContext());
		LinearLayout widgetFrameView = ((LinearLayout)mView.findViewById(android.R.id.widget_frame));
		if (widgetFrameView == null) return;
		widgetFrameView.setVisibility(View.VISIBLE);
		widgetFrameView.setPadding(
			widgetFrameView.getPaddingLeft(),
			widgetFrameView.getPaddingTop(),
			(int)(mDensity * 8),
			widgetFrameView.getPaddingBottom()
		);
		int count = widgetFrameView.getChildCount();
		if (count > 0) {
			widgetFrameView.removeViews(0, count);
		}
		if (mShowCheckBox) {
			widgetFrameView.setOrientation(LinearLayout.HORIZONTAL);
			widgetFrameView.addView(cbPickerEnabled);
		}
		widgetFrameView.addView(iView);
		widgetFrameView.setMinimumWidth(0);
		getPreviewDrawable(iView);
	}
	private Drawable getPreviewDrawable(ImageView image) {
		int color = mValue;
		Drawable bm = getContext().getResources().getDrawable(R.drawable.colorpicker);
		bm.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
		image.setImageDrawable(bm);
		return bm;
	}
	@Override
	public void onColorChanged(int color) {
		if (isPersistent()) { persistInt(color); }
		mValue = color;
		persistBothValues();
		setPreviewColor();
		notifyChanged();
		try { getOnPreferenceChangeListener().onPreferenceChange(this, color); 
		} catch (NullPointerException ignored) {}
	}
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		mPickerEnabled = isChecked; 
		persistBothValues();
        notifyDependencyChange(shouldDisableDependents());
        notifyChanged();
	}
	protected void persistBothValues() {
    	if (shouldPersist()) {
            SharedPreferences.Editor editor = getEditor();
            editor.putInt(getKey(), mValue);
            if (mShowCheckBox)
            	editor.putBoolean(getKey() + "_checkbox", mPickerEnabled);
            if (shouldCommit()) {
                try {
                    editor.apply();
                } catch (AbstractMethodError unused) { editor.commit(); }
            }
		}
    }
    @Override
    public boolean isEnabled() {
    	return super.isEnabled() && mPickerEnabled;
    }
	public void setValue(int color) {
	    if (isPersistent()) { persistInt(color); }
	    mValue = color;
	    setPreviewColor();
	}
	public boolean onPreferenceClick(Preference preference) {
		showDialog(null);
		return false;
	}
	protected void showDialog(Bundle state) {
		mDialog = new ColorPickerDialog(getContext(), mValue);
		mDialog.setOnColorChangedListener(this);
		if (mAlphaSliderEnabled) { mDialog.setAlphaSliderVisible(true);	}
		if (mHexValueEnabled) { mDialog.setHexValueEnabled(true); }
		if (state != null) { mDialog.onRestoreInstanceState(state); }
		mDialog.show();
	}
	public void setAlphaSliderEnabled(boolean enable) { mAlphaSliderEnabled = enable; }
	public void setHexValueEnabled(boolean enable) { mHexValueEnabled = enable;	}
    public static String convertToARGB(int color) {
        String alpha = Integer.toHexString(Color.alpha(color));
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));
        if (alpha.length() == 1) { alpha = "0" + alpha; }
        if (red.length() == 1) { red = "0" + red; }
        if (green.length() == 1) { green = "0" + green; }
        if (blue.length() == 1) { blue = "0" + blue; }
        return "#" + alpha + red + green + blue;
    }
    public static String convertToRGB(int color) {
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));
        if (red.length() == 1) { red = "0" + red; }
        if (green.length() == 1) { green = "0" + green; }
        if (blue.length() == 1) { blue = "0" + blue; }
        return "#" + red + green + blue;
    }
    public static int convertToColorInt(String argb) throws NumberFormatException {
    	if (argb.startsWith("#")) { argb = argb.replace("#", ""); }
        int alpha = -1, red = -1, green = -1, blue = -1;
        if (argb.length() == 8) {
            alpha = Integer.parseInt(argb.substring(0, 2), 16);
            red = Integer.parseInt(argb.substring(2, 4), 16);
            green = Integer.parseInt(argb.substring(4, 6), 16);
            blue = Integer.parseInt(argb.substring(6, 8), 16);
        }
        else if (argb.length() == 6) {
            alpha = 255;
            red = Integer.parseInt(argb.substring(0, 2), 16);
            green = Integer.parseInt(argb.substring(2, 4), 16);
            blue = Integer.parseInt(argb.substring(4, 6), 16);
        }
        else
        	throw new NumberFormatException("string " + argb + "did not meet length requirements");
        return Color.argb(alpha, red, green, blue);
    }
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (mDialog == null || !mDialog.isShowing()) { return superState; }
        final SavedState myState = new SavedState(superState);
        myState.dialogBundle = mDialog.onSaveInstanceState();
        myState.pickerEnabled = mPickerEnabled;
        return myState;
    }
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        mPickerEnabled = myState.pickerEnabled;
        if (myState.dialogBundle != null)
        showDialog(myState.dialogBundle);
    }
    private static class SavedState extends BaseSavedState {
        Bundle dialogBundle;
        boolean pickerEnabled;
        public SavedState(Parcel source) {
            super(source);
            dialogBundle = source.readBundle();
            pickerEnabled = source.readInt() == 1;
        }
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeBundle(dialogBundle);
            dest.writeInt(pickerEnabled ? 1 : 0);
        }
        public SavedState(Parcelable superState) { super(superState); }
        @SuppressWarnings("unused")
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) { return new SavedState(in); }
            public SavedState[] newArray(int size) { return new SavedState[size]; }
        };
    }
}