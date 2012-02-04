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

package com.zapta.apps.maniana.controller;

import javax.annotation.Nullable;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.main.AppContext;
import com.zapta.apps.maniana.quick_action.QuickActionItem;

/**
 * Cached provider for item quick action menu items.
 * 
 * @author Tal Dayan
 */
public class QuickActionsCache {

    public static final int DISMISS_WITH_NO_SELECTION_ID = 0;
    public static final int DONE_ACTION_ID = 1;
    public static final int TODO_ACTION_ID = 2;
    public static final int EDIT_ACTION_ID = 3;
    public static final int LOCK_ACTION_ID = 4;
    public static final int UNLOCK_ACTION_ID = 5;
    public static final int DELETE_ACTION_ID = 6;

    private final AppContext mApp;

    @Nullable
    private QuickActionItem mCachedActionDone;

    @Nullable
    private QuickActionItem mCachedActionTodo;

    @Nullable
    private QuickActionItem mCachedActionEdit;

    @Nullable
    private QuickActionItem mCachedActionLock;

    @Nullable
    private QuickActionItem mCachedActionUnlock;

    @Nullable
    private QuickActionItem mCachedActionDelete;

    public QuickActionsCache(AppContext app) {
        mApp = app;
    }

    public QuickActionItem getDoneAction() {
        if (mCachedActionDone == null) {
            mCachedActionDone = newItem(DONE_ACTION_ID, "Done", R.drawable.item_menu_done);
        }
        return mCachedActionDone;
    }
    
    public QuickActionItem getToDoAction() {
        if (mCachedActionTodo == null) {
            mCachedActionTodo = newItem(TODO_ACTION_ID, "To Do", R.drawable.item_menu_todo);
        }
        return mCachedActionTodo;
    }

    public QuickActionItem getEditAction() {
        if (mCachedActionEdit == null) {
            mCachedActionEdit = newItem(EDIT_ACTION_ID, "Edit", R.drawable.item_menu_edit);
        }
        return mCachedActionEdit;
    }
    
    public QuickActionItem getDeleteAction() {
        if (mCachedActionDelete == null) {
            mCachedActionDelete = newItem(DELETE_ACTION_ID, "Delete", R.drawable.item_menu_delete);
        }
        return mCachedActionDelete;
    }
    
    public QuickActionItem getLockAction() {
        if (mCachedActionLock == null) {
            mCachedActionLock = newItem(LOCK_ACTION_ID, "Lock", R.drawable.item_menu_lock);
        }
        return mCachedActionLock;
    }
    
    public QuickActionItem getUnlockAction() {
        if (mCachedActionUnlock == null) {
            mCachedActionUnlock = newItem(UNLOCK_ACTION_ID, "Unlock", R.drawable.item_menu_unlock);
        }
        return mCachedActionUnlock;
    }
   

    private QuickActionItem newItem(int id, String label, int imageResourceId) {
        return new QuickActionItem(id, label, mApp.resources().getDrawable(imageResourceId));

    }
}
