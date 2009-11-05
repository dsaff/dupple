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

import org.jmock.api.Expectation;
import org.jmock.api.Invocation;
import org.jmock.api.Invokable;
import org.jmock.internal.InvocationDispatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps track of calls that are expected in a given context, and the actions
 * that should be performed in response.
 *
 * @author Google
 */
class ExpectedCalls {
  private List<Expectation> normalExpectations =
      new ArrayList<Expectation>();
  private List<Expectation> lowPriorityExpectations =
      new ArrayList<Expectation>();

  void addNormalExpectation(Expectation expectation) {
    normalExpectations.add(expectation);
  }

  void addLowPriorityExpectation(final Expectation expectation) {
    lowPriorityExpectations.add(expectation);
  }

  Invokable stubInvokable(final String name) {
    return new Invokable() {
      @Override
      public Object invoke(Invocation invocation) throws Throwable {
        InvocationDispatcher dispatcher = new InvocationDispatcher();
        addAll(dispatcher, normalExpectations);
        addAll(dispatcher, lowPriorityExpectations);
        return dispatcher.dispatch(invocation);
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }

  private void addAll(InvocationDispatcher dispatcher, List<Expectation> exps) {
    for (Expectation expectation : new ArrayList<Expectation>(exps)) {
      dispatcher.add(expectation);
    }
  }
}
