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
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.zapta.apps.maniana.model.ItemColor;
import com.zapta.apps.maniana.util.ColorUtil;
import com.zapta.apps.maniana.util.DisplayUtil;

/**
 * A preference type that allows a user to choose an ItemColor.
 * 
 * @author Tal Dayan
 * <p>
 * Based of ColorPickerPreference by Sergey Margaritov.
 */
public class ItemColorPreference extends DialogPreference implements
        DialogInterface.OnClickListener {
    // View in the setting page, not the popup dialog.
    View mView;

    private ItemColor mDefaultValue = ItemColor.NONE;
    private ItemColor mValue = ItemColor.NONE;

    private final float mDensity;

    /** List adaptor over ItemColor.values() */
    public class ItemColorAdapter extends BaseAdapter {
        final Context mContext;

        public ItemColorAdapter(Context mContext) {
            super();
            this.mContext = mContext;
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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            // Handle the case when we need a new view rather than recycling an old one.
            if (view == null) {
                final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(android.R.layout.select_dialog_singlechoice, parent, false);
            }

            final CheckedTextView checkedTextView = (CheckedTextView) view
                    .findViewById(android.R.id.text1);

            final ItemColor itemColor = ItemColor.values()[position];
            checkedTextView.setText(mContext.getString(itemColor.nameResourceId));
            checkedTextView.setBackgroundColor(itemColor.getColor(0xffe0e0e0));
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
        final ItemColorAdapter adapter = new ItemColorAdapter(getContext());
        builder.setSingleChoiceItems(adapter, mValue.ordinal(), this);
        builder.setPositiveButton(null, null);
    }

    // Called from the popup dialog
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which >= 0 && which < Font.values().length) {
            final ItemColor selectedItemColor = ItemColor.values()[which];
            setValue(selectedItemColor);
            dialog.dismiss();
        }
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

    private void setPreviewColor() {
        if (mView == null) {
            return;
        }

        LinearLayout widgetFrameView = ((LinearLayout) mView
                .findViewById(android.R.id.widget_frame));
        if (widgetFrameView == null) {
            return;
        }

        // TAL: required starting from API 14
        widgetFrameView.setVisibility(View.VISIBLE);

        boolean preApi14 = android.os.Build.VERSION.SDK_INT < 14;

        final int rightPaddingDip = preApi14 ? 8 : 5;

        widgetFrameView.setPadding(widgetFrameView.getPaddingLeft(),
                widgetFrameView.getPaddingTop(), (int) (mDensity * rightPaddingDip),
                widgetFrameView.getPaddingBottom());

        // Remove previously created preview image.
        int count = widgetFrameView.getChildCount();
        if (count > 0) {
            widgetFrameView.removeViews(0, count);
        }

        ImageView iView = new ImageView(getContext());
        iView.setImageBitmap(getPreviewBitmap());
        iView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));

        widgetFrameView.addView(iView);
    }

    /** TODO: simplify, use drawing method to setup the bitmap. */
    private Bitmap getPreviewBitmap() {
        int d = (int) (mDensity * 31); // 30dip
        // If disabled we simulate a semi transparent black overlay.
        // NOTE: could simplify compositeColor for the special case where argb2 is aa000000;
        int baseColor = getValue().getColor(0xffffffff);
        int color = isEnabled() ? baseColor : ColorUtil.compositeColor(baseColor, 0xaa000000);
        Bitmap bm = Bitmap.createBitmap(d, d, Config.ARGB_8888);
        // TODO: is w=d, h=d?
        int w = bm.getWidth();
        int h = bm.getHeight();
        int c = color;
        for (int i = 0; i < w; i++) {
            // Iterate over a symmetry triangle. This saves color computations.
            // TODO(tal): why not draw a rectangle and line frames instead?
            for (int j = i; j < h; j++) {
                // Frame or inside color
                c = (i <= 1 || j <= 1 || i >= w - 2 || j >= h - 2) ? Color.GRAY : color;
                bm.setPixel(i, j, c);
                if (i != j) {
                    bm.setPixel(j, i, c);
                }
            }
        }

        return bm;
    }
}
