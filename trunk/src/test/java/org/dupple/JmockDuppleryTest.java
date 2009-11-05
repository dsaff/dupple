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

import com.google.ocean.testing.dupple.DuppleTest.DummySelenium;

import junit.framework.TestCase;

/**
 * Tests JmockDupplery
 * 
 * @author Google
 */
public class JmockDuppleryTest extends TestCase {  
  public void testTargetClass_whenRecordingInterface() {
    DummySelenium recorder = Dupple.recorder(DummySelenium.class);
    final JmockDupplery creator =
        (JmockDupplery) ImposterizationRememberer.Util.creator(recorder);
    assertEquals(Object.class, creator.targetClass(recorder));
  }

  public void testGetInvocations_forRecorderOfStub() {
    DummySelenium stub = Dupple.permissiveStub(DummySelenium.class);
    DummySelenium recorder = Dupple.recorder(stub);
    final JmockDupplery creator =
        (JmockDupplery) ImposterizationRememberer.Util.creator(recorder);

    recorder.getEval("rightUrl");

    assertEquals(1, creator.getInvocations().size());
  }
}
