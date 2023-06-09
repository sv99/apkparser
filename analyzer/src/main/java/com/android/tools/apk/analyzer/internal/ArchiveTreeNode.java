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

package com.android.tools.apk.analyzer.internal;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.apk.analyzer.ArchiveEntry;
import com.android.tools.apk.analyzer.ArchiveNode;
import com.google.common.collect.ImmutableList;

import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import one.util.streamex.StreamEx;

public class ArchiveTreeNode extends DefaultMutableTreeNode implements ArchiveNode {
    public ArchiveTreeNode(@NonNull ArchiveEntry data) {
        setUserObject(data);
    }

    @NonNull
    @Override
    public List<ArchiveNode> getChildren() {
        // Java 8 children has type Vector, never version has type Vector<DefaultMutableTreeNode>
        //noinspection unchecked
        return children == null ? ImmutableList.of() : StreamEx.of(children)
                .select(ArchiveNode.class)
                .toImmutableList();
    }

    @Override
    public void add(MutableTreeNode newChild) {
        if (!(newChild instanceof ArchiveTreeNode)) {
            throw new IllegalArgumentException("Only instances of ArchiveTreeNode can be added.");
        }
        super.add(newChild);
    }

    @Nullable
    @Override
    public ArchiveTreeNode getParent() {
        return (ArchiveTreeNode) super.getParent();
    }

    @NonNull
    @Override
    public ArchiveEntry getData() {
        return (ArchiveEntry) getUserObject();
    }
}
