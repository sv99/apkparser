/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.android.tools.apk.analyzer.internal.rewriters;

import com.android.annotations.NonNull;
import com.android.tools.smali.dexlib2.iface.reference.MethodReference;
import com.android.tools.smali.dexlib2.rewriter.MethodReferenceRewriter;
import com.android.tools.smali.dexlib2.rewriter.Rewriters;

public abstract class MethodReferenceWithNameRewriter extends MethodReferenceRewriter {

    public MethodReferenceWithNameRewriter(@NonNull Rewriters rewriters) {
        super(rewriters);
    }

    @NonNull
    @Override
    public MethodReference rewrite(@NonNull MethodReference methodReference) {
        return new RewrittenMethodReferenceWithName(methodReference);
    }

    public abstract String rewriteName(MethodReference methodReference);

    protected class RewrittenMethodReferenceWithName extends RewrittenMethodReference {

        public RewrittenMethodReferenceWithName(@NonNull MethodReference methodReference) {
            super(methodReference);
        }

        @Override
        @NonNull
        public String getName() {
            return rewriteName(methodReference);
        }
    }
}
