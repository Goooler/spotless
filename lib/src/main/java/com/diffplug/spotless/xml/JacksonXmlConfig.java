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
package com.diffplug.spotless.xml;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.diffplug.spotless.json.JacksonConfig;

/**
 * Specialization of {@link JacksonConfig} for XML documents
 */
public class JacksonXmlConfig extends JacksonConfig {
	private static final long serialVersionUID = 1L;

	protected Map<String, Boolean> xmlFeatureToToggle = new LinkedHashMap<>();

	public Map<String, Boolean> getXmlFeatureToToggle() {
		return Collections.unmodifiableMap(xmlFeatureToToggle);
	}

	/**
	 * Refers to com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator.Feature
	 */
	public void setXmlFeatureToToggle(Map<String, Boolean> xmlFeatureToToggle) {
		this.xmlFeatureToToggle = xmlFeatureToToggle;
	}

	/**
	 * Refers to com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator.Feature
	 */
	public void appendXmlFeatureToToggle(Map<String, Boolean> features) {
		this.xmlFeatureToToggle.putAll(features);
	}
}
