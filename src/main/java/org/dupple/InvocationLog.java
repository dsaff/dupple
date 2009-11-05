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
import org.jmock.api.Invokable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Remembers a record of invocations against one or more objects
 *
 * @author Google
 */
class InvocationLog extends ArrayList<DuppleInvocation> {
  <T> Invokable recordingInvokable(final T target) {
    return new Invokable() {
      @Override
      public Object invoke(Invocation invocation) throws Throwable {
        Method invokedMethod = invocation.getInvokedMethod();
        if (!isIgnoredMethod(invokedMethod)) {
          add(new DuppleInvocation(invocation));
        }
        return invocation.applyTo(target);
      }
    };
  }

  private boolean isIgnoredMethod(Method invokedMethod) {
    return invokedMethod.getDeclaringClass().equals(
        ImposterizationRememberer.class);
  }

  void assertNoUnverifiedInvocations(Object target) throws AssertionError {
    List<DuppleInvocation> unverified = new ArrayList<DuppleInvocation>();

    for (DuppleInvocation each : this) {
      if (each.invokedObjectIs(target) && !each.isVerified()) {
        unverified.add(each);
      }
    }

    if (!unverified.isEmpty()) {
      throw new AssertionError("Also invoked: " + unverified);
    }
  }

  boolean matchesAny(Invocation assertedInvocation,
      InvocationMatchingRuleset ruleset) {
    for (DuppleInvocation recordedInvocation : this) {
      if (recordedInvocation.matchedBy(ruleset
          .expectMatchOf(assertedInvocation))) {
        recordedInvocation.setVerified();
        return true;
      }
    }

    return false;
  }

  public Invokable invokeToCheckMatch(final InvocationMatchingRuleset ruleset,
      final boolean shouldMatch) {
    return new Invokable() {
      @Override
      public Object invoke(Invocation assertedInvocation) throws Throwable {
        if (matchesAny(assertedInvocation, ruleset)) {
          handleMatch(assertedInvocation);
        } else {
          handleNonMatch(assertedInvocation);
        }
        return null;
      }

      private void handleMatch(Invocation assertedInvocation) {
        if (!shouldMatch) {
          throw new AssertionError("Should not have invoked: "
              + new DuppleInvocation(assertedInvocation));
        }
      }

      private void handleNonMatch(Invocation assertedInvocation) {
        if (shouldMatch) {
          throw new AssertionError("Never invoked: "
              + new DuppleInvocation(assertedInvocation)
              + "\nactually saw: {\n" + InvocationLog.this + "\n}");
        }
      }
    };
  }

  @Override
  public String toString() {
    return Join.join("\n", this.toArray(new Object[0]));
  }
}
