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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.android.annotations.NonNull;
import com.android.tools.apk.analyzer.dex.tree.DexElementNode;
import java.io.IOException;
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile;
import com.android.tools.smali.dexlib2.formatter.DexFormatter;
import com.android.tools.smali.dexlib2.iface.reference.Reference;
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableTypeReference;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;

public class DexReferencesTest {
    @Test
    public void getReferenceTreeFor() throws IOException {
        DexBackedDexFile dexFile =
                PackageTreeCreatorTest.getTestDexFile(
                        PackageTreeCreatorTest.getDexPath("Test2.dex"));
        DexReferences references = new DexReferences(new DexBackedDexFile[] {dexFile});
        DexElementNode root = references.getReferenceTreeFor(new ImmutableTypeReference("La;"));
        root.sort(DexReferences.NODE_COMPARATOR);
        StringBuffer sb = new StringBuffer();
        dumpTree(sb, root, 0);
        assertEquals(
                "La;: \n"
                        + "  LTest2;-><init>()V: \n"
                        + "    LTestSubclass;-><init>()V: \n"
                        + "  LTest2;->aClassField:La;: \n"
                        + "    LTest2;-><init>()V: \n"
                        + "      LTestSubclass;-><init>()V: \n",
                sb.toString());

        root = references.getReferenceTreeFor(new ImmutableTypeReference("LSomeAnnotation;"));
        root.sort(DexReferences.NODE_COMPARATOR);
        sb.setLength(0);
        dumpTree(sb, root, 0);
        assertEquals(
                "LSomeAnnotation;: \n"
                        + "  La;: \n"
                        + "    LTest2;-><init>()V: \n"
                        + "      LTestSubclass;-><init>()V: \n"
                        + "    LTest2;->aClassField:La;: \n"
                        + "      LTest2;-><init>()V: \n"
                        + "        LTestSubclass;-><init>()V: \n",
                sb.toString());


    }

    @Test
    public void getReferenceTreeForShallow() throws IOException {
        DexBackedDexFile dexFile =
                PackageTreeCreatorTest.getTestDexFile(
                        PackageTreeCreatorTest.getDexPath("Test2.dex"));
        DexReferences references = new DexReferences(new DexBackedDexFile[] {dexFile});
        DexElementNode root =
                references.getReferenceTreeFor(new ImmutableTypeReference("La;"), true);
        root.sort(DexReferences.NODE_COMPARATOR);
        StringBuffer sb = new StringBuffer();
        dumpTree(sb, root, 0);
        assertEquals(
                "La;: \n"
                        + "  LTest2;-><init>()V: \n"
                        + "    null: \n"
                        + "  LTest2;->aClassField:La;: \n"
                        + "    null: \n",
                sb.toString());

        root = references.getReferenceTreeFor(new ImmutableTypeReference("LSomeAnnotation;"), true);
        root.sort(DexReferences.NODE_COMPARATOR);
        sb.setLength(0);
        dumpTree(sb, root, 0);
        assertEquals("LSomeAnnotation;: \n" + "  La;: \n" + "    null: \n", sb.toString());
    }

    @Test
    public void addReferencesForNode() throws IOException {
        DexBackedDexFile dexFile =
                PackageTreeCreatorTest.getTestDexFile(
                        PackageTreeCreatorTest.getDexPath("Test2.dex"));
        DexReferences references = new DexReferences(new DexBackedDexFile[] {dexFile});
        DexElementNode root =
                references.getReferenceTreeFor(new ImmutableTypeReference("La;"), true);
        root.sort(DexReferences.NODE_COMPARATOR);
        DexElementNode node = (DexElementNode) root.getFirstChild();
        references.addReferencesForNode(node, true);
        StringBuffer sb = new StringBuffer();
        dumpTree(sb, node, 0);
        assertEquals(
                "LTest2;-><init>()V: \n" + "  LTestSubclass;-><init>()V: \n" + "    null: \n",
                sb.toString());
    }

    @Test
    public void isAlreadyLoaded() throws IOException {
        DexBackedDexFile dexFile =
                PackageTreeCreatorTest.getTestDexFile(
                        PackageTreeCreatorTest.getDexPath("Test2.dex"));
        DexReferences references = new DexReferences(new DexBackedDexFile[] {dexFile});
        DexElementNode root =
                references.getReferenceTreeFor(new ImmutableTypeReference("La;"), true);
        DexElementNode node = (DexElementNode) root.getFirstChild();
        assertFalse(DexReferences.isAlreadyLoaded(node));
        references.addReferencesForNode(node, true);
        assertTrue(DexReferences.isAlreadyLoaded(node));
    }

    private static void dumpTree(StringBuffer sb, @NonNull DexElementNode node, int depth) {
        for (int i = 0; i < depth * 2; i++) {
            sb.append(' ');
        }
        sb.append(getReferenceString(node));
        sb.append(": ");
        sb.append('\n');

        for (int i = 0; i < node.getChildCount(); i++) {
            dumpTree(sb, node.getChildAt(i), depth + 1);
        }
    }

    private static String getReferenceString(@NonNull DexElementNode node) {
        Reference ref = node.getReference();
        if (ref != null) {
            return DexFormatter.INSTANCE.getReference(ref);
        } else {
            return null;
        }
    }
}
