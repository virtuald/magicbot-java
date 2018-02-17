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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import io.github.robotpy.magicbot.MagicComponent;
import io.github.robotpy.magicbot.exceptions.InvalidDurationException;
import io.github.robotpy.magicbot.exceptions.MultipleDefaultStatesError;
import io.github.robotpy.magicbot.exceptions.MultipleFirstStatesError;
import io.github.robotpy.magicbot.exceptions.NoFirstStateException;


/**
 * This object is designed to be used to easily implement magicbot
 * components that are basically a big state machine.
 * 
 * You use this by defining a class that inherits from ``StateMachine``.
 * To define each state, you use the ``timed_state`` decorator on a
 * function. When each state is run, the decorated function will be
 * called. Annotated state functions can receive the following
 * parameters:
 * 
 * - ``tm`` - The number of seconds since the state machine has been active
 * - ``stateTm`` - The number of seconds since this state has been active
 *   (note: it may not start at zero!)
 * - ``initialCall`` - Set to true when the state is initially called,
 *   false otherwise. If the state is switched to multiple times, this
 *   will be set to true at the start of each state.
 *   
 * To be consistent with the magicbot philosophy, in order for the
 * state machine to execute its states you must call the
 * ``engage`` function upon each execution of the main
 * robot control loop. If you do not call this function, then
 * execution will cease unless the current executing state is
 * marked as ``must_finish``.
 * 
 * When execution ceases, the ``done`` function will be called
 * unless execution was stopped by calling the ``done`` function.
 * 
 * As a magicbot component, this contains an ``execute`` function that
 * will be called on each control loop. All state execution occurs from
 * within that function call. If you call other components from this
 * component, you should ensure that your component occurs *before*
 * the other components in your Robot class.
 * 
 * @warning This object is not intended to be threadsafe
 */
public class StateMachine implements MagicComponent {
	
	/**
	 * All state function will receive these parameters, but you may drop some
	 * parameters if you don't need them.
	 */
	public static interface StateMethod {
		public void execute(double stateTime, boolean initialCall);
	}
	
	class StateData {
		
		final String name;
		final double duration;
		final String nextState;
		
		final boolean first;
		final boolean mustFinish;
		final boolean isDefault;
		
		final StateMethod stateMethod;
		
		double expires = Double.MAX_VALUE;
		double startTime = 0;
		boolean ran = false;
		
		StateData(String stateName, State s, StateMethod m) {
			name = stateName;
			first = s.first();
			mustFinish = s.mustFinish();
			isDefault = false;
			
			nextState = null;
			duration = Double.MAX_VALUE;
			
			stateMethod = m;
		}
		
		StateData(String stateName, TimedState ts, StateMethod m) {
			name = stateName;
			
			duration = ts.duration();
			if (duration <= 0.0) {
				throw new InvalidDurationException("Must specify positive duration for @TimedState");
			}
			
			nextState = ts.nextState().equals("") ? null : ts.nextState();
			
			first = ts.first();
			mustFinish = ts.mustFinish();
			isDefault = false;
			
			stateMethod = m;
		}
		
		StateData(String stateName, DefaultState s, StateMethod m) {
			name = stateName;
			duration = Double.MAX_VALUE;
			nextState = null;
			
			first = false;
			mustFinish = true;
			isDefault = true;
			
			stateMethod = m;
		}
	}
	
	// Indicates that an external party wishes the state machine to execute
	private boolean m_shouldEngage = false;

	// Indicates that the state machine is currently executing
	private boolean m_engaged = false;
	
	// All states
	private final Map<String, StateData> m_states;

	// The currently executing state, or null if not executing
	private StateData m_state = null;
	
	private final String m_firstState;
	private final StateData m_defaultState;
	
	double m_start = 0.0;
	
	public boolean m_verboseLogging = false;
	
	protected Clock m_clock = Clock.systemUTC();
	
	public StateMachine() {
		
		String firstState = null;
		StateData defaultState = null;
		
		m_states = new HashMap<>();
		
		// for each method
		for (Method method : this.getClass().getDeclaredMethods()) {
			
			// check for state annotations
			State stateAnn = method.getAnnotation(State.class);
			TimedState tsAnn = method.getAnnotation(TimedState.class);
			DefaultState dfAnn = method.getAnnotation(DefaultState.class);
			
			if (stateAnn == null && tsAnn == null && dfAnn == null) {
				continue;
			}
			
			StateMethod stateMethod;
			StateData state;
			String methodName = method.getName();
			
			method.setAccessible(true);
			
			try {
				// transform the method
				MethodHandle handle = MethodHandles.lookup().unreflect(method);
				handle = handle.bindTo(this);
				
				Type[] types = method.getGenericParameterTypes();
				
				if (types.length == 0) {
					handle = MethodHandles.dropArguments(handle, 0, Double.class, Boolean.class);
				} else if (types.length == 1) {
					if (types[0].equals(double.class) || types[0].equals(Double.class)) {
						handle = MethodHandles.dropArguments(handle, 1, boolean.class);
					} else if (types[0].equals(boolean.class) || types[0].equals(Boolean.class)) {
						handle = MethodHandles.dropArguments(handle, 0, double.class);
					} else {
						throw new RuntimeException("@State method '" + methodName + "' has invalid parameter type " + types[0]);
					}
				}
				
				stateMethod = MethodHandleProxies.asInterfaceInstance(StateMethod.class, handle);
			} catch ( IllegalAccessException e) {
				throw new RuntimeException("@State method '" + methodName + "' has invalid parameters", e);
			}
			
			// now that nonsense is done, let's construct our state data and continue
			if (stateAnn != null && tsAnn != null) {
				throw new RuntimeException("Cannot mark a function with @State and @TimedState!");
				
			} else if (stateAnn != null) {
				state = new StateData(methodName, stateAnn, stateMethod);
				
			} else if (tsAnn != null) {
				state = new StateData(methodName, tsAnn, stateMethod);
			
			} else if (dfAnn != null) {
				state = new StateData(methodName, dfAnn, stateMethod);
				
			} else {
				continue;
			}
			
			if (state.first) {
				if (firstState != null) {
					throw new MultipleFirstStatesError("Multiple states were specified as the first state!");
				}
				
				firstState = state.name;	
			}
			
			if (state.isDefault) {
				if (defaultState != null) {
					throw new MultipleDefaultStatesError("Multiple states were specified as the default state!");
				}
				
				defaultState = state;
			}
			
			m_states.put(state.name, state);
		}
		
		if (firstState == null) {
			throw new NoFirstStateException("Starting state not defined!");
		}
		
		m_firstState = firstState;
		m_defaultState = defaultState;
	}
	
	
	/**
	 * @return true if the state machine is executing states
	 */
	public boolean isExecuting() {
		return m_engaged;
	}
	
	/**
	 * @return name of currently executing state
	 */
	public String getCurrentState() {
		return m_state == null ? "" : m_state.name;
	}
	
	/**
	 * MagicComponent API: called when autonomous/teleop is disabled
	 */
	@Override
	public void onDisabled() {
		done();
	}
	
	/**
	 * This signals that you want the state machine to execute its
	 * states.
	 */
	public void engage() {
		engage(null, false);
	}
	
	public void engage(String initialState) {
		engage(initialState, false);
	}
	
	public void engage(boolean force) {
		engage(null, force);
	}
	
	public void engage(String initialState, boolean force) {
		m_shouldEngage = true;
		
		if (force || m_state == null || m_state == m_defaultState) {
			if (initialState != null) {
				nextState(initialState);
			} else {
				nextState(m_firstState);
			}
		}
	}

	/**
	 * Call this function to transition to the next state
	 * 
	 * @param name Name of the state to transition to
	 */
	protected void nextState(String name) {
		StateData state = m_states.get(name);
		if (state == null) {
			throw new RuntimeException("Invalid state '" + name + "' specified");
		}
		
		state.ran = false;
		m_state = state;
	}
	
	/**
	 * Call this function to transition to the next state, and call the next
	 * state function immediately. Prefer to use 'next_state' instead.
	 * 
	 * @param name Name of the state to transition to
	 */
	protected void nextStateNow(String name) {
		nextState(name);
		// TODO: is this a bad idea?
		execute();
	}
	
	/**
	 * Call this function to end execution of the state machine
	 */
	public void done() {
		if (m_verboseLogging && m_state != null) {
			double tm = (m_clock.millis() / 1000.0) - m_start;
			System.out.printf("%.3fs: Stopped state machine execution\n", tm);
		}
		
		m_state = null;
		m_engaged = false;
	}
	
	/**
	 * MagicComponent API: This is called on each iteration of the
	 * control loop. Most of the time, you will not want to override
	 * this function. If you find you want to, use @DefaultState
	 * instead.
	 */
	@Override
	public void execute() {
		
		double now = m_clock.millis() / 1000.0;
		
		if (!m_engaged) {
			if (m_shouldEngage) {
				m_start = now;
				m_engaged = true;
			} else if (m_defaultState == null) {
				return;
			}
		}
		
		// tm is the number of seconds that the state machine has been executing
		double tm = now - m_start;
		StateData state = m_state;
		
		// we adjust this so that if we have states chained together,
        // then the total time it runs is the amount of time of the
        // states. Otherwise, the time drifts.
		double new_state_start = tm;
		
		// determine if the time has passed to execute the next state
		// -> intentionally comes first
		if (state != null && state.ran && state.expires < tm) {
			new_state_start = state.expires;
			
			if (state.nextState == null) {
				// If the state expires and it's the last state, if the machine
				// is still engaged then it should cycle back to the beginning
				if (m_shouldEngage) {
					nextState(m_firstState);
					state = m_state;
				} else {
					state = null;
				}
			} else {
				nextState(state.nextState);
				state = m_state;
			}
		}
        
		// deactivate the current state unless engage was called
		// or mustFinish was set
        if (state != null && !m_shouldEngage && !state.mustFinish) {
        	state = null;
        }
        
        // if there is no state to execute and there is a default
        // state, do the default state
        if (state == null && m_defaultState != null) {
        	state = m_defaultState;
        	
        	if (m_state != m_defaultState) {
				m_state = m_defaultState;
				m_state.ran = false;
			}
        }
        
        // finally, either execute the state
        if (state != null) {
            // is this the first time this was executed?
            boolean initial_call = !state.ran;
            if (initial_call) {
                state.ran = true;
                state.startTime = new_state_start;
                state.expires = new_state_start + state.duration;
                
                if (m_verboseLogging) {
                	System.out.printf("%.3fs: Entering state: %s\n", tm, state.name);
                }
            }
            
            // execute the state function, passing it the arguments
            state.stateMethod.execute(tm - state.startTime, initial_call);
        } else {
        	// or clear the state
        	done();
        }
        
        // Reset this each time
        m_shouldEngage = false;
	}
}
