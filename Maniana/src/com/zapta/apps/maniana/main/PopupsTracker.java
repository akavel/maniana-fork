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

package com.zapta.apps.maniana.main;

import java.util.HashSet;
import java.util.Set;

import com.zapta.apps.maniana.util.LogUtil;
import static com.zapta.apps.maniana.util.Assertions.check;

/**
 * Tracks dialogs and other popups used by the main activity. Used to close left over popups when
 * the main activity pauses.
 * 
 * @author Tal Dayan
 */
public class PopupsTracker {

    /** Trackable popups must implement this method. */
    public static interface TrackablePopup {
        public void closeLeftOver();
    }

    /** Set of tracked popups. */
    private final Set<TrackablePopup> mTrackedPopups = new HashSet<TrackablePopup>();

    /** Indicates if a closeAllLeftOvers is in progress. */
    private boolean mClosingAllLeftOvers = false;

    /** Track a popup. Ignore silently if popup is already tracked. */
    public final void track(TrackablePopup popup) {
        check(!mClosingAllLeftOvers, "Closing popups");
        mTrackedPopups.add(popup);
    }

    /** Stop tracking a popup. Ignore silently if popup is not tracked. */
    public final void untrack(TrackablePopup popup) {
        if (!mClosingAllLeftOvers) {
            mTrackedPopups.remove(popup);
        }
    }

    /** Close dialogs that were left open.*/
    public final void closeAllLeftOvers() {
        LogUtil.debug("closeLeftOvers(), n = %d", mTrackedPopups.size());
        // TODO: relax this assertion?
        check(!mClosingAllLeftOvers, "Already in closing state");
        mClosingAllLeftOvers = true;
        try {
            // Unspecified closing order.
            for (TrackablePopup popup : mTrackedPopups) {
                popup.closeLeftOver();
            }
            mTrackedPopups.clear();
        } finally {
            mClosingAllLeftOvers = false;
        }
    }
}
