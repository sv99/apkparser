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

import static org.junit.jupiter.api.Assertions.*;

import com.android.testutils.TestResources;
import com.android.tools.apk.analyzer.internal.AppBundleArchive;
import com.android.utils.FileUtils;
import com.android.utils.ILogger;
import com.android.utils.StdLogger;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArchivesTest {
    private ArchiveContext archiveContext;
    private Archive archive;
    private FileSystem fs;
    private final ILogger logger = new StdLogger(StdLogger.Level.VERBOSE);

    @BeforeEach
    public void setup() throws IOException {
        archiveContext = Archives.open(TestResources.getFile("/test.apk").toPath(), logger);
        archive = archiveContext.getArchive();
        fs = archive.getContentRoot().getFileSystem();
    }

    @AfterEach
    public void cleanup() throws IOException {
        archiveContext.close();
    }

    @Test
    public void open_windowsPath() throws IOException {
        byte[] apkData = Files.readAllBytes(TestResources.getFile("/test.apk").toPath());

        FileSystem memFs = Jimfs.newFileSystem(Configuration.windows());

        Path folder = Files.createDirectory(memFs.getPath("foo"));
        Path apk = folder.resolve("foo.apk");
        Files.write(apk, apkData, StandardOpenOption.CREATE);

        try (ArchiveContext archiveContext = Archives.open(apk)) {
            assertNotNull(archiveContext);
        }
    }

    @Test
    public void open_pathWithSpaces() throws IOException {
        byte[] apkData = Files.readAllBytes(TestResources.getFile("/test.apk").toPath());

        FileSystem memFs = Jimfs.newFileSystem(Configuration.windows());

        Path folder = Files.createDirectory(memFs.getPath("foo with spaces"));
        Path apk = folder.resolve("foo with spaces.apk");
        Files.write(apk, apkData, StandardOpenOption.CREATE);

        try (ArchiveContext archiveContext = Archives.open(apk)) {
            assertNotNull(archiveContext);
        }
    }


    @Test
    public void getFirstManifestArchiveFromAPK() throws IOException {
        try (ArchiveContext archiveContext = Archives.open(getArchivePath("1.apk"))) {
            ArchiveNode node = ArchiveTreeStructure.create(archiveContext);
            ArchiveEntry entry = Archives.getFirstManifestArchiveEntry(node);
            assertNotNull(entry);
            assertEquals(archiveContext.getArchive(), entry.getArchive());
        }
    }

    @Test
    public void getFirstManifestArchiveFromAIABundle() throws IOException {
        Path archivePath = getArchivePath("bundle.zip");
        Path contentRoot;

        try (ArchiveContext archiveContext = Archives.open(archivePath)) {
            contentRoot = archiveContext.getArchive().getContentRoot();
            ArchiveNode node = ArchiveTreeStructure.create(archiveContext);
            ArchiveEntry entry = Archives.getFirstManifestArchiveEntry(node);
            assertNotNull(entry);
            assertNotEquals(archiveContext, entry.getArchive());
            assertEquals(
                    ((InnerArchiveEntry) node.getChildren().get(0).getData())
                            .asArchiveEntry()
                            .getArchive(),
                    entry.getArchive());
        }

        assertFalse(Files.exists(contentRoot));
        try (FileSystem zipFilesystem = FileUtils.createZipFilesystem(archivePath)) {
            // If we're allowed to create the filesystem for the same file, it means we have not
            // leaked it.
            zipFilesystem.getPath("/");
        }
    }

    @Test
    public void getFirstManifestArchiveFromAppBundle() throws Exception {
        Path archivePath = getArchivePath("android-app-bundle.aab");

        try (ArchiveContext archiveContext = Archives.open(archivePath)) {
            assertInstanceOf(AppBundleArchive.class, archiveContext.getArchive());
            ArchiveNode node = ArchiveTreeStructure.create(archiveContext);
            ArchiveEntry entry = Archives.getFirstManifestArchiveEntry(node);
            assertNotNull(entry);
            assertEquals(archiveContext.getArchive(), entry.getArchive());
        }
    }

    @Test
    public void protoXml_manifest() throws Exception {
        Path archivePath = getArchivePath("android-app-bundle.aab");

        try (ArchiveContext archiveContext = Archives.open(archivePath)) {
            Path path =
                    archiveContext
                            .getArchive()
                            .getContentRoot()
                            .resolve("base/manifest/AndroidManifest.xml");
            assertTrue(archiveContext.getArchive().isProtoXml(path, new byte[] {0x0a}));
        }
    }

    @Test
    public void protoXml_layoutXml() throws Exception {
        Path archivePath = getArchivePath("android-app-bundle.aab");

        try (ArchiveContext archiveContext = Archives.open(archivePath)) {
            Path path =
                    archiveContext.getArchive().getContentRoot().resolve("base/res/layout/foo.xml");
            assertTrue(archiveContext.getArchive().isProtoXml(path, new byte[] {0x0a}));
        }
    }

    @Test
    public void protoXml_wrongContent() throws Exception {
        Path archivePath = getArchivePath("android-app-bundle.aab");

        try (ArchiveContext archiveContext = Archives.open(archivePath)) {
            Path path =
                    archiveContext.getArchive().getContentRoot().resolve("base/res/layout/foo.xml");
            assertFalse(archiveContext.getArchive().isProtoXml(path, new byte[] {0x0b}));
        }
    }

    private static Path getArchivePath(String s) {
        return TestResources.getFile("/" + s).toPath();
    }

    @Test
    public void binaryXml_manifest() {
        assertTrue(archive.isBinaryXml(fs.getPath("/AndroidManifest.xml"), new byte[] {0x3, 0x0}));
    }

    @Test
    public void binaryXml_wrongContent() {
        assertFalse(archive.isBinaryXml(fs.getPath("/AndroidManifest.xml"), new byte[] {0x0, 0x0}));
    }

    @Test
    public void binaryXml_layoutXml() {
        assertTrue(archive.isBinaryXml(fs.getPath("/res/layout/foo.xml"), new byte[] {0x3, 0x0}));
    }

    @Test
    public void binaryXml_rawXml() {
        assertFalse(archive.isBinaryXml(fs.getPath("/res/raw/foo.xml"), new byte[] {0x3, 0x0}));
    }
}
