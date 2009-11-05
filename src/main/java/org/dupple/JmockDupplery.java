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
import org.jmock.api.Imposteriser;
import org.jmock.api.Invokable;
import org.jmock.internal.InvocationDiverter;
import org.jmock.internal.ProxiedObjectIdentity;
import org.jmock.internal.ReturnDefaultValueAction;
import org.jmock.lib.CamelCaseNamingScheme;
import org.jmock.lib.legacy.ClassImposteriser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements {@link Dupplery} through heavy reuse of classes from jMock. By
 * default, there is one Dupplery per double (stub, recorder, etc). For more
 * details, see {@link Dupple}
 * 
 * Note to reviewers and early-adopters: this class was named Dupplery, but was
 * renamed to make room to use Dupplery as the interface name.
 * 
 * @author Google
 */
class JmockDupplery implements Dupplery {
  private final InvocationLog invocations = new InvocationLog();
  private ExpectedCalls expectedCalls = new ExpectedCalls();
  private final Imposteriser baseImposterizer = ClassImposteriser.INSTANCE;

  @SuppressWarnings("unchecked")
  public <T> T assertCalled(T target) {
    return assertCalledWithStandins(defaultRuleset(), target, true);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T assertNotCalled(T target) {
    return assertCalledWithStandins(defaultRuleset(), target, false);
  }

  private InvocationMatchingRuleset defaultRuleset() {
    return InvocationMatchingRuleset.newExactMatch();
  }

  @Override
  public <T> T assertCalledWithStandins(
      final InvocationMatchingRuleset ruleset, T target,
      boolean shouldHaveCalled) {
    return imposterize(target, invocations.invokeToCheckMatch(ruleset,
        shouldHaveCalled));
  }

  /**
   * Creates a mock object of type <var>typeToMock</var> and generates a name
   * for it.
   */
  <T> T stub(Class<T> typeToMock) {
    return stub(typeToMock, nameFor(typeToMock));
  }

  <T> T permissiveStub(Class<T> typeToMock) {
    T stub = stub(typeToMock);
    new StubExpectationBuilder(returnDefaultValueAction()).withLowPriority()
        .fromAnyCallTo(stub);
    return stub;
  }

  private ReturnDefaultValueAction returnDefaultValueAction() {
    ReturnDefaultValueAction action = new ReturnDefaultValueAction();
    action.addResult(File.class, new File("noDefault"));
    return action;
  }

  <T> String nameFor(Class<T> typeToMock) {
    return CamelCaseNamingScheme.INSTANCE.defaultNameFor(typeToMock);
  }

  @SuppressWarnings("unchecked")
  <T> T stub(Class<T> typeToMock, final String name) {
    return (T) objectStub(typeToMock, name);
  }

  private Object objectStub(Class<?> typeToMock, final String name) {
    Invokable mock = expectedCalls.stubInvokable(name);
    return imposterize(new ProxiedObjectIdentity(mock), typeToMock);
  }

  <T> T recordCalls(final Class<T> type) {
    return recordCalls(permissiveStub(type));
  }

  @SuppressWarnings("unchecked")
  public <T> T recordCalls(final T target) {
    return imposterize(target, invocations.recordingInvokable(target));
  }

  Class<? extends Object> targetClass(Object target) {
    if (target instanceof ImposterizationRememberer) {
      Class<?> imposterizedClass =
          ((ImposterizationRememberer) target).getImposterizedClass();
      if (imposterizedClass.isInterface()) {
        return Object.class;
      }
      return imposterizedClass;
    }
    return new DuppleableType(target.getClass()).targetClass();
  }

  public void assertNoOtherCalls(Object target) {
    invocations.assertNoUnverifiedInvocations(target);
  }

  public void addLowPriorityExpectation(final Expectation expectation) {
    expectedCalls.addLowPriorityExpectation(expectation);
  }

  public void addNormalExpectation(Expectation expectation) {
    expectedCalls.addNormalExpectation(expectation);
  }

  private Object imposterize(final Invokable mockObject,
      final Class<?> mockedType, Class<?>... ancilliaryTypes) {
    Invokable invokable = mockObject;
    invokable = addRemembererInterface(invokable, mockedType);
    try {
      return baseImposterizer.imposterise(invokable, mockedType, appendToArray(
          ancilliaryTypes, ImposterizationRememberer.class));
    } catch (IllegalArgumentException e) {
      // TODO: test this in.
      if (mockedType != Object.class) {
        return imposterize(mockObject, mockedType.getSuperclass(),
            ancilliaryTypes);
      }
      throw e;
    }
  }

  private Invokable addRemembererInterface(final Invokable mockObject,
      final Class<?> mockedType) {
    return new InvocationDiverter<ImposterizationRememberer>(
        ImposterizationRememberer.class, rememberer(mockedType), mockObject);
  }

  private ImposterizationRememberer rememberer(final Class<?> rememberedType) {
    return new ImposterizationRememberer() {
      @Override
      public Class<?> getImposterizedClass() {
        return rememberedType;
      }

      @Override
      public JmockDupplery getCreator() {
        return JmockDupplery.this;
      }
    };
  }

  private Class<?>[] appendToArray(Class<?>[] firstClasses, Class<?> lastClass) {
    for (Class<?> each : firstClasses) {
      if (each == lastClass) {
        return firstClasses;
      }
    }
    Class<?>[] result = new Class<?>[firstClasses.length + 1];
    System.arraycopy(firstClasses, 0, result, 0, firstClasses.length);
    result[firstClasses.length] = lastClass;
    return result;
  }

  @Override
  public List<Object> getInvocations() {
    return new ArrayList<Object>(invocations);
  }

  @SuppressWarnings("unchecked")
  <T> T imposterize(final T target, Invokable invokable) {
    return (T) ClassImposteriser.INSTANCE.imposterise(invokable,
        targetClass(target), targetInterfaces(target));
  }

  private <T> Class<?>[] targetInterfaces(final T target) {
    return target.getClass().getInterfaces();
  }
}
