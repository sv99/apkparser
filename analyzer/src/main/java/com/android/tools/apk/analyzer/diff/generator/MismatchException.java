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

import java.io.IOException;

/**
 * Thrown when data does not match expected values.
 */
@SuppressWarnings("serial")
public class MismatchException extends IOException {
  /**
   * Construct an exception with the specified message
   * @param message the message
   */
  public MismatchException(String message) {
    super(message);
  }
}