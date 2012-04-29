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

package com.zapta.apps.maniana.debug;

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.zapta.apps.maniana.main.AppContext;

public class DebugDialog {

    public static final void startDialog(final AppContext app) {
        
        // Create array with command strings
        final int n = DebugCommand.values().length;
        final String[] items = new String[n];
        for (int i = 0; i < n; i++) {
            items[i] = DebugCommand.values()[i].text;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(app.context());
        builder.setTitle("Debug Commands");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int itemIndex) {
                final DebugCommand command = DebugCommand.values()[itemIndex];
                app.debug().onDebugCommand(command);
            }
        }).show();
    }
}
