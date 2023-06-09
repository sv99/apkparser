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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ByteArrayHolder}.
 */
public class ByteArrayHolderTest {

  @Test
  public void testGetters() {
    byte[] data = "hello world".getBytes();
    ByteArrayHolder byteArrayHolder = new ByteArrayHolder(data);
    assertSame(data, byteArrayHolder.getData());
  }

  @Test
  public void testHashCode() {
    byte[] data1a = "hello world".getBytes();
    byte[] data1b = "hello world".getBytes();
    byte[] data2 = "hello another world".getBytes();
    ByteArrayHolder rawText1a = new ByteArrayHolder(data1a);
    ByteArrayHolder rawText1b = new ByteArrayHolder(data1b);
    assertEquals(rawText1a.hashCode(), rawText1b.hashCode());
    ByteArrayHolder rawText2 = new ByteArrayHolder(data2);
    assertNotEquals(rawText1a.hashCode(), rawText2.hashCode());
    ByteArrayHolder rawText3 = new ByteArrayHolder(null);
    assertNotEquals(rawText1a.hashCode(), rawText3.hashCode());
    assertNotEquals(rawText2.hashCode(), rawText3.hashCode());
  }

  @Test
  public void testEquals() {
    byte[] data1a = "hello world".getBytes();
    byte[] data1b = "hello world".getBytes();
    byte[] data2 = "hello another world".getBytes();
    ByteArrayHolder rawText1a = new ByteArrayHolder(data1a);
    assertEquals(rawText1a, rawText1a);
    ByteArrayHolder rawText1b = new ByteArrayHolder(data1b);
    assertEquals(rawText1a, rawText1b);
    ByteArrayHolder rawText2 = new ByteArrayHolder(data2);
    assertNotEquals(rawText1a, rawText2);
    ByteArrayHolder rawText3 = new ByteArrayHolder(null);
    assertNotEquals(rawText1a, rawText3);
    assertNotEquals(rawText3, rawText1a);
    assertNotEquals(rawText1a, 42);
    assertNotEquals(rawText1a, null);
  }
}
