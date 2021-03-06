/*
 * Copyright (c) 2018-2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.lsp4intellij.requests;

import com.github.lsp4intellij.IntellijLanguageClient;
import com.github.lsp4intellij.client.languageserver.ServerStatus;
import com.github.lsp4intellij.client.languageserver.wrapper.LanguageServerWrapper;
import com.github.lsp4intellij.editor.EditorEventManager;
import com.github.lsp4intellij.editor.EditorEventManagerBase;
import com.github.lsp4intellij.utils.ApplicationUtils;
import com.github.lsp4intellij.utils.FileUtils;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FileEventManager {

    /**
     * Indicates that a document will be saved
     *
     * @param doc The document
     */
    public static void willSave(Document doc) {
        String uri = FileUtils.VFSToURI(FileDocumentManager.getInstance().getFile(doc));
        EditorEventManager manager = EditorEventManagerBase.forUri(uri);
        if (manager != null) {
            manager.willSave();
        }
    }

    /**
     * Indicates that all documents will be saved
     */
    public static void willSaveAllDocuments() {
        EditorEventManagerBase.willSaveAll();
    }

    /**
     * Called when a file is changed. Notifies the server if this file was watched.
     *
     * @param file The file
     */
    public static void fileChanged(VirtualFile file) {
        String uri = FileUtils.VFSToURI(file);
        if (uri != null) {
            EditorEventManager manager = EditorEventManagerBase.forUri(uri);
            if (manager != null) {
                manager.documentSaved();
                changedConfiguration(uri, FileChangeType.Changed, manager.wrapper);
            } else {
                changedConfiguration(uri, FileChangeType.Changed);
            }
        }
    }

    /**
     * Called when a file is moved. Notifies the server if this file was watched.
     *
     * @param file The file
     */
    public static void fileMoved(VirtualFile file) {

    }

    /**
     * Called when a file is deleted. Notifies the server if this file was watched.
     *
     * @param file The file
     */
    public static void fileDeleted(VirtualFile file) {
        String uri = FileUtils.VFSToURI(file);
        if (uri != null) {
            changedConfiguration(uri, FileChangeType.Deleted);
        }
    }

    private static void changedConfiguration(String uri, FileChangeType typ) {
        changedConfiguration(uri, typ, null);
    }

    private static void changedConfiguration(String uri, FileChangeType typ, LanguageServerWrapper wrapper) {

        ApplicationUtils.pool(() -> {
            List<FileEvent> event = new ArrayList<>();
            event.add(new FileEvent(uri, typ));
            DidChangeWatchedFilesParams params = new DidChangeWatchedFilesParams(event);
            Set<LanguageServerWrapper> wrappers = IntellijLanguageClient.getAllServerWrappers();
            if (wrappers != null) {
                for (LanguageServerWrapper w : wrappers) {
                    if (w != wrapper && w.getRequestManager() != null && w.getStatus() == ServerStatus.STARTED) {
                        w.getRequestManager().didChangeWatchedFiles(params);
                    }
                }
            }
        });
    }

    /**
     * Called when a file is renamed. Notifies the server if this file was watched.
     *
     * @param oldV The old file name
     * @param newV the new file name
     */
    public static void fileRenamed(String oldV, String newV) {

    }

    /**
     * Called when a file is created. Notifies the server if needed.
     *
     * @param file The file
     */
    public static void fileCreated(VirtualFile file) {
        String uri = FileUtils.VFSToURI(file);
        if (uri != null) {
            changedConfiguration(uri, FileChangeType.Created);
        }
    }
}
