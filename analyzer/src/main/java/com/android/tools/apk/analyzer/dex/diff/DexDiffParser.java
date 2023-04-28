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
import com.android.tools.apk.analyzer.dex.PackageTreeCreator;
import com.android.tools.apk.analyzer.dex.tree.DexClassNode;
import com.android.tools.apk.analyzer.internal.SigUtils;
import com.android.tools.proguard.ProguardMap;
import com.android.tools.smali.dexlib2.dexbacked.DexBackedClassDef;
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile;
import com.android.tools.smali.dexlib2.iface.ClassDef;
import com.android.tools.smali.dexlib2.iface.DexFile;
import com.android.tools.smali.dexlib2.iface.reference.TypeReference;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DexDiffParser {

    @NonNull private final DexBackedDexFile oldFile;

    @NonNull private final DexBackedDexFile newFile;

    @NonNull private final ArrayList<DexDiffEntry> diffs;

    private Map<String, TypeReference> oldTypeRefsByName;

    private Map<String, TypeReference> newTypeRefsByName;

    private Set<? extends DexBackedClassDef> oldClasses;

    private Set<? extends DexBackedClassDef> newClasses;

    public DexDiffParser(@NonNull DexBackedDexFile oldFile, @NonNull DexBackedDexFile newFile) {
        this.oldFile = oldFile;
        this.newFile = newFile;
        this.diffs = new ArrayList<DexDiffEntry>();
    }

    @NonNull
    public ArrayList<DexDiffEntry> parse() throws IOException {

        // compare refs count
        oldTypeRefsByName = PackageTreeCreator.getAllTypeReferencesByClassName(oldFile);
        newTypeRefsByName = PackageTreeCreator.getAllTypeReferencesByClassName(newFile);
        compareRefsCount();

        // compare classes count
        oldClasses = oldFile.getClasses();
        newClasses = newFile.getClasses();
        compareClassesCount();

        for (DexBackedClassDef oldClassDef : oldClasses) {
            TypeReference typeRef = oldTypeRefsByName.get(oldClassDef.getType());
            String className = oldClassDef.getType();
            Optional<? extends DexBackedClassDef> newClassDef = getClassDef(newClasses, className);
            if (newClassDef.isPresent()) {
                // class exists
                compareClassBySize(oldClassDef, newClassDef.get());
            } else {
                // class not exists
                compareClassBySize(oldClassDef, null);
            }

            // compare by size
//            DexClassNode classNode = root.getOrCreateClass("", className, typeRef);
//            classNode.setUserObject(null);
//            classNode.setDefined(true);
//            classNode.setSize(classNode.getSize() + classDef.getSize());
            // addMethods(classNode, classDef.getMethods(), dexFilePath);
            // addFields(classNode, classDef.getFields(), dexFilePath);
        }

        return diffs;
    }

    private void compareRefsCount() {
        int oldSize = oldTypeRefsByName.size();
        int newSize = newTypeRefsByName.size();
        diffs.add(new DexDiffEntry("TypeReferencesCount", oldSize, newSize));
    }

    private void compareClassesCount() {
        int oldSize = oldClasses.size();
        int newSize = newClasses.size();
        diffs.add(new DexDiffEntry("ClassesCount", oldSize, newSize));
    }

    private void compareClassBySize(
            @Nullable DexBackedClassDef oldClassDef, @Nullable DexBackedClassDef newClassDef) {
        int oldSize = oldClassDef == null? 0: oldClassDef.getSize();
        int newSize = newClassDef == null? 0: newClassDef.getSize();

        // compare by size
        if (oldSize != newSize) {
            oldSize = oldClassDef == null? 0: oldClassDef.getSize();
            newSize = newClassDef == null? 0: newClassDef.getSize();
            String className;
            if (oldClassDef == null) {
                className = newClassDef.getType();
            } else {
                className = oldClassDef.getType();
            }
            diffs.add(new DexDiffEntry(className, oldSize, newSize));
        }
    }

    private static Optional<? extends DexBackedClassDef> getClassDef(Set<? extends DexBackedClassDef> classes,
                                                                     @NonNull String fqcn) {
        return classes
                .stream()
//                .filter(c -> fqcn.equals(SigUtils.signatureToName(c.getType())))
                .filter(c -> fqcn.equals(c.getType()))
                .findFirst();
    }
}
