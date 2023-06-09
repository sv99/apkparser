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
package com.android.tools.apk.analyzer;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.ide.common.process.ProcessException;
import com.android.ide.common.xml.AndroidManifestParser;
import com.android.ide.common.xml.ManifestData;
import com.android.tools.apk.analyzer.arsc.BinaryResourceFile;
import com.android.tools.apk.analyzer.arsc.BinaryResourceValue;
import com.android.tools.apk.analyzer.arsc.Chunk;
import com.android.tools.apk.analyzer.arsc.PackageChunk;
import com.android.tools.apk.analyzer.arsc.ResourceTableChunk;
import com.android.tools.apk.analyzer.arsc.StringPoolChunk;
import com.android.tools.apk.analyzer.arsc.TypeChunk;
import com.android.tools.apk.analyzer.arsc.TypeSpecChunk;
import com.android.tools.apk.analyzer.dex.*;
import com.android.tools.apk.analyzer.dex.DexDiffParser;
import com.android.tools.apk.analyzer.dex.tree.DexClassNode;
import com.android.tools.apk.analyzer.dex.tree.DexElementNode;
import com.android.tools.apk.analyzer.dex.tree.DexFieldNode;
import com.android.tools.apk.analyzer.dex.tree.DexMethodNode;
import com.android.tools.apk.analyzer.dex.tree.DexPackageNode;
import com.android.tools.apk.analyzer.internal.ApkDiffEntry;
import com.android.tools.apk.analyzer.internal.ApkDiffParser;
import com.android.tools.apk.analyzer.internal.ApkFileByFileDiffParser;
import com.android.tools.apk.analyzer.internal.SigUtils;
import com.android.tools.proguard.ProguardMap;
import com.android.tools.proguard.ProguardSeedsMap;
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * Tool for getting all kinds of information about an APK, including: - basic package info, sizes
 * and files list - dex code - resources
 */
public class ApkAnalyzerImpl {
    @NonNull
    private final PrintStream out;
    @NonNull
    private final AaptInvoker aaptInvoker;
    private boolean humanReadableFlag;

    /**
     * Constructs a new command-line processor.
     */
    public ApkAnalyzerImpl(@NonNull PrintStream out, @NonNull AaptInvoker aaptInvoker) {
        this.out = out;
        this.aaptInvoker = aaptInvoker;
    }

    public void resPackages(@NonNull Path apk) {
        try (ArchiveContext archiveContext = Archives.open(apk)) {
            ResourceTableChunk resourceTableChunk = getResourceTableChunk(archiveContext);
            resourceTableChunk
                    .getPackages()
                    .forEach(packageChunk -> out.println(packageChunk.getPackageName()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void resXml(@NonNull Path apk, @NonNull String filePath) {
        try (ArchiveContext archiveContext = Archives.open(apk)) {
            Path path = archiveContext.getArchive().getContentRoot().resolve(filePath);
            byte[] bytes = Files.readAllBytes(path);
            if (!archiveContext.getArchive().isBinaryXml(path, bytes)) {
                throw new IOException("The supplied file is not a binary XML resource.");
            }
            out.write(BinaryXmlParser.decodeXml(path.getFileName().toString(), bytes));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void resNames(
            @NonNull Path apk,
            @NonNull String type,
            @NonNull String config,
            @Nullable String packageName) {
        try (ArchiveContext archiveContext = Archives.open(apk)) {
            ResourceTableChunk resourceTableChunk = getResourceTableChunk(archiveContext);
            PackageChunk packageChunk = getPackageChunk(packageName, resourceTableChunk);
            TypeSpecChunk typeSpecChunk = packageChunk.getTypeSpecChunk(type);
            List<TypeChunk> typeChunks =
                    ImmutableList.copyOf(packageChunk.getTypeChunks(typeSpecChunk.getId()));
            for (TypeChunk typeChunk : typeChunks) {
                if (config.equals(typeChunk.getConfiguration().toString())) {
                    for (TypeChunk.Entry typeEntry : typeChunk.getEntries().values()) {
                        out.println(typeEntry.key());
                    }
                    return;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        throw new IllegalArgumentException(
                String.format("Can't find specified resource configuration (%s)", config));
    }

    public void resValue(
            @NonNull Path apk,
            @NonNull String type,
            @NonNull String config,
            @NonNull String name,
            @Nullable String packageName) {
        try (ArchiveContext archiveContext = Archives.open(apk)) {
            ResourceTableChunk resourceTableChunk = getResourceTableChunk(archiveContext);
            StringPoolChunk stringPoolChunk = resourceTableChunk.getStringPool();
            PackageChunk packageChunk = getPackageChunk(packageName, resourceTableChunk);
            TypeSpecChunk typeSpecChunk = packageChunk.getTypeSpecChunk(type);
            List<TypeChunk> typeChunks =
                    ImmutableList.copyOf(packageChunk.getTypeChunks(typeSpecChunk.getId()));
            for (TypeChunk typeChunk : typeChunks) {
                if (config.equals(typeChunk.getConfiguration().toString())) {
                    for (TypeChunk.Entry typeEntry : typeChunk.getEntries().values()) {
                        if (name.equals(typeEntry.key())) {
                            BinaryResourceValue value = typeEntry.value();
                            String valueString = null;
                            if (value != null) {
                                valueString = formatValue(value, stringPoolChunk);
                            } else {
                                Map<Integer, BinaryResourceValue> values = typeEntry.values();
                                if (values != null) {
                                    valueString =
                                            values.values()
                                                    .stream()
                                                    .map(v -> formatValue(v, stringPoolChunk))
                                                    .collect(Collectors.joining(", "));
                                }
                            }
                            if (valueString != null) {
                                out.println(valueString);
                            } else {
                                throw new IllegalArgumentException(
                                        "Can't find specified resource value");
                            }
                        }
                    }
                    return;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        throw new IllegalArgumentException(
                String.format("Can't find specified resource configuration (%s)", config));
    }

    public void resConfigs(@NonNull Path apk, @NonNull String type, @Nullable String packageName) {
        try (ArchiveContext archiveContext = Archives.open(apk)) {
            ResourceTableChunk resourceTableChunk = getResourceTableChunk(archiveContext);
            PackageChunk packageChunk = getPackageChunk(packageName, resourceTableChunk);
            TypeSpecChunk typeSpecChunk = packageChunk.getTypeSpecChunk(type);
            List<TypeChunk> typeChunks =
                    ImmutableList.copyOf(packageChunk.getTypeChunks(typeSpecChunk.getId()));
            for (TypeChunk typeChunk : typeChunks) {
                out.println(typeChunk.getConfiguration().toString());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static ResourceTableChunk getResourceTableChunk(ArchiveContext archiveContext) throws IOException {
        byte[] resContents =
                Files.readAllBytes(
                        archiveContext.getArchive().getContentRoot().resolve("resources.arsc"));
        BinaryResourceFile binaryRes = new BinaryResourceFile(resContents);
        List<Chunk> chunks = binaryRes.getChunks();
        if (chunks.isEmpty()) {
            throw new IOException("no chunks");
        }

        if (!(chunks.get(0) instanceof ResourceTableChunk)) {
            throw new IOException("no res table chunk");
        }

        return (ResourceTableChunk) chunks.get(0);
    }

    @NonNull
    private static PackageChunk getPackageChunk(@Nullable String packageName, ResourceTableChunk resourceTableChunk) {
        Optional<PackageChunk> packageChunk;
        if (packageName != null) {
            packageChunk = Optional.ofNullable(resourceTableChunk.getPackage(packageName));
        } else {
            packageChunk = resourceTableChunk.getPackages().stream().findFirst();
        }
        if (!packageChunk.isPresent()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Can't find package chunk %s",
                            packageName == null ? "" : "(" + packageName + ")"));
        }
        return packageChunk.get();
    }

    public void dexCode(
            @NonNull Path apk,
            @NonNull String fqcn,
            @Nullable String method,
            @Nullable Path proguardFolderPath,
            @Nullable Path proguardMapFilePath) {
        ProguardMappings proguardMappings =
                new ProguardMappings(proguardFolderPath, proguardMapFilePath, null, null);

        try (ArchiveContext archiveContext = Archives.open(apk)) {
            Collection<Path> dexPaths =
                    getDexFilesFrom(archiveContext.getArchive().getContentRoot());

            boolean dexFound = false;
            for (Path dexPath : dexPaths) {
                DexDisassembler disassembler =
                        new DexDisassembler(DexFiles.getDexFile(dexPath), proguardMappings.map);
                if (method == null) {
                    try {
                        out.println(disassembler.disassembleClass(fqcn));
                        dexFound = true;
                    } catch (IllegalStateException e) {
                        //this dex file doesn't contain the given class.
                        //continue searching
                    }
                } else {
                    try {
                        String originalFqcn =
                                PackageTreeCreator.decodeClassName(
                                        SigUtils.typeToSignature(fqcn), proguardMappings.map);
                        out.println(
                                disassembler.disassembleMethod(
                                        fqcn,
                                        SigUtils.typeToSignature(originalFqcn) + "->" + method));
                        dexFound = true;
                    } catch (IllegalStateException e) {
                        //this dex file doesn't contain the given method.
                        //continue searching
                    }
                }
            }
            if (!dexFound) {
                if (method == null) {
                    throw new IllegalArgumentException(
                            String.format("The given class (%s) not found", fqcn));
                } else {
                    throw new IllegalArgumentException(
                            String.format(
                                    "The given class (%s) or method (%s) not found", fqcn, method));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void dexCodeBody(
            @NonNull Path apk,
            @NonNull String fqcn,
            @NonNull String method,
            @Nullable Path proguardFolderPath,
            @Nullable Path proguardMapFilePath) {
        ProguardMappings proguardMappings =
                new ProguardMappings(proguardFolderPath, proguardMapFilePath, null, null);

        try (ArchiveContext archiveContext = Archives.open(apk)) {
            Collection<Path> dexPaths =
                    getDexFilesFrom(archiveContext.getArchive().getContentRoot());

            boolean dexFound = false;
            for (Path dexPath : dexPaths) {
                DexDisassembler disassembler =
                        new DexDisassembler(DexFiles.getDexFile(dexPath), proguardMappings.map);
                try {
                    String originalFqcn =
                            PackageTreeCreator.decodeClassName(
                                    SigUtils.typeToSignature(fqcn), proguardMappings.map);
                    out.println(
                            disassembler.getMethodBody(
                                    fqcn,
                                    SigUtils.typeToSignature(originalFqcn) + "->" + method));
                    dexFound = true;
                } catch (IllegalStateException e) {
                    //this dex file doesn't contain the given method.
                    //continue searching
                }
            }
            if (!dexFound) {
                throw new IllegalArgumentException(
                        String.format(
                                "The given class (%s) or method (%s) not found", fqcn, method));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void dexPackages(
            @NonNull Path apk,
            @Nullable Path proguardFolderPath,
            @Nullable Path proguardMapFilePath,
            @Nullable Path proguardSeedsFilePath,
            @Nullable Path proguardUsagesFilePath,
            boolean showDefinedOnly,
            boolean showRemoved,
            @Nullable List<String> dexFilePaths) {
        ProguardMappings proguardMappings =
                new ProguardMappings(
                        proguardFolderPath,
                        proguardMapFilePath,
                        proguardSeedsFilePath,
                        proguardUsagesFilePath);
        boolean deobfuscateNames = proguardMappings.map != null;

        try (ArchiveContext archiveContext = Archives.open(apk)) {
            Collection<Path> dexPaths;
            if (dexFilePaths == null || dexFilePaths.isEmpty()) {
                dexPaths = getDexFilesFrom(archiveContext.getArchive().getContentRoot());
            } else {
                dexPaths =
                        dexFilePaths
                                .stream()
                                .map(
                                        dexFile ->
                                                archiveContext
                                                        .getArchive()
                                                        .getContentRoot()
                                                        .resolve(dexFile))
                                .collect(Collectors.toList());
            }
            Map<Path, DexBackedDexFile> dexFiles = Maps.newHashMapWithExpectedSize(dexPaths.size());
            for (Path dexPath : dexPaths) {
                dexFiles.put(dexPath, DexFiles.getDexFile(dexPath));
            }

            PackageTreeCreator treeCreator =
                    new PackageTreeCreator(proguardMappings, deobfuscateNames);
            DexPackageNode rootNode = treeCreator.constructPackageTree(dexFiles);

            DexViewFilters filters = new DexViewFilters();
            filters.setShowFields(true);
            filters.setShowMethods(true);
            filters.setShowReferencedNodes(!showDefinedOnly);
            filters.setShowRemovedNodes(showRemoved);

            FilteredTreeModel<DexElementNode> model = new FilteredTreeModel<>(rootNode, filters);
            dumpTree(model, rootNode, proguardMappings.seeds, proguardMappings.map);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void dexCompare(
            @NonNull Path oldApkFile,
            @NonNull Path newApkFile,
            @Nullable List<String> dexFilePaths) {
        try (ArchiveContext archiveContext1 = Archives.open(oldApkFile);
             ArchiveContext archiveContext2 = Archives.open(newApkFile)) {
            ProguardMappings proguardMappings =
                    new ProguardMappings(null, null, null, null);
            // prepare dex files
            Collection<Path> dexPaths1;
            if (dexFilePaths == null || dexFilePaths.isEmpty()) {
                dexPaths1 = getDexFilesFrom(archiveContext1.getArchive().getContentRoot());
            } else {
                dexPaths1 =
                    dexFilePaths
                        .stream()
                        .map(
                            dexFile ->
                                archiveContext1
                                    .getArchive()
                                    .getContentRoot()
                                    .resolve(dexFile))
                        .collect(Collectors.toList());
            }
            Map<String, DexBackedDexFile> dexFiles1 = Maps.newHashMapWithExpectedSize(dexPaths1.size());
            for (Path dexPath : dexPaths1) {
                dexFiles1.put(dexPath.toString(), DexFiles.getDexFile(dexPath));
            }
            Collection<Path> dexPaths2;
            if (dexFilePaths == null || dexFilePaths.isEmpty()) {
                dexPaths2 = getDexFilesFrom(archiveContext2.getArchive().getContentRoot());
            } else {
                dexPaths2 =
                    dexFilePaths
                        .stream()
                        .map(
                            dexFile ->
                                archiveContext2
                                    .getArchive()
                                    .getContentRoot()
                                    .resolve(dexFile))
                        .collect(Collectors.toList());
            }
            Map<String, DexBackedDexFile> dexFiles2 = Maps.newHashMapWithExpectedSize(dexPaths2.size());
            for (Path dexPath : dexPaths2) {
                dexFiles2.put(dexPath.toString(), DexFiles.getDexFile(dexPath));
            }
            // compare dex files one by one
            for (String path : dexFiles2.keySet()) {
                out.printf("%s", path).println();
                DexBackedDexFile newDex = dexFiles2.get(path);
                if (!dexFiles1.containsKey(path)) {
                    System.err.println(
                            "Old apk don't contain dex file: " + path);
                }
                DexBackedDexFile oldDex = dexFiles1.get(path);

                DexDiffParser parser = new DexDiffParser(oldDex, newDex);
                parser.parse(out);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void dumpTree(
            @NonNull TreeModel model,
            @NonNull DexElementNode node,
            ProguardSeedsMap seeds,
            ProguardMap map) {
        StringBuilder sb = new StringBuilder();

        sb.append(node.getNodeTypeShort()).append(" ");

        if (node.isRemoved()) {
            sb.append("x ");
        } else if (node.isSeed(seeds, map, true)) {
            sb.append("k ");
        } else if (!node.isDefined()) {
            sb.append("r ");
        } else {
            sb.append("d ");
        }

        sb.append(node.getMethodDefinitionsCount());
        sb.append('\t');
        sb.append(node.getMethodReferencesCount());
        sb.append('\t');
        sb.append(getSize(node.getSize()));
        sb.append('\t');

        if (node instanceof DexPackageNode) {
            if (node.getParent() == null) {
                sb.append("<TOTAL>");
            } else {
                sb.append(((DexPackageNode) node).getPackageName());
            }
        } else if (node instanceof DexClassNode) {
            DexPackageNode parent = (DexPackageNode) node.getParent();
            if (parent != null && parent.getPackageName() != null) {
                sb.append(parent.getPackageName());
                sb.append(".");
            }
            sb.append(node.getName());
        } else if (node instanceof DexMethodNode | node instanceof DexFieldNode) {
            DexPackageNode parent = (DexPackageNode) node.getParent().getParent();
            if (parent != null && parent.getPackageName() != null) {
                sb.append(parent.getPackageName());
                sb.append(".");
            }
            sb.append(node.getParent().getName());
            sb.append(" ");
            sb.append(node.getName());
        }

        out.println(sb.toString());

        for (int i = 0; i < model.getChildCount(node); i++) {
            dumpTree(model, (DexElementNode) model.getChild(node, i), seeds, map);
        }
    }

    public void dexReferences(@NonNull Path apk, @Nullable List<String> dexFilePaths) {
        try (ArchiveContext archiveContext = Archives.open(apk)) {
            Collection<Path> dexPaths;
            if (dexFilePaths == null || dexFilePaths.isEmpty()) {
                dexPaths = getDexFilesFrom(archiveContext.getArchive().getContentRoot());
            } else {
                dexPaths =
                        dexFilePaths
                                .stream()
                                .map(
                                        dexFile ->
                                                archiveContext
                                                        .getArchive()
                                                        .getContentRoot()
                                                        .resolve(dexFile))
                                .collect(Collectors.toList());
            }
            for (Path dexPath : dexPaths) {
                DexFileStats stats =
                        DexFileStats.create(Collections.singleton(DexFiles.getDexFile(dexPath)));
                out.printf("%s\t%d", dexPath.getFileName().toString(), stats.referencedMethodCount)
                        .println();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void dexList(@NonNull Path apk) {
        try (ArchiveContext archiveContext = Archives.open(apk)) {
            getDexFilesFrom(archiveContext.getArchive().getContentRoot())
                    .stream()
                    .map(path -> path.getFileName().toString())
                    .forEachOrdered(out::println);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void aaptVersion() {
        try {
            out.println(aaptInvoker.getPath());
            for (String s : aaptInvoker.dumpVersion()) {
                out.println(s);
            }
        } catch (ProcessException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    private ManifestData getManifestData(@NonNull Archive archive)
            throws IOException, ParserConfigurationException, SAXException {
        Path manifestPath = archive.getContentRoot().resolve(SdkConstants.ANDROID_MANIFEST_XML);
        byte[] manifestBytes =
                BinaryXmlParser.decodeXml(
                        SdkConstants.ANDROID_MANIFEST_XML, Files.readAllBytes(manifestPath));
        return AndroidManifestParser.parse(new ByteArrayInputStream(manifestBytes));
    }

    public void manifestDebuggable(@NonNull Path apk) {
        try (ArchiveContext archiveContext = Archives.open(apk)) {
            ManifestData manifestData = getManifestData(archiveContext.getArchive());
            boolean debuggable =
                    manifestData.getDebuggable() != null ? manifestData.getDebuggable() : false;
            out.println(String.valueOf(debuggable));
        } catch (SAXException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void manifestPermissions(@NonNull Path apk) {
        List<String> output;
        try {
            output = aaptInvoker.dumpBadging(apk.toFile());
        } catch (ProcessException e) {
            throw new RuntimeException(e);
        }
        AndroidApplicationInfo apkInfo = AndroidApplicationInfo.parseBadging(output);
        for (String name : apkInfo.getPermissions()) {
            out.println(name);
        }
    }

    public void manifestTargetSdk(@NonNull Path apk) {
        try (ArchiveContext archiveContext = Archives.open(apk)) {
            ManifestData manifestData = getManifestData(archiveContext.getArchive());
            out.println(String.valueOf(manifestData.getTargetSdkVersion()));
        } catch (SAXException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void manifestMinSdk(@NonNull Path apk) {
        try (ArchiveContext archiveContext = Archives.open(apk)) {
            ManifestData manifestData = getManifestData(archiveContext.getArchive());
            out.println(
                    manifestData.getMinSdkVersion() != ManifestData.MIN_SDK_CODENAME
                            ? String.valueOf(manifestData.getMinSdkVersion())
                            : manifestData.getMinSdkVersionString());
        } catch (SAXException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void manifestVersionCode(@NonNull Path apk) {
        try (ArchiveContext archiveContext = Archives.open(apk)) {
            ManifestData manifestData = getManifestData(archiveContext.getArchive());
            out.printf("%s", valueToDisplayString(manifestData.getVersionCode())).println();
        } catch (SAXException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void manifestVersionName(@NonNull Path apk) {
        try (ArchiveContext archiveContext = Archives.open(apk)) {
            ManifestData manifestData = getManifestData(archiveContext.getArchive());
            out.printf("%s", valueToDisplayString(manifestData.getVersionName())).println();
        } catch (SAXException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void manifestAppId(@NonNull Path apk) {
        try (ArchiveContext archiveContext = Archives.open(apk)) {
            ManifestData manifestData = getManifestData(archiveContext.getArchive());
            out.println(manifestData.getPackage());
        } catch (SAXException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void manifestPrint(@NonNull Path apk) {
        try (ArchiveContext archiveContext = Archives.open(apk)) {
            Path path =
                    archiveContext
                            .getArchive()
                            .getContentRoot()
                            .resolve(SdkConstants.ANDROID_MANIFEST_XML);
            byte[] bytes = Files.readAllBytes(path);
            out.write(BinaryXmlParser.decodeXml(path.getFileName().toString(), bytes));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void apkDownloadSize(@NonNull Path apk) {
        ApkSizeCalculator sizeCalculator = ApkSizeCalculator.getDefault();
        out.println(getSize(sizeCalculator.getFullApkDownloadSize(apk)));
    }

    public void apkRawSize(@NonNull Path apk) {
        ApkSizeCalculator sizeCalculator = ApkSizeCalculator.getDefault();
        out.println(getSize(sizeCalculator.getFullApkRawSize(apk)));
    }

    public void apkCompare(
            @NonNull Path oldApkFile,
            @NonNull Path newApkFile,
            boolean patchSize,
            boolean showFilesOnly,
            boolean showDifferentOnly) {
        try (ArchiveContext archiveContext1 = Archives.open(oldApkFile);
             ArchiveContext archiveContext2 = Archives.open(newApkFile)) {
            DefaultMutableTreeNode node;
            if (patchSize) {
                node = ApkFileByFileDiffParser.createTreeNode(archiveContext1, archiveContext2);
            } else {
                node = ApkDiffParser.createTreeNode(archiveContext1, archiveContext2);
            }
            dumpCompare(node, "", !showFilesOnly, showDifferentOnly);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void dumpCompare(
            @NonNull DefaultMutableTreeNode node,
            @NonNull String path,
            boolean showDirs,
            boolean diffOnly) {
        Object entry = node.getUserObject();
        if (entry instanceof ApkDiffEntry) {
            ApkDiffEntry diffEntry = (ApkDiffEntry) entry;
            if (node.getParent() == null) {
                path = "/";
            } else if (!path.endsWith("/")) {
                path = path + "/" + diffEntry.getName();
            } else {
                path = path + diffEntry.getName();
            }
            if (showDirs || !path.endsWith("/")) {
                if (!diffOnly || (diffEntry.getOldSize() != diffEntry.getNewSize())) {
                    out.printf(
                                    "%s\t%s\t%s\t%s",
                                    getSize(diffEntry.getOldSize()),
                                    getSize(diffEntry.getNewSize()),
                                    getSize(diffEntry.getSize()),
                                    path)
                            .println();
                }
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            dumpCompare((DefaultMutableTreeNode) node.getChildAt(i), path, showDirs, diffOnly);
        }
    }

    public void apkFeatures(@NonNull Path apk, boolean showNotRequired) {
        List<String> output;
        try {
            output = aaptInvoker.dumpBadging(apk.toFile());
        } catch (ProcessException e) {
            throw new RuntimeException(e);
        }
        AndroidApplicationInfo apkInfo = AndroidApplicationInfo.parseBadging(output);
        for (Map.Entry<String, String> entry : apkInfo.getUsesFeature().entrySet()) {
            String name = entry.getKey();
            String reason = entry.getValue();
            if (reason == null) {
                out.println(name);
            } else {
                out.printf("%s implied: %s", name, reason).println();
            }
        }
        if (showNotRequired) {
            for (String name : apkInfo.getUsesFeatureNotRequired()) {
                out.printf("%s not-required", name).println();
            }
        }
    }

    public void filesCat(@NonNull Path apk, @NonNull String filePath) {
        try (ArchiveContext archiveContext = Archives.open(apk)) {
            Path path = archiveContext.getArchive().getContentRoot().resolve(filePath);
            try (InputStream is = new BufferedInputStream(Files.newInputStream(path))) {
                ByteStreams.copy(is, out);
                out.flush();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void apkSummary(@NonNull Path apk) {
        try (ArchiveContext archiveContext = Archives.open(apk)) {
            ManifestData manifestData = getManifestData(archiveContext.getArchive());
            out.printf(
                            "%s\t%s\t%s",
                            valueToDisplayString(manifestData.getPackage()),
                            valueToDisplayString(manifestData.getVersionCode()),
                            valueToDisplayString(manifestData.getVersionName()))
                    .println();
        } catch (SAXException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void filesList(
            @NonNull Path apk,
            boolean showRawSize,
            boolean showDownloadSize,
            boolean showFilesOnly) {
        try (ArchiveContext archiveContext = Archives.open(apk)) {
            ArchiveNode node = ArchiveTreeStructure.create(archiveContext);
            if (showRawSize) {
                ArchiveTreeStructure.updateRawFileSizes(node, ApkSizeCalculator.getDefault());
            }
            if (showDownloadSize) {
                ArchiveTreeStructure.updateDownloadFileSizes(node, ApkSizeCalculator.getDefault());
            }
            ArchiveTreeStream.preOrderStream(node)
                    .map(
                            n -> {
                                String entrySummary = n.getData().getSummaryDisplayString();
                                long rawSize = n.getData().getRawFileSize();
                                long downloadSize = n.getData().getDownloadFileSize();

                                if (showDownloadSize) {
                                    entrySummary = getSize(downloadSize) + "\t" + entrySummary;
                                }
                                if (showRawSize) {
                                    entrySummary = getSize(rawSize) + "\t" + entrySummary;
                                }
                                return entrySummary;
                            })
                    .filter(path -> !showFilesOnly || !path.endsWith("/"))
                    .forEachOrdered(out::println);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String getSize(long bytes) {
        return humanReadableFlag ? getHumanizedSize(bytes) : String.valueOf(bytes);
    }

    @NonNull
    private static String getHumanizedSize(long sizeInBytes) {
        long kilo = 1024;
        long mega = kilo * kilo;

        DecimalFormat formatter = new DecimalFormat("#.#");
        int sign = sizeInBytes < 0 ? -1 : 1;
        sizeInBytes = Math.abs(sizeInBytes);
        if (sizeInBytes > mega) {
            return formatter.format((sign * sizeInBytes) / (double) mega) + "MB";
        } else if (sizeInBytes > kilo) {
            return formatter.format((sign * sizeInBytes) / (double) kilo) + "KB";
        } else {
            return (sign * sizeInBytes) + "B";
        }
    }

    @NonNull
    private static String formatValue(
            @NonNull BinaryResourceValue value, @NonNull StringPoolChunk stringPoolChunk) {
        if (value.type() == BinaryResourceValue.Type.STRING) {
            return stringPoolChunk.getString(value.data());
        }
        return BinaryXmlParser.formatValue(value, stringPoolChunk);
    }

    public void setHumanReadableFlag(boolean humanReadableFlag) {
        this.humanReadableFlag = humanReadableFlag;
    }

    @NonNull
    private static List<Path> getDexFilesFrom(Path dir) {
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.filter(
                            path ->
                                    Files.isRegularFile(path)
                                            && path.getFileName()
                                            .toString()
                                            .endsWith(".dex"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @NonNull
    private String valueToDisplayString(Object value) {
        return value == null ? "UNKNOWN" : value.toString();
    }
}
