/*
 * Copyright 2016 Google Inc. All Rights Reserved.
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

package com.android.tools.apk.analyzer.arsc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class BinaryResourceIdentifierTest {

  @ParameterizedTest
  @CsvSource({
    "0x01234567, 0x01, 0x23, 0x4567",
    "0xFEDCBA98, 0xFE, 0xDC, 0xBA98"
  })
  void testIdentifier(long resourceId, int packageId, int typeId, int entryId) {
    final BinaryResourceIdentifier resourceIdentifier = BinaryResourceIdentifier.create((int)resourceId);
    assertEquals(packageId, resourceIdentifier.packageId());
    assertEquals(typeId, resourceIdentifier.typeId());
    assertEquals(entryId, resourceIdentifier.entryId());
  }
}
