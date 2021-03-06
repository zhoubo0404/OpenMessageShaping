package io.openmessaging.demo.net.jpountz.xxhash;

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static io.openmessaging.demo.net.jpountz.xxhash.XXHashConstants.PRIME64_1;
import static io.openmessaging.demo.net.jpountz.xxhash.XXHashConstants.PRIME64_2;

abstract class AbstractStreamingXXHash64Java extends StreamingXXHash64 {

  int memSize;
  long v1, v2, v3, v4;
  long totalLen;
  final byte[] memory;

  AbstractStreamingXXHash64Java(long seed) {
    super(seed);
    memory = new byte[32];
    reset();
  }

  @Override
  public void reset() {
    v1 = seed + PRIME64_1 + PRIME64_2;
    v2 = seed + PRIME64_2;
    v3 = seed + 0;
    v4 = seed - PRIME64_1;
    totalLen = 0;
    memSize = 0;
  }

}
