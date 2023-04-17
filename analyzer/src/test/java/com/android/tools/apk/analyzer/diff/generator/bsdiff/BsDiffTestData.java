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

import java.nio.charset.Charset;

class BsDiffTestData {
  public static final int[] SHORT_GROUP_ARRAY =
      new int[] {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
        25, 26, 27, 28, 29, 30, 31
      };

  public static final int[] LONG_GROUP_ARRAY_100 =
      new int[] {
        68, 14, 46, 66, 36, 59, 47, 3, 27, 0, 43, 61, 53, 22, 75, 72, 59, 1, 27, 42, 8, 19, 23, 78,
        25, 49, 69, 56, 11, 67, 46, 23, 8, 34, 30, 77, 74, 74, 47, 45, 71, 35, 59, 49, 0, 70, 73,
        10, 60, 64, 71, 69, 67, 38, 6, 0, 67, 6, 17, 49, 27, 0, 20, 18, 31, 52, 76, 23, 64, 13, 80,
        67, 80, 24, 63, 61, 17, 44, 35, 14, 11, 63, 47, 54, 2, 68, 2, 79, 28, 18, 7, 4, 32, 77, 82,
        24, 31, 72, 65, 10
      };

  public static final int[] LONG_INVERSE_ARRAY_100 =
      new int[] {
        0, 3, 33, 33, 50, 71, 39, 53, 11, 44, 14, 29, 28, 25, 4, 0, 28, 7, 28, 84, 80, 47, 7, 47,
        28, 34, 43, 16, 80, 70, 79, 65, 64, 69, 35, 56, 76, 25, 73, 58, 56, 2, 13, 3, 78, 37, 83,
        70, 71, 13, 68, 16, 70, 17, 1, 64, 40, 80, 73, 44, 71, 76, 63, 55, 7, 51, 79, 59, 51, 27,
        36, 75, 51, 1, 16, 83, 45, 12, 6, 19, 54, 49, 12, 54, 16, 65, 37, 61, 40, 46, 37, 47, 17,
        43, 2, 50, 32, 1, 16, 25
      };

  public static final int[] LONGER_GROUP_ARRAY_350 =
      new int[] {
        239, 252, 85, 126, 72, 15, 203, 97, 136, 3, 252, 27, 197, 56, 306, 94, 196, 26, 121, 240,
        281, 244, 192, 152, 106, 244, 153, 288, 227, 27, 116, 124, 193, 103, 77, 283, 266, 185, 30,
        107, 175, 116, 20, 22, 9, 87, 230, 165, 296, 261, 69, 68, 250, 249, 261, 296, 278, 21, 40,
        277, 49, 160, 103, 302, 128, 6, 172, 306, 127, 185, 84, 306, 45, 251, 191, 37, 146, 214,
        108, 28, 87, 24, 263, 73, 203, 27, 267, 233, 106, 286, 243, 16, 216, 58, 215, 64, 64, 198,
        254, 260, 25, 160, 171, 48, 229, 228, 292, 4, 73, 235, 166, 49, 261, 71, 261, 169, 59, 185,
        267, 191, 87, 88, 227, 120, 301, 154, 226, 144, 126, 109, 9, 283, 194, 198, 239, 271, 110,
        105, 161, 47, 223, 283, 59, 215, 118, 15, 270, 8, 20, 114, 56, 59, 248, 3, 108, 96, 79, 231,
        49, 202, 260, 229, 41, 54, 165, 144, 0, 227, 78, 80, 38, 102, 305, 206, 86, 190, 244, 61,
        285, 54, 154, 214, 303, 212, 282, 203, 288, 279, 75, 307, 190, 77, 210, 55, 197, 16, 86, 5,
        134, 274, 63, 10, 28, 152, 136, 270, 28, 79, 297, 88, 223, 178, 12, 261, 204, 198, 97, 286,
        156, 299, 251, 57, 276, 120, 31, 289, 296, 227, 27, 170, 80, 236, 50, 262, 302, 76, 122,
        225, 265, 102, 114, 13, 55, 120, 98, 280, 1, 17, 243, 3, 77, 8, 220, 268, 93, 226, 38, 255,
        200, 94, 186, 49, 14, 248, 32, 89, 298, 83, 282, 301, 175, 2, 35, 247, 45, 268, 5, 284, 235,
        249, 30, 93, 245, 157, 132, 19, 231, 255, 8, 193, 67, 97, 97, 155, 96, 245, 94, 195, 263,
        93, 23, 32, 276, 306, 113, 215, 257, 293, 191, 135, 16, 257, 197, 71, 210, 227, 60, 6, 220,
        138, 307, 9, 109, 206, 123, 84, 58, 2, 147, 154, 101, 35, 132, 130, 223, 85, 42, 248, 26,
        38, 164, 114, 280, 245, 221, 158, 154, 215, 80, 246
      };

  public static final int[] LONGER_INVERSE_ARRAY_350 =
      new int[] {
        38, 57, 109, 42, 15, 53, 29, 11, 41, 187, 158, 258, 116, 232, 261, 294, 156, 190, 26, 205,
        77, 233, 142, 47, 60, 227, 273, 119, 157, 174, 202, 80, 120, 250, 59, 123, 206, 236, 277,
        12, 13, 126, 217, 153, 18, 140, 30, 8, 94, 179, 101, 148, 52, 165, 52, 278, 152, 152, 287,
        209, 74, 200, 257, 28, 78, 234, 214, 110, 185, 163, 224, 213, 91, 62, 174, 250, 41, 85, 156,
        178, 6, 294, 94, 237, 32, 134, 63, 179, 266, 133, 209, 96, 39, 141, 184, 209, 48, 208, 110,
        148, 271, 196, 291, 120, 26, 44, 107, 126, 2, 177, 205, 219, 84, 33, 169, 175, 34, 88, 80,
        166, 231, 118, 128, 245, 106, 264, 92, 153, 57, 16, 173, 128, 263, 108, 252, 298, 162, 298,
        176, 16, 171, 22, 121, 81, 265, 162, 248, 108, 30, 2, 302, 46, 278, 181, 130, 71, 300, 137,
        48, 181, 220, 119, 132, 270, 241, 202, 223, 91, 22, 162, 225, 38, 199, 64, 12, 69, 10, 55,
        295, 67, 100, 205, 125, 269, 117, 13, 307, 51, 111, 236, 30, 280, 7, 52, 56, 154, 144, 127,
        180, 176, 111, 118, 217, 299, 129, 175, 184, 303, 92, 162, 67, 259, 239, 250, 239, 259, 262,
        251, 29, 120, 182, 179, 42, 273, 176, 43, 138, 214, 281, 168, 24, 60, 238, 6, 64, 216, 255,
        296, 273, 296, 263, 92, 181, 30, 82, 262, 14, 80, 288, 240, 220, 168, 23, 45, 260, 260, 18,
        249, 241, 23, 124, 31, 5, 286, 15, 298, 138, 173, 70, 212, 236, 285, 166, 247, 76, 38, 107,
        293, 190, 229, 183, 282, 168, 170, 198, 291, 249, 207, 306, 62, 229, 260, 212, 289, 22, 298,
        200, 306, 72, 289, 198, 90, 25, 126, 301, 245, 20, 47, 180, 216, 77, 86, 231, 307, 152, 297,
        84, 144, 13, 233, 33, 79, 183, 177, 160, 257, 195, 250, 247, 37, 138, 49, 46, 43, 294, 245,
        85, 214, 190, 45, 75, 114, 143, 100, 136, 263, 130, 63, 237, 45
      };

  public static final int[] SPLIT_BASE_CASE_INVERSE_TEST_ARRAY =
      new int[] {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
        25, 26, 27, 28, 29, 30, 31
      };

  public static final int[] SPLIT_BASE_CASE_TEST_GA_CONTROL =
      new int[] {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1
      };

  public static final int[] SPLIT_BASE_CASE_TEST_IA_CONTROL =
      new int[] {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
        25, 26, 27, 28, 29, 30, 31
      };

  public static final int[] SPLIT_BASE_CASE_TEST_GROUP_ARRAY_2 = new int[] {0, 1, 2, 3, 3, 1, 0, 2};
  public static final int[] SPLIT_BASE_CASE_TEST_INVERSE_ARRAY_2 =
      new int[] {3, 2, 0, 3, -1, 0, 2, 3};
  public static final int[] SPLIT_BASE_CASE_TEST_GA_CONTROL_2 =
      new int[] {0, 1, 2, 3, 3, -1, -1, -1};
  public static final int[] SPLIT_BASE_CASE_TEST_IA_CONTROL_2 = new int[] {7, 5, 6, 4, -1, 0, 2, 3};

  public static final int[] SPLIT_TEST_GA_CONTROL =
      new int[] {
        68, 14, 46, 66, 36, -1, 65, 46, 35, 35, 6, 6, -1, 56, 14, 0, 0, 0, 0, 74, 74, 69, 69, -1, 2,
        2, 76, 43, 19, -1, 71, 71, 61, 61, 8, 8, 10, 4, 10, -1, -1, 17, 17, 82, 78, 80, 80, 18, 18,
        -1, -1, -1, 64, 60, 64, 27, 27, 27, 63, 63, -1, 72, 72, 52, 75, -1, 31, 31, 59, 59, 59, -1,
        -1, 47, 47, 24, 47, 24, -1, 77, 77, 23, 23, 23, -1, -1, -1, -1, 49, 49, 49, -1, -1, 67, 67,
        38, 67, 67, 11, 11
      };

  public static final int[] SPLIT_TEST_IA_CONTROL =
      new int[] {
        18, 49, 25, 40, 38, 71, 11, 5, 38, 44, 38, 99, 28, 50, 14, 0, 28, 42, 48, 28, 92, 47, 91,
        83, 77, 85, 43, 57, 87, 70, 86, 67, 65, 69, 23, 9, 76, 25, 97, 58, 56, 2, 84, 28, 78, 29, 7,
        77, 71, 90, 68, 16, 64, 60, 72, 64, 14, 80, 73, 70, 54, 33, 63, 59, 54, 7, 79, 97, 39, 22,
        12, 31, 62, 51, 22, 64, 28, 80, 44, 71, 46, 49, 44, 54, 16, 65, 37, 61, 40, 46, 37, 47, 17,
        43, 2, 50, 32, 1, 16, 25
      };

  public static final int[] SPLIT_TEST_GA_CONTROL_2 =
      new int[] {
        239, 252, 85, 126, 72, 15, 203, 97, 136, 3, 252, 27, 197, 56, 306, 94, 196, 71, 71, 38, 38,
        38, 30, 30, 165, 165, -1, 255, 255, 120, 120, 120, 130, 35, 35, 247, -1, 285, 132, 132, 243,
        243, 250, -1, -1, 9, 9, 9, 54, 54, -1, -1, -1, -1, -1, -1, -1, 96, 96, 244, 244, 244, 14,
        298, 45, 45, -1, 87, 87, 87, 185, 185, 185, -1, -1, -1, 235, 235, 64, 64, 280, 280, 289, 77,
        77, 77, 225, 164, -1, -1, -1, -1, 301, 301, 109, 109, 134, 55, 55, 307, 103, 103, 307, -1,
        302, 302, 108, 108, -1, 63, 158, 73, 73, -1, -1, 97, 97, 97, 267, 267, 138, 124, 58, 58, 89,
        -1, 26, 26, -1, 257, 257, -1, 94, 94, 191, 191, 191, 32, 32, 3, 3, 175, 175, -1, 152, 152,
        210, 210, 80, 80, 251, 80, 251, 113, 122, -1, -1, -1, -1, 84, 84, -1, 305, 47, 48, -1, -1,
        -1, -1, -1, 200, 127, 136, 160, 160, -1, 110, 263, 263, 220, 220, -1, -1, 20, 20, 106, 106,
        190, 215, 215, 215, 215, 190, 40, 78, 212, 21, 156, -1, 288, 288, 123, 276, 276, -1, -1, 28,
        28, 28, 5, 5, -1, 16, 16, 16, 6, 6, 226, 226, -1, -1, -1, -1, 59, 59, 59, 283, 283, 283, 88,
        88, 49, 49, 49, 49, -1, 248, 248, 248, -1, -1, 144, 144, 296, 296, 296, -1, 254, 101, -1,
        -1, 197, 197, 206, 206, 203, 203, 147, 12, -1, 114, 114, 114, -1, 27, 27, 27, -1, 86, 86,
        260, 260, -1, 193, 193, 102, 102, 262, -1, -1, -1, -1, -1, 8, 8, 8, 270, 270, 261, 261, 261,
        261, 261, 223, 223, 223, -1, 249, 249, 245, 245, 245, -1, 204, 24, 227, 227, 227, 227, 227,
        2, 2, -1, 282, 282, -1, 231, 231, 79, 79, -1, -1, -1, 116, 116, 154, 154, 154, 154, 229,
        229, 93, 93, 93, 214, 214, -1, -1, 306, 306, 306, 128, 286, 286, 268, 268, 198, 198, 198, -1
      };

  public static final int[] SPLIT_TEST_IA_CONTROL_2 =
      new int[] {
        205, 169, 311, 140, 239, 210, 216, 11, 285, 47, 246, 258, 258, 131, 63, 75, 214, 235, 26,
        168, 184, 197, 73, 158, 304, 74, 127, 266, 208, 174, 25, 26, 138, 250, 59, 35, 206, 50, 21,
        12, 195, 114, 161, 153, 18, 65, 30, 164, 164, 234, 279, 148, 52, 165, 49, 98, 282, 272, 124,
        225, 175, 278, 257, 110, 81, 234, 214, 55, 103, 167, 224, 18, 91, 112, 174, 51, 157, 85,
        195, 319, 152, 294, 94, 54, 160, 240, 269, 69, 230, 124, 209, 96, 39, 333, 133, 209, 58,
        119, 128, 148, 271, 248, 277, 102, 26, 181, 186, 52, 107, 95, 178, 219, 84, 154, 262, 175,
        324, 88, 165, 166, 32, 125, 154, 203, 121, 264, 349, 174, 343, 16, 32, 128, 39, 108, 98,
        321, 174, 298, 121, 16, 171, 22, 121, 81, 242, 162, 90, 258, 30, 2, 302, 46, 145, 156, 328,
        220, 197, 222, 110, 181, 174, 221, 132, 270, 87, 25, 89, 91, 22, 211, 88, 113, 263, 64, 12,
        142, 10, 55, 66, 67, 100, 205, 125, 269, 117, 72, 166, 51, 111, 236, 192, 136, 143, 274,
        322, 155, 144, 252, 348, 176, 174, 118, 312, 256, 304, 175, 254, 303, 92, 162, 147, 259,
        195, 250, 335, 192, 56, 251, 29, 120, 180, 43, 42, 295, 176, 87, 218, 309, 336, 330, 337,
        317, 238, 250, 64, 77, 315, 296, 273, 320, 296, 92, 181, 42, 61, 301, 198, 35, 238, 298, 42,
        152, 23, 45, 248, 28, 18, 130, 241, 23, 271, 292, 277, 178, 15, 91, 53, 119, 345, 212, 287,
        219, 166, 247, 182, 38, 203, 302, 267, 259, 81, 280, 314, 228, 204, 39, 343, 207, 200, 85,
        229, 260, 108, 44, 22, 298, 245, 36, 63, 249, 198, 93, 105, 281, 301, 164, 340, 102, 180,
        216, 77, 86, 231, 307, 152, 297, 84, 144, 13, 233, 33, 79, 183, 177, 160, 257, 195, 250,
        247, 37, 138, 49, 46, 43, 294, 245, 85, 214, 190, 45, 75, 114, 143, 100, 136, 263, 130, 63,
        237, 45
      };

  public static final String LONG_DATA_99_S =
      "Ad\"u%uuFm0BWP)-2)S-p7qI{zw@Jt|-E$Yxetqiv'I>EH|`|Y"
          + "A72g|.fr]F^\\(tnGKjTyO{7{e@Odm,tW}e+rRgRvkA(TNF&a>r";

  public static final String LONGER_DATA_349_S =
      "TL?I\"a)tAARPRLH)ZNuYRBZ>+A2lO;D<SL;9EPhz?y7W>#U"
          + "<`bDa80CNDT)AvVZRq3' Gcn6*o3U_)`E={[q;,)T/5Ntk,>K=v<Q4?o&Q!m2Za;Dt5'hMTZ?.\\ri#'Qu*>3"
          + "bo9pO}-?Ar/;\\9epjz&`Y_i{FZw'@HTfLI\\3(kOi^6{_9TC_m8C^zAuV'2hg%[AC@op(/=V+PvPNh<s^vqa"
          + "b@KWddY+425eS_j(jGX<!ZxzL$042\"$PnU<cZ|Us#%4R_5s)&: vS*:sk#tGC=V'W|^\"O&nY+5og8[oTf!$"
          + "Dm@Hu3M+bD?6knRa,>k_(-O4P^_kOk}<D_Jxo^s+(9R\\y.uv::ng";

  public static final byte[] LONG_DATA_99 = LONG_DATA_99_S.getBytes(Charset.forName("US-ASCII"));
  public static final byte[] LONGER_DATA_349 =
      LONGER_DATA_349_S.getBytes(Charset.forName("US-ASCII"));

  public static final RandomAccessObject LONG_DATA_99_RO =
      new RandomAccessObject.RandomAccessByteArrayObject(LONG_DATA_99);
  public static final RandomAccessObject LONGER_DATA_349_RO =
      new RandomAccessObject.RandomAccessByteArrayObject(LONGER_DATA_349);

  public static final int[] QUICK_SUFFIX_SORT_INIT_TEST_GA_CONTROL =
      new int[] {
        -1, -1, -1, -1, -1, -1, 61, 91, 13, 16, -1, -1, 14, 18, 30, -1, -1, 15, 51, 20, 50, 71, 42,
        97, 26, 74, 0, 49, 90, -1, 31, 43, 7, 58, 94, -1, -1, 22, 41, -1, -1, -1, 69, 75, -1, 85,
        87, -1, 67, 92, 11, 80, 33, 48, -1, -1, -1, -1, -1, 1, 76, 35, 73, 82, -1, 52, 86, -1, -1,
        -1, 8, 77, -1, -1, 21, 37, 56, 84, 98, 28, 36, 62, 79, 3, 5, 6, 39, 88, -1, -1, -1, -1, 23,
        70, 72, 29, 45, 47, 53, -1
      };

  public static final int[] QUICK_SUFFIX_SORT_INIT_TEST_IA_CONTROL =
      new int[] {
        28, 60, 1, 85, 3, 85, 85, 34, 71, 16, 29, 51, 44, 9, 14, 18, 9, 47, 14, 73, 21, 75, 38, 94,
        91, 88, 25, 39, 82, 98, 14, 31, 2, 53, 89, 63, 82, 75, 67, 87, 5, 38, 23, 31, 36, 98, 57,
        98, 53, 28, 21, 18, 66, 98, 15, 64, 78, 55, 34, 56, 54, 7, 82, 72, 35, 40, 68, 49, 90, 43,
        94, 21, 94, 63, 25, 43, 60, 71, 11, 82, 51, 99, 63, 10, 78, 46, 66, 46, 87, 69, 28, 7, 49,
        41, 34, 4, 58, 23, 78, 0
      };

  public static final int[] QUICK_SUFFIX_SORT_INIT_TEST_GA_CONTROL_2 =
      new int[] {
        -1, 67, 264, 105, 234, 295, 4, 243, 281, 45, 124, 254, 271, 239, 244, 296, 191, 255, 103,
        149, 262, 283, 66, 114, 125, 158, 187, 277, 167, 198, 229, 317, 337, 6, 15, 58, 77, 86, 261,
        72, 128, 267, 24, 202, 221, 286, 304, 336, 85, 93, 313, 137, 318, 120, 342, 88, 141, 199,
        53, 240, 26, 107, 188, 223, 242, 65, 74, 130, 166, 302, 100, 222, 241, 256, 320, 89, 113,
        224, 259, 287, 71, 172, 308, -1, 52, 180, 290, 35, 133, 144, 175, 338, 263, 268, 345, 346,
        29, 34, 84, 110, 142, 31, 47, 98, 208, 233, 248, 328, 80, 96, 200, 275, 23, 44, 94, 129,
        314, 2, 40, 101, 119, 138, 307, 159, 195, 215, 299, 8, 9, 25, 59, 139, 184, 193, -1, 54,
        177, 181, 194, 274, 30, 50, 56, 111, 297, 306, 329, 36, 79, -1, 68, 231, 273, 14, 160, 300,
        3, 164, -1, 95, 216, 1, 13, 33, 163, 238, 116, 303, 17, 55, 90, 206, 28, 135, 169, 282, 319,
        325, 11, 37, 203, 205, 245, 321, 99, 104, 126, 10, 12, 20, 63, 257, 311, 339, 32, 226, 266,
        0, 57, 87, 117, 161, 176, 293, 46, 75, 247, 252, 61, 186, 201, 276, 43, 217, 278, -1, 19,
        151, 220, 285, 16, 22, 62, 108, 118, 156, 235, 250, 82, 192, 291, 121, 143, 165, 340, 171,
        182, 210, 280, 322, 334, 76, 152, 174, 178, 227, 258, 316, 323, 330, 48, 78, 150, 5, 51,
        109, 213, 312, 49, 131, 214, 305, 69, 249, 218, 219, 145, 225, 162, 294, 190, 289, 348, 38,
        115, 189, 207, 123, 153, 170, 147, 228, 230, 92, 168, 270, 309, 315, 324, 326, -1, 106, 179,
        298, 70, 246, 284, 310, 347, 73, 102, 132, 196, 288, 292, 333, 134, 146, 197, 64, 83, 212,
        122, 140, 209, 253, 260, 269, 335, 7, 91, 112, 272, 18, 127, 185, 301, 343, 60, 97, 204,
        211, 265, 344, -1, 236, 332, 41, 341, 39, 148, 183, 237, 81, 154, 173, 251, 279, 136, 327
      };

  public static final int[] QUICK_SUFFIX_SORT_INIT_TEST_IA_CONTROL_2 =
      new int[] {
        203, 165, 122, 157, 8, 257, 38, 322, 133, 133, 193, 183, 193, 165, 155, 38, 227, 171, 327,
        219, 193, 134, 227, 116, 47, 133, 64, 290, 177, 100, 146, 107, 196, 165, 100, 91, 148, 183,
        276, 342, 122, 338, 83, 214, 116, 12, 207, 107, 252, 261, 146, 257, 86, 59, 139, 171, 146,
        203, 38, 133, 333, 211, 227, 193, 311, 69, 27, 2, 152, 263, 298, 82, 41, 305, 69, 207, 249,
        38, 252, 148, 111, 345, 230, 311, 100, 50, 38, 203, 57, 79, 171, 322, 289, 50, 116, 160,
        111, 333, 107, 186, 74, 122, 305, 21, 186, 5, 293, 64, 227, 257, 100, 146, 322, 79, 27, 276,
        167, 203, 227, 122, 54, 234, 313, 279, 12, 27, 186, 327, 41, 116, 69, 261, 305, 91, 308,
        177, 349, 52, 122, 133, 313, 57, 100, 234, 91, 267, 308, 282, 342, 21, 252, 219, 249, 279,
        345, 149, 227, 334, 27, 126, 155, 203, 269, 165, 157, 234, 69, 32, 289, 177, 279, 240, 82,
        345, 249, 91, 203, 139, 249, 293, 86, 139, 240, 342, 133, 327, 211, 27, 64, 276, 272, 17,
        230, 133, 139, 126, 305, 308, 32, 57, 111, 211, 47, 183, 333, 183, 171, 276, 107, 318, 240,
        333, 311, 257, 261, 126, 160, 214, 265, 265, 219, 47, 74, 64, 79, 267, 196, 249, 282, 32,
        282, 152, 215, 107, 5, 227, 336, 342, 165, 15, 59, 74, 64, 8, 15, 183, 298, 207, 107, 263,
        227, 347, 207, 318, 12, 17, 74, 193, 249, 79, 318, 38, 21, 95, 2, 333, 196, 41, 95, 318,
        289, 12, 322, 152, 139, 111, 211, 27, 214, 347, 240, 8, 177, 21, 298, 219, 47, 79, 305, 272,
        86, 230, 305, 203, 269, 5, 15, 146, 293, 126, 155, 327, 69, 167, 47, 261, 146, 122, 82, 289,
        298, 193, 257, 50, 116, 289, 249, 32, 52, 177, 74, 183, 240, 249, 289, 177, 289, 349, 107,
        146, 249, 158, 336, 305, 240, 318, 47, 32, 91, 193, 234, 338, 54, 327, 333, 95, 95, 298,
        272, 0
      };

  public static final int[] QUICK_SUFFIX_SORT_TEST_GA_CONTROL =
      new int[] {
        99, 2, 32, 4, 95, 40, 91, 61, 13, 16, 83, 78, 14, 30, 18, 54, 9, 15, 51, 50, 20, 71, 42, 97,
        26, 74, 90, 49, 0, 10, 31, 43, 94, 58, 7, 64, 44, 41, 22, 27, 65, 93, 75, 69, 12, 85, 87,
        17, 92, 67, 11, 80, 48, 33, 60, 57, 59, 46, 96, 1, 76, 82, 73, 35, 55, 86, 52, 38, 66, 89,
        77, 8, 63, 19, 21, 37, 98, 84, 56, 79, 62, 36, 28, 3, 6, 5, 39, 88, 25, 34, 68, 24, 70, 72,
        23, 29, 53, 47, 45, 81
      };

  public static final int[] QUICK_SUFFIX_SORT_TEST_IA_CONTROL =
      new int[] {
        349, 67, 264, 295, 234, 105, 243, 281, 4, 254, 124, 45, 271, 239, 296, 244, 255, 191, 262,
        103, 149, 283, 66, 187, 158, 125, 277, 114, 317, 198, 337, 229, 167, 261, 58, 86, 15, 77, 6,
        267, 128, 72, 336, 221, 286, 24, 202, 304, 85, 93, 313, 137, 318, 120, 342, 88, 141, 199,
        240, 53, 242, 223, 107, 188, 26, 65, 166, 302, 74, 130, 241, 222, 100, 320, 256, 113, 89,
        224, 287, 259, 71, 308, 172, 42, 52, 180, 290, 35, 338, 175, 144, 133, 263, 345, 346, 268,
        84, 34, 29, 110, 142, 233, 328, 98, 31, 47, 248, 208, 275, 200, 96, 80, 44, 23, 129, 94,
        314, 119, 307, 138, 2, 101, 40, 159, 299, 215, 195, 25, 8, 193, 9, 139, 184, 59, 21, 274,
        194, 54, 181, 177, 30, 306, 56, 329, 50, 297, 111, 79, 36, 155, 273, 231, 68, 14, 160, 300,
        3, 164, 331, 95, 216, 238, 33, 1, 13, 163, 303, 116, 55, 206, 90, 17, 282, 319, 28, 169,
        325, 135, 205, 11, 321, 37, 245, 203, 104, 99, 126, 20, 12, 10, 339, 257, 311, 63, 266, 32,
        226, 57, 87, 176, 0, 117, 293, 161, 46, 247, 75, 252, 186, 276, 201, 61, 43, 217, 278, 232,
        220, 285, 19, 151, 22, 118, 16, 62, 108, 156, 235, 250, 192, 291, 82, 165, 143, 121, 340,
        280, 171, 322, 334, 210, 182, 316, 76, 258, 174, 330, 152, 227, 323, 178, 78, 150, 48, 5,
        312, 51, 109, 213, 214, 305, 49, 131, 249, 69, 219, 218, 225, 145, 294, 162, 348, 190, 289,
        207, 115, 189, 38, 123, 170, 153, 228, 230, 147, 270, 92, 168, 324, 315, 309, 326, 27, 106,
        179, 298, 70, 310, 246, 284, 347, 102, 73, 132, 292, 333, 288, 196, 197, 134, 146, 64, 83,
        212, 140, 122, 253, 260, 335, 209, 269, 112, 7, 272, 91, 127, 301, 185, 18, 343, 344, 97,
        204, 265, 60, 211, 157, 332, 236, 341, 41, 148, 39, 183, 237, 154, 81, 173, 251, 279, 136,
        327
      };

  public static final String LONG_DATA_104_NEW_S =
      "Ad\"u%uuFm0B___-2)S-p7qI{zw@Jt|-E$Yxetqi==v2h"
          + "3oH|`|YA72g|.fr]F^\\(tnGKjTys{7{e@Odm,tW}e+rRgRvkA(TNF&a532>8";

  public static final String LONGER_DATA_354_NEW_S =
      "TL?I\"a)tAARPRLH)ZNuYRsdf8yu032D<SL;9EPh2nz"
          + "?y7W>#U<`bDa80CNDT)AvVZRq3' Gcn6*o3U_)`E={[q;,)T/5Ntk,>K=v<Q4?o&Q!m2Za;Dt5'hMTZ?.\\ri"
          + "#'Qu*>3bKo9pO}-?Ar/;\\9epjz&`Y_i{FZw'@HTfLI\\3(kOi^6{_9TC_m8C^zAuV'2hg%[AC@op(/=V+PvP"
          + "Nh<s^vqab@KWddY+425eS_j.jGX<!ZxzL$042\"$PnU<cZ|Us#%4R_5s)&: vS*:sk#tGC=V'W|^\"O&nY+5o"
          + "g8[oT2h80otugs7s9+bD?6knRa,>k_(-O4P^_kOk}<D_Jxo^s+(9R\\y.uv::ng";

  public static final byte[] LONG_DATA_104_NEW =
      LONG_DATA_104_NEW_S.getBytes(Charset.forName("US-ASCII"));
  public static final byte[] LONGER_DATA_354_NEW =
      LONGER_DATA_354_NEW_S.getBytes(Charset.forName("US-ASCII"));

  public static final RandomAccessObject LONG_DATA_104_NEW_RO =
      new RandomAccessObject.RandomAccessByteArrayObject(LONG_DATA_104_NEW);
  public static final RandomAccessObject LONGER_DATA_354_NEW_RO =
      new RandomAccessObject.RandomAccessByteArrayObject(LONGER_DATA_354_NEW);
}
