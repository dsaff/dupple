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

import static org.hamcrest.CoreMatchers.anything;

import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.api.Invokable;
import org.jmock.internal.InvocationExpectation;
import org.jmock.internal.ReturnDefaultValueAction;
import org.jmock.lib.legacy.ClassImposteriser;

/**
 * Builds expectations for calls to a Dupple stub. Internal class, used only in
 * builder expressions. See {@link Dupple} for example use.
 * 
 * @author Google
 */
public class StubExpectationBuilder {
  private final Action action;
  private boolean lowPriority = false;

  StubExpectationBuilder(Action action) {
    this.action = action;
  }

  /**
   * Returns a builder that knows:
   * <ol>
   * <li>The {@code action} to be performed when a future call happens
   * <li>The {@code target} of that call
   * </ol>
   * The next call in the chain should be the actual method call, for example:
   * 
   * <pre>
   * Dupple.willReturn(5).from(myList).size();
   * </pre>
   */
  @SuppressWarnings("unchecked")
  public <U> U from(final U target) {
    return (U) objectForFrom(target);
  }

  /**
   * Stubs {@code action} as the result of any call to target.
   */
  void fromAnyCallTo(Object target) {
    addExpectation(target, anyCallExpectation());
  }

  private InvocationExpectation anyCallExpectation() {
    InvocationExpectation expectation = new InvocationExpectation();
    expectation.setObjectMatcher(anything());
    expectation.setAction(action);
    return expectation;
  }

  /**
   * Marks this as a low-priority expectation, which can be overridden by
   * following normal-prioriy expectations.
   */
  StubExpectationBuilder withLowPriority() {
    lowPriority = true;
    return this;
  }

  private void addExpectation(Object target, InvocationExpectation expectation) {
    Dupplery dupplery = ImposterizationRememberer.Util.creator(target);
    if (lowPriority) {
      dupplery.addLowPriorityExpectation(expectation);
    } else {
      dupplery.addNormalExpectation(expectation);
    }
  }

  private <U> Object objectForFrom(final U target) {
    return ClassImposteriser.INSTANCE.imposterise(new Invokable() {
      @Override
      public Object invoke(Invocation invocation) throws Throwable {
        InvocationExpectation builder =
            InvocationMatchingRuleset.newExactMatch().expectMatchOf(invocation);
        builder.setAction(action);
        addExpectation(target, builder);
        return new ReturnDefaultValueAction().invoke(invocation);
      }
    }, ((ImposterizationRememberer) target).getImposterizedClass());
  }
}
