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

import static org.junit.Assert.*;

import org.junit.Test;

public class InjectorTest {
	
	class InjectFrom {
		int theField = 42;
	}
	
	class InjectTo {
		@MagicInject
		int theField = 4;
		
		public int getTheField() {
			return theField;
		}
	}
	
	@Test
	public void testInjection() {
		
		InjectFrom from = new InjectFrom();
		InjectTo to = new InjectTo();
		
		assertEquals(4, to.getTheField());
		
		MagicInjector.inject(from, to, "ignored");
		
		assertEquals(42, to.getTheField());
		
	}

}
