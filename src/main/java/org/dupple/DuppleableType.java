// Copyright 2009 Google Inc.
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//      http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.dupple;

import java.lang.reflect.Modifier;

/**
 * Determines what the most specific superclass of a given Class can be
 * subclassed automatically. This is somewhat harder than it should be, because,
 * for example, anonymous inner classes return false from
 * {@link Modifier#isFinal(int)}, but cannot be subclassed.
 * 
 * @author Google
 */
class DuppleableType {
  private Class<?> type;

  // copied from Modifier
  private static final int SYNTHETIC = 0x00001000;

  DuppleableType(Class<?> type) {
    this.type = type;
  }

  Class<? extends Object> targetClass() {
    if (isEffectivelyFinal()) {
      return new DuppleableType(type.getSuperclass()).targetClass();
    }
    return this.type;
  }

  private boolean isSynthetic() {
    return (type.getModifiers() & DuppleableType.SYNTHETIC) != 0;
  }

  private boolean isMarkedFinal() {
    return Modifier.isFinal(type.getModifiers());
  }

  private boolean isEffectivelyFinal() {
    return isMarkedFinal() || isSynthetic()
        || Modifier.isPrivate(type.getModifiers());
  }
}
