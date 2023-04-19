// Copyright 2016 Google Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.android.tools.apk.analyzer.diff.generator;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

/**
* Tests for {@link TempFileHolder}.
*/
public class TempFileHolderTest {
  @Test
  public void testConstructAndClose() throws IOException {
    // Tests that a temp file can be created and that it is deleted upon close().
    File allocated = null;
    try(TempFileHolder holder = new TempFileHolder()) {
      assertNotNull(holder.file);
      assertTrue(holder.file.exists());
      allocated = holder.file;
    }
    assertFalse(allocated.exists());
  }
}
