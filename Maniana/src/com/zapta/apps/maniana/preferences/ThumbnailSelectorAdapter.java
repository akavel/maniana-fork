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

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * Provide assertion methods.
 * 
 * @author Tal Dayan
 */
public class ThumbnailSelectorAdapter <T extends Thumbnail> extends BaseAdapter {
    
    private final Context mContext;
    
    private final T[] mThumbnails;

    public ThumbnailSelectorAdapter(Context c, T[] thumbnails) {
        mContext = c;
        mThumbnails = thumbnails;
    }

    public int getCount() {
        return mThumbnails.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView != null) { 
            // Recycle
            imageView = (ImageView) convertView;
        } else {
            // New view
            imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setPadding(0, 40, 0, 40);
        } 

        imageView.setImageResource(mThumbnails[position].getDrawableId());
        return imageView;
    }
}
