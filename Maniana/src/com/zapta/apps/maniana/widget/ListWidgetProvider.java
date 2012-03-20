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
import java.util.List;

import javax.annotation.Nullable;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
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
import com.zapta.apps.maniana.preferences.WidgetItemFontVariation;
import com.zapta.apps.maniana.services.AppServices;
import com.zapta.apps.maniana.util.BitmapUtil;
import com.zapta.apps.maniana.util.DisplayUtil;
import com.zapta.apps.maniana.util.FileUtil;
import com.zapta.apps.maniana.util.LogUtil;
import com.zapta.apps.maniana.util.Orientation;
import com.zapta.apps.maniana.widget.ListWidgetSize.OrientationInfo;

/**
 * Base class for the task list widgets.
 * 
 * @author Tal Dayan
 */
public abstract class ListWidgetProvider extends BaseWidgetProvider {

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

        // Set template background. If paper backend is enable the template rendered with
        // a transparent background and the paper layer is placed at the back at the
        // remote views level (reduces template bitmap size and speeds up rendering).
        final boolean backgroundPaper = PreferencesTracker
                .readWidgetBackgroundPaperPreference(sharedPreferences);
        final int templateBackgroundColor = backgroundPaper ? 0x00000000 : PreferencesTracker
                .readWidgetBackgroundColorPreference(sharedPreferences);
        template.setBackgroundColor(templateBackgroundColor);

        // TODO: cache variation or at least custom typefaces
        final WidgetItemFontVariation fontVariation = WidgetItemFontVariation
                .newFromCurrentPreferences(context, sharedPreferences);

        // Set template view toolbar
        final boolean toolbarEanbled = PreferencesTracker
                .readWidgetShowToolbarPreference(sharedPreferences);
        final boolean showToolbarBackground = (toolbarEanbled && !backgroundPaper);
        final int titleSize = WidgetUtil.titleTextSize(listWidgetSize, fontVariation.getTextSize());
        setTemplateToolbar(context, template, toolbarEanbled, showToolbarBackground, titleSize);

        // Set template view item list
        final LinearLayout itemListView = (LinearLayout) template
                .findViewById(R.id.widget_list_template_item_list);
        populateTemplateItemList(context, itemListView, model, fontVariation, sharedPreferences,
                layoutInflater);

        // Create the widget remote view
        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.widget_list_layout);
        setOnClickLaunch(context, remoteViews, R.id.widget_list_bitmaps,
                ResumeAction.ONLY_RESET_PAGE);

        setRemoteViewsToolbar(context, remoteViews, toolbarEanbled);

        renderOrientation(context, remoteViews, template, listWidgetSize, Orientation.PORTRAIT,
                backgroundPaper);
        renderOrientation(context, remoteViews, template, listWidgetSize, Orientation.LANDSCAPE,
                backgroundPaper);

        // Flush the remote view
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }

    /** Set the image of a single orientation. */
    private static final void renderOrientation(Context context, RemoteViews remoteViews,
            View template, ListWidgetSize listWidgetSize, Orientation orientation,
            boolean backgroundPaper) {

        // Template view is now fully populated. Render it as a bitmap. First we render it
        // using screen native resolution.
        final float density = DisplayUtil.getDensity(context);

        final OrientationInfo orientationInfo = orientation.isPortrait ? listWidgetSize.portraitInfo
                : listWidgetSize.landscapeInfo;

        final int widthPixels = (int) context.getResources().getDimensionPixelSize(
                orientationInfo.widthDipResourceId);

        final int heightPixels = (int) context.getResources().getDimensionPixelSize(
                orientationInfo.heightDipResourceId);

        // NTOE: ARGB_4444 results in a smaller file than ARGB_8888 (e.g. 50K vs 150k)
        // but does not look as good.
        final Bitmap bitmap1 = Bitmap.createBitmap(widthPixels, heightPixels,
                Bitmap.Config.ARGB_8888);

        final Canvas canvas = new Canvas(bitmap1);

        template.measure(MeasureSpec.makeMeasureSpec(widthPixels, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(heightPixels, MeasureSpec.EXACTLY));
        // TODO: substract '1' from ends?
        template.layout(0, 0, widthPixels, heightPixels);

        template.draw(canvas);

        // NOTE: rounding the bitmap here when paper background is selected will do nothing
        // since the paper background is added later via the remote views.
        final Bitmap bitmap2;
        if (backgroundPaper) {
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
        LogUtil.info("Updating widget bitmap: " + fileName);
        FileUtil.writeBitmapToPngFile(context, bitmap2, fileName, true);

        final Uri uri = Uri.fromFile(new File(context.getFilesDir(), fileName));

        // Set the bitmap images of given orientation. The bitmap of the size we currently
        // process is set and the other are made GONE. Only bitmaps of the given orientation
        // are touched here
        for (ListWidgetSize iterListWidgetSize : ListWidgetSize.LIST_WIDGET_SIZES) {
            final boolean thisSize = (iterListWidgetSize == listWidgetSize);
            final OrientationInfo iterOrientationInfo = orientation.isPortrait ? iterListWidgetSize.portraitInfo
                    : iterListWidgetSize.landscapeInfo;
            final int iterBitmapResource = iterOrientationInfo.imageViewId;
            if (thisSize) {
                // NOTE: setting up a temporary dummy image to cause the image view to reload the
                // file URI in case it changed.
                remoteViews.setInt(iterBitmapResource, "setImageResource", R.drawable.place_holder);
                remoteViews.setUri(iterBitmapResource, "setImageURI", uri);
                // Set paper background if used or transparent otherwise.
                // TODO: will using the background solid color here rather than the template bitmap
                // reduce the file size? If so, make this change.
                if (backgroundPaper) {
                    final PaperBackground paperBackground = PaperBackground.getBestSize(
                            widthPixels, heightPixels);
                    remoteViews.setInt(iterBitmapResource, "setBackgroundResource",
                            paperBackground.drawableResourceId);
                } else {
                    remoteViews.setInt(iterBitmapResource, "setBackgroundColor", 0x00000000);
                }
            } else {
                // A different size. Disable it, regardless of orientation.
                remoteViews.setInt(iterBitmapResource, "setVisibility", View.GONE);
            }
        }
    }

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
                // TODO: file an Android bug about the different ICS behavior.
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
     * Add an informative message to the item list. These messages are formatted differently than
     * actual tasks.
     */
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

    /**
     * Set the toolbar portion of the template view.
     * 
     * showToolbarBackground and titleSize are ignored if not toolbarEnabled. titleSize.
     */
    private static final void setTemplateToolbar(Context context, View template,
            boolean toolbarEnabled, boolean showToolbarBackground, int titleSize) {
        final View templateToolbarView = template.findViewById(R.id.widget_list_template_toolbar);
        final TextView templateToolbarTitleView = (TextView) templateToolbarView
                .findViewById(R.id.widget_list_template_toolbar_title);
        final View templateAddTextByVoiceButton = templateToolbarView
                .findViewById(R.id.widget_list_template_toolbar_add_by_voice);

        if (!toolbarEnabled) {
            templateToolbarView.setVisibility(View.GONE);
            return;
        }

        // Make toolbar visible
        templateToolbarView.setVisibility(View.VISIBLE);
        templateToolbarTitleView.setTextSize(titleSize);

        // Show or hide toolbar background.
        if (showToolbarBackground) {
            templateToolbarView.setBackgroundResource(R.drawable.widget_toolbar_background);
        } else {
            templateToolbarView.setBackgroundColor(0x00000000);
        }

        // The voice recognition button is shown only if this device supports voice recognition.
        if (AppServices.isVoiceRecognitionSupported(context)) {
            templateAddTextByVoiceButton.setVisibility(View.VISIBLE);
        } else {
            templateAddTextByVoiceButton.setVisibility(View.GONE);
        }
    }

    /** Set/disable the toolbar click overlay in the remote views layout. */
    private static final void setRemoteViewsToolbar(Context context, RemoteViews remoteViews,
            boolean toolbarEnabled) {
        // Set or disable the click overlay of the add-item-by-text button.
        if (toolbarEnabled) {
            remoteViews.setInt(R.id.widget_list_toolbar_add_by_text_overlay, "setVisibility",
                    View.VISIBLE);
            setOnClickLaunch(context, remoteViews, R.id.widget_list_toolbar_add_by_text_overlay,
                    ResumeAction.ADD_NEW_ITEM_BY_TEXT);
        } else { // templateAddTextByVoiceButton.setVisibility(View.GONE);
            remoteViews.setInt(R.id.widget_list_toolbar_add_by_text_overlay, "setVisibility",
                    View.GONE);
        }

        // Set or disable the click overlay of the add-item-by-voice button.
        if (toolbarEnabled && AppServices.isVoiceRecognitionSupported(context)) {
            remoteViews.setInt(R.id.widget_list_toolbar_add_by_voice_overlay, "setVisibility",
                    View.VISIBLE);
            setOnClickLaunch(context, remoteViews, R.id.widget_list_toolbar_add_by_voice_overlay,
                    ResumeAction.ADD_NEW_ITEM_BY_VOICE);
        } else {
            remoteViews.setInt(R.id.widget_list_toolbar_add_by_voice_overlay, "setVisibility",
                    View.GONE);
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
