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

package com.zapta.apps.maniana.util;

import static com.zapta.apps.maniana.util.Assertions.check;
import android.graphics.Color;

/**
 * @author Tal Dayan
 */
public final class ColorUtil {

    /** Do not instantiate */
    private ColorUtil() {
    }

    /**
     * Compute the distance between two colors. The alpha channel is ignored. Returned distance is
     * always >= 0.
     */
    public static final int distance(int r, int g, int b, int color) {
        return Math.abs(r - Color.red(color)) + Math.abs(g - Color.green(color))
                        + Math.abs(b - Color.blue(color));
    }
    
    /**
     * Returns candidate color furtherest from given reference color. In case of a tie, prefer
     * candidates with lower indices. Candidates array must contain at least one member. Alpha
     * channel is ignored.
     */
    public static final int selectFurthestColor(int referenceColor, int candidates[]) {
       final int index = selectFurthestColorIndex(referenceColor, candidates);
       return candidates[index];
    }

    /**
     * Returns index of candidate color furtherest from given reference color. In case of a tie, prefer
     * candidates with lower indices. Candidates array must contain at least one member. Alpha
     * channel is ignored.
     */
    public static final int selectFurthestColorIndex(int referenceColor, int candidates[]) {
        check(candidates.length > 0);

        final int r = Color.red(referenceColor);
        final int g = Color.green(referenceColor);
        final int b = Color.blue(referenceColor);

        int bestCandidateIndex = -1;
        int bestDistance = -1;

        for (int i = 0; i < candidates.length; i++) {
            final int nextColor = candidates[i];
            final int nextDistance = distance(r, g, b, nextColor);
            check(nextDistance >= 0);
            if (nextDistance > bestDistance) {
                bestCandidateIndex = i;
                bestDistance = nextDistance;
            }
        }

        check(bestCandidateIndex >= 0);
        return bestCandidateIndex;
    }
}
