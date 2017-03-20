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

package io.github.robotpy.magicbot;

/**
 * Must be implemented by all components used with Magicbot
 */
public interface MagicComponent {

	/**
	 * Called when the robot enters autonomous or teleoperated mode. This
	 * function should initialize your component to a "safe" state so that
	 * unexpected things don't happen when enabling the robot.
	 * 
	 * Note: There isn't a separate initialization function for autonomous
	 *       and teleoperated modes. This is intentional, as they should be the
	 *       same.
	 */
	default void onEnabled() {
		// empty
	};
	
	/**
	 * Called when the robot leaves autonomous or teleoperated mode
	 */
	default void onDisabled() {
		// empty
	}
	
	/**
	 * This function is called at the end of the control loop
	 */
	public void execute();
}
