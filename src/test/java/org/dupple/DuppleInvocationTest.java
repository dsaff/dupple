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

import org.dupple.DuppleTest.ExampleInterface;

import junit.framework.TestCase;

/**
 * Tests DuppleInvocation
 * 
 * @author Google
 */
public class DuppleInvocationTest extends TestCase {
  public void testEquals_false() {
    ExampleInterface r1 = Dupple.recorder(ExampleInterface.class);
    ExampleInterface r2 = Dupple.recorder(ExampleInterface.class);
    r1.getEval("a");
    r2.getEval("b");
    assertFalse(Dupple.callsTo(r1).equals(Dupple.callsTo(r2)));
  }
  
  public void testEquals_null() {
    assertFalse(new DuppleInvocation(null).equals(null));
  }

  public void testEquals_true() {
    ExampleInterface r1 = Dupple.recorder(ExampleInterface.class);
    ExampleInterface r2 = Dupple.recorder(ExampleInterface.class);
    r1.getEval("a");
    r2.getEval("a");
    assertTrue(Dupple.callsTo(r1).equals(Dupple.callsTo(r2)));
  }

  public void testHashCode() {
    ExampleInterface r1 = Dupple.recorder(ExampleInterface.class);
    ExampleInterface r2 = Dupple.recorder(ExampleInterface.class);
    r1.getEval("a");
    r2.getEval("a");
    assertEquals(Dupple.callsTo(r1).hashCode(), Dupple.callsTo(r2).hashCode());
  }
}
