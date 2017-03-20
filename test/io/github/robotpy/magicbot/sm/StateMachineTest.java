/**
   Copyright 2017 Dustin Spicuzza

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package io.github.robotpy.magicbot.sm;

import static org.junit.Assert.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import io.github.robotpy.magicbot.exceptions.InvalidDurationException;
import io.github.robotpy.magicbot.exceptions.MultipleFirstStatesError;
import io.github.robotpy.magicbot.exceptions.NoFirstStateException;

public class StateMachineTest {
	
	class FakeClock extends Clock {
		
		public long now = 0;

		@Override
		public ZoneId getZone() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Instant instant() {
			return null;
		}
		
		@Override
		public long millis() {
			return now;
		}
		
	}
	
	class AllTheStateTypes {
		
		@State(first=true)
		private void all(double tm, boolean init) {}
		
		@State
		private void noinit(double tm) {}
		
		@State
		private void notm(boolean init) {}
		
		@State
		private void nada() {}
		
	}
	
	@Test
	public void testAllTheStateTypes() {
		new AllTheStateTypes();
	}
	

	class NoTimedStateDuration extends StateMachine {
		@TimedState()
		private void tmp() {}
	}
	
	@Test
	public void testNoTimedState() {
		try {
			new NoTimedStateDuration();
			fail();
		} catch (InvalidDurationException e) {
			// ok
		}
	}
	
	class NoStartState extends StateMachine {
	}
	
	@Test
	public void testNoStartState() {
		try {
			new NoStartState();
			fail();
		} catch (NoFirstStateException e) {
			// ok
		}
	}
	
	class MultipleFirstStates extends StateMachine {
		@State(first=true)
		private void tmp1(Double tm) {}
		
		@State(first=true)
		private void tmp2(double tm, boolean initial) {}
	}
	
	@Test
	public void testMultipleFirstStates() {
		try {
			new MultipleFirstStates();
			fail();
		} catch (MultipleFirstStatesError e) {
			// ok
		}
	}
	
	class TestStateMachine1 extends StateMachine {
		public List<String> executed = new ArrayList<>();
		
		public TestStateMachine1() {
			super();
			m_verboseLogging = true;
		}
		
		public void some_fn() {
			executed.add("sf");
		}
		
		@State(first=true)
		private void first_state(boolean initialState) {
			executed.add("1");
			nextState("second_state");
		}
		
		@TimedState(duration=1, nextState="third_state")
		private void second_state(double tm) {
			executed.add("2");
		}
		
		@State
		private void third_state() {
			executed.add("3");
		}
	}
	
	@Test
	public void testStateMachine1() {
		
		TestStateMachine1 sm = new TestStateMachine1();
		FakeClock wpitime = new FakeClock();
		sm.m_clock = wpitime;
		
		sm.some_fn();
		assertEquals("", sm.m_currentState);
		assertFalse(sm.isExecuting());
		
		sm.engage();
	    assertEquals("first_state", sm.m_currentState);
	    assertFalse(sm.isExecuting());
	    
	    sm.execute();
	    assertEquals("second_state", sm.m_currentState); 
	    assertTrue(sm.isExecuting());
	    
	    // should not change
	    sm.engage();
	    assertEquals("second_state", sm.m_currentState);
	    assertTrue(sm.isExecuting());
	    
	    sm.execute();
	    assertEquals("second_state", sm.m_currentState);
	    assertTrue(sm.isExecuting());
	    
	    wpitime.now += 1500;
	    sm.engage();
	    sm.execute();
	    assertEquals("third_state", sm.m_currentState); 
	    assertTrue(sm.isExecuting());
	    
	    sm.engage();
	    sm.execute();
	    assertEquals("third_state", sm.m_currentState);
	    assertTrue(sm.isExecuting());
	    
	    // should be done
	    sm.done();
	    assertEquals("", sm.m_currentState);
	    assertFalse(sm.isExecuting());
	    
	    // should be able to start directly at second state
	    sm.engage("second_state");
	    sm.execute();
	    assertEquals("second_state", sm.m_currentState); 
	    assertTrue(sm.isExecuting());
	    
	    wpitime.now += 1500;
	    sm.engage();
	    sm.execute();
	    assertEquals("third_state", sm.m_currentState);
	    assertTrue(sm.isExecuting());
	    
	    // test force
	    sm.engage();
	    sm.execute();
	    assertEquals("third_state", sm.m_currentState);
	    assertTrue(sm.isExecuting());
	    
	    sm.engage(true);
	    assertEquals("first_state", sm.m_currentState); 
	    assertTrue(sm.isExecuting());
	    
	    sm.execute();
	    sm.execute();
	    assertFalse(sm.isExecuting());
	    assertEquals("", sm.m_currentState);
	    
	    assertEquals(
	    	Arrays.asList("sf", "1", "2", "3", "3", "2", "3", "3", "1"),
	    	sm.executed
	    );
	}
	
	/*
	 * Unfortunately, Java doesn't support inheriting annotations...
	 * 
	class InhBase extends StateMachine {
		
		@State
		protected void second_state() {
			done();
		}
	}
	
	class InhChild extends InhBase {
		@State(first=true)
		private void first_state() {
			nextState("second_state");
		}
	}
	
	@Test
	public void testSmInheritance() {
		InhChild sm = new InhChild();
		
		sm.engage();
		assertEquals("first_state", sm.m_currentState);
		
		sm.execute();
		assertEquals("second_state", sm.m_currentState);
		
		sm.execute();
		assertEquals("", sm.m_currentState);
		assertFalse(sm.isExecuting());
	}*/
	
	class MustFinishSm extends StateMachine {
		public List<String> executed = new ArrayList<>();
		
		public MustFinishSm() {
			super();
			m_verboseLogging = true;
		}
		
		@State(first=true)
		private void ordinary1() {
			nextState("ordinary2");
			executed.add("1");
		}
		
		@State
		private void ordinary2() {
			nextState("must_finish");
			executed.add("2");
		}
		
		@State(mustFinish=true)
		private void must_finish() {
			executed.add("mf");
		}
		
		@State
		private void ordinary3() {
			executed.add("3");
			nextStateNow("timed_must_finish");
		}
		
		@TimedState(duration=1, mustFinish=true)
		private void timed_must_finish() {
			executed.add("tmf");
		}
	}
	
	@Test
	public void testMustFinish() {
		MustFinishSm sm = new MustFinishSm();
		FakeClock wpitime = new FakeClock();
		sm.m_clock = wpitime;
		
		sm.engage();
	    sm.execute();
	    sm.execute();
	    
	    assertEquals("", sm.m_currentState);
	    assertFalse(sm.isExecuting());
	    
	    sm.engage();
	    sm.execute();
	    sm.engage();
	    sm.execute();
	    sm.execute();
	    sm.execute();
	    
	    assertEquals("must_finish", sm.m_currentState); 
	    assertTrue(sm.isExecuting());
	    
	    sm.nextState("ordinary3");
	    sm.engage();
	    sm.execute();
	    
	    assertEquals("timed_must_finish", sm.m_currentState); 
	    
	    sm.execute();
	    assertTrue(sm.isExecuting());
	    assertEquals("timed_must_finish", sm.m_currentState); 
	    
	    for (int i = 0; i < 7; i++) {
	        wpitime.now += 100;
	    
	        sm.execute();
	        assertTrue(sm.isExecuting());
	        assertEquals("timed_must_finish", sm.m_currentState);
	    }
	    
	    wpitime.now += 1000;
	    sm.execute();
	    assertFalse(sm.isExecuting());
	    
	    assertEquals(
	    	Arrays.asList("1", "1", "2", "mf", "mf", "3",
	    				  "tmf", "tmf", "tmf", "tmf", "tmf", "tmf", "tmf", "tmf", "tmf"),
	    	sm.executed
	    );
	}
	
}
