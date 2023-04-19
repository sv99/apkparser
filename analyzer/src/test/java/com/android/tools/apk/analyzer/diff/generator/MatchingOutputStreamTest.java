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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link MatchingOutputStream}.
 */
public class MatchingOutputStreamTest {
  /**
   * The data to write to the stream.
   */
  private byte[] data;

  /**
   * Input for matching.
   */
  private ByteArrayInputStream inputStream;

  /**
   * The stream under test.
   */
  private MatchingOutputStream outputStream;

  @BeforeEach
  public void setup() {
    data = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    inputStream = new ByteArrayInputStream(data);
    outputStream = new MatchingOutputStream(inputStream, 3 /* buffer size */);
  }

  @Test
  public void testWrite_OneByte() throws IOException {
    for (int x = 0; x < data.length; x++) {
      outputStream.write(data[x] & 0xff);
    }
  }

  @Test
  public void testWrite_WholeBuffer() throws IOException {
    outputStream.write(data);
  }

  @Test
  public void testWrite_WholeBuffer_RealisticCopyBuffer() throws IOException {
    outputStream = new MatchingOutputStream(inputStream, 32768); // realistic copy buffer size
    outputStream.write(data);
  }

  @Test
  public void testWrite_PartialBuffer() throws IOException {
    for (int x = 0; x < data.length; x++) {
      outputStream.write(data, x, 1);
    }
  }

  @Test
  public void testExpectEof() throws IOException {
    outputStream.write(data);
    outputStream.expectEof();
  }

  @Test
  public void testWrite_OneByte_MatchFail() throws IOException {
    assertThrows(MismatchException.class, () -> {
      outputStream.write(0);
      outputStream.write(77);
    });
  }

  @Test
  public void testWrite_OneByte_StreamFail() throws IOException {
    // Write one byte more than the data match stream contains
    assertThrows(MismatchException.class, () -> {
      for (int x = 0; x <= data.length; x++) {
        outputStream.write(x);
      }
    });
  }

  @Test
  public void testWrite_WholeBuffer_Fail() throws IOException {
    assertThrows(MismatchException.class, () -> {
      byte[] tweaked = new byte[]{0, 1, 2, 3, 4, 55, 6, 7, 8, 9};
      outputStream.write(tweaked);
    });
  }

  @Test
  public void testWrite_PartialBuffer_Fail() throws IOException {
    assertThrows(MismatchException.class, () -> {
      byte[] tweaked = new byte[]{0, 1, 2, 3, 4, 55, 6, 7, 8, 9};
      outputStream.write(tweaked, 0, 8);
    });
  }

  @Test
  public void testExpectEof_Fail() throws IOException {
    assertThrows(MismatchException.class, () -> {
      outputStream.write(data, 0, data.length - 1);
      outputStream.expectEof();
    });
  }

  @Test
  public void testWrite_PastEndOfMatchStream() throws IOException {
    assertThrows(MismatchException.class, () -> {
      outputStream.write(data);
      outputStream.write(data);
    });
  }

//  @SuppressWarnings({"resource", "unused"})
  @Test
  public void testConstructor_BadMatchBufferLength() {
    assertThrows(IllegalArgumentException.class, () -> {
      new MatchingOutputStream(inputStream, 0);
    });
  }
}
