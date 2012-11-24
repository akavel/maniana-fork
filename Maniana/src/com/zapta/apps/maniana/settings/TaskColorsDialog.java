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

//import net.margaritov.preference.colorpicker.R;

import static com.zapta.apps.maniana.util.Assertions.checkNotNull;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;
import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.model.ItemColor;
import com.zapta.apps.maniana.util.LogUtil;

public class TaskColorsDialog extends Dialog {

    public static int adapterInstance = 0;
    
    // List adaptor over Font.values()
    public class ColorAdapter extends BaseAdapter implements DropListener {
        @SuppressWarnings("unused")
        private final Context mContext;

        private final ArrayList<ItemColor> colors = new ArrayList<ItemColor>();
        
        private final int instance;

        public ColorAdapter(Context mContext) {
            super();
            this.instance = ++adapterInstance;
            this.mContext = mContext;

            for (ItemColor color : ItemColor.values()) {
                colors.add(color);
            }
            LogUtil.info("*** Adapter %d construted.", instance);
        }

        @Override
        public int getCount() {
            return colors.size();
        }

        @Override
        public Object getItem(int position) {
            // return ItemColor.values()[position];
            return colors.get(position);
        }

        @Override
        public long getItemId(int position) {
            // We use the position as ID
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LogUtil.error("*** Adapter %d: getView(): %d  %s", instance, position, (convertView == null) ? "NULL" : "NON NULL");
            View view = convertView;

            // Handle the case when we need a new view rather than recycling an old one.
            if (view == null) 
            {
                LogUtil.info("*** Adapter %d getView(), creating new view.", instance);
                final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.task_colors_item_layout, parent, false);
            }

            // final ItemColor color = ItemColor.values()[position];
            final ItemColor color = colors.get(position);
            final View colorView = view.findViewById(R.id.task_colors_item_drag_color);
            
            final int colorValue = color.getColor(0xffffffff);
            LogUtil.info("***  color value: %032x %s", colorValue, color);
            
            colorView.setBackgroundColor(colorValue);
            // final TextView textView = (TextView) view.findViewById(R.id.task_colors_item_text);
            // final Font font = Font.values()[position];
            // final TypefaceSpec fontSpec = font.getTypefaceSpec(getContext());
            // checkedTextView.setTypeface(fontSpec.typeface);

            // If you want to make the selected item having different foreground or background
            // color, be aware of themes. In some of them your foreground color may be the
            // background
            // color. So we don't mess with anything here.
            // textView.setText(font.getName(mContext));
            // textView.setTextSize(20 * fontSpec.scale);
            
           // view.invalidate();
            return view;
        }

        /** From DragListener interface. Called when the user completes a drag */
        @Override
        public void drop(int from, int to) {
            LogUtil.error("*** Adapter DRAG: %d -> %d", from, to);
            
            // @@@ TODO: share logic between the two cases.
            if (from < to) {
                final ItemColor color = colors.remove(from);
                colors.add(to, color);
                notifyDataSetChanged();
                //return;
            }

            if (to < from) {
                final ItemColor color = colors.remove(from);
                colors.add(to, color);
                notifyDataSetChanged();
               // return;
            }
            
            for (int i = 0; i < colors.size(); i++) {
                LogUtil.error("  Color %d : %s", i, colors.get(i));
            }

            // TODO Auto-generated method stub

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
            LogUtil.error("*** DSLV controller constructed");
        }

        @Override
        public View onCreateFloatView(int position) {
            LogUtil.error("*** DSLV controller: onCreateFloatView");
            checkNotNull(adapter);
            // Log.INFO("***", "Adapter = " + adapter);
            View v = adapter.getView(position, null, mDslv);
            checkNotNull(v);
            checkNotNull(v.getBackground());
            v.getBackground().setLevel(10000);
            return v;
        }

        @Override
        public void onDestroyFloatView(View floatView) {
            LogUtil.error("*** DSLV controller: onDestroyFloatView");
            // do nothing; block super from crashing
        }

        // @Override
        // public int startDragPosition(MotionEvent ev) {
        // int res = super.dragHandleHitPosition(ev);
        // // int width = mDslv.getWidth();
        //
        // // if (ev.getX() < (width * 0.8)) {
        // LogUtil.error("*** DSLV controller: startDragPosition: %d", res);
        // return res;
        // // }
        // // LogUtil.error("*** DSLV controller: startDragPosition: MISS");
        // // return DragSortController.MISS;
        //
        // }
    }

    // private ColorPickerView mColorPicker;
    //
    // private ColorPickerPanelView mOldColor;
    //
    // private ColorPickerPanelView mNewColor;
    //
    // private OnColorChangedListener mListener;
    //
    // public interface OnColorChangedListener {
    // public void onColorChanged(int color);
    // }

    public TaskColorsDialog(Context context) {
        super(context);

        // init(initialColor);
        // }
        //
        // private void init(int color) {
        // // To fight color branding.
        // getWindow().setFormat(PixelFormat.RGBA_8888);
        //
        // setUp(color);
        //
        // }
        //
        // private void setUp(int color) {

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

        // dslvController.set

        setContentView(layout);

        setTitle("Reorder And Select Colors");

        // mColorPicker = (ColorPickerView) layout.findViewById(R.id.color_picker_view);
        // mOldColor = (ColorPickerPanelView) layout.findViewById(R.id.old_color_panel);
        // mNewColor = (ColorPickerPanelView) layout.findViewById(R.id.new_color_panel);
        //
        // ((LinearLayout) mOldColor.getParent()).setPadding(
        // Math.round(mColorPicker.getDrawingOffset()), 0,
        // Math.round(mColorPicker.getDrawingOffset()), 0);
        //
        // mOldColor.setOnClickListener(this);
        // mNewColor.setOnClickListener(this);
        // mColorPicker.setOnColorChangedListener(this);
        // mOldColor.setColor(color);
        // mColorPicker.setColor(color, true);

    }

    // @Override
    // public void onColorChanged(int color) {
    //
    // mNewColor.setColor(color);
    //
    // /*
    // * if (mListener != null) { mListener.onColorChanged(color); }
    // */
    //
    // }

    // public void setAlphaSliderVisible(boolean visible) {
    // mColorPicker.setAlphaSliderVisible(visible);
    // }

    // /**
    // * Set a OnColorChangedListener to get notified when the color selected by the user has
    // changed.
    // *
    // * @param listener
    // */
    // public void setOnColorChangedListener(OnColorChangedListener listener) {
    // mListener = listener;
    // }
    //
    // public int getColor() {
    // return mColorPicker.getColor();
    // }
    //
    // public void setJustHsNoV(float maxSaturation) {
    // mColorPicker.setJustHsNoV(maxSaturation);
    // }
    //
    // @Override
    // public void onClick(View v) {
    // if (v.getId() == R.id.new_color_panel) {
    // if (mListener != null) {
    // mListener.onColorChanged(mNewColor.getColor());
    // }
    // }
    // dismiss();
    // }

}
