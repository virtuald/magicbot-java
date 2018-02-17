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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * If this decorator is applied to a function in an object that inherits
 * from :class:`.StateMachine`, it indicates that the function
 * is a state that will run for a set amount of time unless interrupted
 *
 * It is guaranteed that a TimedState will execute at least once, even if
 * it expires prior to being executed
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TimedState {
	
	/** The length of time to run the state before progressing
        to the next state */
	double duration() default -1.0;
	
	/** The name of the next state. If not specified, then
	 *  this will be the last state executed if time expires
	 */
    String nextState() default "";

    /** If set, this state function will be ran first */
    boolean first() default false;
    
    /**
     *  If set, this state will continue executing even if engage
     *  is not called. However, if `done` is called, execution will
     *  stop regardless of whether this is set
     */
    boolean mustFinish() default false;
}
