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

package com.zapta.apps.maniana.settings;

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
                PreferenceConstants.DEFAULT_PAGE_PAPER_COLOR,
                PreferenceConstants.DEFAULT_PAGE_BACKGROUND_SOLID_COLOR,
                PreferenceConstants.DEFAULT_PAGE_ICON_SET,
                PreferenceConstants.DEFAULT_PAGE_TITLE_FONT,
                PreferenceConstants.DEFAULT_PAGE_TITLE_SIZE,
                PreferenceConstants.DEFAULT_PAGE_TITLE_TODAY_COLOR,
                PreferenceConstants.DEFAULT_PAGE_TITLE_TOMORROW_COLOR,
                PreferenceConstants.DEFAULT_PAGE_ITEM_FONT,
                PreferenceConstants.DEFAULT_PAGE_ITEM_FONT_SIZE,
                PreferenceConstants.DEFAULT_ITEM_TEXT_COLOR,
                PreferenceConstants.DEFAULT_COMPLETED_ITEM_TEXT_COLOR,
                PreferenceConstants.DEFAULT_PAGE_ITEM_DIVIDER_COLOR),

        new PageTheme("Yellow Pages", R.drawable.page_theme2_preview, false, 0xffffffff,
                0xfffcfcb8, PageIconSet.HAND_DRAWN, Font.IMPACT, 20, 0xff0077ff, 0xff88aaff,
                Font.SAN_SERIF, 18, 0xff333333, 0xff909090, 0x4def9900),

        new PageTheme("Dark Knight", R.drawable.page_theme3_preview, false, 0xffffffff, 0xff000000,
                PageIconSet.HAND_DRAWN, Font.IMPACT, 20, 0xff0077ff, 0xff88aaff, Font.ELEGANT, 20,
                0xffff8080, 0xff00aa00, 0x80ffff00),

        new PageTheme("Spartan", R.drawable.page_theme4_preview, false, 0xffffffff, 0xffffffff,
                PageIconSet.WHITE, Font.IMPACT, 20, 0xff0077ff, 0xff88aaff, Font.SERIF, 14,
                0xff000000, 0xff000000, 0x00000000),

        new PageTheme("Kermit", R.drawable.page_theme5_preview, true, 0xfff0fff0, 0xffaaffff,
                PageIconSet.MODERN, Font.IMPACT, 20, 0xff0077ff, 0xff88aaff, Font.CASUAL, 16,
                0xff111111, 0xff555555, 0x30000000),
    };

    public final boolean backgroundPaper;
    public final int paperColor;
    public final int backgroundSolidColor;
    public final PageIconSet iconSet;
    public final Font titleFont;
    public final int titleFontSize;
    public final int titleTodayTextColor;
    public final int titleTomorrowTextColor;
    public final Font itemFont;
    public final int itemFontSize;
    public final int itemTextColor;
    public final int itemCompletedTextColor;
    public final int itemDividerColor;

    public PageTheme(String name, int drawableId, boolean backgroundPaper, int paperColor,
            int backgroundSolidColor, PageIconSet iconSet, Font titleFont, int titleFontSize,
            int titleTodayTextColor, int titleTomorrowTextColor, Font itemFont, int itemFontSize,
            int itemTextColor, int itemCompletedTextColor, int itemDividerColor) {
        super(name, drawableId);
        this.backgroundPaper = backgroundPaper;
        this.paperColor = paperColor;
        this.backgroundSolidColor = backgroundSolidColor;
        this.iconSet = iconSet;
        this.titleFont = titleFont;
        this.titleFontSize = titleFontSize;
        this.titleTodayTextColor = titleTodayTextColor;
        this.titleTomorrowTextColor = titleTomorrowTextColor;
        this.itemFont = itemFont;
        this.itemFontSize = itemFontSize;
        this.itemTextColor = itemTextColor;
        this.itemCompletedTextColor = itemCompletedTextColor;
        this.itemDividerColor = itemDividerColor;
    }
}
