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

package com.zapta.apps.maniana.preferences;

import static com.zapta.apps.maniana.util.Assertions.checkNotNull;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

/**
 * @author George Yunaev (http://tinyurl.com/7jnoubo)
 * @author Tal Dayan
 */
public class FontPreference extends DialogPreference implements DialogInterface.OnClickListener {

    private final ItemFontType mDefaultValue;
    
    private ItemFontType mValue;

    /**
     * Format string for preference summary string (when dialog is closed). Can contain a single %s
     * for currently selected font name.
     */
    private String mSummaryFormat;

    // Font adaptor over ItemFontType.values()
    public class FontAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return ItemFontType.values().length;
        }

        @Override
        public Object getItem(int position) {
            return ItemFontType.values()[position].name;
        }

        @Override
        public long getItemId(int position) {
            // We use the position as ID
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            // Handle the case when we need a new view rather than recycling an old one.
            if (view == null) {
                final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(android.R.layout.select_dialog_singlechoice, parent, false);
            }

            // TODO: can this be null? Get rid of the condition?
            if (view != null) {
                final CheckedTextView checkedTextView = (CheckedTextView) view.findViewById(android.R.id.text1);
                final ItemFontType fontType = ItemFontType.values()[position];
                checkedTextView.setTypeface(fontType.getTypeface(getContext()));

                // If you want to make the selected item having different foreground or background
                // color, be aware of themes. In some of them your foreground color may be the background
                // color. So we don't mess with anything here.
                checkedTextView.setText(fontType.name);
                checkedTextView.setTextSize(20 * fontType.scale);
            }
            return view;
        }
    }

    public FontPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        final String defaultFontkey = attrs.getAttributeValue(PreferenceConstants.ANDROID_NAME_SPACE, "defaultValue");
        mDefaultValue = ItemFontType.fromKey(defaultFontkey, null);
        checkNotNull(mDefaultValue, "Key: [%s]", defaultFontkey);
        
        mValue = mDefaultValue;

        mSummaryFormat = attrs.getAttributeValue(PreferenceConstants.ANDROID_NAME_SPACE, "summary");
        updateSummaryWithCurrentValue();
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);
        final FontAdapter adapter = new FontAdapter();
        builder.setSingleChoiceItems(adapter, mValue.ordinal(), this);
        builder.setPositiveButton(null, null);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which >= 0 && which < ItemFontType.values().length) {
            final ItemFontType selectedFont = ItemFontType.values()[which];
            setValue(selectedFont);
            dialog.dismiss();
        }
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        super.onSetInitialValue(restore, defaultValue);
        if (restore) {
            mValue = shouldPersist() ? readValue() : mDefaultValue;
        } else {
            mValue = ItemFontType.fromKey((String)defaultValue, mDefaultValue);
        }
        updateSummaryWithCurrentValue();
    }

    public final void setValue(ItemFontType font) {
        mValue = font;
        Editor editor = getSharedPreferences().edit();
        editor.putString(getKey(), font.getKey());
        editor.commit();
        updateSummaryWithCurrentValue();
    }

    private final ItemFontType readValue() {
        final SharedPreferences sharedPreferences = getSharedPreferences();
        if (sharedPreferences == null) {
            // Shared preferences not bound yet
            return mDefaultValue;
        }
        final String selectedFontKey = sharedPreferences.getString(getKey(), mDefaultValue.getKey());
        return ItemFontType.fromKey(selectedFontKey, mDefaultValue);
    }
    
    private final void updateSummaryWithCurrentValue() {
        super.setSummary(String.format(mSummaryFormat, mValue.name));
    }
}