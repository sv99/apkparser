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

package com.android.tools.apk.analyzer.diff.generator.bsdiff;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class BsUtilTest {
  @Test
  public void writeFormattedLongTest() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(16);
    BsUtil.writeFormattedLong(0x12345678, outputStream);
    BsUtil.writeFormattedLong(0x0eadbeef, outputStream);
    byte[] actual = outputStream.toByteArray();

    byte[] expected = {
      (byte) 0x78,
      (byte) 0x56,
      (byte) 0x34,
      (byte) 0x12,
      (byte) 0,
      (byte) 0,
      (byte) 0,
      (byte) 0,
      (byte) 0xef,
      (byte) 0xbe,
      (byte) 0xad,
      (byte) 0x0e,
      (byte) 0,
      (byte) 0,
      (byte) 0,
      (byte) 0
    };
    assertArrayEquals(expected, actual);
  }

  @Test
  public void readFormattedLongTest() throws IOException {
    byte[] data = {
      (byte) 0x78,
      (byte) 0x56,
      (byte) 0x34,
      (byte) 0x12,
      (byte) 0,
      (byte) 0,
      (byte) 0,
      (byte) 0,
      (byte) 0xef,
      (byte) 0xbe,
      (byte) 0xad,
      (byte) 0x0e,
      (byte) 0,
      (byte) 0,
      (byte) 0,
      (byte) 0
    };
    ByteArrayInputStream inputStream = new ByteArrayInputStream(data);

    assertEquals(0x12345678, BsUtil.readFormattedLong(inputStream));
    assertEquals(0x0eadbeef, BsUtil.readFormattedLong(inputStream));
  }

  private long writeThenReadFormattedLong(long value) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(8);
    BsUtil.writeFormattedLong(value, outputStream);
    byte[] outputBytes = outputStream.toByteArray();
    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputBytes);
    return BsUtil.readFormattedLong(inputStream);
  }

  @Test
  public void writeThenReadFormattedLongTest() throws IOException {
    assertEquals(-1, writeThenReadFormattedLong(-1));
    assertEquals(0x7fffffff, writeThenReadFormattedLong(0x7fffffff));
    assertEquals(0, writeThenReadFormattedLong(0));
    assertEquals(Long.MAX_VALUE, writeThenReadFormattedLong(Long.MAX_VALUE));
    assertEquals(Long.MIN_VALUE, writeThenReadFormattedLong(Long.MIN_VALUE));
  }

  @Test
  public void lexicographicalCompareTest() throws IOException {
    String s1 = "this is a string";
    String s2 = "that was a string";
    byte[] s1b = s1.getBytes(Charset.forName("US-ASCII"));
    byte[] s2b = s2.getBytes(Charset.forName("US-ASCII"));
    RandomAccessObject s1ro = new RandomAccessObject.RandomAccessByteArrayObject(s1b);
    RandomAccessObject s2ro = new RandomAccessObject.RandomAccessByteArrayObject(s2b);

    int r = BsUtil.lexicographicalCompare(s1ro, 0, s1b.length, s2ro, 0, s2b.length);
    assertTrue(r > 0);

    r = BsUtil.lexicographicalCompare(s1ro, 5, s1b.length - 5, s2ro, 5, s2b.length - 5);
    assertTrue(r < 0);

    r = BsUtil.lexicographicalCompare(s1ro, 7, s1b.length - 7, s2ro, 8, s2b.length - 7);
    assertTrue(r < 0);

    r = BsUtil.lexicographicalCompare(s1ro, 7, s1b.length - 8, s2ro, 8, s2b.length - 8);
    assertTrue(r < 0);

    r = BsUtil.lexicographicalCompare(s1ro, 0, 2, s2ro, 0, 2);
    assertEquals(0, r);

    r = BsUtil.lexicographicalCompare(s1ro, 0, 1, s2ro, 0, 2);
    assertTrue(r < 0);

    r = BsUtil.lexicographicalCompare(s1ro, 0, 2, s2ro, 0, 1);
    assertTrue(r > 0);
  }
}
