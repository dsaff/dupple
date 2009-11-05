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

import java.util.List;

/**
 * Represents testing-specific operations that can be performed on a test
 * double. There can be many duppleries in operation at one time, each managing
 * a set of stubs and recorders
 * 
 * @author Google
 */
public interface Dupplery {
  /**
   * Add {@code expectation} to the list of normal expectations for stubs
   * created by this dupplery. When client code calls a stub, each normal
   * expectation is checked in order for a matching expectation, whose action is
   * then invoked.
   */
  void addNormalExpectation(Expectation expectation);


  /**
   * Add {@code expectation} to the list of low-priority expectations for stubs
   * created by this dupplery. These are checked after all normal expectations,
   * including normal expectations that are added later.
   */
  void addLowPriorityExpectation(Expectation expectation);

  /**
   * Returns a proxy that builds an assertion. If a method is invoked against
   * the proxy that does not <em>match</em> a method already invoked against a
   * recorder from this Dupplery, an {@link AssertionError} is thrown (see class
   * comment on {@link Dupple} for an example). The default matching ruleset is
   * used, in which all parameters must be exactly equal for two invocations to
   * be considered matching.
   */
  <T> T assertCalled(T target);

  /**
   * Returns a proxy that builds an assertion. If a method is invoked against
   * the proxy that does <em>match</em> a method already invoked against a
   * recorder from this Dupplery, an {@link AssertionError} is thrown (see class
   * comment on {@link Dupple} for an example). The default matching ruleset is
   * used, in which all parameters must be exactly equal for two invocations to
   * be considered matching.
   */
  <T> T assertNotCalled(T target);

  /**
   * Returns a proxy that builds an assertion. If a method is invoked against
   * the proxy that does not <em>match</em> a method already invoked against a
   * recorder from this Dupplery, an {@link AssertionError} is thrown (see class
   * comment on {@link Dupple} for an example). The definition of "match" is
   * provided by {@code ruleset}.
   */
  <T> T assertCalledWithStandins(final InvocationMatchingRuleset ruleset,
      T target, boolean shouldHaveCalled);

  /**
   * If any recorded calls to {@code target} have not been matched by calls to
   * {@link #assertCalled(Object)}, this will throw an {@link AssertionError}.
   */
  void assertNoOtherCalls(Object target);


  /**
   * Returns a list of objects representing the method calls that have been
   * recorded against targets created by this dupplery. No guarantees are made
   * about the type of objects stored in the returned list, but they will give
   * correct answers to {@code equals()}, {@code hashCode()}, and {@code
   * toString()}.
   */
  List<Object> getInvocations();
  
  <T> T recordCalls(T target);
}
