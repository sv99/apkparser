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

import com.android.tools.apk.analyzer.diff.shared.UnitTestZipArchive;
import com.android.tools.apk.analyzer.diff.shared.UnitTestZipEntry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.CRC32;

/**
 * Tests for {@link MinimalZipParser}.
 */
public class MinimalZipArchiveTest {
  private byte[] unitTestZipArchive;
  private File tempFile;

  @BeforeEach
  public void setup() throws Exception {
    unitTestZipArchive = UnitTestZipArchive.makeTestZip();
    tempFile = File.createTempFile("MinimalZipArchiveTest", "zip");
    tempFile.deleteOnExit();
    try {
      FileOutputStream out = new FileOutputStream(tempFile);
      out.write(unitTestZipArchive);
      out.flush();
      out.close();
    } catch (IOException e) {
      try {
        tempFile.delete();
      } catch (Exception ignored) {
        // Nothing
      }
      throw e;
    }
  }

  @AfterEach
  public void tearDown() {
    if (tempFile != null) {
      try {
        tempFile.delete();
      } catch (Exception ignored) {
        // Nothing
      }
    }
  }

  @Test
  public void testListEntries() throws IOException {
    // Ensure all entries are found, and that they are in file order.
    List<MinimalZipEntry> parsedEntries = MinimalZipArchive.listEntries(tempFile);
    long lastSeenHeaderOffset = -1;
    for (int x = 0; x < UnitTestZipArchive.allEntriesInFileOrder.size(); x++) {
      UnitTestZipEntry expected = UnitTestZipArchive.allEntriesInFileOrder.get(x);
      MinimalZipEntry actual = parsedEntries.get(x);
      assertEquals(expected.path, actual.getFileName());
      assertEquals(expected.level == 0 ? 0 : 8, actual.getCompressionMethod());
      assertEquals(expected.getCompressedBinaryContent().length, actual.getCompressedSize());
      assertEquals(
          expected.getUncompressedBinaryContent().length, actual.getUncompressedSize());
      assertEquals(false, actual.getGeneralPurposeFlagBit11());
      CRC32 crc32 = new CRC32();
      crc32.update(expected.getUncompressedBinaryContent());
      assertEquals(crc32.getValue(), actual.getCrc32OfUncompressedData());

      // Offset verification is a little trickier
      // 1. Verify that the offsets are in ascending order and increasing.
      assertTrue(actual.getFileOffsetOfLocalEntry() > lastSeenHeaderOffset);
      lastSeenHeaderOffset = actual.getFileOffsetOfLocalEntry();

      // 2. Verify that the local signature header is at the calculated position
      byte[] expectedSignatureBlock = new byte[] {0x50, 0x4b, 0x03, 0x04};
      for (int index = 0; index < 4; index++) {
        byte actualByte = unitTestZipArchive[((int) actual.getFileOffsetOfLocalEntry()) + index];
        assertEquals(expectedSignatureBlock[index], actualByte);
      }

      // 3. Verify that the data is at the calculated position
      byte[] expectedContent = expected.getCompressedBinaryContent();
      int calculatedDataOffset = (int) actual.getFileOffsetOfCompressedData();
      for (int index = 0; index < expectedContent.length; index++) {
        assertEquals(
            expectedContent[index], unitTestZipArchive[calculatedDataOffset + index]);
      }
    }
  }
}
