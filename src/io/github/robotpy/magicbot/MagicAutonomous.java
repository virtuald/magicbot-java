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
 * Must be implemented by all autonomous modes used with Magicbot
 */
public interface MagicAutonomous {
	/**
	 * Called when autonomous mode is initially enabled
	 */
	default void onEnabled() {
		// empty
	};
	
	/**
	 * Called when autonomous mode is no longer active
	 */
	default void onDisabled() {
		// empty
	}
	
	/**
	 * This function is called every 20ms during autonomous mode
	 */
	public void autonomousPeriodic();
}
