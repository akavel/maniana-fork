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

package com.zapta.apps.maniana.quick_action;

import static com.zapta.apps.maniana.util.Assertions.check;
import static com.zapta.apps.maniana.util.Assertions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.main.AppContext;

/**
 * Quick action popup menu.
 * 
 * @author Tal Dayan (adapted to Maniana)
 * Based on example by Lorensius W. L. T <lorenz@londatiga.net>.
 */
public class QuickActionMenu implements OnDismissListener {

    private final AppContext mApp;

    /** The window that contains the menu's top view. */
    private final PopupWindow mMenuWindow;

    private View mTopView;

    private ImageView mUpArrowView;
    private ImageView mDownArrowView;

    private ViewGroup mItemContainerView;

    private final OnActionItemOutcomeListener mOutcomeListener;

    private final List<QuickActionItem> actionItems = new ArrayList<QuickActionItem>();

    private boolean mActioWasSelected;

    /**
     * Constructor allowing orientation override
     * 
     * @param mContext Context
     * @param orientation Layout orientation, can be vartical or horizontal
     */
    public QuickActionMenu(AppContext app, OnActionItemOutcomeListener outcomeListener) {
        mApp = app;
        mMenuWindow = new PopupWindow(app.context());

        mMenuWindow.setTouchInterceptor(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    mMenuWindow.dismiss();
                    return true;
                }
                return false;
            }
        });

        mOutcomeListener = checkNotNull(outcomeListener);

        mTopView = (ViewGroup) mApp.services().layoutInflater().inflate(R.layout.quick_action_menu,
                        null);

        mItemContainerView = (ViewGroup) mTopView.findViewById(R.id.itemsContainer);

        mDownArrowView = (ImageView) mTopView.findViewById(R.id.arrow_down);
        mUpArrowView = (ImageView) mTopView.findViewById(R.id.arrow_up);

        mTopView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT));

        mMenuWindow.setContentView(mTopView);
        mMenuWindow.setOnDismissListener(this);
    }

    /**
     * Get action item at given index
     */
    public final QuickActionItem getActionItem(int index) {
        return actionItems.get(index);
    }

    /**
     * Add an action item to the end of the list.
     */
    public final void addActionItem(final QuickActionItem actionItem) {
        actionItems.add(actionItem);

        // TODO: rename this to action_wrapper here and in the layout.
        final View wrapperView = mApp.services().layoutInflater().inflate(
                        R.layout.quick_action_item, null);

        final ImageView imageView = (ImageView) wrapperView
                        .findViewById(R.id.quick_action_item_icon);
        imageView.setImageDrawable(actionItem.getIcon());

        final TextView textView = (TextView) wrapperView.findViewById(R.id.quick_action_item_text);
        textView.setText(actionItem.getLabel());

        // Set a listener to track touches and highlight pressed items.
        wrapperView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    wrapperView.setBackgroundResource(R.drawable.quick_action_item_selected);
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL
                                || event.getAction() == MotionEvent.ACTION_UP
                                || !wrapperView.isPressed()) {
                    wrapperView.setBackgroundColor(Color.TRANSPARENT);
                }
                return false;
            }
        });

        wrapperView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mOutcomeListener.onOutcome(QuickActionMenu.this, actionItem);
                mActioWasSelected = true;
                mMenuWindow.dismiss();
            }
        });

        wrapperView.setFocusable(true);
        wrapperView.setClickable(true);

        // If not first, add seperator before it.
        if (mItemContainerView.getChildCount() > 0) {
            final View separator = mApp.services().layoutInflater().inflate(
                            R.layout.quick_action_item_separator, null);
            // TODO: move this configuration to the XML
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                            LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
            separator.setLayoutParams(params);
            separator.setPadding(5, 0, 5, 0);

            mItemContainerView.addView(separator);
        }

        mItemContainerView.addView(wrapperView);
    }

    /**
     * Show the popup action menu over a given anchor view.
     * 
     * @param anchorView a view to which the action menu's arrow will point it.
     */
    public final void show(View anchorView) {

        if (mTopView == null) {
            throw new IllegalStateException("setContentView was not called with a view to display.");
        }

        // if (mBackground == null) {

        // Set transparent window background. This will clear the horizontal strips above and below
        // the menu defined by the two arrows.
        mMenuWindow.setBackgroundDrawable(new BitmapDrawable());

        // } else {
        // // TODO: remove mBackground member.
        // throw new RuntimeException("Non reachable.");
        // // mMenuWindow.setBackgroundDrawable(mBackground);
        // }

        mMenuWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        mMenuWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mMenuWindow.setTouchable(true);
        mMenuWindow.setFocusable(true);
        mMenuWindow.setOutsideTouchable(true);

        mMenuWindow.setContentView(mTopView);

        mActioWasSelected = false;

        final int[] anchorXYOnsScreen = new int[2];

        anchorView.getLocationOnScreen(anchorXYOnsScreen);

        final Rect anchorRectOnScreen = new Rect(anchorXYOnsScreen[0], anchorXYOnsScreen[1],
                        anchorXYOnsScreen[0] + anchorView.getWidth(), anchorXYOnsScreen[1]
                                        + anchorView.getHeight());

        mTopView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        final int rootHeight = mTopView.getMeasuredHeight();
        final int screenHeight = mApp.services().windowManager().getDefaultDisplay().getHeight();

        // Arrow position is slightly to the left.
        // TODO: define const.
        final int xPos = 50;

        final int arrowPos = anchorRectOnScreen.left + xPos;

        int spaceAbove = anchorRectOnScreen.top;
        int spaceBelow = screenHeight - anchorRectOnScreen.bottom;

        final boolean showAbove = spaceAbove >= rootHeight;

        // TODO: make a const or param.
        final int ARROW_VERTICAL_OVERLAP = 15;

        final int yPos;

        if (showAbove) {
            check(rootHeight <= spaceAbove);
            yPos = anchorRectOnScreen.top - rootHeight + ARROW_VERTICAL_OVERLAP;
        } else {
            yPos = anchorRectOnScreen.bottom - ARROW_VERTICAL_OVERLAP;
            if (rootHeight > spaceBelow) {
                mItemContainerView.getLayoutParams().height = spaceBelow;
            }
        }

        showArrow(((showAbove) ? R.id.arrow_down : R.id.arrow_up), arrowPos);
        mMenuWindow.setAnimationStyle((showAbove) ? R.style.Animations_QuickActionAbove
                        : R.style.Animations_QuickActionBelow);
        mMenuWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, xPos, yPos);
    }

    /**
     * Show arrow
     * 
     * @param whichArrow arrow type resource id
     * @param requestedX distance from left screen
     */
    private final void showArrow(int whichArrow, int requestedX) {
        // Decide which of the two up and down arrows will be shown and hidden respectivly.
        final View showArrow = (whichArrow == R.id.arrow_up) ? mUpArrowView : mDownArrowView;
        final View hideArrow = (whichArrow == R.id.arrow_up) ? mDownArrowView : mUpArrowView;

        final int arrowWidth = mUpArrowView.getMeasuredWidth();
        showArrow.setVisibility(View.VISIBLE);
        ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) showArrow
                        .getLayoutParams();
        param.leftMargin = requestedX - arrowWidth / 2;
        hideArrow.setVisibility(View.INVISIBLE);
    }

    @Override
    public final void onDismiss() {
        if (!mActioWasSelected) {
            mOutcomeListener.onOutcome(this, null);
        }
    }

    public interface OnActionItemOutcomeListener {
        /** Action item is null if dismissed with no selection. */
        void onOutcome(QuickActionMenu source, /* @Nullable */QuickActionItem actionItem);
    }
}