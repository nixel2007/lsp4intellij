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
package com.github.lsp4intellij.client.languageserver.serverdefinition;

import com.github.lsp4intellij.client.connection.ProcessStreamConnectionProvider;
import com.github.lsp4intellij.client.connection.StreamConnectionProvider;

import java.util.Arrays;

/**
 * A base trait for every command-line server definition
 */
public class CommandServerDefinition extends UserConfigurableServerDefinition {
    private static final CommandServerDefinition INSTANCE = new CommandServerDefinition();

    public static CommandServerDefinition getInstance() {
        return INSTANCE;
    }

    protected String[] command;

    protected CommandServerDefinition() {
        this.presentableTyp = "Command";
        this.typ = "command";
    }

    /**
     * Transforms an array of string into the corresponding UserConfigurableServerDefinition
     *
     * @param arr The array
     * @return The server definition
     */
    public UserConfigurableServerDefinition fromArray(String[] arr) {
        CommandServerDefinition raw = RawCommandServerDefinition.getInstance().fromArray(arr);
        if (raw == null) {
            return ExeLanguageServerDefinition.getInstance().fromArray(arr);
        } else {
            return raw;
        }
    }

    @Override
    public StreamConnectionProvider createConnectionProvider(String workingDir) {
        return new ProcessStreamConnectionProvider(Arrays.asList(command), workingDir);
    }
}
