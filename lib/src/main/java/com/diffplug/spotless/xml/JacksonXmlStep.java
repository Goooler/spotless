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

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import com.diffplug.spotless.FormatterFunc;
import com.diffplug.spotless.FormatterStep;
import com.diffplug.spotless.JarState;
import com.diffplug.spotless.Provisioner;

/**
 * Simple XML formatter which reformats the file according to Jackson XMLFactory.
 */
// https://stackoverflow.com/questions/14515994/convert-json-string-to-pretty-print-json-output-using-jackson
// https://stackoverflow.com/questions/60891174/i-want-to-load-a-yaml-file-possibly-edit-the-data-and-then-dump-it-again-how
public class JacksonXmlStep {
	static final String MAVEN_COORDINATE = "com.fasterxml.jackson.dataformat:jackson-dataformat-xml:";
	// https://mvnrepository.com/artifact/com.fasterxml.jackson.dataformat/jackson-dataformat-xml
	static final String DEFAULT_VERSION = "2.14.1";

	private JacksonXmlStep() {}

	public static String defaultVersion() {
		return DEFAULT_VERSION;
	}

	public static FormatterStep create(JacksonXmlConfig jacksonConfig,
			String jacksonVersion,
			Provisioner provisioner) {
		Objects.requireNonNull(jacksonConfig, "jacksonConfig cannot be null");
		Objects.requireNonNull(provisioner, "provisioner cannot be null");
		return FormatterStep.createLazy("xml",
				() -> new State(jacksonConfig, jacksonVersion, provisioner),
				State::toFormatter);
	}

	public static FormatterStep create(Provisioner provisioner) {
		return create(new JacksonXmlConfig(), defaultVersion(), provisioner);
	}

	private static final class State implements Serializable {
		private static final long serialVersionUID = 1L;

		private final JacksonXmlConfig jacksonConfig;

		private final JarState jarState;

		private State(JacksonXmlConfig jacksonConfig,
				String jacksonVersion,
				Provisioner provisioner) throws IOException {
			this.jacksonConfig = jacksonConfig;

			this.jarState = JarState.from(JacksonXmlStep.MAVEN_COORDINATE + jacksonVersion, provisioner);
		}

		FormatterFunc toFormatter() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
				InstantiationException, IllegalAccessException {
			Class<?> formatterFunc = jarState.getClassLoader().loadClass("com.diffplug.spotless.glue.xml.JacksonXmlFormatterFunc");
			Constructor<?> constructor = formatterFunc.getConstructor(JacksonXmlConfig.class);
			return (FormatterFunc) constructor.newInstance(jacksonConfig);
		}
	}
}
