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

import junit.framework.AssertionFailedError;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.jmock.api.Invocation;
import org.jmock.api.Invokable;
import org.jmock.internal.ReturnDefaultValueAction;

/**
 * Remembers a desired property of an value that is expected to be returned from
 * an upcoming method call.
 * 
 * This is an internal class, used only in builders for returned-value-matching
 * assertions. See {@link Dupple#assertReturned(Matcher)} for example use.
 * 
 * @author Google
 */
public class ReturnedValueAssertionBuilder {
  private final Matcher<?> expectedReturnMatcher;

  /**
   * Constructs a {@link ReturnedValueAssertionBuilder} that will look for
   * exceptions matching {@code matcher}
   */
  protected ReturnedValueAssertionBuilder(Matcher<?> matcher) {
    this.expectedReturnMatcher = matcher;
  }

  /**
   * Returns a wrapped return-value-asserting proxy for {@code target}. When the
   * proxy receives a method call {@code invocation}, it asserts that calling
   * {@code invocation} on {@code target} will return a value that matches
   * {@code matcher}
   * 
   * Example usage:
   * 
   * <pre>
   * assertReturned(is(3)).from(list).size();
   * </pre>
   */
  public <T> T from(final T target) {
    return imposterize(target);
  }

  public <T> T imposterize(final T target) {
    return new JmockDupplery().imposterize(target, new Invokable() {
      boolean firstInvocation = true;
      @Override
      public Object invoke(Invocation invocation) throws Throwable {
        Object returned = invocation.applyTo(target);
        if (firstInvocation) {
          firstInvocation = false;
          checkReturn(target, invocation, returned);
        }
        return new ReturnDefaultValueAction().invoke(invocation);
      }
    });
  }

  /**
   * Asserts that {@code actuallyReturned} matches {@code matcher}. Override to
   * change behavior
   */
  protected void checkReturn(Object target, Invocation invocation,
      Object actuallyThrown) throws Throwable {
    if (!expectedReturnMatcher.matches(actuallyThrown)) {
      throw new AssertionFailedError("When calling " + target
          + StringDescription.toString(invocation) + ", Expected: "
          + expectedReturnMatcher + " but got " + actuallyThrown);
    }
  }
}
