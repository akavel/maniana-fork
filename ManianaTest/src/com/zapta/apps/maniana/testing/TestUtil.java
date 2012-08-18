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

package com.zapta.apps.maniana.testing;


/**
 * Test utilities.
 * 
 * @author Tal Dayan
 */
public class TestUtil {

    /** Encode a string as a java source code string declaration. */
    public static String constructLiteralString(String name, String value) {
        final StringBuilder sb = new StringBuilder();
        sb.append("private static final String ");
        sb.append(name);
        sb.append(" =\n");
        sb.append("\"");
        final int n = value.length();
        for (int i = 0; i < n; i++) {
            final char c = value.charAt(i);
            if (c == '\n') {
                final boolean isLast = (i == (n - 1));
                sb.append("\\n");
                if (!isLast) {
                    sb.append("\" + \n\"");
                }
            } else if (c == '\r') {
                sb.append("\\r");
            } else if (c == '"') {
                sb.append("\\\"");
            } else if (c == '\\') {
                sb.append("\\\\");
            } else if (c < 0x20) {
                sb.append(String.format("\\%03o", (int) c));
            } else if (c >= 0x80) {
                sb.append(String.format("\\u%04x", (int) c));
            } else {
                sb.append(c);
            }
        }
        sb.append("\";\n");
        return sb.toString();
    }
}
