/*
 * Copyright (C) 2011 The original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.zapta.apps.maniana.editors;

import android.app.Dialog;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.EditText;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.main.AppContext;
import com.zapta.apps.maniana.util.PopupsTracker.TrackablePopup;

/**
 * Item text editor dialog. Used to create new itmes and to edit existing ones.
 * <p>
 * TODO: see if we can use the info in http://tinyurl.com/8yucabx to show the Done button on the
 * soft keyboard.
 * 
 * @author Tal Dayan
 */
public class ItemEditor extends Dialog implements TrackablePopup {

    public interface ItemEditorListener {
        void onTextChange(String newText);

        void onDismiss(String finalText);
    }

    private class EventAdapter implements DialogInterface.OnDismissListener, TextWatcher {
        @Override
        public void onDismiss(DialogInterface dialog) {
            handleOnDismiss();
        }

        @Override
        public void afterTextChanged(Editable s) {
            handleTextChanged();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Nothing to do here
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Nothing to do here
        }
    }

    private final AppContext mApp;

    private final ItemEditorListener mListener;

    /** The text edit field of the dialog. */
    private final EditText mEditText;

    /** Used to avoid double reporting of dismissal. */
    private boolean dismissAlreadyReported = false;

    /** Private constructor. Use startEditor() to create and launch an editor. */
    private ItemEditor(final AppContext app, String title, String initialText,
            ItemEditorListener listener) {
        // TODO: reorder and organize the statements below for better reliability.
        super(app.context());
        mApp = app;
        mListener = listener;
        setContentView(R.layout.editor_layout);
        setTitle(title);
        setOwnerActivity(app.mainActivity());

        mEditText = (EditText) findViewById(R.id.editor_text);
        mEditText.setText(initialText);

        // Always using non completed variation, even if the item is completed.
        app.pref().getPageItemFontVariation().apply(mEditText, false, false);

        EventAdapter eventAdapter = new EventAdapter();
        setOnDismissListener(eventAdapter);
        mEditText.addTextChangedListener(eventAdapter);

        getWindow().setGravity(Gravity.TOP);

        // TODO: why this setting does not work when done in the layout XML?
        mEditText.setHorizontallyScrolling(false);
        mEditText.setSingleLine(false);
        mEditText.setMinLines(3);
        mEditText.setMaxLines(5);

        // Position cursor at the end of the text
        mEditText.setSelection(initialText.length());

        // TODO: this does not open automatically the keyaobrd when in landscape mode.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    /** Called when the dialog get dismissed. */
    private final void handleOnDismiss() {
        mApp.popupsTracker().untrack(this);
        // If not already reported during the close leftover.
        if (!dismissAlreadyReported) {
            mListener.onDismiss(mEditText.getText().toString());
        }
    }

    /** Called when text field value changes. */
    private final void handleTextChanged() {
        final String value = mEditText.getText().toString();
        // We detect the Enter key by the '\n' char it inserts.
        if (value.contains("\n")) {
            final String cleanedValue = value.replace("\n", "").trim();
            mEditText.setText(cleanedValue);
            // This will trigger the handleOnDismiss above that will call the listener of this
            // editor.
            dismiss();
        } else {
            mListener.onTextChange(value.trim());
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
            mListener.onDismiss(mEditText.getText().toString());
            dismiss();
        }
    }

    /**
     * Show an item editor.
     * 
     * @param app app context.
     * @param title title to display in the editor.
     * @param initialText initial edited item text
     * @param listener listener to callback on changes and on end.
     */
    public static void startEditor(final AppContext app, String title, String initialText,
            final ItemEditorListener listener) {
        final ItemEditor dialog = new ItemEditor(app, title, initialText, listener);
        app.popupsTracker().track(dialog);
        dialog.show();
    }
}
