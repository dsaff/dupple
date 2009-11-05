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

/**
 * Enables a chain where a call is searched for, but some of the params can be
 * members of a set of acceptable values given by a Matcher.
 * 
 * @see Dupple#where(Object, Matcher)
 * 
 * @author Google
 */
public class AssertWhereCollector {
  // TODO: clean up package
  private InvocationMatchingRuleset standinMap =
      InvocationMatchingRuleset.newExactMatch();

  AssertWhereCollector(Object standIn, Matcher<?> matcher) {
    addStandIn(standIn, matcher);
  }

  <U> U assertCalled(U target) {
    return creator(target).assertCalledWithStandins(standinMap, target, true);
  }

  <U> U assertNotCalled(U target) {
    return creator(target).assertCalledWithStandins(standinMap, target, false);
  }

  private Dupplery creator(Object target) {
    return ImposterizationRememberer.Util.creator(target);
  }

  /**
   * Adds a standIn value, and returns this for chaining
   */
  public AssertWhereCollector andWhere(Object standIn, Matcher<?> matcher) {
    addStandIn(standIn, matcher);
    return this;
  }

  private void addStandIn(Object standIn, Matcher<?> matcher) {
    standinMap.addStandIn(standIn, matcher);
  }
}
