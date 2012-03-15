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

package com.zapta.apps.maniana.persistence;

/** 
 * Field names used in the serialization.
 * 
 * @author Tal Dayan
 */
public interface FieldNames {
    
    // WARNINNG: changing these value may break comparability with old data files.
    
    static String FIELD_FORMAT = "format";
    static String FIELD_MODEL = "model";
    static String FIELD_METADATA = "metadata";
    static String FIELD_LAST_PUSH_DATE = "last_push_date";
    static String TODAY = "today";
    // NOTE: do not correct the value's spelling. This will break compatability.
    static String TOMOROW = "tomorow";
    static String TEXT = "text";
    static String DONE = "done";
    static String LOCKED = "locked";
    static String COLOR = "color";
}
