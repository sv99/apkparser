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
import com.android.tools.smali.dexlib2.dexbacked.DexBackedClassDef;

public class DexDiffClassEntry {
    @NonNull private final String name;
    @Nullable private final DexBackedClassDef oldEntry;
    @Nullable private final DexBackedClassDef newEntry;
    private final long oldSize;
    private final long newSize;

    DexDiffClassEntry(
            @NonNull String name,
            @Nullable DexBackedClassDef oldEntry,
            @Nullable DexBackedClassDef newEntry,
            long oldSize,
            long newSize) {
        this.name = name;
        this.oldEntry = oldEntry;
        this.newEntry = newEntry;
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
