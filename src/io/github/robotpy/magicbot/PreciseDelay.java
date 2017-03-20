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

import edu.wpi.first.wpilibj.Timer;

public class PreciseDelay {

	private final double m_period;
	private double m_nextDelay;

	/**
	 * @param period Delay time in seconds
	 */
	public PreciseDelay(double period) {
		m_period = period;
		m_nextDelay = Timer.getFPGATimestamp() + m_period;
	}
	
	/**
	 * Delay for a fixed period of time
	 */
	public void delay() {
		
		// we must always yield here, just in case
		Timer.delay(0.0002);
		
		double delayPeriod = m_nextDelay - Timer.getFPGATimestamp();
		if (delayPeriod > 0.0001) {
			Timer.delay(delayPeriod);
		}
		
		m_nextDelay += m_period;
	}
}
