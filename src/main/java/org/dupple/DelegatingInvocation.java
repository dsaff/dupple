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

import org.jmock.api.Invocation;

/**
 * Redirects a jmock-style Invocation to a new target.
 * 
 * This is most useful for wrappers that want to add functionality when the
 * redirected call returns 
 * 
 * @author Google
 */
public class DelegatingInvocation {
  /**
   * Wraps anything thrown by the redirectTo method.
   * 
   * @author Google
   */
  public static class RedirectionException extends Exception {
    public RedirectionException(Throwable t) {
      super(t);
    }
  }

  private final Invocation invocation;

  public DelegatingInvocation(Invocation invocation) {
    this.invocation = invocation;
  }

  public Object redirectTo(Object target) throws RedirectionException {
    try {
      return invocation.applyTo(target);
    } catch (Throwable t) { // lint doesn't like it, which is why I'm only
                            // going to do it once.  Really.
      throw new RedirectionException(t);
    }
  }
}
