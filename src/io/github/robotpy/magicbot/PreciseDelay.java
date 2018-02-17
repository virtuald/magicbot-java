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

import java.util.concurrent.atomic.AtomicInteger;

import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.hal.NotifierJNI;

public class PreciseDelay implements AutoCloseable {

	private final double m_period;
	private double m_expirationTime;
	
	private final AtomicInteger m_notifier = new AtomicInteger();

	/**
	 * @param period Delay time in seconds
	 */
	public PreciseDelay(double period) {
		m_notifier.set(NotifierJNI.initializeNotifier());
		m_period = period;
		m_expirationTime = RobotController.getFPGATime() * 1e-6 + m_period;
		NotifierJNI.updateNotifierAlarm(m_notifier.get(), (long) (m_expirationTime * 1e6));
	}
	
	/**
	 * Delay for a fixed period of time
	 */
	public void delay() {
		
		int notifier = m_notifier.get();
		if (notifier == 0) {
			throw new RuntimeException("Cannot use PreciseDelay object after closing it");
		}
		
		NotifierJNI.waitForNotifierAlarm(notifier);
		
		// update the wait period
		m_expirationTime += m_period;
		NotifierJNI.updateNotifierAlarm(notifier, (long) (m_expirationTime * 1e6));
	}
	
	@Override
	public void close() {
		int handle = m_notifier.getAndSet(0);
		if (handle != 0) {
			NotifierJNI.stopNotifier(handle);
			NotifierJNI.cleanNotifier(handle);
		}
	}
}
