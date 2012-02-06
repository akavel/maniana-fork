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

package com.zapta.apps.maniana.model;


/**
 * Represents the data of a single item.
 * 
 * @author Tal Dayan.
 */
public class ItemModel implements ItemModelReadOnly {
	
    /** The item text. */
	private String mText;
	
	/** Is this item done? */
	private boolean mIsCompleted;
	
	/** Is this item blocked. */
	private boolean mIsLocked;
	
	/** The item color. */
	private ItemColor mColor;

	/** Default constructor. */
	public ItemModel() {
		mText = "";
		mIsCompleted = false;
		mIsLocked = false;
		mColor = ItemColor.NONE;
	}
	
	/** Constructor with initial values. */
	public ItemModel(String text, boolean isCompleted, boolean isLocked, ItemColor color) {
		mText = text;
		mIsCompleted = isCompleted;
		mIsLocked = isLocked;
		mColor = color;
	}
	
	/** Copy constructor. Create an identical but independent instance */
	public ItemModel(ItemModel other) {
		copyFrom(other);
	}
	
	/** Set to same values as other item. */
	public final void copyFrom(ItemModel other) {
		mText = other.mText;
		mIsCompleted = other.mIsCompleted;
		mIsLocked = other.mIsLocked;
		mColor = other.mColor;
	}

	@Override
    public final String getText() {
        return mText;
    }

    public final void setText(String text) {
        mText = text;
    }

    @Override
    public final boolean isCompleted() {
        return mIsCompleted;
    }

    public final void setIsCompleted(boolean isCompleted) {
        mIsCompleted = isCompleted;
    }
    
    public final int sortingGroupIndex() {
        return mIsCompleted ? (mIsLocked? 2 : 1) : (mIsLocked? 3 : 0);
     }

    @Override
    public final boolean isLocked() {
        return mIsLocked;
    }

    public final void setIsLocked(boolean isLocked) {
        mIsLocked = isLocked;
    }

    @Override
    public final ItemColor getColor() {
        return mColor;
    }

    public final void setColor(ItemColor color) {
        mColor = color;
    }
}
