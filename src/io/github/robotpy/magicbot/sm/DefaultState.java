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
 * If this annotation is applied to a method in an object that inherits
 * from :class:`.StateMachine`, it indicates that the function
 * is a default state; that is, if no other states are executing, this
 * state will execute.  If the state machine is always executing, the
 * default state will never execute.
 *
 * There can only be a single default state in a StateMachine object.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultState {
}
