/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.apk.analyzer.dex.diff;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.apk.analyzer.dex.tree.DexElementNode;

public class DexDiffRefEntry {
    @NonNull private final String name;
    @Nullable private final DexElementNode oldNode;
    @Nullable private final DexElementNode newNode;
    private final long oldSize;
    private final long newSize;

    DexDiffRefEntry(
            @NonNull String name,
            @Nullable DexElementNode oldNode,
            @Nullable DexElementNode newNode,
            long oldSize,
            long newSize) {
        this.name = name;
        this.oldNode = oldNode;
        this.newNode = newNode;
        this.oldSize = oldSize;
        this.newSize = newSize;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public long getSize() {
        return newSize - oldSize;
    }

    public long getOldSize() {
        return oldSize;
    }

    public long getNewSize() {
        return newSize;
    }

    @Override
    public String toString() {
        return getName();
    }
}
