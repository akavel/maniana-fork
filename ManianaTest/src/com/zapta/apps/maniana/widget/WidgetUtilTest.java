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

package com.zapta.apps.maniana.widget;

import junit.framework.TestCase;

/**
 * Unit test for WidgetlUtil.
 * 
 * @author Tal Dayan
 */
public class WidgetUtilTest extends TestCase {
        
    public void testTitleTextSize() {
        ListWidgetSize W;
        
        // 1 row widget 
        W = ListWidgetSize.LIST_WIDGET_SIZE1; 
        assertEquals(1, W.heightCells);
        assertEquals(9, WidgetUtil.titleTextSize(W, 10));
        assertEquals(12, WidgetUtil.titleTextSize(W, 16));
        assertEquals(16, WidgetUtil.titleTextSize(W, 24));  
        
        // 2 rows widget  
        W = ListWidgetSize.LIST_WIDGET_SIZE2; 
        assertEquals(2, W.heightCells);
        assertEquals(9, WidgetUtil.titleTextSize(W, 10));
        assertEquals(12, WidgetUtil.titleTextSize(W, 16));
        assertEquals(16, WidgetUtil.titleTextSize(W, 24));  
        
        // 4 rows widget 
        W = ListWidgetSize.LIST_WIDGET_SIZE5; 
        assertEquals(4, W.heightCells);
        assertEquals(10, WidgetUtil.titleTextSize(W, 10));
        assertEquals(13, WidgetUtil.titleTextSize(W, 16));
        assertEquals(17, WidgetUtil.titleTextSize(W, 24));  
    }
}
