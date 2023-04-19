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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.android.tools.apk.analyzer.diff.generator.DefaultDeflateCompressionDiviner.DivinationResult;
import com.android.tools.apk.analyzer.diff.shared.ByteArrayInputStreamFactory;
import com.android.tools.apk.analyzer.diff.shared.DefaultDeflateCompatibilityWindow;
import com.android.tools.apk.analyzer.diff.shared.DeflateCompressor;
import com.android.tools.apk.analyzer.diff.shared.JreDeflateParameters;
import com.android.tools.apk.analyzer.diff.shared.UnitTestZipArchive;
import com.android.tools.apk.analyzer.diff.shared.UnitTestZipEntry;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DefaultDeflateCompressionDiviner}.
 */
public class DefaultDeflateCompressionDivinerTest {
  /**
   * The object under test.
   */
  private DefaultDeflateCompressionDiviner diviner = null;

  /**
   * Test delivery written to the file.
   */
  private byte[] testData = null;

  @BeforeEach
  public void setup() {
    testData = new DefaultDeflateCompatibilityWindow().getCorpus();
    diviner = new DefaultDeflateCompressionDiviner();
  }

  /**
   * Deflates the test delivery using the specified parameters, storing them in a temp file and
   * returns the temp file created.
   * @param parameters the parameters to use for deflating
   * @return the temp file with the delivery
   */
  private byte[] deflate(JreDeflateParameters parameters) throws IOException {
    DeflateCompressor compressor = new DeflateCompressor();
    compressor.setNowrap(parameters.nowrap);
    compressor.setStrategy(parameters.strategy);
    compressor.setCompressionLevel(parameters.level);
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    compressor.compress(new ByteArrayInputStream(testData), buffer);
    return buffer.toByteArray();
  }

  @Test
  public void testDivineDeflateParameters_JunkData() throws IOException {
    final byte[] junk = new byte[] {0, 1, 2, 3, 4};
    assertNull(diviner.divineDeflateParameters(new ByteArrayInputStreamFactory(junk)));
  }

  @Test
  public void testDivineDeflateParameters_AllValidSettings() throws IOException {
    for (boolean nowrap : new boolean[] {true, false}) {
      for (int strategy : new int[] {0, 1, 2}) {
        for (int level : new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9}) {
          JreDeflateParameters trueParameters = JreDeflateParameters.of(level, strategy, nowrap);
          final byte[] buffer = deflate(trueParameters);
          JreDeflateParameters divinedParameters =
              diviner.divineDeflateParameters(new ByteArrayInputStreamFactory(buffer));
          assertNotNull(divinedParameters);
          // TODO(andrewhayden) make *CERTAIN 100%( that strategy doesn't matter for level < 4.
          if (strategy == 1 && level <= 3) {
            // Strategy 1 produces identical output at levels 1, 2 and 3.
            assertEquals(
                /*expected=*/ JreDeflateParameters.of(level, 0, nowrap),
                /*actual=*/ divinedParameters);
          } else if (strategy == 2) {
            // All levels are the same with strategy 2.
            // TODO: Assert only one test gets done for this, should be the first level always.
            assertEquals(nowrap, divinedParameters.nowrap);
            assertEquals(strategy, divinedParameters.strategy);
          } else {
            assertEquals(trueParameters, divinedParameters);
          }
        } // End of iteration on level
      } // End of iteration on strategy
    } // End of iteration on nowrap
  }

  @Test
  public void testDivineDeflateParameters_File() throws IOException {
    File tempFile = File.createTempFile("ddcdt", "tmp");
    tempFile.deleteOnExit();
    try {
      UnitTestZipArchive.saveTestZip(tempFile);
      List<DivinationResult> results = diviner.divineDeflateParameters(tempFile);
      assertEquals(UnitTestZipArchive.allEntriesInFileOrder.size(), results.size());
      for (int x = 0; x < results.size(); x++) {
        UnitTestZipEntry expected = UnitTestZipArchive.allEntriesInFileOrder.get(x);
        DivinationResult actual = results.get(x);
        assertEquals(expected.path, actual.minimalZipEntry.getFileName());
        int expectedLevel = expected.level;
        if (expectedLevel > 0) {
          // Compressed entry
          assertTrue(actual.minimalZipEntry.isDeflateCompressed());
          assertNotNull(actual.divinedParameters);
          assertEquals(expectedLevel, actual.divinedParameters.level);
          assertEquals(0, actual.divinedParameters.strategy);
          assertTrue(actual.divinedParameters.nowrap);
        } else {
          // Uncompressed entry
          assertFalse(actual.minimalZipEntry.isDeflateCompressed());
          assertNull(actual.divinedParameters);
        }
      }
    } finally {
      try {
        tempFile.delete();
      } catch (Exception ignoreD) {
        // Nothing
      }
    }
  }
}
