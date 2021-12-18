// Copyright 2019 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#ifndef THIRD_PARTY_BAZEL_SRC_MAIN_NATIVE_UTF8_JNI_H_
#define THIRD_PARTY_BAZEL_SRC_MAIN_NATIVE_UTF8_JNI_H_

#include <vector>

#include <jni.h>

namespace blaze_jni {

// jByteArray -> UTF8 array conversion function.
// The JVM uses 'Modified UTF-8', these helpers are for conversion to the lower
// 8 bits of the UTF-8 character set, expected on macOS for names of extended
// attributes.
// https://docs.oracle.com/javase/6/docs/api/java/io/DataInput.html#modified-utf-8

/**
 * @brief Returns a vector of UTF-8 chars from a jbyteArray.
 * Assumes only the Latin-1 / lower characters of UTF-8 alphabet are used,
 * otherwise throws an exception.
 *
 * @param env the JNI environment.
 * @param array the byte array to convert to lower UTF-8.
 * @return A null-terminated char vector of the byte array.
 */
std::vector<char> GetByteArrayUtf8Chars(JNIEnv *env, jbyteArray array);

/**
 * @brief Returns a vector of UTF-8 chars from a jstring.
 * Assumes only the Latin-1 / lower characters of UTF-8 alphabet are used,
 * otherwise throws an exception.
 *
 * @param env the JNI environment.
 * @param string the string to convert to lower UTF-8.
 * @return A null-terminated char vector of the byte array.
 */
std::vector<char> GetStringUtf8chars(JNIEnv *env, jstring string);

}  // namespace blaze_jni

#endif  // THIRD_PARTY_BAZEL_SRC_MAIN_NATIVE_UTF8_JNI_H_
