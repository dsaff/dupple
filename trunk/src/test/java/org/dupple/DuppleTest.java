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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.hamcrest.Matchers;

import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * Tests the Dupple framework
 *
 * @author Google
 */
public class DuppleTest extends TestCase {
  /**
   * A interface for testing, which happens to look like a small subset of
   * Selenium
   */
  public interface ExampleInterface {
    void keyPress(String locator, String key);

    void answerOnNextPrompt(String answer);

    String getEval(String expression);
  }

  public void testAssertCalledFailsIfQuotedCallWasNotObserved() {
    final ExampleInterface exi = Dupple.recorder(ExampleInterface.class);
    try {
      Dupple.assertCalled(exi).keyPress("name=password", "\n");
    } catch (AssertionError e) {
      return;
    }
    fail("should have thrown assertion error");
  }

  public void testAssertCalled_whenFailingIndicatesActuallyCalledMethods() {
    String notCalled = "notCalled";
    ExampleInterface exi = Dupple.recorder(ExampleInterface.class);
    String actuallyCalled = "actuallyCalled";
    exi.answerOnNextPrompt(actuallyCalled);
    try {
      Dupple.assertCalled(exi).answerOnNextPrompt(notCalled);
    } catch (AssertionError e) {
      assertThat(e.getMessage(),
          containsString("{\nanswerOnNextPrompt(actuallyCalled)\n}"));
    }
  }

  /**
   * Returns null
   */
  public static class NullCallable implements Callable<Object> {
    public Object call() throws Exception {
      return null;
    }
  }

  public void testAssertThrownFailsWhenQuotedMethodThrowsNoException()
      throws Exception {
    Callable<Object> callable = new NullCallable();
    try {
      Dupple.assertThrown(new RuntimeException()).from(callable).call();
    } catch (AssertionError ae) {
      return; // SUCCESS!
    }
    fail("Should have caught exception");
  }

  /**
   * This actually throws the exceptions thrown by the quoted method, the
   * reverse of what ThrownAssertionTargetCollector does.
   */
  private static class ReversedTaeCollector extends
      ThrownExceptionAssertionBuilder {
    private ReversedTaeCollector() {
      super(null);
    }

    @Override
    protected void handleSomethingThrown(Throwable t) throws Throwable {
      // Shouldn't be called at all
      throw t;
    }

    @Override
    protected void handleNothingThrown() throws AssertionError {
      // expect this call
    }
  }

  /**
   * ThrownAssertionTargetCollector can be reversed by overriding (If this
   * breaks, there's something wrong in the imposterization/ exception-catching
   * logic)
   */
  public void testThrownAssertionTargetCollectorCanBeReversedByOverriding()
      throws Exception {
    new ReversedTaeCollector().from(new NullCallable()).call();
    // no exception thrown? Good!
  }

  /**
   * Throws an exception when called
   */
  public static class ThrowingCallable implements Callable<Object> {
    public Object call() throws Exception {
      throw new Exception("only thrown within Callable");
    }
  }

  public void testAssertThrown_throwableFailsIfTheQuotedMethodThrowsWrongException()
      throws Exception {
    Callable<Object> callable = new ThrowingCallable();
    try {
      Dupple.assertThrown(new Exception("a different exception"))
          .from(callable).call();
    } catch (AssertionFailedError ae) {
      return; // SUCCESS!
    }
    fail("Should have caught exception");
  }

  public void testAssertThrown_matcherFailsIfTheQuotedMethodThrowsWrongException()
      throws Exception {
    Callable<Object> callable = new ThrowingCallable();
    try {
      Dupple.assertThrown(is(ArithmeticException.class)).from(callable).call();
    } catch (AssertionFailedError ae) {
      return; // SUCCESS!
    }
    fail("Should have caught exception");
  }

  public void testAssertThrown_whenFailingShowsStackTraceOfActualException()
      throws Exception {
    Callable<Object> callable = new ThrowingCallable();
    try {
      Dupple.assertThrown(new Exception("a different exception"))
          .from(callable).call();
    } catch (AssertionFailedError ae) {
      assertThat(ae.getMessage(), Matchers.containsString("ThrowingCallable"));
      return; // SUCCESS!
    }
    fail("Should have caught exception");
  }

  public void testNoOtherCallsPasses() {
    ExampleInterface exi = Dupple.recorder(ExampleInterface.class);
    exi.answerOnNextPrompt("a");
    Dupple.assertCalled(exi).answerOnNextPrompt("a");
    Dupple.assertNoOtherCalls(exi);
  }

  public void testNoOtherCallsFails() {
    ExampleInterface exi = Dupple.recorder(ExampleInterface.class);
    exi.answerOnNextPrompt("a");
    exi.answerOnNextPrompt("b");
    Dupple.assertCalled(exi).answerOnNextPrompt("a");

    boolean assertionErrorThrown = false;
    try {
      Dupple.assertNoOtherCalls(exi);
    } catch (AssertionError e) {
      assertionErrorThrown = true;
    }
    assertTrue(assertionErrorThrown);
  }

  public void testFirstExpectationWins() {
    ExampleInterface stub = Dupple.stub(ExampleInterface.class);

    Dupple.willReturn("first").fromAnyCallTo(stub);
    Dupple.willReturn("second").from(stub).getEval("a");
    assertEquals("first", stub.getEval("a"));
  }

  public void testLowPriorityExpectationsComeLast() {
    ExampleInterface stub = Dupple.stub(ExampleInterface.class);

    Dupple.willReturn("first").withLowPriority().fromAnyCallTo(stub);
    Dupple.willReturn("second").from(stub).getEval("a");
    assertEquals("second", stub.getEval("a"));
  }

  public void testCallsTo() {
    ExampleInterface recorder = Dupple.recorder(ExampleInterface.class);
    assertEquals(0, Dupple.callsTo(recorder).size());
    recorder.getEval("a");
    assertEquals(1, Dupple.callsTo(recorder).size());
    recorder.getEval("b");
    assertEquals(2, Dupple.callsTo(recorder).size());
  }

  public void testAssertWhere_passes() {
    ExampleInterface recorder = Dupple.recorder(ExampleInterface.class);
    recorder.getEval("here's sub");
    Dupple.where("x", containsString("sub")).assertCalled(recorder)
        .getEval("x");
  }

  public void testAssertWhere_notCalledFails() {
    ExampleInterface recorder = Dupple.recorder(ExampleInterface.class);
    recorder.getEval("here's sub");
    try {
      Dupple.where("x", containsString("sub")).assertNotCalled(recorder)
          .getEval("x");
    } catch (AssertionError e) {
      // success!
      return;
    }
    fail("Should have thrown exception");
  }

  public void testAssertWhere_passesTwoParams() {
    ExampleInterface recorder = Dupple.recorder(ExampleInterface.class);
    recorder.keyPress("here's sub", "here's tub");
    Dupple.where("x", containsString("sub")).andWhere("y",
        containsString("tub")).assertCalled(recorder).keyPress("x", "y");
  }

  public void testAssertWhere_fails() {
    ExampleInterface recorder = Dupple.recorder(ExampleInterface.class);
    recorder.getEval("I won't say it");
    try {
      Dupple.where("x", containsString("sub")).assertCalled(recorder).getEval(
          "x");
    } catch (AssertionError e) {
      // success!
      return;
    }
    fail("Should have thrown exception");
  }

  /**
   * Returns int
   * 
   * @author Google
   */
  public static interface ReturnsInt {
    public int getValue();
  }

  public void testRecordCallsReturningInt() {
    Dupple.recorder(ReturnsInt.class).getValue();
  }

  /**
   * Has nothing to do with imposterization
   * 
   * @author Google
   */
  public static interface NothingToDoWithImposterization {
    public void getCreator();
  }

  public void testRestrictIgnoredMethodCalls() {
    NothingToDoWithImposterization recorder =
        Dupple.recorder(NothingToDoWithImposterization.class);
    recorder.getCreator();
    Dupple.assertCalled(recorder).getCreator();
  }

  public void testAssertReturned_passes() {
    Dupple.assertReturned(is(3)).from(Arrays.asList(1, 2, 3)).size();
  }

  public void testAssertReturned_onToString() {
    Dupple.assertReturned(containsString("3")).from(Arrays.asList(1, 2, 3))
        .toString();
  }

  public void testAssertReturned_fails() {
    try {
      Dupple.assertReturned(is(2)).from(Arrays.asList(1, 2, 3)).size();
    } catch (AssertionFailedError e) {
      assertThat(e.toString(), containsString("[1, 2, 3].size()"));
      return;
    }
    fail("Should have thrown exception");
  }

  public void testAssertReturned_failsOnToString() {
    try {
      Dupple.assertReturned(containsString("5")).from(Arrays.asList(1, 2, 3))
          .toString();
    } catch (AssertionFailedError e) {
      assertThat(e.toString(), containsString("[1, 2, 3].toString()"));
      return;
    }
    fail("Should have thrown exception");
  }
}
