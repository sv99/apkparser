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

package com.android.tools.apk.analyzer.diff.shared;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Tests for {@link CountingOutputStream}.
 */
public class CountingOutputStreamTest {
  private ByteArrayOutputStream outBuffer;
  private CountingOutputStream stream;

  /**
   * Helper class that discards all output.
   */
  private static class NullStream extends OutputStream {
    @Override
    public void write(int b) throws IOException {
      // Do nothing
    }

    @Override
    public void write(byte[] b) throws IOException {
      // Do nothing
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      // Do nothing
    }
  }

  @BeforeEach
  public void setup() {
    outBuffer = new ByteArrayOutputStream();
    stream = new CountingOutputStream(outBuffer);
  }

  @Test
  public void testGetNumBytesWritten_Zero() {
    assertEquals(0, stream.getNumBytesWritten());
  }

  @Test
  public void testGetNumBytesWritten_FewBytes() throws IOException {
    stream.write(1);
    assertEquals(1, stream.getNumBytesWritten());
    stream.write(new byte[] {2, 3, 4});
    assertEquals(4, stream.getNumBytesWritten());
    stream.write(new byte[] {4, 5, 6, 7, 8}, 1, 3); // Write only {5, 6, 7}
    assertEquals(7, stream.getNumBytesWritten());
    byte[] expected = new byte[] {1, 2, 3, 4, 5, 6, 7};
    assertArrayEquals(expected, outBuffer.toByteArray());
  }

  @Test
  public void testGetNumBytesWritten_PastIntegerMaxValue() throws IOException {
    // Make a 1MB buffer. Iterating over this 2048 times will take the test to the 2GB limit of
    // Integer.maxValue. Use a NullStream to avoid excessive memory usage and make the test fast.
    stream = new CountingOutputStream(new NullStream());
    byte[] buffer = new byte[1024 * 1024];
    for (int x = 0; x < 2048; x++) {
      stream.write(buffer);
    }
    long expected = 2048L * 1024L * 1024L; // == 2GB, Integer.MAX_VALUE + 1
    assertTrue(expected > Integer.MAX_VALUE);
    assertEquals(expected, stream.getNumBytesWritten());
    // Push it well past 4GB
    for (int x = 0; x < 78053; x++) {
      stream.write(buffer);
      expected += buffer.length;
      assertEquals(expected, stream.getNumBytesWritten());
    }
  }
}
