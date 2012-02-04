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

package com.zapta.apps.maniana.model;

import java.util.ListIterator;

import com.zapta.apps.maniana.util.LogUtil;

/**
 * Contains the app data. Persisted across app activations. Controlled by the app controller.
 * Observed by the viewer via the ItemListViewAdapter.
 * 
 * @author Tal Dayan
 */
public class AppModel {

    /** Selected to not match any valid timestamp. */
    private static final String DEFAULT_DATE_STAMP = "";

    /** Model of Today page. */
    private final PageModel mTodayPageModel;

    /** Model of Tomorrow page. */
    private final PageModel mTomorrowPageMode;

    /** True if current state is not persisted */
    private boolean mIsDirty = true;

    /**
     * Last date in which items were pushed from Tomorrow to Today pages. Used to determine when
     * next push should be done. Using an empty string to indicate no datestamp.
     * 
     * NOTE(tal): for now the format of this timestamp is opaque though it must be consistent.
     */
    private String mLastPushDateStamp;

    public AppModel() {
        this.mTodayPageModel = new PageModel(PageKind.TODAY);
        this.mTomorrowPageMode = new PageModel(PageKind.TOMOROW);
        this.mLastPushDateStamp = DEFAULT_DATE_STAMP;
    }

    public final boolean isDirty() {
        return mIsDirty;
    }

    public final void setDirty() {
        if (!mIsDirty) {
            LogUtil.info("Model became dirty");
            mIsDirty = true;
        }
    }

    public final void setClean() {
        if (mIsDirty) {
            LogUtil.info("Model became clean");
            mIsDirty = false;
        }
    }

    /** Get the model of given page. */
    private final PageModel getPageModel(PageKind pageKind) {
        switch (pageKind) {
            case TODAY:
                return mTodayPageModel;
            case TOMOROW:
                return mTomorrowPageMode;
            default:
                throw new RuntimeException("Unknown page kind: " + pageKind);
        }
    }

    /** Get read only aspect of the item of given index in given page. */
    public final ItemModelReadOnly getItemReadOnly(PageKind pageKind, int itemIndex) {
        return getPageModel(pageKind).getItem(itemIndex);
    }

    /** Get a mutable item of given page and index. */
    // TODO: replace with a setItem(,,,) method. Safer this way.
    public final ItemModel getItemForMutation(PageKind pageKind, int itemIndex) {
        setDirty();
        return getPageModel(pageKind).getItem(itemIndex);
    }

    /** Get number of items in given page. */
    public final int getPageItemCount(PageKind pageKind) {
        return getPageModel(pageKind).itemCount();
    }
    
    /** Get total number of items. */
    public final int getItemCount() {
        return mTodayPageModel.itemCount() + mTomorrowPageMode.itemCount();
    }

    /** Clear the model. */
    public final void clear() {
        mTodayPageModel.clear();
        mTomorrowPageMode.clear();
        mLastPushDateStamp = DEFAULT_DATE_STAMP;
        setDirty();
    }

    /** Clear undo buffers of both pages. */
    public final void clearAllUndo() {
        mTodayPageModel.clearUndo();
        mTomorrowPageMode.clearUndo();

        // NOTE(tal): does not affect dirty flag.
    }

    /** Clear undo buffer of given page. */
    public final void clearPageUndo(PageKind pageKind) {
        getPageModel(pageKind).clearUndo();

        // NOTE(tal): does not affect dirty flag.
    }

    /** Test if given page has an active undo buffer. */
    public final boolean pageHasUndo(PageKind pageKind) {
        return getPageModel(pageKind).hasUndo();
    }
    
    /** Test if the page items are already sorted. */
    public final boolean isPageSorted(PageKind pageKind) {
        return getPageModel(pageKind).isPageSorted();
    }

    /** Insert item to given page at given item index. */
    public final void insertItem(PageKind pageKind, int itemIndex, ItemModel item) {
        setDirty();
        getPageModel(pageKind).insertItem(itemIndex, item);
    }

    /** Add an item to the end of given page. */
    public void appendItem(PageKind pageKind, ItemModel item) {
        getPageModel(pageKind).appendItem(item);
        setDirty();
    }

    /** Remove item of given index from given page. */
    public final ItemModel removeItem(PageKind pageKind, int itemIndex) {
        setDirty();
        ItemModel result = getPageModel(pageKind).removeItem(itemIndex);
        return result;
    }

    /** Remove item of given idnex from given page and set a corresponding undo at that page. */
    public final void removeItemWithUndo(PageKind pageKind, int itemIndex) {
        setDirty();
        getPageModel(pageKind).removeItemWithUndo(itemIndex);
    }

    /**
     * Organize the given page with undo. See details at
     * {@link PageModel#organizePageWithUndo(boolean, PageOrganizeResult)()}.
     */
    public final void organizePageWithUndo(PageKind pageKind, boolean deleteCompletedItems, int itemOfInteresetIndex,
                    OrganizePageSummary summary) {
        getPageModel(pageKind).organizePageWithUndo(deleteCompletedItems, itemOfInteresetIndex, summary);
        if (summary.pageChanged()) {
            setDirty();
        }
    }

    /**
     * Apply active undo operation of given page. The method asserts that the page has an active
     * undo.
     * 
     * @return the number of items resotred by the undo operation.
     */
    public final int applyUndo(PageKind pageKind) {
        final int result = getPageModel(pageKind).applyUndo();
        setDirty();
        return result;
    }

    /**
     * Move non locked items from Tomorow to Today. It's the caller responsibility to also set the
     * last push datestamp.
     * 
     * @param expireAllLocks if true, locked items are also pushed, after changing their status to
     *            unlocked.
     * 
     * @return number of moved items
     */
    public final int pushToToday(boolean expireAllLocks) {
        ListIterator<ItemModel> listIterator = mTomorrowPageMode.listIterator();
        int itemsMoved = 0;
        while (listIterator.hasNext()) {
            ItemModel item = listIterator.next();
            if (expireAllLocks || !item.isLocked()) {
                item.setIsLocked(false);
                listIterator.remove();
                // We add the moved items to the beginning of Today, preserving there
                // internal order.
                mTodayPageModel.insertItem(itemsMoved, item);
                itemsMoved++;
            }
        }

        if (itemsMoved > 0) {
            setDirty();
        }

        return itemsMoved;
    }

    /** Get the datestamp of last item push. */
    public final String getLastPushDateStamp() {
        return mLastPushDateStamp;
    }

    /** Set the last item push datestamp. */
    public final void setLastPushDateStamp(String lastPushDateStamp) {
        // TODO: no need to set the dirty bit, right?
        this.mLastPushDateStamp = lastPushDateStamp;
    }
}
