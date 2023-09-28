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

import java.io.IOException;

import org.junit.jupiter.api.Test;

class XmlExtensionTest extends GradleIntegrationHarness {
	@Test
	void testFormatXml_WithJackson_defaultConfig_separatorComments() throws IOException {
		setFile("build.gradle").toLines(
				"plugins {",
				"    id 'java'",
				"    id 'com.diffplug.spotless'",
				"}",
				"repositories { mavenCentral() }",
				"spotless {",
				"    xml {",
				"        target 'src/**/*.xml'",
				"        jackson()",
				"    }",
				"}");
		setFile("src/main/resources/example.xml").toResource("xml/separator_comments.xml");
		gradleRunner().withArguments("spotlessApply").build();
		assertFile("src/main/resources/example.xml").sameAsResource("xml/separator_comments.clean.xml");
	}

	// see ToXmlGenerator.Feature.WRITE_DOC_START_MARKER
	@Test
	void testFormatXml_WithJackson_skipDocStartMarker() throws IOException {
		setFile("build.gradle").toLines(
				"plugins {",
				"    id 'java'",
				"    id 'com.diffplug.spotless'",
				"}",
				"repositories { mavenCentral() }",
				"spotless {",
				"    xml {",
				"        target 'src/**/*.xml'",
				"        jackson()",
				"	        .xmlFeature('WRITE_DOC_START_MARKER', false)",
				"	        .xmlFeature('MINIMIZE_QUOTES', true)",
				"    }",
				"}");
		setFile("src/main/resources/example.xml").toResource("xml/array_with_bracket.xml");
		gradleRunner().withArguments("spotlessApply", "--stacktrace").build();
		assertFile("src/main/resources/example.xml").sameAsResource("xml/array_with_bracket.clean.no_start_marker.no_quotes.xml");
	}

	@Test
	void testFormatXml_WithJackson_multipleDocuments() throws IOException {
		setFile("build.gradle").toLines(
				"plugins {",
				"    id 'java'",
				"    id 'com.diffplug.spotless'",
				"}",
				"repositories { mavenCentral() }",
				"spotless {",
				"    xml {",
				"        target 'src/**/*.xml'",
				"        jackson()",
				"    }",
				"}");
		setFile("src/main/resources/example.xml").toResource("xml/multiple_documents.xml");
		gradleRunner().withArguments("spotlessApply", "--stacktrace").build();
		assertFile("src/main/resources/example.xml").sameAsResource("xml/multiple_documents.clean.jackson.xml");
	}

	@Test
	void testFormatXml_WithJackson_arrayAtRoot() throws IOException {
		setFile("build.gradle").toLines(
				"plugins {",
				"    id 'java'",
				"    id 'com.diffplug.spotless'",
				"}",
				"repositories { mavenCentral() }",
				"spotless {",
				"    xml {",
				"        target 'src/**/*.xml'",
				"        jackson()",
				"    }",
				"}");
		setFile("src/main/resources/example.xml").toResource("xml/array_at_root.xml");
		gradleRunner().withArguments("spotlessApply", "--stacktrace").build();
		assertFile("src/main/resources/example.xml").sameAsResource("xml/array_at_root.clean.xml");
	}

}
