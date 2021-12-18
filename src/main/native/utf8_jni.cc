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

#include "src/main/native/utf8_jni.h"

#include <stdexcept>
#include <sstream>

namespace {
  static const int latin1Mask = 0x00ff;
}

namespace blaze_jni {

std::vector<char> GetByteArrayUtf8Chars(JNIEnv *env, jbyteArray array) {
  int size = env->GetArrayLength(array);
  jbyte* b = env->GetByteArrayElements(array, nullptr);
  std::vector<char> value_utf8(size);

  for (int i = 0; i < size; i++) {
    jchar unicode = b[i];
    if (unicode <= latin1Mask) {
      value_utf8[i] = unicode;
    } else {
      throw std::runtime_error("Extended UTF-8 char: " + std::to_string(unicode));
    }
  }

  value_utf8[size] = 0;
  env->ReleaseByteArrayElements(array, b, 0);

  return value_utf8;
}

std::vector<char> GetStringUtf8chars(JNIEnv *env, jstring string) {
  jint size = env->GetStringLength(string);
  const jchar *str = env->GetStringCritical(string, nullptr);

  if (str == nullptr) {
    return std::vector<char>();
  }

  std::vector<char> value_utf8(size);

  for (int i = 0; i < size; i++) {
    jchar unicode = str[i];
    if (unicode <= latin1Mask) {
      value_utf8[i] = unicode;
    } else {
      throw std::runtime_error("Extended UTF-8 char: " + std::to_string(unicode));
    }
  }

  value_utf8[size] = 0;
  env->ReleaseStringCritical(string, str);

  return value_utf8;
}

}  // namespace blaze_jni
