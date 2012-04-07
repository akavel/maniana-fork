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
import static com.zapta.apps.maniana.util.Assertions.checkNotNull;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.text.TextUtils;

/**
 * @author Tal Dayan
 */
public final class TextUtil {

    /** Do not instantiate */
    private TextUtil() {
    }

    /**
     * Expand macros in the form ${macro_name} with given set of values.
     * 
     * Null values are not allowed. The method asserts that all macro names are given.
     *
     * TODO: add unit test coverage
     */
    public static final String expandMacros(String text, Map<String, Object> macroValues,
            boolean htmlEscapeValues) {
        // TODO: make this a static value?
        final Pattern pattern = Pattern.compile("[$][{]([^{]*)[}]", Pattern.DOTALL);
        final Matcher matcher = pattern.matcher(text);
        final StringBuffer builder = new StringBuffer();
        while (matcher.find()) {
            final String macroName = matcher.group(1);
            // NOTE: we treat null values as not found. Caller should not use null values since
            // it's not well defined how to expand.
            final Object value = macroValues.get(macroName);
            checkNotNull(value, "Unknown macro name [%s]", macroName);
            // NOTE: if we expand the same macro multiple time, we expand each time. Could
            // cache or ask the caller to pre-escape if this becomes significant.
            final String valueString = htmlEscapeValues ? TextUtils.htmlEncode(value.toString())
                    : value.toString();
            matcher.appendReplacement(builder, valueString);
        }
        matcher.appendTail(builder);
        return builder.toString();
    }

    /** A quick test if a text contains macros. */
    public static final boolean constainsMacros(String text) {
        return text.contains("${");
    }
}
