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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Tests for {@link RandomAccessFileInputStream}.
 */
public class RandomAccessFileInputStreamTest {
  /**
   * The object under test.
   */
  private RandomAccessFileInputStream stream = null;

  /**
   * Test data written to the file.
   */
  private byte[] testData = null;

  /**
   * The temp file.
   */
  private File tempFile = null;

  @BeforeEach
  public void setup() throws IOException {
    testData = new byte[128];
    for (int x = 0; x < 128; x++) {
      testData[x] = (byte) x;
    }
    tempFile = File.createTempFile("ra-fist", "tmp");
    tempFile.deleteOnExit();
    try {
      FileOutputStream out = new FileOutputStream(tempFile);
      out.write(testData);
      out.flush();
      out.close();
    } catch (IOException e) {
      try {
        tempFile.delete();
      } catch (Exception ignoreD) {
        // Nothing
      }
      throw new RuntimeException(e);
    }
    stream = new RandomAccessFileInputStream(tempFile);
  }

  @AfterEach
  public void tearDown() {
    try {
      stream.close();
    } catch (Exception ignored) {
      // Nothing to do
    }
    try {
      tempFile.delete();
    } catch (Exception ignored) {
      // Nothing to do
    }
  }

  @Test
  public void testRead_OneByte() throws IOException {
    for (int x = 0; x < testData.length; x++) {
      assertEquals(x, stream.read());
    }
    assertEquals(-1, stream.read());
  }

  @Test
  public void testRead_WithBuffer() throws IOException {
    int bytesLeft = testData.length;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buffer = new byte[10];
    while (bytesLeft > 0) {
      int numRead = stream.read(buffer);
      if (numRead > 0) {
        bytesLeft -= numRead;
        out.write(buffer, 0, numRead);
      }
    }
    assertEquals(-1, stream.read(buffer, 0, 1));
    assertArrayEquals(testData, out.toByteArray());
  }

  @Test
  public void testRead_WithBuffer_NegativeLength() throws IOException {
    assertEquals(0, stream.read(new byte[] {}, 0, -1));
  }

  @Test
  public void testRead_WithPartialBuffer() throws IOException {
    int bytesLeft = testData.length;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buffer = new byte[10];
    while (bytesLeft > 0) {
      int numRead = stream.read(buffer, 0, 2); // At most 2 bytes of the buffer can be used
      if (numRead > 0) {
        bytesLeft -= numRead;
        out.write(buffer, 0, numRead);
      }
    }
    assertEquals(-1, stream.read());
    assertArrayEquals(testData, out.toByteArray());
  }

  @Test
  public void testMarkSupported() {
    assertTrue(stream.markSupported());
  }

  @Test
  public void testMarkAndReset_WithOffsetFile() throws IOException {
    // Reset the stream, this time one byte in to exercise non-zero offset values
    stream.close();
    stream = new RandomAccessFileInputStream(tempFile, 1, testData.length - 2);
    // Set a mark after the first byte, which should be 1. Read a second byte, which should be 2.
    assertEquals(1, stream.read());
    stream.mark(1337 /* any value here, it is ignored */);
    assertEquals(2, stream.read());
    // Reset the stream, it should be back to 1 now.
    stream.reset();
    assertEquals(2, stream.read());
  }

  @Test
  public void testSkip() throws IOException {
    // Skip values <= 0 should always produce 0 and not actually skip anything.
    assertEquals(0, stream.skip(-1));
    assertEquals(0, stream.skip(0));
    // Skip the first 5 bytes and read the 6th, which should have the value 5.
    assertEquals(5, stream.skip(5));
    assertEquals(5, stream.read());
    // 6 bytes have been read, so the max skip is testDataLength - 6. Ensure this is true.
    assertEquals(testData.length - 5 - 1, stream.skip(testData.length));
    // At the end of the file, skip should always return 0.
    assertEquals(0, stream.skip(17));
  }

  @Test
  public void testAvailable() throws IOException {
    // Available always knows the answer precisely unless the file length exceeds Integer.MAX_VALUE
    assertEquals(testData.length, stream.available());
    stream.read(new byte[17]);
    assertEquals(testData.length - 17, stream.available());
    stream.read(new byte[testData.length]);
    assertEquals(0, stream.available());
    stream.read();
    assertEquals(0, stream.available());
  }

  @Test
  public void testSetRange() throws IOException {
    // Mess with the stream range multiple times
    stream.setRange(1, 3);
    assertEquals(1, stream.read());
    assertEquals(2, stream.read());
    assertEquals(3, stream.read());
    assertEquals(-1, stream.read());
    stream.setRange(99, 2);
    assertEquals(99, stream.read());
    assertEquals(100, stream.read());
    assertEquals(-1, stream.read());
  }

  @Test
  public void testSetRange_TooLong() throws IOException {
    assertThrows(IllegalArgumentException.class, () ->
      stream.setRange(0, testData.length + 1)
    );
  }

  @Test
  public void testSetRange_NegativeOffset() throws IOException {
    assertThrows(IllegalArgumentException.class, () ->
      stream.setRange(-1, testData.length)
    );
  }

  @Test
  public void testSetRange_NegativeLength() throws IOException {
    assertThrows(IllegalArgumentException.class, () ->
      stream.setRange(0, -1)
    );
  }

  @Test
  public void testSetRange_LongOverflow() throws IOException {
    assertThrows(IllegalArgumentException.class, () ->
      stream.setRange(Long.MAX_VALUE, 1) // Oh dear.
    );
  }

  @Test
  public void testReset_NoMarkSet() throws IOException {
    assertThrows(IOException.class, () ->
      stream.reset()
    );
  }

  @Test
  public void testMark_IOExceptionInRaf() throws IOException {
    stream =
        new RandomAccessFileInputStream(tempFile, 0, testData.length) {
          @Override
          protected RandomAccessFile getRandomAccessFile(File file) throws IOException {
            return new RandomAccessFile(file, "r") {
              @Override
              public long getFilePointer() throws IOException {
                throw new IOException("Blah314159");
              }
            };
          }
        };
    try {
      stream.mark(0);
      fail("Executed code that should have failed.");
    } catch (Exception e) {
      assertEquals("Blah314159", e.getCause().getMessage());
    }
  }

  @Test
  public void testClose() throws IOException {
    stream.close();
    try {
      stream.read();
      fail("read after close");
    } catch (IOException expected) {
      // Good.
    }
  }

  @Test
  public void testLength() {
    assertEquals(testData.length, stream.length());
  }

  @Test
  public void testConstructorWithSpecificLength() throws IOException {
    stream = new RandomAccessFileInputStream(tempFile, 5, 2);
    assertEquals(5, stream.read());
    assertEquals(6, stream.read());
    assertEquals(-1, stream.read());
  }

  @Test
  public void testGetPosition() throws IOException {
    stream = new RandomAccessFileInputStream(tempFile, 5, 2);
    assertEquals(5, stream.getPosition());
    stream.read();
    assertEquals(6, stream.getPosition());
    stream.setRange(0, 1);
    assertEquals(0, stream.getPosition());
  }
}
