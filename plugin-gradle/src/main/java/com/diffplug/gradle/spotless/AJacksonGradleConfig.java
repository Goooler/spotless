/*
 * Copyright 2023-2024 DiffPlug
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
package com.diffplug.gradle.spotless;

import java.util.Collections;

import com.diffplug.spotless.FormatterStep;
import com.diffplug.spotless.json.JacksonConfig;
import com.diffplug.spotless.json.JacksonJsonStep;

public abstract class AJacksonGradleConfig<JGC extends AJacksonGradleConfig<JGC, JC>, JC extends JacksonConfig> {
	protected final FormatExtension formatExtension;

	protected JC jacksonConfig;

	protected String version = JacksonJsonStep.defaultVersion();

	// Make sure to call 'formatExtension.addStep(createStep());' in the extented constructors
	public AJacksonGradleConfig(JC jacksonConfig, FormatExtension formatExtension) {
		this.formatExtension = formatExtension;
		this.jacksonConfig = jacksonConfig;
	}

	public JGC feature(String feature, boolean toggle) {
		this.jacksonConfig.appendFeatureToToggle(Collections.singletonMap(feature, toggle));
		formatExtension.replaceStep(createStep());
		//noinspection unchecked
		return (JGC) this;
	}

	public JGC version(String version) {
		this.version = version;
		formatExtension.replaceStep(createStep());
		//noinspection unchecked
		return (JGC) this;
	}

	protected abstract FormatterStep createStep();
}
