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

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.zapta.apps.maniana.R;

/**
 * Dialog to select one of a few thumbnails (e.g. theme selection)
 * 
 * @author Tal Dayan
 */
public class ThumbnailSelector<T extends Thumbnail> extends Dialog {

    public interface ThumbnailSelectorListener<T extends Thumbnail> {
        void onThumbnailSelection(T thumbnail);
    }

    private final T[] mThumbnails;
    private final ThumbnailSelectorListener<T> mListener;

    public ThumbnailSelector(Context context, String title, T[] thumbnails,
            ThumbnailSelectorListener<T> listener) {
        super(context);
        mThumbnails = thumbnails;
        mListener = listener;
        setContentView(R.layout.thumbnail_selector_layout);
        setTitle(title);

        ListView listView = (ListView) findViewById(R.id.selector_list);
        listView.setAdapter(new ThumbnailSelectorAdapter<T>(context, thumbnails));

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handleItemClick(position);
            }
        });
    }

    private final void handleItemClick(int index) {
        dismiss();
        mListener.onThumbnailSelection(mThumbnails[index]);   
    }
}