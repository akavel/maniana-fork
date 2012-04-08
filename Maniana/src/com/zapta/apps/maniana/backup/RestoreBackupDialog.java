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

package com.zapta.apps.maniana.backup;

import static com.zapta.apps.maniana.util.Assertions.check;

import java.util.Hashtable;
import java.util.Map;

import android.app.Dialog;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.help.PopupMessageActivity;
import com.zapta.apps.maniana.main.AppContext;
import com.zapta.apps.maniana.model.AppModel.ProjectedImportStats;
import com.zapta.apps.maniana.util.FileUtil;
import com.zapta.apps.maniana.util.FileUtil.FileReadResult;
import com.zapta.apps.maniana.util.FileUtil.FileReadResult.FileReadOutcome;
import com.zapta.apps.maniana.util.PopupsTracker.TrackablePopup;
import com.zapta.apps.maniana.util.TextUtil;

/**
 * Dialog for restore/merge confiratmion
 * 
 * @author Tal Dayan
 */
public class RestoreBackupDialog extends Dialog implements TrackablePopup {

    public static enum Action {
        CANCEL,
        REPLACE,
        MERGE
    };

    // Called on actual selection, not on dismiss.
    public interface RestoreBackupDialogListener {
        void onSelection(Action action);
    }

    private final AppContext mApp;

    private final RestoreBackupDialogListener mListener;

    /** Private constructor. Use startDialog() to create and launch a dialog. */
    private RestoreBackupDialog(final AppContext app, RestoreBackupDialogListener listener,
            Map<String, Object> macroValues) {
        super(app.context());
        mApp = app;
        mListener = listener;
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.restore_backup_dialog_layout);
        setOwnerActivity(app.mainActivity());

        getWindow().setGravity(Gravity.CENTER);

        final WebView webView = (WebView) findViewById(R.id.restore_backup_web_view);

        final String assetFilePath = "forms/restore_backup_dialog.html";
        final FileReadResult fileReadResult = FileUtil.readFileToString(mApp.context(),
                assetFilePath, true);

        // TODO: handle this more gracefully?
        check(fileReadResult.outcome == FileReadOutcome.READ_OK,
                "Error reading asset file: %s, outcome: %s", assetFilePath, fileReadResult.outcome);

        findViewById(R.id.restore_backup_merge).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonClick(Action.MERGE);
            }
        });
        
        findViewById(R.id.restore_backup_replace).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonClick(Action.REPLACE);
            }
        });

        findViewById(R.id.restore_backup_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonClick(Action.CANCEL);
            }
        });

        super.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mApp.popupsTracker().untrack(RestoreBackupDialog.this);
            }
        });
        
        final String expandedText = TextUtil.expandMacros(fileReadResult.content, macroValues, true);

        webView.loadDataWithBaseURL(PopupMessageActivity.ASSETS_BASE_URL + assetFilePath,
                expandedText, null, "UTF-8", null);
    }

    private final void onButtonClick(Action action) {
        mListener.onSelection(action);
        dismiss();
    }

    /** Called when the dialog was left open and the main activity pauses. */
    @Override
    public final void closeLeftOver() {
        // LogUtil.debug("*** reastore backup dialog: close leftover");
        if (isShowing()) {
            dismiss();
        }
    }

    public static void startDialog(final AppContext app,
            final RestoreBackupDialogListener listener, ProjectedImportStats stats) {

        // NOTE: macro names matches those in the html asset file.
        final Hashtable<String, Object> macroValues = new Hashtable<String, Object>();
        macroValues.put("merge-delete", stats.mergeDelete);
        macroValues.put("merge-keep", stats.mergeKeep);
        macroValues.put("merge-add", stats.mergeAdd);
        macroValues.put("merge-total", stats.mergeKeep + stats.mergeAdd);
        macroValues.put("replace-delete", stats.replaceDelete);
        macroValues.put("replace-keep", stats.replaceKeep);
        macroValues.put("replace-add", stats.replaceAdd);
        macroValues.put("replace-total", stats.replaceKeep + stats.replaceAdd);

        final RestoreBackupDialog dialog = new RestoreBackupDialog(app, listener, macroValues);
        app.popupsTracker().track(dialog);
        dialog.show();
    }
}
