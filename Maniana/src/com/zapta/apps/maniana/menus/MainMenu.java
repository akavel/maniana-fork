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

package com.zapta.apps.maniana.menus;

import static com.zapta.apps.maniana.util.Assertions.checkNotNull;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.annotations.MainActivityScope;
import com.zapta.apps.maniana.main.MainActivityState;
import com.zapta.apps.maniana.util.LogUtil;
import com.zapta.apps.maniana.util.PopupsTracker.TrackablePopup;

/**
 * Main menu.
 * 
 * @author Tal Dayan (adapted to Maniana) Based on example by Lorensius W. L. T
 *         <lorenz@londatiga.net>.
 */
@MainActivityScope
public class MainMenu implements OnDismissListener, TrackablePopup {

    public interface OnActionItemOutcomeListener {
        void onOutcome(MainMenu source, MainMenuEntry selectedEntry);
    }

    private final MainActivityState mMainActivityState;

    /** The window that contains the menu's top view. */
    private final PopupWindow mMenuWindow;

    private View mTopView;

    private ImageView mUpArrowView;

    private ViewGroup mItemContainerView;

    private final OnActionItemOutcomeListener mOutcomeListener;

    /**
     * Constructor allowing orientation override
     * 
     * @param mContext Context
     * @param orientation Layout orientation, can be vartical or horizontal
     */
    public MainMenu(MainActivityState mainActivityState, OnActionItemOutcomeListener outcomeListener) {
        mMainActivityState = mainActivityState;
        mMenuWindow = new PopupWindow(mainActivityState.context());

        mMenuWindow.setTouchInterceptor(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO: dismiss if outside of mItemContainsView.
                LogUtil.debug("*** onTouch: %f, %f", event.getX(), event.getY());
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    mMenuWindow.dismiss();
                    return true;
                }
                return false;
            }
        });

        mOutcomeListener = checkNotNull(outcomeListener);

        mTopView = (ViewGroup) mMainActivityState.services().layoutInflater()
                .inflate(R.layout.main_menu, null);

        mItemContainerView = (ViewGroup) mTopView.findViewById(R.id.items_container);

        mUpArrowView = (ImageView) mTopView.findViewById(R.id.arrow_up);

        mMenuWindow.setContentView(mTopView);
        mMenuWindow.setOnDismissListener(this);
    }

    /**
     * Get action item at given index
     */
    public final MainMenuEntry getActionItem(int index) {
        return MainMenuEntry.values()[index];
        // return actionItems.get(index);
    }

    /**
     * Add an action item to the end of the list.
     */
    private final void addEntry(final MainMenuEntry entry) {
        // actionItems.add(actionItem);

        // TODO: rename this to action_wrapper here and in the layout.
        final View entryTopView = mMainActivityState.services().layoutInflater()
                .inflate(R.layout.main_menu_entry, null);

        final ImageView imageView = (ImageView) entryTopView
                .findViewById(R.id.main_menu_entry_icon);
        imageView.setImageResource(entry.iconResourceId);

        final TextView textView = (TextView) entryTopView.findViewById(R.id.main_menu_entry_text);
        textView.setText(entry.textResourceId);

        // Set a listener to track touches and highlight pressed items.
        entryTopView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // NOTE: this clears any padding set on the top view (in case it is set in xml).
                    entryTopView.setBackgroundResource(R.drawable.popup_menu_entry_selected);
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL
                        || event.getAction() == MotionEvent.ACTION_UP || !entryTopView.isPressed()) {
                    entryTopView.setBackgroundResource(0);
                }
                return false;
            }
        });

        entryTopView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOutcomeListener.onOutcome(MainMenu.this, entry);
                // mActioWasSelected = true;
                mMenuWindow.dismiss();
            }
        });

        entryTopView.setFocusable(true);
        entryTopView.setClickable(true);

        mItemContainerView.addView(entryTopView);
    }

    /**
     * Show the popup action menu over a given anchor view.
     * 
     * @param anchorView a view to which the action menu's arrow will point it.
     */
    public final void show(View parentView, View anchorView) {

        for (MainMenuEntry entry : MainMenuEntry.values()) {
            addEntry(entry);
        }

        checkNotNull(mTopView, "setContentView was not called with a view to display.");

        // Set transparent window background. This will clear the horizontal strips above and below
        // the menu defined by the two arrows.
        mMenuWindow.setBackgroundDrawable(new BitmapDrawable());

        mMenuWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        mMenuWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mMenuWindow.setTouchable(true);
        mMenuWindow.setFocusable(true);
        mMenuWindow.setOutsideTouchable(true);

        mMenuWindow.setContentView(mTopView);
        
        // @@@ experimental
//        mTopView.setFocusable(true);
//        mTopView.setClickable(true);
        //mTopView.set

        // mActioWasSelected = false;

        final int[] anchorXYOnsScreen = new int[2];

        anchorView.getLocationOnScreen(anchorXYOnsScreen);

        final Rect anchorRectOnScreen = new Rect(anchorXYOnsScreen[0], anchorXYOnsScreen[1],
                anchorXYOnsScreen[0] + anchorView.getWidth(), anchorXYOnsScreen[1]
                        + anchorView.getHeight());

        mTopView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

//        final int rootHeight = mTopView.getMeasuredHeight();
        final int screenHeight = mMainActivityState.services().windowManager().getDefaultDisplay()
                .getHeight();

        // Arrow position is slightly to the right of the left upper/lower cornet.
        // TODO: define const.
        // TODO: scale by density?
        final int xPosPixels = 50;

        final int arrowPos = anchorRectOnScreen.left + xPosPixels;

       // int spaceAbove = anchorRectOnScreen.top;
        int spaceBelow = screenHeight - anchorRectOnScreen.bottom;

        // TODO: make a const or param.
        // TODO: scale by density?
        //final int ARROW_VERTICAL_OVERLAP = 15;

        final int yPosPixels;

        yPosPixels = anchorRectOnScreen.bottom; // - ARROW_VERTICAL_OVERLAP;
//        if (rootHeight > spaceBelow) {
//            mItemContainerView.getLayoutParams().height = spaceBelow;
//        }

        // TODO: see if PopupWindow.showAsDropDown() helps here

        setArrow(arrowPos);
        mMenuWindow.setAnimationStyle(R.style.Animations_MainMenu);
        mMainActivityState.popupsTracker().track(this);
        mMenuWindow.showAtLocation(parentView, Gravity.NO_GRAVITY, xPosPixels, yPosPixels);
    }

    /**
     * Show arrow
     * 
     * @param whichArrow arrow type resource id
     * @param requestedX distance from left screen
     */
    private final void setArrow(int requestedX) {
        // Decide which of the two up and down arrows will be shown and hidden respectivly.
        // final View showArrow = (whichArrow == R.id.arrow_up) ? mUpArrowView : mDownArrowView;
        // final View hideArrow = (whichArrow == R.id.arrow_up) ? mDownArrowView : mUpArrowView;

        final int arrowWidth = mUpArrowView.getMeasuredWidth();
        // showArrow.setVisibility(View.VISIBLE);
        ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) mUpArrowView
                .getLayoutParams();
        param.leftMargin = requestedX - arrowWidth / 2;
        // hideArrow.setVisibility(View.INVISIBLE);
    }

    /** Called when the window is dismissed. */
    @Override
    public final void onDismiss() {
        mMainActivityState.popupsTracker().untrack(this);
    }

    public final boolean isShowing() {
        return mMenuWindow.isShowing();
    }

    /** Public method to dismiss the menu. */
    public final void dismiss() {
        if (isShowing()) {
            mMenuWindow.dismiss();
        }
    }

    /** Called by the popup tracker. */
    @Override
    public final void closeLeftOver() {
        dismiss();
    }
}
