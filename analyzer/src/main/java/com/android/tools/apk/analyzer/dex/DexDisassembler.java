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
package com.android.tools.apk.analyzer.dex;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.apk.analyzer.internal.SigUtils;
import com.android.tools.apk.analyzer.internal.rewriters.FieldReferenceWithNameRewriter;
import com.android.tools.apk.analyzer.internal.rewriters.MethodReferenceWithNameRewriter;
import com.android.tools.proguard.ProguardMap;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Optional;
import java.util.stream.StreamSupport;
import com.android.tools.smali.baksmali.Adaptors.ClassDefinition;
import com.android.tools.smali.baksmali.Adaptors.MethodDefinition;
import com.android.tools.smali.baksmali.BaksmaliOptions;
import com.android.tools.smali.baksmali.formatter.BaksmaliWriter;
import com.android.tools.smali.dexlib2.Opcode;
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile;
import com.android.tools.smali.dexlib2.formatter.DexFormatter;
import com.android.tools.smali.dexlib2.iface.ClassDef;
import com.android.tools.smali.dexlib2.iface.DexFile;
import com.android.tools.smali.dexlib2.iface.Method;
import com.android.tools.smali.dexlib2.iface.MethodImplementation;
import com.android.tools.smali.dexlib2.iface.instruction.Instruction;
import com.android.tools.smali.dexlib2.iface.reference.FieldReference;
import com.android.tools.smali.dexlib2.iface.reference.MethodReference;
import com.android.tools.smali.dexlib2.rewriter.DexRewriter;
import com.android.tools.smali.dexlib2.rewriter.DexFileRewriter;
import com.android.tools.smali.dexlib2.rewriter.Rewriter;
import com.android.tools.smali.dexlib2.rewriter.RewriterModule;
import com.android.tools.smali.dexlib2.rewriter.Rewriters;
import com.android.tools.smali.dexlib2.rewriter.TypeRewriter;

public class DexDisassembler {
    @NonNull private final DexFile dexFile;
    @Nullable private final ProguardMap proguardMap;

    public DexDisassembler(@NonNull DexBackedDexFile dexFile, @Nullable ProguardMap proguardMap) {
        this.dexFile = proguardMap == null ? dexFile : rewriteDexFile(dexFile, proguardMap);
        this.proguardMap = proguardMap;
    }

    @NonNull
    public String disassembleMethod(@NonNull String fqcn, @NonNull String methodDescriptor)
            throws IOException {
        fqcn = PackageTreeCreator.decodeClassName(SigUtils.typeToSignature(fqcn), proguardMap);
        Optional<? extends ClassDef> classDef = getClassDef(fqcn);
        if (!classDef.isPresent()) {
            throw new IllegalStateException("Unable to locate class definition for " + fqcn);
        }

        Optional<? extends Method> method =
                StreamSupport.stream(classDef.get().getMethods().spliterator(), false)
                        .filter(m -> methodDescriptor.equals(DexFormatter.INSTANCE.getMethodDescriptor(m)))
                        .findFirst();

        if (!method.isPresent()) {
            throw new IllegalStateException(
                    "Unable to locate method definition in class for method " + methodDescriptor);
        }

        return getMethodDexCode(classDef.get(), method.get());
    }

    @NonNull
    public String disassembleMethod(@NonNull String fqcn, @NonNull MethodReference methodRef)
            throws IOException {
        String jvmFqcn = PackageTreeCreator.decodeClassName(SigUtils.typeToSignature(fqcn), proguardMap);
        Optional<? extends ClassDef> classDef = getClassDef(jvmFqcn);
        if (!classDef.isPresent()) {
            throw new IllegalStateException("Unable to locate class definition for " + fqcn);
        }
        MethodReference finalMethodRef =
                proguardMap != null
                        ? getRewriter(proguardMap).getMethodReferenceRewriter().rewrite(methodRef)
                        : methodRef;

        Optional<? extends Method> method =
                StreamSupport.stream(classDef.get().getMethods().spliterator(), false)
                        .filter(finalMethodRef::equals)
                        .findFirst();

        if (!method.isPresent()) {
            throw new IllegalStateException(
                    "Unable to locate method definition in class for method " + methodRef);
        }

        return getMethodDexCode(classDef.get(), method.get());
    }

    public String getMethodBody(@NonNull String fqcn, @NonNull String methodDescriptor)
            throws IOException {
        String jvmFqcn = PackageTreeCreator.decodeClassName(SigUtils.typeToSignature(fqcn), proguardMap);
        Optional<? extends ClassDef> classDef = getClassDef(jvmFqcn);
        if (!classDef.isPresent()) {
            throw new IllegalStateException("Unable to locate class definition for " + fqcn);
        }

        Optional<? extends Method> method =
                StreamSupport.stream(classDef.get().getMethods().spliterator(), false)
                        .filter(m -> methodDescriptor.equals(DexFormatter.INSTANCE.getMethodDescriptor(m)))
                        .findFirst();

        if (!method.isPresent()) {
            throw new IllegalStateException(
                    "Unable to locate method definition in class for method " + methodDescriptor);
        }

        MethodImplementation impl = method.get().getImplementation();
        if (impl == null) {
            throw new IllegalStateException(
                    "The method don't have implementation " + methodDescriptor);
        }
        StringWriter writer = new StringWriter(1024);
        writer.write(methodDescriptor);
        writer.write("\n");
        writer.write(String.format("\t.registers %s\n", impl.getRegisterCount()));
        for (Instruction ins: impl.getInstructions()) {
            writer.write(String.format("%s: %s\n", ins.getOpcode().toString(), ins.getCodeUnits()));
        }
        return writer.toString();
    }

    @NonNull
    private static String getMethodDexCode(ClassDef classDef, Method method) throws IOException {
        BaksmaliOptions options = new BaksmaliOptions();
        ClassDefinition classDefinition = new ClassDefinition(options, classDef);

        StringWriter writer = new StringWriter(1024);
        try (BaksmaliWriter bw = new BaksmaliWriter(writer)) {
            MethodImplementation methodImpl = method.getImplementation();
            if (methodImpl == null) {
                MethodDefinition.writeEmptyMethodTo(bw, method, classDefinition);
            } else {
                MethodDefinition methodDefinition =
                        new MethodDefinition(classDefinition, method, methodImpl);
                methodDefinition.writeTo(bw);
            }
        }

        return writer.toString().replace("\r", "");
    }

    @NonNull
    public String disassembleClass(@NonNull String fqcn) throws IOException {
        fqcn = PackageTreeCreator.decodeClassName(SigUtils.typeToSignature(fqcn), proguardMap);
        Optional<? extends ClassDef> classDef = getClassDef(fqcn);
        if (!classDef.isPresent()) {
            throw new IllegalStateException("Unable to locate class definition for " + fqcn);
        }

        BaksmaliOptions options = new BaksmaliOptions();
        ClassDefinition classDefinition = new ClassDefinition(options, classDef.get());

        StringWriter writer = new StringWriter(1024);
        try (BaksmaliWriter bw = new BaksmaliWriter(writer)) {
            classDefinition.writeTo(bw);
        }
        return writer.toString().replace("\r", "");
    }

    private static DexFile rewriteDexFile(@NonNull DexFile dexFile, @NonNull ProguardMap map) {
        DexFileRewriter rewriter = new DexFileRewriter(getRewriter(map));
        return rewriter.rewrite(dexFile);
    }

    @NonNull
    private static DexRewriter getRewriter(@NonNull ProguardMap map) {
        return new DexRewriter(
                new RewriterModule() {
                    @NonNull
                    @Override
                    public Rewriter<String> getTypeRewriter(@NonNull Rewriters rewriters) {
                        return new TypeRewriter() {
                            @NonNull
                            @Override
                            public String rewrite(@NonNull String typeName) {
                                return SigUtils.typeToSignature(
                                        PackageTreeCreator.decodeClassName(typeName, map));
                            }
                        };
                    }

                    @NonNull
                    @Override
                    public Rewriter<FieldReference> getFieldReferenceRewriter(
                            @NonNull Rewriters rewriters) {
                        return new FieldReferenceWithNameRewriter(rewriters) {
                            @Override
                            public String rewriteName(FieldReference fieldReference) {
                                return PackageTreeCreator.decodeFieldName(fieldReference, map);
                            }
                        };
                    }

                    @NonNull
                    @Override
                    public Rewriter<MethodReference> getMethodReferenceRewriter(
                            @NonNull Rewriters rewriters) {
                        return new MethodReferenceWithNameRewriter(rewriters) {
                            @Override
                            public String rewriteName(MethodReference methodReference) {
                                return PackageTreeCreator.decodeMethodName(methodReference, map);
                            }
                        };
                    }
                });
    }

    @NonNull
    private Optional<? extends ClassDef> getClassDef(@NonNull String fqcn) {
        return dexFile.getClasses()
                .stream()
                .filter(c -> fqcn.equals(SigUtils.signatureToName(c.getType())))
                .findFirst();
    }
}
