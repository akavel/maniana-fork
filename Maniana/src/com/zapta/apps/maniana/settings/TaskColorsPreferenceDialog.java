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
package com.zapta.apps.maniana.settings;

import static com.zapta.apps.maniana.util.Assertions.checkNotNull;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;
import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.model.ItemColor;

public class TaskColorsPreferenceDialog extends Dialog {

    private static class ListItem {
        private final ItemColor color;
        boolean isEnabled;

        private ListItem(ItemColor color, boolean isEnabled) {
            this.color = color;
            this.isEnabled = isEnabled;
        }
    }

    public class ColorAdapter extends BaseAdapter implements DropListener {
        @SuppressWarnings("unused")
        private final Context mContext;

        private final ArrayList<ListItem> listItems = new ArrayList<ListItem>();

        public ColorAdapter(Context mContext) {
            super();
            this.mContext = mContext;

            for (ItemColor color : ItemColor.values()) {
                listItems.add(new ListItem(color, true));
            }
        }

        @Override
        public int getCount() {
            return listItems.size();
        }

        @Override
        public Object getItem(int position) {
            return listItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            // We use the position as ID
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;

            // If not recycling an existing item view, create a new one.
            if (view == null) {
                final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.task_colors_item_layout, parent, false);
            }

            final ListItem listItem = listItems.get(position);

            // Set color
            final View colorView = view.findViewById(R.id.task_colors_item_drag_color);
            int colorValue = listItem.color.getColor(0xffffffff);

            // @@@ TODO: update the bit arithmetic to force 40 regardless of previus alpha value.
            final int mask = listItem.isEnabled ? 0xffffffff : 0x60ffffff;
            colorValue = colorValue & mask;
            colorView.setBackgroundColor(colorValue);

            // Set checkbox

            final CheckBox checkbox = (CheckBox) view.findViewById(R.id.task_colors_item_checkbox);
            // Disable the old listener so we don't callback the handle when setting a reused view.
            checkbox.setOnCheckedChangeListener(null);
            checkbox.setChecked(listItem.isEnabled);

            // Set the new listener.
            if (listItem.color == ItemColor.NONE) {
                checkbox.setEnabled(false);
            } else {
                checkbox.setEnabled(true);
                checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(final CompoundButton buttonView,
                            final boolean isChecked) {
                        handleCheckboxChange(position, isChecked);
                    }
                });
            }

            return view;
        }

        private void handleCheckboxChange(int position, boolean isChecked) {
            final ListItem listItem = listItems.get(position);
            listItem.isEnabled = isChecked;

            // Refresh the display to propagate color change due to check change.
            notifyDataSetChanged();
        }

        /** From DragListener interface. Called when the user completes a drag */
        @Override
        public void drop(int from, int to) {
            // NOTE: we don't filter out the special case of from == to, since it
            // is handled gracefully.

            final ListItem listItem = listItems.remove(from);
            listItems.add(to, listItem);
            notifyDataSetChanged();
        }
    }

    private class MyDSController extends DragSortController {

        DragSortListView mDslv;
        ColorAdapter adapter;

        public MyDSController(DragSortListView dslv, ColorAdapter adapter) {
            super(dslv);
            this.mDslv = dslv;
            this.adapter = adapter;
            // View id in the item layout that is used as a drag handle.
            setDragHandleId(R.id.task_colors_item_drag_handle);
        }

        @Override
        public View onCreateFloatView(int position) {
            checkNotNull(adapter);
            View v = adapter.getView(position, null, mDslv);
            checkNotNull(v);
            checkNotNull(v.getBackground());
            v.getBackground().setLevel(10000);
            return v;
        }

        @Override
        public void onDestroyFloatView(View floatView) {
            // LogUtil.error("*** DSLV controller: onDestroyFloatView");
            // do nothing; block super from crashing
        }
    }

    public TaskColorsPreferenceDialog(Context context) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        View layout = inflater.inflate(R.layout.task_colors_layout, null);

        DragSortListView dslv = (DragSortListView) layout.findViewById(R.id.task_colors_list_view);

        ColorAdapter adapter = new ColorAdapter(getContext());

        dslv.setAdapter(adapter);

        MyDSController dslvController = new MyDSController(dslv, adapter);
        dslv.setFloatViewManager(dslvController);
        dslv.setOnTouchListener(dslvController);
        dslv.setDragEnabled(true);

        dslv.setDropListener(adapter);

        setContentView(layout);

        // TODO: get the string from a resource
        setTitle("Reorder And Select Colors");
    }
}
