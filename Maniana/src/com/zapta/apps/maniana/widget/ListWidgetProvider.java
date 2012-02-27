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

import static com.zapta.apps.maniana.util.Assertions.check;

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
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.main.MainActivity;
import com.zapta.apps.maniana.main.ResumeAction;
import com.zapta.apps.maniana.model.AppModel;
import com.zapta.apps.maniana.model.ItemModelReadOnly;
import com.zapta.apps.maniana.preferences.LockExpirationPeriod;
import com.zapta.apps.maniana.preferences.PreferencesTracker;
import com.zapta.apps.maniana.preferences.WidgetBackgroundType;
import com.zapta.apps.maniana.services.AppServices;
import com.zapta.apps.maniana.util.FileUtil;
import com.zapta.apps.maniana.util.LogUtil;

/**
 * Base class for the task list widgets.
 * 
 * @author Tal Dayan
 */
public abstract class ListWidgetProvider extends BaseWidgetProvider {

    /** List of all widget provider classes. */
    @SuppressWarnings("rawtypes")
    public static final Class[] LIST_WIDGET_PROVIDER_CLASSES = new Class[] {
            ListWidgetProvider4.class, ListWidgetProvider1.class, ListWidgetProvider2.class,
            ListWidgetProvider3.class, ListWidgetProviderX.class };

    public ListWidgetProvider() {
    }

    /** Called by the widget host. */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        update(context, appWidgetManager, appWidgetIds, loadModel(context));
    }

    /** Internal widget update method that accepts the model as a parameter */
    private static final void update(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds, @Nullable AppModel model) {

        if (appWidgetIds.length == 0) {
            return;
        }

        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        // Create the widget remote view
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.widget_list_layout);
        // remoteViews.setBitmap(R.id.widget_list_bitmap, "setImageBitmap", bm);
        setOnClickLaunch(context, remoteViews, R.id.widget_list_bitmap, ResumeAction.NONE);

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

        // Set template view toolbar
        final boolean toolbarEanbled = PreferencesTracker
                .readWidgetShowToolbarPreference(sharedPreferences);
        final boolean showToolbarBackground = toolbarEanbled
                && (backgroundType != WidgetBackgroundType.PAPER);
        setToolbar(context, remoteViews, template, toolbarEanbled, showToolbarBackground);

        // Set template view item list
        final int textColor = PreferencesTracker.readWidgetTextColorPreference(sharedPreferences);
        final LinearLayout itemListView = (LinearLayout) template
                .findViewById(R.id.widget_list_template_item_list);
        populateItemList(context, itemListView, model, textColor, sharedPreferences, layoutInflater);

        // Render the template view to a bitmap
        final int widthPixels = 300;
        final int heightPixels = 250;

        final Bitmap bm = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888);

        final Canvas canvas = new Canvas(bm);
        template.measure(MeasureSpec.makeMeasureSpec(widthPixels, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(heightPixels, MeasureSpec.EXACTLY));
        // TODO: substract '1' from ends?
        template.layout(0, 0, widthPixels, heightPixels);
        template.draw(canvas);

        // For debugging. Should be off in production releases.
        final boolean DUMP_DEBUG_FILE = false;
        if (DUMP_DEBUG_FILE) {
            LogUtil.debug("*** Writing list widget bitmap to debug file");
            FileUtil.writeBitmapToPngFile(context, bm, "debug_list_widget.png");
        }

        // Set the template rendered bitmap in the remote views.
        remoteViews.setBitmap(R.id.widget_list_bitmap, "setImageBitmap", bm);

        // Flush the remote view
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }

    private static final void populateItemList(Context context, LinearLayout itemListView,
            AppModel model, int textColor, SharedPreferences sharedPreferences,
            LayoutInflater layoutInflater) {
        // Get text size preference in sp units
        final int fontSizeSp = PreferencesTracker.readWidgetItemFontSizePreference(
                sharedPreferences).getSizeSp();

        // For debugging
        final boolean debugTimestamp = false;
        if (debugTimestamp) {
            final String message = String.format("[%s]", SystemClock.elapsedRealtime() / 1000);
            addMessageItem(context, itemListView, message, textColor, layoutInflater);
        }

        if (model == null) {
            addMessageItem(context, itemListView, "(Maniana data not found)", textColor,
                    layoutInflater);
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
            addMessageItem(context, itemListView, "(no active tasks)", textColor, layoutInflater);
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
            textView.setTextColor(textColor);
            textView.setTextSize(fontSizeSp);

            // If color is NONE show a gray solid color to help visually
            // grouping item text lines.
            final int itemColor = item.getColor().isNone() ? 0xff808080 : item.getColor()
                    .getColor();

            colorView.setBackgroundColor(itemColor);

            itemListView.addView(itemView);
        }
    }

    /**
     * Set toolbar. This updates both the template portion of the toolbar and the click overlays of
     * the remote views.
     */
    private static final void setToolbar(Context context, RemoteViews remoteViews, View template,
            boolean toolbarEnabled, boolean showToolbarBackground) {
        final View toolbarView = template.findViewById(R.id.widget_list_template_toolbar);
        // TODO: add toobar click actions
        final View addTextByTextButton = toolbarView
                .findViewById(R.id.widget_list_template_toolbar_add_by_text);
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

        // Set new task by text action.
        setOnClickLaunch(context, remoteViews, R.id.widget_list_toolbar_add_by_text_overlay,
                ResumeAction.ADD_NEW_ITEM_BY_TEXT);

        // The voice recognition button is shown only if this device supports voice recognition.
        if (AppServices.isVoiceRecognitionSupported(context)) {
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

    private static final void addMessageItem(Context context, LinearLayout itemListView,
            String message, int textColor, LayoutInflater layoutInflater) {

        final LinearLayout itemView = (LinearLayout) layoutInflater.inflate(
                R.layout.widget_list_template_item_layout, null);
        final TextView textView = (TextView) itemView.findViewById(R.id.widget_item_text_view);
        final View colorView = itemView.findViewById(R.id.widget_item_color);

        // TODO: setup message text using widget font size preference?
        textView.setSingleLine(false);
        textView.setText(message);
        textView.setTextColor(textColor);
        colorView.setVisibility(View.GONE);

        itemListView.addView(itemView);
    }

    // TODO: decide what we want to do with this.
    // An attempt to update all list widgtes by a direct call.
    public static void updateAllIconWidgetsFromModel(Context context, @Nullable AppModel model) {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        // Get ids of all widgets of this type.
        @Nullable
        final int[] widgetIds = getWidgetIds(context, appWidgetManager);

        // Update
        if (widgetIds != null) {
            update(context, appWidgetManager, widgetIds, model);
        }
    }

    /** Return null if none, or an array if at least one. */
    private static int[] getWidgetIds(Context context, AppWidgetManager appWidgetManager) {
        final int[][] widgetIdLists = new int[LIST_WIDGET_PROVIDER_CLASSES.length][];

        int n = 0;
        for (int i = 0; i < LIST_WIDGET_PROVIDER_CLASSES.length; i++) {
            widgetIdLists[i] = appWidgetManager.getAppWidgetIds(new ComponentName(context,
                    LIST_WIDGET_PROVIDER_CLASSES[i]));
            n += widgetIdLists[i].length;
        }

        if (n == 0) {
            return null;
        }

        final int[] widgetIds = new int[n];
        int m = 0;
        for (int[] src : widgetIdLists) {
            System.arraycopy(src, 0, widgetIds, m, src.length);
            m += src.length;
        }
        check(m == n, "%s vs %s", m, n);

        return widgetIds;
    }
}
