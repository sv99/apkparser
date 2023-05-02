/*
 * Copyright (C) 2023 The Android Open Source Project
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
package com.android.tools.apk.analyzer.dex;

import com.android.annotations.NonNull;
import com.android.tools.apk.analyzer.internal.SigUtils;
import com.android.tools.smali.dexlib2.dexbacked.DexBackedClassDef;
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DexDiffParser {

    @NonNull private final DexBackedDexFile oldFile;

    @NonNull private final DexBackedDexFile newFile;

    private final ProguardMappings proguardMappings;

    public DexDiffParser(@NonNull DexBackedDexFile oldFile, @NonNull DexBackedDexFile newFile) {
        this.oldFile = oldFile;
        this.newFile = newFile;
        // now empty proguard mapping
        this.proguardMappings = new ProguardMappings(null, null, null);

    }

    public void parse(@NonNull PrintStream out) throws IOException {

        // compare refs count
        int oldRefsCount = PackageTreeCreator.getAllTypeReferencesByClassName(oldFile).size();
        int newRefsCount = PackageTreeCreator.getAllTypeReferencesByClassName(newFile).size();
        out.printf("%s\t%s\t%s",
                oldRefsCount,
                newRefsCount,
                "TypeReferencesCount").println();

        // compare classes count
        Set<? extends DexBackedClassDef> oldClasses = oldFile.getClasses();
        Set<? extends DexBackedClassDef> newClasses = newFile.getClasses();
        out.printf("%s\t%s\t%s",
                oldClasses.size(),
                newClasses.size(),
                "ClassesCount").println();

        Set<String> oldClassesNames = oldClasses.stream().
                map(def -> SigUtils.signatureToName(def.getType())).
                collect(Collectors.toSet());
        Set<String> newClassesNames = newClasses.stream().
                map(def -> SigUtils.signatureToName(def.getType())).
                collect(Collectors.toSet());
        // print classes only in the old dex
        for (String className : oldClassesNames.stream()
                .filter(c -> !newClassesNames.contains(c)).collect(Collectors.toSet())) {
            out.printf("=>\t%s", className);
        }
        // print classes only in the new dex
        for (String className : newClassesNames.stream()
                .filter(c -> !oldClassesNames.contains(c)).collect(Collectors.toSet())) {
            out.printf("<=\t%s", className);
        }
        // compare decompiled classes
        for (String className : oldClassesNames.stream()
                .filter(newClassesNames::contains).collect(Collectors.toSet())) {
            compareDecompiledClasses(out, className);
        }
    }

    private void compareDecompiledClasses(
            @NonNull PrintStream out, String className) throws IOException{

        DexDisassembler oldDisassembler =
                new DexDisassembler(oldFile, proguardMappings.map);

        String oldDis = oldDisassembler.disassembleClass(className);

        DexDisassembler newDisassembler =
                new DexDisassembler(oldFile, proguardMappings.map);

        String newDis = newDisassembler.disassembleClass(className);

        if (!oldDis.equals(newDis)) {
            out.printf("!=\t%s", className);
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
