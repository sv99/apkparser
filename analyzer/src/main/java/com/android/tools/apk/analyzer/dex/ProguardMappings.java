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
import com.android.tools.apk.analyzer.internal.ProguardMappingFiles;
import com.android.tools.proguard.ProguardMap;
import com.android.tools.proguard.ProguardSeedsMap;
import com.android.tools.proguard.ProguardUsagesMap;
import com.google.common.base.Charsets;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class ProguardMappings {
    @Nullable public final ProguardMap map;
    @Nullable public final ProguardSeedsMap seeds;
    @Nullable public final ProguardUsagesMap usage;

    public ProguardMappings(
            @Nullable ProguardMap map,
            @Nullable ProguardSeedsMap seeds,
            @Nullable ProguardUsagesMap usage) {
        this.map = map;
        this.seeds = seeds;
        this.usage = usage;
    }

    // init from files
    public ProguardMappings(
            @Nullable Path proguardFolderPath,
            @Nullable Path proguardMapFilePath,
            @Nullable Path proguardSeedsFilePath,
            @Nullable Path proguardUsagesFilePath) {
        ProguardMappingFiles pfm;
        if (proguardFolderPath != null) {
            try {
                pfm = ProguardMappingFiles.from(new Path[]{proguardFolderPath});
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            pfm =
                    new ProguardMappingFiles(
                            proguardMapFilePath, proguardSeedsFilePath, proguardUsagesFilePath);
        }

        List<String> loaded = new ArrayList<>(3);
        List<String> errors = new ArrayList<>(3);

        ProguardMap proguardMap = null;
        if (pfm.mappingFile != null) {
            proguardMap = new ProguardMap();
            try {
                proguardMap.readFromReader(
                        new InputStreamReader(
                                Files.newInputStream(pfm.mappingFile), Charsets.UTF_8));
                loaded.add(pfm.mappingFile.getFileName().toString());
            } catch (IOException | ParseException e) {
                errors.add(pfm.mappingFile.getFileName().toString());
                proguardMap = null;
            }
        }
        ProguardSeedsMap seeds = null;
        if (pfm.seedsFile != null) {
            try {
                seeds =
                        ProguardSeedsMap.parse(
                                new InputStreamReader(
                                        Files.newInputStream(pfm.seedsFile), Charsets.UTF_8));
                loaded.add(pfm.seedsFile.getFileName().toString());
            } catch (IOException e) {
                errors.add(pfm.seedsFile.getFileName().toString());
            }
        }
        ProguardUsagesMap usage = null;
        if (pfm.usageFile != null) {
            try {
                usage =
                        ProguardUsagesMap.parse(
                                new InputStreamReader(
                                        Files.newInputStream(pfm.usageFile), Charsets.UTF_8));
                loaded.add(pfm.usageFile.getFileName().toString());
            } catch (IOException e) {
                errors.add(pfm.usageFile.getFileName().toString());
            }
        }

        if (!errors.isEmpty() && loaded.isEmpty()) {
            System.err.println(
                    "No Proguard mapping files found. The filenames must match one of: mapping.txt, seeds.txt, usage.txt");
        } else if (errors.isEmpty() && !loaded.isEmpty()) {
            System.err.println(
                    "Successfully loaded maps from: "
                            + String.join(", ", loaded));
        } else if (!errors.isEmpty() && !loaded.isEmpty()) {
            System.err.println(
                    "Successfully loaded maps from: "
                            + String.join(", ", loaded)
                            + "\n"
                            + "There were problems loading: "
                            + String.join(", ", errors));
        }

        this.map = proguardMap;
        this.seeds = seeds;
        this.usage = usage;
    }
}
