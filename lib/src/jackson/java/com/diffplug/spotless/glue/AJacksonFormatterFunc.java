/*
 * Copyright 2021-2024 DiffPlug
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
package com.diffplug.spotless.glue;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.diffplug.spotless.FormatterFunc;
import com.diffplug.spotless.json.JacksonConfig;

/**
 * A {@link FormatterFunc} based on Jackson library
 */
// https://github.com/FasterXML/jackson-dataformats-text/issues/372
public abstract class AJacksonFormatterFunc<JC extends JacksonConfig> implements FormatterFunc {
	protected final JC jacksonConfig;

	public AJacksonFormatterFunc(JC jacksonConfig) {
		this.jacksonConfig = jacksonConfig;
	}

	@Override
	public String apply(String input) throws Exception {
		ObjectMapper objectMapper = makeObjectMapper();

		return format(objectMapper, input);
	}

	protected String format(ObjectMapper objectMapper, String input) throws IllegalArgumentException, IOException {
		try {
			// ObjectNode is not compatible with SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS
			Object objectNode = objectMapper.readValue(input, inferType(input));
			String output = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode);

			return output;
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("Unable to format. input='" + input + "'", e);
		}
	}

	/**
	 *
	 * @param input
	 * @return the {@link Class} into which the String has to be deserialized
	 */
	protected abstract Class<?> inferType(String input);

	protected ObjectMapper makeObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();

		objectMapper.setDefaultPrettyPrinter(makePrettyPrinter());

		// Configure the ObjectMapper
		// https://github.com/FasterXML/jackson-databind#commonly-used-features
		jacksonConfig.getFeatureToToggle().forEach((rawFeature, toggle) -> {
			Object feature;
			try {
				feature = SerializationFeature.valueOf(rawFeature);
			} catch (IllegalArgumentException e) {
				try {
					feature = DeserializationFeature.valueOf(rawFeature);
				} catch (IllegalArgumentException e2) {
					try {
						feature = MapperFeature.valueOf(rawFeature);
					} catch (IllegalArgumentException e3) {
						try {
							feature = JsonParser.Feature.valueOf(rawFeature);
						} catch (IllegalArgumentException e4) {
							try {
								feature = JsonGenerator.Feature.valueOf(rawFeature);
							} catch (IllegalArgumentException e5) {
								throw new IllegalArgumentException("Unknown feature: " + rawFeature);
							}
						}
					}
				}
			}
			if (feature instanceof SerializationFeature) {
				objectMapper.configure((SerializationFeature) feature, toggle);
			} else if (feature instanceof DeserializationFeature) {
				objectMapper.configure((DeserializationFeature) feature, toggle);
			} else if (feature instanceof MapperFeature) {
				objectMapper.configure((MapperFeature) feature, toggle);
			} else if (feature instanceof JsonParser.Feature) {
				objectMapper.configure((JsonParser.Feature) feature, toggle);
			} else if (feature instanceof JsonGenerator.Feature) {
				objectMapper.configure((JsonGenerator.Feature) feature, toggle);
			}
		});

		return objectMapper;
	}

	protected PrettyPrinter makePrettyPrinter() {
		return new DefaultPrettyPrinter();
	}
}
