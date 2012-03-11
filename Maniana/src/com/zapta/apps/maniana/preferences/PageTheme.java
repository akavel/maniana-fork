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

package com.zapta.apps.maniana.preferences;

import com.zapta.apps.maniana.R;

/**
 * Today/Tomorrow pages predefined themes.
 * 
 * @author Tal Dayan
 */
public class PageTheme extends Thumbnail {

    public static final PageTheme[] PAGE_THEMES = {
        // Default
        new PageTheme("On Paper", R.drawable.page_theme1_preview,
                PreferenceConstants.DEFAULT_PAGE_BACKGROUND_PAPER,
                PreferenceConstants.DEFAULT_PAGE_BACKGROUND_SOLID_COLOR,
                PreferenceConstants.DEFAULT_PAGE_FONT_TYPE,
                PreferenceConstants.DEFAULT_PAGE_FONT_SIZE,
                PreferenceConstants.DEFAULT_ITEM_TEXT_COLOR,
                PreferenceConstants.DEFAULT_COMPLETED_ITEM_TEXT_COLOR,
                PreferenceConstants.DEFAULT_PAGE_ITEM_DIVIDER_COLOR),

        new PageTheme("Yellow Pages", R.drawable.page_theme2_preview, false, 0xfffcfcb8,
                ItemFontType.SAN_SERIF, 18, 0xff333333, 0xff909090, 0x4def9900),

        new PageTheme("Dark Knight", R.drawable.page_theme3_preview, false, 0xff000000,
                ItemFontType.ELEGANT, 20, 0xffff8080, 0xff00aa00, 0x80ffff00),

        new PageTheme("Spartan", R.drawable.page_theme4_preview, false, 0xffffffff,
                ItemFontType.SERIF, 14, 0xff000000, 0xff000000, 0x00000000),
    };

    public final boolean backgroundPaper;
    public final int backgroundSolidColor;
    public final ItemFontType fontType;
    public final int fontSize;
    public final int textColor;
    public final int completedTextColor;
    public final int itemDividerColor;

    public PageTheme(String name, int drawableId, boolean backgroundPaper,
            int backgroundSolidColor, ItemFontType fontType, int fontSize, int textColor,
            int completedTextColor, int itemDividerColor) {
        super(name, drawableId);
        this.backgroundPaper = backgroundPaper;
        this.backgroundSolidColor = backgroundSolidColor;
        this.fontType = fontType;
        this.fontSize = fontSize;
        this.textColor = textColor;
        this.completedTextColor = completedTextColor;
        this.itemDividerColor = itemDividerColor;
    }
}
