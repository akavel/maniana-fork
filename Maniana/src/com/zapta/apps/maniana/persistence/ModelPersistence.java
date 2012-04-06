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

import org.json.JSONException;

import android.content.Context;

import com.zapta.apps.maniana.main.AppContext;
import com.zapta.apps.maniana.model.AppModel;
import com.zapta.apps.maniana.persistence.ModelLoadingResult.ModelLoadingOutcome;
import com.zapta.apps.maniana.util.FileUtil;
import com.zapta.apps.maniana.util.FileUtil.FileReadResult;
import com.zapta.apps.maniana.util.FileUtil.FileReadResult.FileReadOutcome;
import com.zapta.apps.maniana.util.LogUtil;

/**
 * Manages model persistence.
 * 
 * @author Tal Dayan
 */
public class ModelPersistence {

    /** Path to asset with sample model in JSON format. */
    private static final String NEW_USER_MODEL_ASSET_NAME = "data/new_user_data.json";

    /** Path to file where model is persisted. */
    public static final String DATA_FILE_NAME = "maniana_data.json";

    /** Static lock protecting the access to the data file. */
    public static final Object sDataFileLock = new Object();

    public static final ModelLoadingResult loadModelDataFile(Context context, AppModel resultModel) {
        ModelLoadingResult result = internalLoadModelFile(context, resultModel, DATA_FILE_NAME,
                false);
        if (result.outcome.isOk()) {
            // Model is same as persistence file and no version change.
            resultModel.setClean();
        } else {
            // Model need to be rewritten
            resultModel.setDirty();
        }
        return result;
    }

    public static final ModelLoadingResult loadSampleModelAsset(Context context,
            AppModel resultModel) {
        ModelLoadingResult result = internalLoadModelFile(context, resultModel,
                NEW_USER_MODEL_ASSET_NAME, true);
        // Since we did not load the model from the data file, it is dirty and need to be
        // persisted.
        resultModel.setDirty();
        return result;
    }

    /**
     * Caller is expected to manager the model's dirty bit. In case of an error, the returned model
     * is cleared.
     */
    private static final ModelLoadingResult internalLoadModelFile(Context context,
            AppModel resultModel, String fileName, boolean isAsset) {
        LogUtil.info("Going to read data from " + (isAsset ? "assert" : "data File") + " "
                + fileName);

        resultModel.clear();

        // Try to read the model file
        final FileReadResult fileReadResult;
        synchronized (sDataFileLock) {
            fileReadResult = FileUtil.readFileToString(context, fileName, isAsset);
        }

        if (fileReadResult.outcome == FileReadOutcome.NOT_FOUND) {
            return new ModelLoadingResult(ModelLoadingOutcome.FILE_NOT_FOUND);
        }

        // Try to parse the json file
        try {
            PersistenceMetadata resultMetadata = new PersistenceMetadata();
            ModelDeserialization.deserializeModel(resultModel, resultMetadata,
                    fileReadResult.content);
            return new ModelLoadingResult(ModelLoadingOutcome.FILE_READ_OK, resultMetadata);

        } catch (JSONException e) {
            LogUtil.error(e, "Error parsing model JSON");
            resultModel.clear();
            return new ModelLoadingResult(ModelLoadingOutcome.FILE_HAS_ERRORS);
        }
    }

    /** Save model to file. */
    public static final void saveData(AppContext app, AppModel model, PersistenceMetadata metadata) {
        LogUtil.info("Saving model to file: " + DATA_FILE_NAME);
        final String json = ModelSerialization.serializeModel(model, metadata);
        synchronized (sDataFileLock) {
            FileUtil.writeStringToFile(app.context(), json, DATA_FILE_NAME, Context.MODE_PRIVATE);
        }
        // Model reflects persisted state.
        model.setClean();
    }
}
