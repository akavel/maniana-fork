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

import junit.framework.TestCase;

import org.json.JSONException;

import com.zapta.apps.maniana.model.AppModel;
import com.zapta.apps.maniana.model.ItemColor;
import com.zapta.apps.maniana.model.ItemModel;
import com.zapta.apps.maniana.model.ItemModelReadOnly;
import com.zapta.apps.maniana.model.PageKind;
import com.zapta.apps.maniana.testing.TestUtil;
import com.zapta.apps.maniana.util.LogUtil;

/**
 * Unit test for AppModel
 * 
 * @author Tal Dayan
 */
public class SerializationTest extends TestCase {

    // Generated using constructLiteralString()
    private static final String SERIALIZED_MODEL = "{\n" + " \"model\": {\n" + "  \"tomorow\": [\n"
            + "   {\n" + "    \"color\": \"red\",\n" + "    \"text\": \"Item5\",\n"
            + "    \"done\": true\n" + "   },\n" + "   {\n" + "    \"color\": \"blue\",\n"
            + "    \"text\": \"Item6\",\n" + "    \"locked\": true\n" + "   },\n" + "   {\n"
            + "    \"color\": \"green\",\n" + "    \"text\": \"Item7\",\n"
            + "    \"locked\": true,\n" + "    \"done\": true\n" + "   },\n" + "   {\n"
            + "    \"text\": \"Item8\"\n" + "   },\n" + "   {\n" + "    \"color\": \"red\",\n"
            + "    \"text\": \"Item9\",\n" + "    \"done\": true\n" + "   },\n" + "   {\n"
            + "    \"color\": \"blue\",\n" + "    \"text\": \"Item10\",\n"
            + "    \"locked\": true\n" + "   },\n" + "   {\n" + "    \"color\": \"green\",\n"
            + "    \"text\": \"Item11\",\n" + "    \"locked\": true,\n" + "    \"done\": true\n"
            + "   }\n" + "  ],\n" + "  \"today\": [\n" + "   {\n" + "    \"text\": \"Item0\"\n"
            + "   },\n" + "   {\n" + "    \"color\": \"red\",\n" + "    \"text\": \"Item1\",\n"
            + "    \"done\": true\n" + "   },\n" + "   {\n" + "    \"color\": \"blue\",\n"
            + "    \"text\": \"Item2\"\n" + "   },\n" + "   {\n" + "    \"color\": \"green\",\n"
            + "    \"text\": \"Item3\",\n" + "    \"done\": true\n" + "   },\n" + "   {\n"
            + "    \"text\": \"Item4\"\n" + "   }\n" + "  ],\n"
            + "  \"last_push_date\": \"20120315\"\n" + " },\n" + " \"metadata\": {\n"
            + "  \"writer_ver_name\": \"version-x.y.z\",\n" + "  \"writer_ver_code\": 123\n"
            + " },\n" + " \"format\": 2\n" + "}";

    private AppModel createFakeModel() {
        final AppModel model = new AppModel();
        final long ts = 1234567;
        for (int i = 0; i < 12; i++) {
            final PageKind pageKind = (i < 5) ? PageKind.TODAY : PageKind.TOMOROW;
            final boolean isCompleted = (i & 0x1) != 0;
            final boolean isLocked = pageKind.isTomorrow() && ((i & 0x2) != 0);
            final ItemColor itemColor = ItemColor.values()[i % ItemColor.values().length];
            final ItemModel item = new ItemModel(ts, "id1", "Item" + i, isCompleted, isLocked,
                    itemColor);
            model.appendItem(pageKind, item);
        }
        model.setDirty();
        model.setLastPushDateStamp("20120315");
        return model;
    }

    public void testSerialization() {
        final AppModel model = createFakeModel();
        final PersistenceMetadata metadata = new PersistenceMetadata(123, "version-x.y.z");
        final String actual = ModelSerialization.serializeModel(model, metadata);
        LogUtil.info(TestUtil.constructLiteralString(actual));
        assertEquals(SERIALIZED_MODEL, actual);
    }

    public void testDeserialization() {
        final AppModel actualModel = new AppModel();
        final PersistenceMetadata actualMetadata = new PersistenceMetadata();
        try {
            ModelDeserialization.deserializeModel(actualModel, actualMetadata, SERIALIZED_MODEL);
        } catch (JSONException e) {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }

        final AppModel expectedModel = createFakeModel();

        for (PageKind pageKind : PageKind.values()) {
            final int n = expectedModel.getPageItemCount(pageKind);
            assertEquals(n, actualModel.getPageItemCount(pageKind));
            for (int i = 0; i < n; i++) {
                final ItemModelReadOnly expectedItem = expectedModel.getItemReadOnly(pageKind, i);
                final ItemModelReadOnly actualItem = actualModel.getItemReadOnly(pageKind, i);
                assertEquals(expectedItem.getText(), actualItem.getText());
                assertEquals(expectedItem.isCompleted(), actualItem.isCompleted());
                assertEquals(expectedItem.isLocked(), actualItem.isLocked());
                assertEquals(expectedItem.getColor(), actualItem.getColor());
            }
        }
        assertEquals(expectedModel.getLastPushDateStamp(), actualModel.getLastPushDateStamp());

        assertEquals(123, actualMetadata.writerVersionCode);
        assertEquals("version-x.y.z", actualMetadata.writerVersionName);
    }
}
