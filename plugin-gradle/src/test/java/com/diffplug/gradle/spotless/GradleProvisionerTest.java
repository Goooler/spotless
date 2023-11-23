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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class GradleProvisionerTest extends GradleIntegrationHarness {
	private static final String JUPITER_COORDINATE = "org.junit.jupiter:junit-jupiter:5.10.0";
	private static final String JUPITER_TRANSITIVE = "junit-jupiter-params-5.10.0.jar, junit-jupiter-engine-5.10.0.jar, junit-jupiter-api-5.10.0.jar, junit-platform-engine-1.10.0.jar, junit-platform-commons-1.10.0.jar, junit-jupiter-5.10.0.jar, opentest4j-1.3.0.jar";
	private static final String JUPITER_INTRANSITIVE = "junit-jupiter-5.10.0.jar";

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	void canResolveMavenCoordinates(boolean withTransitives) throws IOException {
		String output = resolveDeps(withTransitives, "'" + JUPITER_COORDINATE + "'");
		assertThat(output).contains(withTransitives ? JUPITER_TRANSITIVE : JUPITER_INTRANSITIVE);
	}

	private String resolveDeps(boolean withTransitives, Object dep) throws IOException {
		setFile("build.gradle").toLines(
				"import com.diffplug.gradle.spotless.GradleProvisioner",
				"plugins {",
				"    id 'com.diffplug.spotless'",
				"}",
				"repositories { mavenCentral() }",
				"tasks.register('resolveDeps') {",
				"    def files = GradleProvisioner.forProject(project)",
				String.format(".provisionWithTransitives(%s, [%s])", withTransitives, dep),
				"    println files.collect(File::getName).join(', ')",
				"}");
		return gradleRunner().withArguments("resolveDeps").build().getOutput();
	}
}
