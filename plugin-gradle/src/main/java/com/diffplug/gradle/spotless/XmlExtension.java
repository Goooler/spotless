/*
 * Copyright 2023 DiffPlug
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

import javax.inject.Inject;

import com.diffplug.spotless.FormatterStep;
import com.diffplug.spotless.xml.JacksonXmlConfig;
import com.diffplug.spotless.xml.JacksonXmlStep;

public class XmlExtension extends FormatExtension {
	static final String NAME = "xml";

	@Inject
	public XmlExtension(SpotlessExtension spotless) {
		super(spotless);
	}

	@Override
	protected void setupTask(SpotlessTask task) {
		if (target == null) {
			throw noDefaultTargetException();
		}
		super.setupTask(task);
	}

	public JacksonXmlGradleConfig jackson() {
		return new JacksonXmlGradleConfig(this);
	}

	public class JacksonXmlGradleConfig extends AJacksonGradleConfig<JacksonXmlGradleConfig> {
		protected JacksonXmlConfig jacksonConfig;

		public JacksonXmlGradleConfig(JacksonXmlConfig jacksonConfig, FormatExtension formatExtension) {
			super(jacksonConfig, formatExtension);

			this.jacksonConfig = jacksonConfig;

			formatExtension.addStep(createStep());
		}

		public JacksonXmlGradleConfig(FormatExtension formatExtension) {
			this(new JacksonXmlConfig(), formatExtension);
		}

		/**
		 * Refers to com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
		 */
		public JacksonXmlGradleConfig xmlFeature(String feature, boolean toggle) {
			this.jacksonConfig.appendXmlFeatureToToggle(Collections.singletonMap(feature, toggle));
			formatExtension.replaceStep(createStep());
			return this;
		}

		@Override
		public JacksonXmlGradleConfig self() {
			return this;
		}

		// 'final' as it is called in the constructor
		@Override
		protected final FormatterStep createStep() {
			return JacksonXmlStep.create(jacksonConfig, version, provisioner());
		}
	}
}
