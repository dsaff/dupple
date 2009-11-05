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

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsArray;
import org.jmock.api.Invocation;
import org.jmock.internal.InvocationExpectation;
import org.jmock.internal.matcher.MethodMatcher;

import java.util.HashMap;
import java.util.Map;

/**
 * Decides whether a recorded invocation matches a desired invocation,
 * potentially with "stand-ins": objects that stand for a set of acceptable
 * parameters.
 *
 * @author Google
 */
public class InvocationMatchingRuleset {
  private final Map<Object, Matcher<?>> standinMap =
      new HashMap<Object, Matcher<?>>();

  static InvocationMatchingRuleset newExactMatch() {
    return new InvocationMatchingRuleset();
  }

  private InvocationMatchingRuleset() {
    // prevent construction
  }

  @SuppressWarnings("unchecked")
  Matcher<Object> matcher(Object param) {
    if (standinMap.containsKey(param)) {
      return (Matcher<Object>) standinMap.get(param);
    }
    return Matchers.is(param);
  }

  void addStandIn(Object standIn, Matcher<?> matcher) {
    standinMap.put(standIn, matcher);
  }

  InvocationExpectation expectMatchOf(Invocation invocation) {
    InvocationExpectation expectation = new InvocationExpectation();
    expectation.setMethodMatcher(new MethodMatcher(invocation
        .getInvokedMethod()));
    Object[] params = invocation.getParametersAsArray();
    int length = params.length;
    Matcher<Object>[] matchers = matcherArray(length);
    for (int i = 0; i < length; i++) {
      matchers[i] = matcher(params[i]);
    }
    expectation.setParametersMatcher(new IsArray<Object>(matchers));
    return expectation;
  }

  @SuppressWarnings("unchecked")
  private Matcher<Object>[] matcherArray(int length) {
    return new Matcher[length];
  }
}
