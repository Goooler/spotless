/*
 * Copyright 2016-2023 DiffPlug
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
import java.util.Collections;

import javax.inject.Inject;

import com.diffplug.spotless.kotlin.DiktatStep;
import com.diffplug.spotless.kotlin.KtLintStep;
import com.diffplug.spotless.kotlin.KtfmtStep;

public class KotlinGradleExtension extends BaseKotlinExtension {
	private static final String GRADLE_KOTLIN_DSL_FILE_EXTENSION = "*.gradle.kts";

	static final String NAME = "kotlinGradle";

	@Inject
	public KotlinGradleExtension(SpotlessExtension spotless) {
		super(spotless);
	}

	/** Adds the specified version of <a href="https://github.com/pinterest/ktlint">ktlint</a>. */
	public KtlintConfig ktlint(String version) throws IOException {
		return new KtlintConfig(version, true, Collections.emptyMap(), Collections.emptyMap());
	}

	public KtlintConfig ktlint() throws IOException {
		return ktlint(KtLintStep.defaultVersion());
	}

	/** Uses the <a href="https://github.com/facebookincubator/ktfmt">ktfmt</a> jar to format source code. */
	public KtfmtConfig ktfmt() {
		return ktfmt(KtfmtStep.defaultVersion());
	}

	/**
	 * Uses the given version of <a href="https://github.com/facebookincubator/ktfmt">ktfmt</a> to format source
	 * code.
	 */
	public KtfmtConfig ktfmt(String version) {
		return new KtfmtConfig(version);
	}

	/** Adds the specified version of <a href="https://github.com/cqfn/diKTat">diktat</a>. */
	public DiktatConfig diktat(String version) {
		return new DiktatConfig(version, true);
	}

	public DiktatConfig diktat() {
		return diktat(DiktatStep.defaultVersionDiktat());
	}

	@Override
	protected void setupTask(SpotlessTask task) {
		if (target == null) {
			target = parseTarget(GRADLE_KOTLIN_DSL_FILE_EXTENSION);
		}
		super.setupTask(task);
	}
}
