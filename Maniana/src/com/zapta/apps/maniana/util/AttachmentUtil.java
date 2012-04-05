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

package com.zapta.apps.maniana.util;

import java.io.File;

import android.content.Context;

import com.zapta.apps.maniana.persistence.ModelPersistence;
import com.zapta.apps.maniana.util.FileUtil.FileReadResult;

/**
 * Operations related to sending the data file as an attachment.
 * 
 * @author Tal Dayan
 */
public final class AttachmentUtil {
    
    public static final String BACKUP_ATTACHMENT_FILE_NAME = "maniana_backup.json";

    /** Do not instantiate */
    private AttachmentUtil() {
    }

    /** 
     * Create attachment file. 
     * This is a copy of the data file but with world read access so it can be read by
     * the email client
     */
    public static boolean createAttachmentFile(Context context) {
        
        final FileReadResult readResult = FileUtil.readFileToString(context, ModelPersistence.DATA_FILE_NAME, false);
        if (readResult.outcoe != FileReadResult.FileReadOutcome.READ_OK) {
            return false;
        }
        
        FileUtil.writeStringToFile(context, readResult.content, BACKUP_ATTACHMENT_FILE_NAME, Context.MODE_WORLD_READABLE);
        return true;
    }
    
    /** 
     * 
     */
    public static void garbageCollectAttachmentFile(Context context) {
        
        final File file = new File(context.getFilesDir(), BACKUP_ATTACHMENT_FILE_NAME);
        if (file.exists()) {
            final long lastModifiedMillis = file.lastModified();
            final long deltaMillis = System.currentTimeMillis() - lastModifiedMillis;
            // NOTE: we also delete files that somehow have modification time in the future, just
            // in case.
            if (Math.abs(deltaMillis) >= 5 * 60 * 1000) {
               final boolean deletedOk = file.delete();
               if (deletedOk) {
                   LogUtil.info("Deleted attachment file: %s (%d secs old)", file.getAbsolutePath(), deltaMillis / 1000);
               } else {
                   LogUtil.error("Failed to delete attachment file: %s (%d secs old)", file.getAbsolutePath(), deltaMillis / 1000);
               }
            }
        }
    }
}
