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
package com.zapta.apps.maniana.menus;

import javax.annotation.Nullable;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.LinearLayout;

import com.zapta.apps.maniana.util.LogUtil;

/**
 * A subclass of LinearLayout that can intercept key events as they are dispatched down.
 * 
 * @author Tal Dayan
 */
public class KeyInterceptingLinearLayout extends LinearLayout {

    public interface KeyInterceptionListener {
        /** A delegation of ViewGroup.dispatchKeyEvent() */
        public boolean onKeyIntercept(KeyEvent event);
    }

    @Nullable
    private KeyInterceptionListener mListener;

    public KeyInterceptingLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        LogUtil.debug("*** intercepted: %d", event.getKeyCode());
        if (mListener != null && mListener.onKeyIntercept(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    public void setKeyInterceptionListener(@Nullable KeyInterceptionListener mListener) {
        this.mListener = mListener;
    }
}