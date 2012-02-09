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
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.main.AppContext;

/**
 * Provides popup item editor functionality.
 * <p>
 * TODO: see if we can use the info in http://tinyurl.com/8yucabx to show the Done
 * button on the soft keyboard.
 * 
 * @author Tal Dayan
 */
public final class ItemEditor {

    public interface ItemEditorListener {
        void onTextChange(String newText);

        void onDismiss(String finalText);
    }

    /** Do not instantiate */
    private ItemEditor() {
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

        final Dialog dialog = new Dialog(app.context());
        dialog.setContentView(R.layout.editor_layout);
        dialog.setTitle(title);
        dialog.setOwnerActivity(app.mainActivity());

        final EditText editText = (EditText) dialog.findViewById(R.id.editor_text);
        editText.setText(initialText);
        
        // Always using non completed variation, even if the item is completed.
        app.resources().getItemFontVariation().apply(editText, false, false);
        //editText.settextc

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                listener.onDismiss(editText.getText().toString());
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                final String value = editText.getText().toString();
                // We detect the Enter key by the '\n' char it inserts.
                if (value.contains("\n")) {
                    final String cleanedValue = value.replace("\n", "").trim();
                    editText.setText(cleanedValue);
                    // This will trigger the onDismiss above that will call the listener of this
                    // editor.
                    dialog.dismiss();
                } else {
                    listener.onTextChange(value.trim());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        dialog.getWindow().setGravity(Gravity.TOP);

        // TODO: why this setting does not work when done in the layout XML?
        editText.setHorizontallyScrolling(false);
        editText.setSingleLine(false);
        editText.setMinLines(3);
        editText.setMaxLines(5);
        
        // Position cursor at the end of the text
        editText.setSelection(initialText.length());
        
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        dialog.show();
    }

}
