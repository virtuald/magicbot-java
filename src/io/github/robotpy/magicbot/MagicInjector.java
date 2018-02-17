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

import java.lang.reflect.Field;

class MagicInjector {

	static public void inject(Object source, Object to, String toName) {
		
		Class<?> toClass = to.getClass();
		Field[] fields = toClass.getDeclaredFields();
		
		for (Field injectField: fields) {
			if (injectField.isAnnotationPresent(MagicInject.class)) {
				
				// try to find the field in the source object
				String injectFieldName = injectField.getName();
				Field sourceField = null;
				
				try {
					sourceField = source.getClass().getDeclaredField(injectFieldName);
				} catch (NoSuchFieldError | NoSuchFieldException | SecurityException ex1) {
					try {
						if (toName != null) {
							sourceField = source.getClass().getDeclaredField(toName + injectFieldName);
						}
					} catch (NoSuchFieldError | NoSuchFieldException | SecurityException ex2) {
						
					}
				}
				
				if (sourceField == null) {
					throw new RuntimeException("Field + " + injectFieldName + " in " + toName + " does not exist in" + toClass);
				}
				
				sourceField.setAccessible(true);
				injectField.setAccessible(true);
				
				try {
					injectField.set(to, sourceField.get(source));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException("Error setting field + " + injectFieldName + " in " + toName, e);
				}
			}
		}
	}
	
	static public void injectChildren(Object parent, Object source) {
		Field [] fields = parent.getClass().getDeclaredFields();
		for (Field sourceField: fields) {
			sourceField.setAccessible(true);
			Object o = null;
			try {
				o = sourceField.get(parent);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// empty
			}
			
			if (o != null) {
				inject(source, o, null);
			}
		}
	}
}
