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
package com.android.tools.apk.analyzer.dex.tree;

import com.android.annotations.NonNull;
import com.android.tools.apk.analyzer.internal.SigUtils;
import com.android.tools.smali.dexlib2.iface.reference.FieldReference;
import com.android.tools.smali.dexlib2.iface.reference.MethodReference;
import com.android.tools.smali.dexlib2.iface.reference.TypeReference;
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableFieldReference;
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference;
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableReference;
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableTypeReference;

public class DexElementNodeFactory {

    @NonNull
    public static DexElementNode from(@NonNull ImmutableReference ref) {
        if (ref instanceof ImmutableTypeReference) {
            return new DexClassNode(
                    SigUtils.signatureToName(((TypeReference) ref).getType()),
                    (ImmutableTypeReference) ref);
        } else if (ref instanceof ImmutableFieldReference) {
            return new DexFieldNode(
                    ((FieldReference) ref).getName(), (ImmutableFieldReference) ref);
        } else if (ref instanceof ImmutableMethodReference) {
            return new DexMethodNode(
                    ((MethodReference) ref).getName(), (ImmutableMethodReference) ref);
        } else {
            throw new IllegalArgumentException("This method accepts a Type/Field/MethodReference");
        }
    }
}
