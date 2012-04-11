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

package com.zapta.apps.maniana.widget;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.model.AppModel;
import com.zapta.apps.maniana.model.ItemModelReadOnly;
import com.zapta.apps.maniana.services.AppServices;
import com.zapta.apps.maniana.settings.ItemFontVariation;
import com.zapta.apps.maniana.util.BitmapUtil;
import com.zapta.apps.maniana.util.DebugTimer;
import com.zapta.apps.maniana.util.DisplayUtil;
import com.zapta.apps.maniana.util.FileUtil;
import com.zapta.apps.maniana.util.LogUtil;
import com.zapta.apps.maniana.util.Orientation;
import com.zapta.apps.maniana.widget.ListWidgetSize.OrientationInfo;

/**
 * 
 * List widget template to be drawn as a bitmap. It does not include the paper background which is
 * added later at the remoteviews level.
 * 
 * @author Tal Dayan
 */
public class ListWidgetProviderTemplate {

    @Nullable
    private final AppModel mModel;
    private final Context mContext;
    private final LinearLayout mTopView;
    private final TextView mToolbarTitleTextView;
    private final LinearLayout mItemListView;
    private final List<View> mItemViews;
    private final List<TextView> mItemTextViews;
    private final LayoutInflater mLayoutInflater;

    // Preferences
    private final ItemFontVariation mFontVariationPreference;
    private final boolean mAutoFitPreference;
    private final boolean mPaperPreference;
    private final int mBackgroundColorPreference;
    private final boolean mToolbarEanbledPreference;
    private final boolean mIncludeCompletedItemsPreference;
    private final boolean mSingleLinePreference;

    public ListWidgetProviderTemplate(Context context, @Nullable AppModel model,
            boolean paperPreference, int backgroundColorPreference,
            boolean toolbarEanbledPreference, boolean includeCompletedItemsPreference,
            boolean singleLinePreference, ItemFontVariation fontVariationPreference,
            boolean autoFitPreference) {
        mContext = context;
        mModel = model;
        mPaperPreference = paperPreference;
        mBackgroundColorPreference = backgroundColorPreference;
        mToolbarEanbledPreference = toolbarEanbledPreference;
        mIncludeCompletedItemsPreference = includeCompletedItemsPreference;
        mSingleLinePreference = singleLinePreference;
        mFontVariationPreference = fontVariationPreference;
        mAutoFitPreference = autoFitPreference;

        // TODO: need this only is using auto shrink
        mLayoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mTopView = (LinearLayout) mLayoutInflater.inflate(R.layout.widget_list_template_layout,
                null);
        mToolbarTitleTextView = (TextView) mTopView
                .findViewById(R.id.widget_list_template_toolbar_title);
        mItemListView = (LinearLayout) mTopView.findViewById(R.id.widget_list_template_item_list);

        mItemViews = new ArrayList<View>();
        mItemTextViews = new ArrayList<TextView>();

        // Set template background. This can be the background solid color or the paper color.
        final View backgroundColorView = mTopView.findViewById(R.id.widget_list_background_color);
        backgroundColorView.setBackgroundColor(mBackgroundColorPreference);

        // Does not set title size. This is done later.
        setTemplateToolbar();

        // Set template view item list
        populateTemplateItemList();
    }

    /**
     * Resize text. Used to fit text to the widget area. Returns the widget content height in pixels
     */
    private final void resizeText(ListWidgetSize listWidgetSize, int itemTextSize,
            boolean singleLine) {
        if (mToolbarEanbledPreference) {
            final int titleTextSize = WidgetUtil.titleTextSize(listWidgetSize, itemTextSize);
            mToolbarTitleTextView.setTextSize(titleTextSize);
        }
        for (TextView itemTextView : mItemTextViews) {
            itemTextView.setTextSize(itemTextSize);

            // NOTE: TextView has a bug that does not allows more than
            // two lines when using ellipsize. Otherwise we would give the user more
            // choices about the max number of lines. More details here:
            // http://code.google.com/p/android/issues/detail?id=2254
            if (singleLine) {
                itemTextView.setSingleLine(true);
                itemTextView.setMaxLines(1);
            } else {
                itemTextView.setSingleLine(false);
                // NOTE: on ICS (API 14) the text view behaves
                // differently and does not limit the lines to two when ellipsize. For
                // consistency, we limit it explicitly to two lines.
                //
                // TODO: file an Android bug about the different ICS behavior.
                //
                itemTextView.setMaxLines(2);
            }
        }
    }

    /** Set the image of a single orientation. */
    public final Uri renderOrientation(ListWidgetSize listWidgetSize, Orientation orientation,
            int widthPixels, int heightPixels, @Nullable PaperBackground paperBackground) {

        // Template view is now fully populated. Render it as a bitmap. First we render it
        // using screen native resolution.
        final float density = DisplayUtil.getDensity(mContext);

        final OrientationInfo orientationInfo = orientation.isPortrait ? listWidgetSize.portraitInfo
                : listWidgetSize.landscapeInfo;

        final int shadowRightPixels = mPaperPreference ? paperBackground
                .shadowRightPixels(widthPixels) : 0;
        final int shadowBottomPixels = mPaperPreference ? paperBackground
                .shadowBottomPixels(heightPixels) : 0;

        // Set padding to match the drop shadow portion of paper background, if used.
        mTopView.setPadding(0, 0, shadowRightPixels, shadowBottomPixels);

        resizeToFit(widthPixels, heightPixels, shadowBottomPixels, listWidgetSize, orientation);

        // NTOE: ARGB_4444 results in a smaller file than ARGB_8888 (e.g. 50K vs 150k)
        // but does not look as good.
        final Bitmap bitmap1 = Bitmap.createBitmap(widthPixels, heightPixels,
                Bitmap.Config.ARGB_8888);

        final Canvas canvas = new Canvas(bitmap1);
        mTopView.draw(canvas);

        // NOTE: rounding the bitmap here when paper background is selected will do nothing
        // since the paper background is added later via the remote views.
        final Bitmap bitmap2;
        if (mPaperPreference) {
            bitmap2 = bitmap1;
        } else {
            bitmap2 = BitmapUtil.roundCornersRGB888(bitmap1, (int) (4 * density + 0.5f));
        }

        // NOTE: RemoteViews class has an issue with transferring large bitmaps. As a workaround, we
        // transfer the bitmap using a file URI. We could transfer small widgets directly
        // as bitmap but use file based transfer for all sizes for the sake of simplicity.
        // For more information on this issue see http://tinyurl.com/75jh2yf

        final String fileName = orientationInfo.imageFileName;

        // We make the file world readable so the home launcher can pull it via the file URI.
        // TODO: if there are security concerns about having this file readable, append to it
        // a long random suffix and cleanup the old ones.
        FileUtil.writeBitmapToPngFile(mContext, bitmap2, fileName, true);

        final Uri fileUri = Uri.fromFile(new File(mContext.getFilesDir(), fileName));

        return fileUri;
    }

    // Resize the template in preparation for rendering.
    private final void resizeToFit(int widthPixels, int heightPixels, int shadowBottomPixels,
            ListWidgetSize listWidgetSize, Orientation orientation) {
        DebugTimer timer = new DebugTimer();

        // TODO: normalize min size
        final int maxHeight = heightPixels - shadowBottomPixels;
        boolean singleLine = mSingleLinePreference;
        final int minSize = 13;
        int textSize = mFontVariationPreference.getTextSize();
        for (;;) {
            // TODO: iterate to shrink if needed
            resizeText(listWidgetSize, textSize, singleLine);

            mTopView.measure(MeasureSpec.makeMeasureSpec(widthPixels, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(heightPixels, MeasureSpec.EXACTLY));
            // TODO: subtract '1' from ends?
            mTopView.layout(0, 0, widthPixels, heightPixels);

            final int bottomPixels = mItemListView.getBottom();
            LogUtil.debug(
                    "*** Orientation: %s, textSize: %s, singleLine: %s, bottom: %d, height: %d",
                    orientation, textSize, singleLine, bottomPixels, heightPixels);

            if (!mAutoFitPreference) {
                break;
            }

            // TODO: Consider also paper shadow
            // TODO: normalize the margin from dips to pixels
            if (bottomPixels < maxHeight - 2) {
                break;
            }
            if (textSize > minSize) {
                textSize = Math.max(minSize, textSize - 1);
                continue;
            }
            if (!singleLine) {
                singleLine = true;
                textSize = mFontVariationPreference.getTextSize();
                continue;
            }

            // Failed to fit
            // TODO: make N last items invisible and enable a '8 more items ...'.
            break;
        }

        timer.report("Fit done");
    }

    /**
     * Populate the given template layout with task data and/or informative messages.
     */
    private final void populateTemplateItemList() {
        // For debugging
        final boolean debugTimestamp = false;
        if (debugTimestamp) {
            final String message = String.format("[%s]", SystemClock.elapsedRealtime() / 1000);
            addTemplateMessageItem(message);
        }

        if (mModel == null) {
            addTemplateMessageItem("(Maniana data not found)");
            return;
        }

        final List<ItemModelReadOnly> items = WidgetUtil.selectTodaysItems(mModel,
                mIncludeCompletedItemsPreference);

        if (items.isEmpty()) {
            final String emptyMessage = mIncludeCompletedItemsPreference ? "(no tasks)"
                    : "(no active tasks)";
            addTemplateMessageItem(emptyMessage);
            return;
        }

        for (ItemModelReadOnly item : items) {
            final LinearLayout itemView = (LinearLayout) mLayoutInflater.inflate(
                    R.layout.widget_list_template_item_layout, null);
            final TextView textView = (TextView) itemView.findViewById(R.id.widget_item_text_view);
            final View colorView = itemView.findViewById(R.id.widget_item_color);

            textView.setText(item.getText());
            mFontVariationPreference.apply(textView, item.isCompleted(), true);

            // If color is NONE show a gray solid color to help visually
            // grouping item text lines.
            colorView.setBackgroundColor(item.getColor().getColor(0xff808080));
            mItemListView.addView(itemView);

            mItemViews.add(itemView);
            mItemTextViews.add(textView);
        }
    }

    /**
     * Add an informative message to the item list. These messages are formatted differently than
     * actual tasks.
     */
    private final void addTemplateMessageItem(String message) {

        final LinearLayout itemView = (LinearLayout) mLayoutInflater.inflate(
                R.layout.widget_list_template_item_layout, null);
        final TextView textView = (TextView) itemView.findViewById(R.id.widget_item_text_view);
        final View colorView = itemView.findViewById(R.id.widget_item_color);

        // TODO: setup message text using widget font size preference?
        textView.setSingleLine(false);
        textView.setText(message);
        mFontVariationPreference.apply(textView, false, true);
        colorView.setVisibility(View.GONE);

        mItemListView.addView(itemView);

        mItemViews.add(itemView);
        mItemTextViews.add(textView);
    }

    /**
     * Set the toolbar portion of the template view.
     * 
     * showToolbarBackground and titleSize are ignored if not toolbarEnabled. titleSize.
     */
    private final void setTemplateToolbar() {
        final View templateToolbarView = mTopView.findViewById(R.id.widget_list_template_toolbar);

        final View templateAddTextByVoiceButton = templateToolbarView
                .findViewById(R.id.widget_list_template_toolbar_add_by_voice);

        if (!mToolbarEanbledPreference) {
            templateToolbarView.setVisibility(View.GONE);
            return;
        }

        // Make toolbar visible
        templateToolbarView.setVisibility(View.VISIBLE);

        // Show or hide toolbar background.
        if (mPaperPreference) {
            templateToolbarView.setBackgroundColor(0x00000000);
        } else {
            templateToolbarView.setBackgroundResource(R.drawable.widget_toolbar_background);
        }

        // The voice recognition button is shown only if this device supports voice recognition.
        if (AppServices.isVoiceRecognitionSupported(mContext)) {
            templateAddTextByVoiceButton.setVisibility(View.VISIBLE);
        } else {
            templateAddTextByVoiceButton.setVisibility(View.GONE);
        }
    }
}
