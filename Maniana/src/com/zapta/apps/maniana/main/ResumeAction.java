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

package com.zapta.apps.maniana.main;

import javax.annotation.Nullable;

import android.content.Intent;

import com.zapta.apps.maniana.util.LogUtil;

/**
 * The main activity is always resumed with one of these actions.
 * 
 * @author Tal Dayan
 */
public enum ResumeAction {
	 NONE, ADD_NEW_ITEM_BY_TEXT, ADD_NEW_ITEM_BY_VOICE;

	/** Key for serializing resume actions in intents. Not persisted. */
	private static final String RESUME_ACTION_KEY = "maniana_resume_action";

	/** Default action when action is not specified in the launch intent. */
	private static final ResumeAction DEFAULT_ACTION = NONE;

	public boolean isNone() {
		return this == NONE;
	}
	
	/** Serialize a resume action in an intent. */
	public static void setInIntent(Intent intent, ResumeAction resumeAction) {
		intent.putExtra(RESUME_ACTION_KEY, resumeAction.toString());
	}

	/** Deserialize a resume action from an intent */
	public static ResumeAction fromIntent(Intent intent) {
		@Nullable
		final String strValue = intent.getStringExtra(RESUME_ACTION_KEY);
		if (strValue == null) {
			return DEFAULT_ACTION;
		}

		@Nullable
		final ResumeAction value = ResumeAction.valueOf(strValue);
		if (value == null) {
			LogUtil.error("Unknown resume action string: [%s]", strValue);
			return DEFAULT_ACTION;
		}

		return value;
	}
}
