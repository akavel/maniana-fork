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

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.main.MainActivity;
import com.zapta.apps.maniana.main.ResumeAction;
import com.zapta.apps.maniana.model.AppModel;
import com.zapta.apps.maniana.model.ItemModelReadOnly;
import com.zapta.apps.maniana.preferences.LockExpirationPeriod;
import com.zapta.apps.maniana.preferences.PreferencesTracker;
import com.zapta.apps.maniana.preferences.WidgetBackgroundType;
import com.zapta.apps.maniana.preferences.WidgetItemFontVariation;
import com.zapta.apps.maniana.services.AppServices;
import com.zapta.apps.maniana.util.DebugTimer;
import com.zapta.apps.maniana.util.FileUtil;
import com.zapta.apps.maniana.util.LogUtil;

/**
 * Base class for the task list widgets.
 * 
 * @author Tal Dayan
 */
public abstract class ListWidgetProvider extends BaseWidgetProvider {

    /** ImageView slices in the template layout. */
    private static final int[] IMAGE_SLICES = new int[] { R.id.widget_list_bitmap_slice1,
            R.id.widget_list_bitmap_slice2, R.id.widget_list_bitmap_slice3,
            R.id.widget_list_bitmap_slice4 };

    public ListWidgetProvider() {
    }

    protected abstract ListWidgetSize listWidgetSize();

    /**
     * Called by the widget host. Updates one or more widgets of the same size.
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        update(context, appWidgetManager, listWidgetSize(), appWidgetIds, loadModel(context));
    }

    /**
     * Internal widget update method that accepts the model as a parameter. Updates one or more
     * widgets of the same size.
     */
    private static final void update(Context context, AppWidgetManager appWidgetManager,
            ListWidgetSize listWidgetSize, int[] appWidgetIds, @Nullable AppModel model) {

        if (appWidgetIds.length == 0) {
            return;
        }

        // For debugging only
        final boolean DEBUG_TIME = true;
        final DebugTimer debugTimer = DEBUG_TIME ? new DebugTimer() : null;

        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        // Create the template view. We will later render it to a bitmap.
        //
        // NOTE: we use a template layout that is rendered to a bitmap rather rendering directly
        // a remote view. This allows us to use custom fonts which are not supported by
        // remote view. This also increase the complexity and makes the widget more sensitive
        // to resizing.
        //
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final LinearLayout template = (LinearLayout) layoutInflater.inflate(
                R.layout.widget_list_template_layout, null);

        // Set template view background
        final WidgetBackgroundType backgroundType = PreferencesTracker
                .readWidgetBackgroundTypePreference(sharedPreferences);
        switch (backgroundType) {
            case PAPER:
                template.setBackgroundResource(R.drawable.widget_background);
                break;

            default:
                LogUtil.error("Unknown widget background type: %s", backgroundType);
                // fall through to Solid
            case SOLID:
                final int backgroundColor = PreferencesTracker
                        .readWidgetBackgroundColorPreference(sharedPreferences);
                template.setBackgroundColor(backgroundColor);
        }

        final boolean isVoiceRecognitionSupported = AppServices
                .isVoiceRecognitionSupported(context);

        // Set template view toolbar
        final boolean toolbarEnabled = PreferencesTracker
                .readWidgetShowToolbarPreference(sharedPreferences);
        final boolean showToolbarBackground = toolbarEnabled
                && (backgroundType != WidgetBackgroundType.PAPER);
        setTemplateToolbar(context, template, toolbarEnabled, isVoiceRecognitionSupported,
                showToolbarBackground);

        // TODO: cache variation or at least custom typefaces
        final WidgetItemFontVariation fontVariation = WidgetItemFontVariation
                .newFromCurrentPreferences(context, sharedPreferences);

        // Set template view item list final int textColor =
        final LinearLayout itemListView = (LinearLayout) template
                .findViewById(R.id.widget_list_template_item_list);
        populateTemplateItemList(context, itemListView, model, fontVariation, sharedPreferences,
                layoutInflater);

        if (DEBUG_TIME) {
            debugTimer.report("Template populated");
        }

        final boolean isPortrait = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        final float density = context.getResources().getDisplayMetrics().density;

        // Render the template view to a bitmap
        final Point widgetGrossSizeInPixels = listWidgetSize.currentGrossSizeInPixels(density,
                isPortrait);

        // In percents. 100 means no change.
        final int widthAdjust = isPortrait ? PreferencesTracker
                .readWidgetPortraitWidthAdjustPreference(sharedPreferences) : PreferencesTracker
                .readWidgetLandscapeWidthAdjustPreference(sharedPreferences);

        final int heightAdjust = isPortrait ? PreferencesTracker
                .readWidgetPortraitHeightAdjustPreference(sharedPreferences) : PreferencesTracker
                .readWidgetLandscapeHeightAdjustPreference(sharedPreferences);

        final int widthPixels = (widgetGrossSizeInPixels.x * 95 * widthAdjust) / (100 * 100);
        final int heightPixels = (widgetGrossSizeInPixels.y * 95 * heightAdjust) / (100 * 100);

        // NOTE: ARGB_4444 bitmaps are smaller than ARGB_8888
        final Bitmap bitmap = Bitmap.createBitmap(widthPixels, heightPixels,
                Bitmap.Config.ARGB_4444);

        final Canvas canvas = new Canvas(bitmap);

        template.measure(MeasureSpec.makeMeasureSpec(widthPixels, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(heightPixels, MeasureSpec.EXACTLY));
        // TODO: substract '1' from ends?
        template.layout(0, 0, widthPixels, heightPixels);
        template.draw(canvas);

        if (DEBUG_TIME) {
            debugTimer.report("Template rendered to bitmap");
        }

        final boolean DEBUG_FILE = false;
        if (DEBUG_FILE) {
            LogUtil.debug("*** Writing widget debug bitmap file");
            FileUtil.writeBitmapToPngFile(context, bitmap, "debug_widget_list_image", true);
            if (DEBUG_TIME) {
                debugTimer.report("Bitmap written to debug file");
            }
        }

        updateRemoteViews(context, appWidgetManager, appWidgetIds, bitmap, toolbarEnabled,
                isVoiceRecognitionSupported);

        bitmap.recycle();

        if (DEBUG_TIME) {
            debugTimer.report("Sent remote view update.");
        }
    }

    /** Add model items to the template layout. */
    private static final void populateTemplateItemList(Context context, LinearLayout itemListView,
            AppModel model, WidgetItemFontVariation fontVariation,
            SharedPreferences sharedPreferences, LayoutInflater layoutInflater) {
        // For debugging
        final boolean debugTimestamp = false;
        if (debugTimestamp) {
            final String message = String.format("[%s]", SystemClock.elapsedRealtime() / 1000);
            addTemplateMessageItem(context, itemListView, message, fontVariation, layoutInflater);
        }

        if (model == null) {
            addTemplateMessageItem(context, itemListView, "(Maniana data not found)",
                    fontVariation, layoutInflater);
            return;
        }

        final LockExpirationPeriod lockExpirationPeriod = PreferencesTracker
                .readLockExpierationPeriodPreference(sharedPreferences);
        // TODO: reorganize the code. No need to read lock preference if date now is same as the
        // model
        Time now = new Time();
        now.setToNow();

        final List<ItemModelReadOnly> items = WidgetUtil.selectTodaysActiveItemsByTime(model, now,
                lockExpirationPeriod);
        if (items.isEmpty()) {
            addTemplateMessageItem(context, itemListView, "(no active tasks)", fontVariation,
                    layoutInflater);
            return;
        }

        final boolean singleLine = PreferencesTracker
                .readWidgetSingleLinePreference(sharedPreferences);

        for (ItemModelReadOnly item : items) {
            final LinearLayout itemView = (LinearLayout) layoutInflater.inflate(
                    R.layout.widget_list_template_item_layout, null);
            final TextView textView = (TextView) itemView.findViewById(R.id.widget_item_text_view);
            final View colorView = itemView.findViewById(R.id.widget_item_color);

            // NOTE: TextView has a bug that does not allows more than
            // two lines when using ellipsize. Otherwise we would give the user more
            // choices about the max number of lines. More details here:
            // http://code.google.com/p/android/issues/detail?id=2254
            if (!singleLine) {
                textView.setSingleLine(false);
                // NOTE: on ICS (API 14) the text view behaves
                // differently and does not limit the lines to two when ellipsize. For
                // consistency, we limit it explicitly to two lines.
                //
                // TODO: file an Android bug.
                //
                textView.setMaxLines(2);
            }

            textView.setText(item.getText());
            fontVariation.apply(textView);

            // If color is NONE show a gray solid color to help visually
            // grouping item text lines.
            final int itemColor = item.getColor().isNone() ? 0xff808080 : item.getColor()
                    .getColor();
            colorView.setBackgroundColor(itemColor);
            itemListView.addView(itemView);
        }
    }

    /**
     * Set toolbar in the template layout.
     */
    private static final void setTemplateToolbar(Context context, View template,
            boolean toolbarEnabled, boolean isVoiceRecognitionSupported,
            boolean showToolbarBackground) {
        final View toolbarView = template.findViewById(R.id.widget_list_template_toolbar);
        final View addTextByVoiceButton = toolbarView
                .findViewById(R.id.widget_list_template_toolbar_add_by_voice);

        if (!toolbarEnabled) {
            toolbarView.setVisibility(View.GONE);
            return;
        }

        // Make toolbar visible
        toolbarView.setVisibility(View.VISIBLE);

        // Show or hide toolbar background.
        if (showToolbarBackground) {
            toolbarView.setBackgroundResource(R.drawable.widget_toolbar_background);
        } else {
            toolbarView.setBackgroundColor(0x00000000);
        }

        // The voice recognition button is shown only if this device supports voice recognition.
        if (isVoiceRecognitionSupported) {
            addTextByVoiceButton.setVisibility(View.VISIBLE);
        } else {
            addTextByVoiceButton.setVisibility(View.GONE);
        }
    }

    /**
     * Update the widget remote views with the rendered template bitmap.
     * 
     * NOTE: RemoteViews class has an issue with transferring large bitmaps. As a workaround, we
     * Updated the remote view images in smaller slices. For more information on this issue see
     * http://tinyurl.com/75jh2yf
     */
    private static final void updateRemoteViews(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds, Bitmap bitmap, boolean toolbarEnabled,
            boolean isVoiceRecognitionSupported) {

       

        final int N = IMAGE_SLICES.length;
        final int W = bitmap.getWidth();
        final int H = bitmap.getHeight();

        LogUtil.debug("Bitmap to slice: %d x %d", W, H);

        // For debug only. Used to confirm the update and stiching.
        final boolean DEBUG_WATERMARK = true;
        if (DEBUG_WATERMARK) {
            LogUtil.info("Drawing list widget bitmap debug watermark");

            final Paint paint = new Paint();
            paint.setARGB(200, 100, 100, 50);
            final Canvas c = new Canvas(bitmap);
//            c.drawLine(0, 0, W - 1, H - 1, paint);
//            c.drawLine(W - 1, 0, 0, H - 1, paint);
            final Random rand = new Random();
            final int x = rand.nextInt(W - 20) + 10;
            c.drawLine(x, 0, x, H - 1, paint);
        }

        // TODO: for smaller widgets use less slices. This will speedup the rendering.
        int sliceBaseY = 0;
        for (int i = 0; i < N; i++) {
            System.gc();
            
            final boolean isLastSlice = (i == (N - 1));
            final RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_list_layout);

            // We set the onclick intents only in the first remoteviews update. No need to repeat.
            if (isLastSlice) {
                setOnClickLaunch(context, remoteViews, R.id.widget_list_bitmaps, ResumeAction.NONE);
                setOnClickLaunch(context, remoteViews,
                        R.id.widget_list_toolbar_add_by_text_overlay,
                        ResumeAction.ADD_NEW_ITEM_BY_TEXT);
                if (isVoiceRecognitionSupported) {
                    // addTextByVoiceButton.setVisibility(View.VISIBLE);
                    remoteViews.setInt(R.id.widget_list_toolbar_add_by_voice_overlay,
                            "setVisibility", View.VISIBLE);
                    setOnClickLaunch(context, remoteViews,
                            R.id.widget_list_toolbar_add_by_voice_overlay,
                            ResumeAction.ADD_NEW_ITEM_BY_VOICE);
                } else {
                    // addTextByVoiceButton.setVisibility(View.GONE);
                    remoteViews.setInt(R.id.widget_list_toolbar_add_by_voice_overlay,
                            "setVisibility", View.GONE);
                }
            }

            final int sliceHeight = isLastSlice ? (H - sliceBaseY) : (H / N);
            LogUtil.debug("Slice %d: base = %d, height = %d", i, sliceBaseY, sliceHeight);
            final Bitmap slice = Bitmap.createBitmap(bitmap, 0, sliceBaseY, W, sliceHeight);
            sliceBaseY += sliceHeight;

//            final Paint paint = new Paint();
//            paint.setARGB(200, 100, 100, 50);
//            final Canvas c = new Canvas(slice);
//            c.drawLine(0, 0, W - 1, sliceHeight - 1, paint);
//            c.drawLine(W - 1, 0, 0, sliceHeight - 1, paint);

            remoteViews.setImageViewBitmap(IMAGE_SLICES[i], slice);
            // if (i == 0) {
            LogUtil.debug("Flushing remoteview slice %d", i);
            appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

            slice.recycle();
            // }

        }
    }

    /** Set onClick() action of given remote view element to launch the app. */
    private static final void setOnClickLaunch(Context context, RemoteViews remoteViews,
            int viewId, ResumeAction resumeAction) {
        final Intent intent = new Intent(context, MainActivity.class);
        ResumeAction.setInIntent(intent, resumeAction);
        // Setting unique intent action and using FLAG_UPDATE_CURRENT to avoid cross
        // reuse of pending intents. See http://tinyurl.com/8axhrlp for more info.
        intent.setAction("maniana.list_widget." + resumeAction.toString());
        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(viewId, pendingIntent);
    }

    private static final void addTemplateMessageItem(Context context, LinearLayout itemListView,
            String message, WidgetItemFontVariation fontVariation, LayoutInflater layoutInflater) {

        final LinearLayout itemView = (LinearLayout) layoutInflater.inflate(
                R.layout.widget_list_template_item_layout, null);
        final TextView textView = (TextView) itemView.findViewById(R.id.widget_item_text_view);
        final View colorView = itemView.findViewById(R.id.widget_item_color);

        // TODO: setup message text using widget font size preference?
        textView.setSingleLine(false);
        textView.setText(message);
        fontVariation.apply(textView);
        colorView.setVisibility(View.GONE);

        itemListView.addView(itemView);
    }

    // TODO: decide what we want to do with this.
    // An attempt to update all list widgtes by a direct call.
    public static void updateAllListWidgetsFromModel(Context context, @Nullable AppModel model) {

        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        for (ListWidgetSize listWidgetSize : ListWidgetSize.LIST_WIDGET_SIZES) {
            final int widgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context,
                    listWidgetSize.widgetProviderClass));
            // Update
            if (widgetIds != null) {
                update(context, appWidgetManager, listWidgetSize, widgetIds, model);
            }
        }
    }
}
