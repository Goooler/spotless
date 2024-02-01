/*
 * Copyright 2024 DiffPlug
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.diffplug.spotless.java;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.diffplug.spotless.FormatterFunc;
import com.diffplug.spotless.FormatterStep;
import com.diffplug.spotless.RoundedStep;

/**
 * A step to disable wildcard imports.
 */
public class DisableStarImportsStep implements RoundedStep {
	private static final long serialVersionUID = 1L;
	private static final String NAME = "DisableStarImports";

	private DisableStarImportsStep() {}

	public static FormatterStep create() {
		return FormatterStep.create(NAME,
				new State(),
				DisableStarImportsStep.State::toFormatter);
	}

	private static final class State implements Serializable {
		private static final long serialVersionUID = 1L;
		private static final Pattern IMPORT_PATTERN = Pattern.compile("import\\s+[^\\*\\s]+\\*;(\\r\\n|\\r|\\n)");

		FormatterFunc toFormatter() {
			return (FormatterFunc.NeedsFile) (unix, file) -> {
				Matcher matcher = IMPORT_PATTERN.matcher(unix);
				if (matcher.find()) {
					throw new IllegalStateException("Wildcard imports" + matcher.group() + "are found in " + file);
				}
				return unix;
			};
		}
	}
}
