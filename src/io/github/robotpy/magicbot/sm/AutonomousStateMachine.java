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

import io.github.robotpy.magicbot.MagicAutonomous;

/**
 * This is a specialized version of the StateMachine that is designed
 * to be used as an autonomous mode. There are a few key differences:
 * 
 * - The engage function is always called, so the state machine
 *   will always run to completion unless done() is called
 * - Messages will always be printed out upon each state transition
 */
public class AutonomousStateMachine extends StateMachine implements MagicAutonomous {

	private boolean m_engaged = false;
	
	@Override
	public void onEnabled() {
		super.onEnabled();
		m_engaged = true;
		m_verboseLogging = true;
	}
	
	public void autonomousPeriodic() {
		
		// Only engage the state machine until its execution finishes, otherwise
        // it will just keep repeating
        //
        // This is because if you keep calling engage(), the state machine will
        // loop. I'm tempted to change that, but I think it would lead to unexpected
        // side effects. Will have to contemplate this...
		
		if (m_engaged) {
			engage();
			execute();
			m_engaged = isExecuting();
		}
	}
}
