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
package com.diffplug.spotless.maven.xml;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diffplug.spotless.maven.MavenIntegrationHarness;

public class XmlTest extends MavenIntegrationHarness {
	private static final Logger LOGGER = LoggerFactory.getLogger(XmlTest.class);

	@Test
	public void testFormatXml_WithJackson_defaultConfig_separatorComments() throws Exception {
		writePomWithXmlSteps("<jackson/>");

		setFile("xml_test.xml").toResource("xml/separator_comments.xml");
		mavenRunner().withArguments("spotless:apply").runNoError();
		assertFile("xml_test.xml").sameAsResource("xml/separator_comments.clean.xml");
	}

	@Test
	public void testFormatXml_WithJackson_defaultConfig_arrayBrackets() throws Exception {
		writePomWithXmlSteps("<jackson/>");

		setFile("xml_test.xml").toResource("xml/array_with_bracket.xml");
		mavenRunner().withArguments("spotless:apply").runNoError();
		assertFile("xml_test.xml").sameAsResource("xml/array_with_bracket.clean.xml");
	}

	@Test
	public void testFormatXml_WithJackson_defaultConfig_multipleDocuments() throws Exception {
		writePomWithXmlSteps("<jackson/>");

		setFile("xml_test.xml").toResource("xml/multiple_documents.xml");
		mavenRunner().withArguments("spotless:apply").runNoError();
		assertFile("xml_test.xml").sameAsResource("xml/multiple_documents.clean.jackson.xml");
	}
}
