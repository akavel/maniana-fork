/*
 * Copyright (C) 2011 Sergey Margaritov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zapta.apps.maniana.settings;

import static com.zapta.apps.maniana.util.Assertions.checkNotNull;

import javax.annotation.Nullable;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.model.ItemColor;
import com.zapta.apps.maniana.util.BitmapUtil;
import com.zapta.apps.maniana.util.DisplayUtil;

/**
 * A preference type that allows a user to choose an ItemColor.
 * <p>
 * Based of ColorPickerPreference by Sergey Margaritov.
 * 
 * @author Tal Dayan
 */
public class ItemColorPreference extends DialogPreference {
    private static final boolean PRE_API_14 = android.os.Build.VERSION.SDK_INT < 14;
    
    // View in the setting page, not the popup dialog.
    View mView;

    private ItemColor mDefaultValue = ItemColor.NONE;
    private ItemColor mValue = ItemColor.NONE;

    private final float mDensity;

    /** List adaptor over ItemColor.values() */
    public class ItemColorAdapter extends BaseAdapter {
        final Context mContext;

        @Nullable
        private final ItemColor mSelectedItemColor;

        public ItemColorAdapter(Context mContext, @Nullable ItemColor selectedItemColor) {
            super();
            this.mContext = mContext;
            this.mSelectedItemColor = selectedItemColor;
        }

        @Override
        public int getCount() {
            return ItemColor.values().length;
        }

        @Override
        public Object getItem(int position) {
            return ItemColor.values()[position];
        }

        @Override
        public long getItemId(int position) {
            // We use the position as ID
            return position;
        }

        /** Return layout of a single row. */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            // Handle the case when we need a new view rather than recycling an old one.
            if (view == null) {
                final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                // The row layout.
                view = inflater.inflate(R.layout.item_default_color_preference_row_layout, parent,
                        false);
            }

            // Set color
            final ItemColor itemColor = ItemColor.values()[position];

            final ImageView imageView = (ImageView) view
                    .findViewById(R.id.item_default_color_preference_image);
            imageView.setImageBitmap(BitmapUtil.getPreferenceColorPreviewBitmap(
                    itemColor.getColor(0xffffffff), true, mDensity));

            // Set text
            final TextView textView = (TextView) view
                    .findViewById(R.id.item_default_color_preference_text);
            textView.setText(mContext.getString(itemColor.nameResourceId));

            // Set radio button
            final RadioButton radioButton = (RadioButton) view
                    .findViewById(R.id.item_default_color_preference_radio_button);
            radioButton.setChecked(itemColor == mSelectedItemColor);

            // Set click action
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemColorClicked(itemColor);
                }
            });

            return view;
        }
    }

    public ItemColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDensity = DisplayUtil.getDensity(context);
        final String defaultFontkey = attrs.getAttributeValue(
                PreferenceConstants.ANDROID_NAME_SPACE, "defaultValue");
        mDefaultValue = ItemColor.fromKey(defaultFontkey, null);
        checkNotNull(mDefaultValue, "Key: [%s]", defaultFontkey);
        mValue = mDefaultValue;
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);
        final ItemColorAdapter adapter = new ItemColorAdapter(getContext(), getValue());
        builder.setAdapter(adapter, this);
        builder.setPositiveButton(null, null);
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        super.onSetInitialValue(restore, defaultValue);
        if (restore) {
            mValue = shouldPersist() ? readValue() : mDefaultValue;
        } else {
            mValue = ItemColor.fromKey((String) defaultValue, mDefaultValue);
        }
    }

    public final void setValue(ItemColor itemColor) {
        mValue = itemColor;
        Editor editor = getSharedPreferences().edit();
        editor.putString(getKey(), itemColor.getKey());
        editor.commit();
        setPreviewColor();
    }

    private final ItemColor readValue() {
        final SharedPreferences sharedPreferences = getSharedPreferences();
        if (sharedPreferences == null) {
            // Shared preferences not bound yet
            return mDefaultValue;
        }
        final String selectedFontKey = sharedPreferences
                .getString(getKey(), mDefaultValue.getKey());
        return ItemColor.fromKey(selectedFontKey, mDefaultValue);
    }

    // @Override
    public ItemColor getValue() {
        if (isPersistent()) {
            String valueKey = getPersistedString("");
            mValue = ItemColor.fromKey(valueKey, mDefaultValue);
        }
        return mValue;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mView = view;
        setPreviewColor();
    }

    // Set the color swatch with the current color in the settings screen.
    private void setPreviewColor() {
        if (mView == null) {
            return;
        }

        LinearLayout widgetFrameView = ((LinearLayout) mView
                .findViewById(android.R.id.widget_frame));
        if (widgetFrameView == null) {
            return;
        }

        // NOTE: required starting from API 14
        widgetFrameView.setVisibility(View.VISIBLE);

        //boolean preApi14 = android.os.Build.VERSION.SDK_INT < 14;

        final int rightPaddingDip = PRE_API_14 ? 8 : 5;

        widgetFrameView.setPadding(widgetFrameView.getPaddingLeft(),
                widgetFrameView.getPaddingTop(), (int) (mDensity * rightPaddingDip),
                widgetFrameView.getPaddingBottom());

        // Remove previously created preview image.
        int count = widgetFrameView.getChildCount();
        if (count > 0) {
            widgetFrameView.removeViews(0, count);
        }

        final ImageView imageView = new ImageView(getContext());
        // TODO: do the same think in the general color preference, replacing the current
        // bitmap creation code there.
        final Bitmap colorPreviewBitmap = BitmapUtil.getPreferenceColorPreviewBitmap(getValue()
                .getColor(0xffffffff), isEnabled(), mDensity);
        imageView.setImageBitmap(colorPreviewBitmap);
        imageView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));

        widgetFrameView.addView(imageView);
    }

    /** Called when an item color is clicked in the list. */
    private void onItemColorClicked(ItemColor selectedItemColor) {
        setValue(selectedItemColor);
        getDialog().dismiss();
    }
}
