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

import static org.hamcrest.CoreMatchers.is;

import junit.framework.AssertionFailedError;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;
import org.jmock.internal.ReturnDefaultValueAction;
import org.jmock.lib.action.ReturnValueAction;
import org.jmock.lib.action.ThrowAction;

import java.util.List;

/**
 * Dupple is a wrapper for jMock that exposes a simple static interface for
 * recording and stubbing (like Mockito), but eschews global state (like jMock),
 * by having each mock object remember its controller (the Dupplery).
 * 
 * <p>
 * (Many of these methods (see, for example, {@link #assertCalled} and
 * {@link StubExpectationBuilder#from}) return <em>imposters</em>: values of an
 * appropriate type, which exhibit drastically different behavior from normal
 * objects of that type.)
 * 
 * <h2>Examples:</h2>
 * 
 * Recording calls:
 * 
 * <pre>
 * // test that andSubmit() calls click with the right params
 * public void testSignInFormSubmitsRightly() {
 *   Selenium sel = Dupple.recorder(Selenium.class);
 *   SignInForm.forBrowser(sel).andSubmit();
 *   Dupple.assertCalled(sel).click(&quot;[[buttonNamed(SIGN_IN)]]&quot;);
 * }
 * </pre>
 * 
 * Stubbing:
 * 
 * <pre>
 * //return a FileNameSource that returns fileName from getFilename() 
 * private FileNameSource nameSourceReturning(File fileName) throws IOException {
 *   FileNameSource nameSource = Dupple.stub(FileNameSource.class);
 *   Dupple.willReturn(fileName).from(nameSource).getFilename();
 *   return nameSource;
 * }
 * </pre>
 * 
 * <pre>
 * // return a Selenium instance that returns source from getHtmlSource, 
 * // and a default value for any other call
 * private Selenium seleniumReturningSource(String source) {
 *   Selenium sel = Dupple.stub(Selenium.class);
 *   Dupple.willReturn(source).from(sel).getHtmlSource();
 *   Dupple.willReturnDefaultValue().fromAnyOtherCallTo(sel);
 *   return sel;
 * }
 * </pre>
 * 
 * Exception assertion:
 * 
 * <pre>
 * Dupple.assertThrown(is(ArithmeticException.class)).from(callable).call();
 * </pre>
 * 
 * Method call assertion:
 * 
 * <pre>
 * Dupple.assertReturned(is(true)).from(list1).contains(list2);
 * </pre>
 * 
 * @author Google
 */
public class Dupple {
  private Dupple() {
    // prevent construction
  }

  // RECORDING

  /**
   * Returns a double for {@code type} that remembers all incoming calls, for
   * use in a later call to {@code assertCalled}. A default return value is
   * supplied for each call.
   */
  public static <T> T recorder(Class<T> type) {
    return new JmockDupplery().recordCalls(type);
  }

  /**
   * Returns a wrapper around {@code target} that remembers all incoming calls,
   * for use in a later call to {@code assertCalled}. Each call is delegated to
   * target for return value and side effects.
   */
  public static <T> T recorder(T target) {
    return ImposterizationRememberer.Util.creator(target).recordCalls(target);
  }

  /**
   * Returns a list of objects representing the method calls that have been
   * recorded against target {@code target}. No guarantees are made about the
   * type of objects stored in the returned list, but they will give correct
   * answers to {@code equals()}, {@code hashCode()}, and {@code toString()}.
   */
  public static List<Object> callsTo(Object target) {
    return ImposterizationRememberer.Util.creator(target).getInvocations();
  }

  /**
   * Returns a builder that remembers {@code target}, and expects to receive a
   * message to continue the build. See class comment for an example. The method
   * call that is invoked against the returned builder is known as the "quoted
   * call" in tests and docs.
   */
  public static <T> T assertCalled(T target) {
    return ImposterizationRememberer.Util.creator(target).assertCalled(target);
  }

  /**
   * Returns a builder that remembers {@code target}, and expects to receive a
   * message to continue the build. See class comment for an example. The method
   * call that is invoked against the returned builder is known as the "quoted
   * call" in tests and docs.
   */
  public static <T> T assertNotCalled(T target) {
    return ImposterizationRememberer.Util.creator(target).assertNotCalled(
        target);
  }

  /**
   * Asserts that all recorded calls to {@code target} have been matched by
   * previous calls to {@code assertCalled}.
   */
  public static void assertNoOtherCalls(Object target) {
    ImposterizationRememberer.Util.creator(target).assertNoOtherCalls(target);
  }

  /**
   * Sets up a chain for asserting a call where one or more parameters should
   * not be checked by equality.
   * 
   * For example:
   * 
   * <code>
   * Dupple.where("x", containsString("Bob")).assertCalled(person).setName("x");
   * </code>
   */
  public static AssertWhereCollector where(Object standIn, Matcher<?> matcher) {
    return new AssertWhereCollector(standIn, matcher);
  }

  // STUBS

  /**
   * Returns a stub for {@code type}. This stub will throw exceptions on any
   * call, unless further expectations are set with {@link #willReturn(Object)},
   * {@link #willThrow(Throwable)}, etc.
   */
  public static <T> T stub(Class<T> type) {
    return new JmockDupplery().stub(type);
  }

  /**
   * Returns a stub for {@code type}. This stub will return a default value for
   * every call. For object return types, the returned value will be a
   * permissive stub for that type.
   */
  public static <T> T permissiveStub(Class<T> type) {
    return new JmockDupplery().permissiveStub(type);
  }

  /**
   * Returns a call builder to set an expectation for a call that will return
   * {@code value} See class comment for an example.
   */
  public static StubExpectationBuilder willReturn(Object value) {
    return new StubExpectationBuilder(new ReturnValueAction(value));
  }

  /**
   * Returns a call builder to set an expectation for a call that will return a
   * default value. This default value is false for booleans, zero for numeric
   * primitive types, and a fresh permissive stub for most reference types. See
   * class comment for an example.
   */
  public static StubExpectationBuilder willReturnDefaultValue() {
    return new StubExpectationBuilder(new ReturnDefaultValueAction());
  }

  /**
   * Returns a call builder to set an expectation for a call that will throw an
   * exception {@code e}. For example:
   * 
   * <pre>
   * Dupple.willThrow(new IOException()).from(source).getFilename();
   * </pre>
   */
  public static StubExpectationBuilder willThrow(Throwable e) {
    return new StubExpectationBuilder(new ThrowAction(e));
  }

  // ONE-LINE BEHAVIOR ASSERTIONS

  /**
   * Returns a call builder that will assert a given call throws an expected
   * exception {@code e}. See class comment for example. The method call that is
   * invoked against the returned object is known as the "quoted call" in tests
   * and docs.
   */
  public static ThrownExceptionAssertionBuilder assertThrown(Throwable e) {
    return assertThrown(IsEqual.equalTo(e));
  }

  /**
   * Returns a call builder that will assert a given call throws an exception
   * matching {@code m} See class comment for example.
   */
  public static ThrownExceptionAssertionBuilder assertThrown(Matcher<?> m) {
    return new ThrownExceptionAssertionBuilder(m);
  }

  /**
   * Returns a call builder that will assert a given call throws an
   * {@link AssertionFailedError}
   */
  public static <T> T assertFails(T target) {
    return assertThrown(is(AssertionFailedError.class)).from(target);
  }

  /**
   * Returns a call builder that will assert a given call returns a value
   * matching {@code m} See class comment for example.
   */
  public static ReturnedValueAssertionBuilder assertReturned(Matcher<?> m) {
    return new ReturnedValueAssertionBuilder(m);
  }
}
