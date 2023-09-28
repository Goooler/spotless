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
package com.diffplug.spotless.glue.xml;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlFactoryBuilder;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import com.diffplug.spotless.glue.json.AJacksonFormatterFunc;
import com.diffplug.spotless.xml.JacksonXmlConfig;

public class JacksonXmlFormatterFunc extends AJacksonFormatterFunc {
	final JacksonXmlConfig xmlConfig;

	public JacksonXmlFormatterFunc(JacksonXmlConfig jacksonConfig) {
		super(jacksonConfig);
		this.xmlConfig = jacksonConfig;

		if (jacksonConfig == null) {
			throw new IllegalArgumentException("ARG");
		}
	}

	protected JsonFactory makeJsonFactory() {
		XmlFactoryBuilder xmlFactoryBuilder = new XmlFactoryBuilder(new XmlFactory());

		// Configure the ObjectMapper
		// https://github.com/FasterXML/jackson-databind#commonly-used-features
		xmlConfig.getXmlFeatureToToggle().forEach((rawFeature, toggle) -> {
			// https://stackoverflow.com/questions/3735927/java-instantiating-an-enum-using-reflection
			ToXmlGenerator.Feature feature = ToXmlGenerator.Feature.valueOf(rawFeature);

			xmlFactoryBuilder.configure(feature, toggle);
		});

		return xmlFactoryBuilder.build();
	}

	@Override
	protected Class<?> inferType(String input) {
		return JsonNode.class;
	}

	@Override
	protected String format(ObjectMapper objectMapper, String input) throws IllegalArgumentException, IOException {
		try {
			// https://stackoverflow.com/questions/25222327/deserialize-pojos-from-multiple-yaml-documents-in-a-single-file-in-jackson
			// https://github.com/FasterXML/jackson-dataformats-text/issues/66#issuecomment-375328648
			JsonParser xmlParser = objectMapper.getFactory().createParser(input);
			List<?> documents = objectMapper.readValues(xmlParser, inferType(input)).readAll();

			// https://github.com/FasterXML/jackson-dataformats-text/issues/66#issuecomment-554265055
			// https://github.com/FasterXML/jackson-dataformats-text/issues/66#issuecomment-554265055
			StringWriter stringWriter = new StringWriter();
			objectMapper.writer().writeValues(stringWriter).writeAll(documents).close();
			return stringWriter.toString();
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("Unable to format. input='" + input + "'", e);
		}
	}
}
