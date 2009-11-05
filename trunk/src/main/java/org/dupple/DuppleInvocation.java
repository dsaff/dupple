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
import org.jmock.internal.InvocationExpectation;

/**
 * Stores an invocation that was observed against a recorder, and whether this
 * invocation has been verified yet.
 *
 * @author Google
 */
class DuppleInvocation {
  private Invocation invocation;
  private boolean verified = false;

  DuppleInvocation(Invocation invocation) {
    this.invocation = invocation;
  }

  @Override
  public String toString() {
    String methodName = invocation.getInvokedMethod().getName();
    Object[] parametersAsArray = invocation.getParametersAsArray();
    return methodName + "(" + Join.join(", ", parametersAsArray) + ")";
  }

  boolean invokedObjectIs(Object target) {
    return invocation.getInvokedObject() == target;
  }

  void setVerified() {
    verified = true;
  }

  boolean isVerified() {
    return verified;
  }

  boolean matchedBy(InvocationExpectation expectation) {
    return expectation.matches(invocation);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof DuppleInvocation
        && InvocationMatchingRuleset.newExactMatch().expectMatchOf(invocation)
            .matches(((DuppleInvocation) obj).invocation);
  }

  @Override
  public int hashCode() {
    return hashCodeThatCallsNoExtraMethods();
  }

  private int hashCodeThatCallsNoExtraMethods() {
    return 1;
  }
}
