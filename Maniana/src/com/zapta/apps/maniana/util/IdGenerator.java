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

import java.util.UUID;

import javax.annotation.Nullable;

import com.zapta.apps.maniana.annotations.ApplicationScope;

/**
 * Generates universally unique ids.
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public final class IdGenerator {

    private static final IdGenerator singleton = new IdGenerator();

    /** Prefix common to all ids generated by this instance. */
    @Nullable
    private String mPrefix = null;

    /** Suffix of next generated id. */
    private int mCount = 0;

    private IdGenerator() {
    }

    private String internalGetFreshId() {
        synchronized (this) {
            // Lazy initialization to speed up app start (saves 5-10ms).
            if (mPrefix == null) {
                mPrefix = UUID.randomUUID().toString() + "-";
            }
            return mPrefix + mCount++;
        }
    }

    /** Return a fresh globally unique id. Thread safe. */
    public static String getFreshId() {
        return singleton.internalGetFreshId();
    }
}
