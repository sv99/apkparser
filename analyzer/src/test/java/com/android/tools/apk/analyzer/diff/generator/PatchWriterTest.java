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

import com.android.tools.apk.analyzer.diff.shared.JreDeflateParameters;
import com.android.tools.apk.analyzer.diff.shared.PatchConstants;
import com.android.tools.apk.analyzer.diff.shared.TypedRange;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * Tests for {@link PatchWriter}.
 */
public class PatchWriterTest {
  // This is Integer.MAX_VALUE + 1.
  private static final long BIG = 2048L * 1024L * 1024L;

  private static final JreDeflateParameters DEFLATE_PARAMS = JreDeflateParameters.of(6, 0, true);

  private static final TypedRange<Void> OLD_DELTA_FRIENDLY_UNCOMPRESS_RANGE =
      new TypedRange<Void>(BIG, 17L, null);

  private static final TypedRange<JreDeflateParameters> NEW_DELTA_FRIENDLY_UNCOMPRESS_RANGE =
      new TypedRange<JreDeflateParameters>(BIG - 100L, BIG, DEFLATE_PARAMS);

  private static final TypedRange<JreDeflateParameters> NEW_DELTA_FRIENDLY_RECOMPRESS_RANGE =
      new TypedRange<JreDeflateParameters>(BIG, BIG, DEFLATE_PARAMS);

  private static final List<TypedRange<Void>> OLD_DELTA_FRIENDLY_UNCOMPRESS_PLAN =
      Collections.singletonList(OLD_DELTA_FRIENDLY_UNCOMPRESS_RANGE);

  private static final List<TypedRange<JreDeflateParameters>> NEW_DELTA_FRIENDLY_UNCOMPRESS_PLAN =
      Collections.singletonList(NEW_DELTA_FRIENDLY_UNCOMPRESS_RANGE);

  private static final List<TypedRange<JreDeflateParameters>> NEW_DELTA_FRIENDLY_RECOMPRESS_PLAN =
      Collections.singletonList(NEW_DELTA_FRIENDLY_RECOMPRESS_RANGE);

  private static final long DELTA_FRIENDLY_OLD_FILE_SIZE = BIG - 75L;

  private static final long DELTA_FRIENDLY_NEW_FILE_SIZE = BIG + 75L;

  private static final PreDiffPlan PLAN =
      new PreDiffPlan(
          Collections.<QualifiedRecommendation>emptyList(),
          OLD_DELTA_FRIENDLY_UNCOMPRESS_PLAN,
          NEW_DELTA_FRIENDLY_UNCOMPRESS_PLAN,
          NEW_DELTA_FRIENDLY_RECOMPRESS_PLAN);

  private static final String DELTA_CONTENT = "this is a really cool delta, woo";

  private File deltaFile = null;

  private ByteArrayOutputStream buffer = null;

  @BeforeEach
  public void setup() throws IOException {
    buffer = new ByteArrayOutputStream();
    deltaFile = File.createTempFile("patchwritertest", "delta");
    deltaFile.deleteOnExit();
    try (FileOutputStream out = new FileOutputStream(deltaFile)) {
      out.write(DELTA_CONTENT.getBytes());
      out.flush();
    }
  }

  @AfterEach
  public void tearDown() {
    deltaFile.delete();
  }

  @Test
  public void testWriteV1Patch() throws IOException {
    // ---------------------------------------------------------------------------------------------
    // CAUTION - DO NOT CHANGE THIS FUNCTION WITHOUT DUE CONSIDERATION FOR BREAKING THE PATCH FORMAT
    // ---------------------------------------------------------------------------------------------
    // This test writes a simple patch with all the static data listed above and verifies it.
    // This code MUST be INDEPENDENT of the real patch parser code, even if it is partially
    // redundant; this guards against accidental changes to the patch writer that could alter the
    // format and otherwise escape detection.
    PatchWriter writer =
        new PatchWriter(
            PLAN, DELTA_FRIENDLY_OLD_FILE_SIZE, DELTA_FRIENDLY_NEW_FILE_SIZE, deltaFile);
    writer.writeV1Patch(buffer);
    DataInputStream patchIn = new DataInputStream(new ByteArrayInputStream(buffer.toByteArray()));
    byte[] eightBytes = new byte[8];

    // Start by reading the signature and flags
    patchIn.readFully(eightBytes);
    assertArrayEquals(PatchConstants.IDENTIFIER.getBytes(StandardCharsets.US_ASCII), eightBytes);
    assertEquals(0, patchIn.readInt()); // Flags, all reserved in v1

    assertEquals(DELTA_FRIENDLY_OLD_FILE_SIZE, patchIn.readLong());

    // Read the uncompression instructions
    assertEquals(1, patchIn.readInt()); // Number of old archive uncompression instructions
    assertEquals(OLD_DELTA_FRIENDLY_UNCOMPRESS_RANGE.getOffset(), patchIn.readLong());
    assertEquals(OLD_DELTA_FRIENDLY_UNCOMPRESS_RANGE.getLength(), patchIn.readLong());

    // Read the recompression instructions
    assertEquals(1, patchIn.readInt()); // Number of new archive recompression instructions
    assertEquals(NEW_DELTA_FRIENDLY_RECOMPRESS_RANGE.getOffset(), patchIn.readLong());
    assertEquals(NEW_DELTA_FRIENDLY_RECOMPRESS_RANGE.getLength(), patchIn.readLong());
    // Now the JreDeflateParameters for the record
    assertEquals(
        PatchConstants.CompatibilityWindowId.DEFAULT_DEFLATE.patchValue, patchIn.read());
    assertEquals(DEFLATE_PARAMS.level, patchIn.read());
    assertEquals(DEFLATE_PARAMS.strategy, patchIn.read());
    assertEquals(DEFLATE_PARAMS.nowrap ? 1 : 0, patchIn.read());

    // Delta section. V1 patches have exactly one delta entry and it is always mapped to the entire
    // file contents of the delta-friendly archives
    assertEquals(1, patchIn.readInt()); // Number of difference records
    assertEquals(PatchConstants.DeltaFormat.BSDIFF.patchValue, patchIn.read());
    assertEquals(0, patchIn.readLong()); // Old delta-friendly range start
    assertEquals(DELTA_FRIENDLY_OLD_FILE_SIZE, patchIn.readLong()); // old range length
    assertEquals(0, patchIn.readLong()); // New delta-friendly range start
    assertEquals(DELTA_FRIENDLY_NEW_FILE_SIZE, patchIn.readLong()); // new range length
    byte[] expectedDeltaContent = DELTA_CONTENT.getBytes(StandardCharsets.US_ASCII);
    assertEquals(expectedDeltaContent.length, patchIn.readLong());
    byte[] actualDeltaContent = new byte[expectedDeltaContent.length];
    patchIn.readFully(actualDeltaContent);
    assertArrayEquals(expectedDeltaContent, actualDeltaContent);
  }
}
