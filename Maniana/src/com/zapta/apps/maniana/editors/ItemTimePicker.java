package com.zapta.apps.maniana.editors;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Dialog;
import android.content.DialogInterface;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.annotations.MainActivityScope;
import com.zapta.apps.maniana.main.MainActivityState;
import com.zapta.apps.maniana.util.PopupsTracker.TrackablePopup;

@MainActivityScope
public class ItemTimePicker extends Dialog implements TrackablePopup {

	public interface ItemTimePickerListener {
		void onDismiss(Date finalDate);
	}

	private final MainActivityState mMainActivityState;

	private final ItemTimePickerListener mListener;

	private final DatePicker mDatePicker;
	private final TimePicker mTimePicker;

	/** Used to avoid double reporting of dismissal. */
	private boolean dismissAlreadyReported = false;

	private GregorianCalendar mCalendar;

	private ItemTimePicker(final MainActivityState mainActivityState,
			Date initialItemDate, ItemTimePickerListener listener) {
		super(mainActivityState.context());
		mMainActivityState = mainActivityState;
		mListener = listener;
		mCalendar = new GregorianCalendar(); // FIXME: make sure we use good
												// calendar & timezone here
		mCalendar.setTime(initialItemDate);
		setContentView(R.layout.dialog_datetime_picker);
		updateTitle();
		setOwnerActivity(mainActivityState.mainActivity());

		// FIXME: allow clearing item date (?)
		// FIXME: try to start this dialog from ItemTextEditor, not from ItemMenu
		// FIXME: add OK (Save?)/Cancel
		// FIXME: make sure that the dialog fits in screen
		
		// Get sub views
		mDatePicker = (DatePicker) findViewById(R.id.date_picker);
		mTimePicker = (TimePicker) findViewById(R.id.time_picker);
		// mColorView = findViewById(R.id.editor_color); // FIXME: do we need
		// this too?

		// mDatePicker.setMinDate() // TODO: add if possible in API v8
		mDatePicker.init(mCalendar.get(Calendar.YEAR),
				mCalendar.get(Calendar.MONTH),
				mCalendar.get(Calendar.DAY_OF_MONTH),
				new DatePicker.OnDateChangedListener() {
					@Override
					public void onDateChanged(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						mCalendar.set(year, monthOfYear, dayOfMonth);
						updateTitle();
					}
				});
		mTimePicker.setCurrentHour(mCalendar.get(Calendar.HOUR_OF_DAY));
		mTimePicker.setCurrentMinute(mCalendar.get(Calendar.MINUTE));
		mTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
				mCalendar.set(Calendar.MINUTE, minute);
				updateTitle();
			}
		});

		// TODO: do we need this below?
		/*
		 * // For API 11+ these values come from the default theme. For holo, //
		 * this will be white text on dark gray background. This makes sure the
		 * // text // cursor is at at color visible over the background.:w if
		 * (PRE_API_11) { final View topView = findViewById(R.id.editor_top);
		 * topView.setBackgroundColor(0xffffffff);
		 * mExtendedEditTextView.setBackgroundColor(0xffffffff);
		 * mExtendedEditTextView.setTextColor(0xff000000); }
		 * 
		 * mainActivityState.prefTracker().getPageItemFontVariation()
		 * .apply(mExtendedEditTextView, false, false);
		 */

		// EditorEventAdapter eventAdapter = new EditorEventAdapter();
		setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface arg0) {
				handleOnDismiss();
			}
		});

		// FIXME: if possible, add listener to mDatePicker, to display full date
		// in dialog title, especially with weekday info !!

		getWindow().setGravity(Gravity.TOP);
	}

	private void updateTitle() {
		// TODO Auto-generated method stub
		setTitle(DateUtils.formatDateTime(getContext(),
				mCalendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_WEEKDAY
						| DateUtils.FORMAT_ABBREV_WEEKDAY
						| DateUtils.FORMAT_SHOW_DATE
						| DateUtils.FORMAT_SHOW_TIME));
	}

	/** Called when the dialog get dismissed. */
	private final void handleOnDismiss() {
		mMainActivityState.popupsTracker().untrack(this);
		// If not already reported during the close leftover.
		if (!dismissAlreadyReported) {
			mListener.onDismiss(mCalendar.getTime());
		}
	}

    /** Called when the dialog was left open and the main activity pauses. */
    @Override
    public final void closeLeftOver() {
        if (isShowing()) {
            // We provide an early dismiss event. Otherwise, this would be reported later when the
            // UI thread will get to handle the queued event. The controller rely on the fact that
            // the editor was dismissed and any pending new item text was submitted to the model.
            dismissAlreadyReported = true;
            mListener.onDismiss(mCalendar.getTime());
            dismiss();
        }
    }

	/**
	 * Show a date/time picker.
	 * 
	 * @param mainActivityState
	 *            mainActivityState context.
	 * @param title
	 *            title to display in the editor.
	 * @param initialText
	 *            initial edited item text
	 * @param listener
	 *            listener to callback on changes and on end.
	 */
	public static void startEditor(final MainActivityState mainActivityState,
			Date date, final ItemTimePickerListener listener) {
		final ItemTimePicker dialog = new ItemTimePicker(mainActivityState,
				date, listener);
		mainActivityState.popupsTracker().track(dialog);
		dialog.show();
	}
}
