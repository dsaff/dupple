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

/**
 * Most test doubles produced by Dupple implement this interface, so that Dupple
 * can determine which type they represent and which Dupplery produced them
 */
public interface ImposterizationRememberer {
  /**
   * Allows the expression
   * 
   * <pre>
   * ImposterisationRememberer.Util.creator(target)
   * </pre>
   * 
   * (Java does not allow static methods on interfaces.)
   * 
   * @author Google
   */
  static class Util {
    private Util() {
      // prevent creation
    }
    
    static Dupplery creator(Object target) {
      return ((ImposterizationRememberer) target).getCreator();
    }
  }

  /**
   * Returns the main type that this impostor is imposterizing.
   */
  Class<?> getImposterizedClass();

  /**
   * Returns the Dupplery that created this impostor
   */
  Dupplery getCreator();
}
