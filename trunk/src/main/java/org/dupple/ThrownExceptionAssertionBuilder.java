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

import org.dupple.DelegatingInvocation.RedirectionException;

import junit.framework.AssertionFailedError;
import org.hamcrest.Matcher;
import org.jmock.api.Invocation;
import org.jmock.api.Invokable;
import org.jmock.internal.ReturnDefaultValueAction;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Remembers a desired property of an exception that is expected to be thrown
 * from an upcoming method call.
 * 
 * This is an internal class, used only in builders for
 * thrown-exception-matching assertions. See
 * {@link Dupple#assertThrown(Matcher)} for example use.
 * 
 * @author Google
 */
public class ThrownExceptionAssertionBuilder {
  private final Matcher<?> expectedExceptionMatcher;

  /**
   * Constructs a {@link ThrownExceptionAssertionBuilder} that will look for
   * exceptions matching {@code matcher}
   */
  protected ThrownExceptionAssertionBuilder(Matcher<?> matcher) {
    this.expectedExceptionMatcher = matcher;
  }

  /**
   * Returns a wrapped exception-asserting proxy for {@code target}. When the
   * proxy receives a method call {@code invocation}, it asserts that calling
   * {@code invocation} on {@code target} will produce an exception that matches
   * {@code matcher}
   * 
   * Example usage:
   * 
   * <pre>
   * assertThrown(is(UnsupportedOperationException.class)).from(turingMachine)
   *     .terminatesWithInput(string);
   * </pre>
   */
  public <T> T from(final T target) {
    return imposterize(target);
  }

  private <T> T imposterize(final T target) {
    return new JmockDupplery().imposterize(target, new Invokable() {
      @Override
      public Object invoke(Invocation invocation) throws Throwable {
        Object result;
        try {
          result = new DelegatingInvocation(invocation).redirectTo(target);
        } catch (RedirectionException t) {
          handleSomethingThrown(t.getCause());
          return new ReturnDefaultValueAction().invoke(invocation);
        }
        handleNothingThrown();
        return result;
      }
    });
  }

  /**
   * Asserts that {@code actuallyThrown} matches {@code matcher}. Override to
   * change behavior
   */
  protected void handleSomethingThrown(Throwable actuallyThrown)
      throws Throwable {
    if (!expectedExceptionMatcher.matches(actuallyThrown)) {
      StringWriter writer = new StringWriter();
      actuallyThrown.printStackTrace(new PrintWriter(writer));
      throw new AssertionFailedError("Expected: " + expectedExceptionMatcher
          + " but got " + actuallyThrown + "\n" + writer);
    }
  }

  /**
   * Throws an assertion error: something should have been thrown. Override to
   * change behavior
   */
  protected void handleNothingThrown() throws AssertionError {
    throw new AssertionError("No exception thrown");
  }
}
