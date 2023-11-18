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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Consumer;

import javax.inject.Inject;

import com.diffplug.spotless.FileSignature;
import com.diffplug.spotless.FormatterStep;
import com.diffplug.spotless.kotlin.DiktatStep;
import com.diffplug.spotless.kotlin.KtLintStep;
import com.diffplug.spotless.kotlin.KtfmtStep;
import com.diffplug.spotless.kotlin.KtfmtStep.KtfmtFormattingOptions;
import com.diffplug.spotless.kotlin.KtfmtStep.Style;

public class KotlinGradleExtension extends BaseKotlinExtension {
	private static final String GRADLE_KOTLIN_DSL_FILE_EXTENSION = "*.gradle.kts";

	static final String NAME = "kotlinGradle";

	@Inject
	public KotlinGradleExtension(SpotlessExtension spotless) {
		super(spotless);
	}

	/** Adds the specified version of <a href="https://github.com/pinterest/ktlint">ktlint</a>. */
	public KtlintConfig ktlint(String version) throws IOException {
		Objects.requireNonNull(version, "version");
		File defaultEditorConfig = getProject().getRootProject().file(".editorconfig");
		FileSignature editorConfigPath = defaultEditorConfig.exists() ? FileSignature.signAsList(defaultEditorConfig) : null;
		return new KtlintConfig(version, true, editorConfigPath, Collections.emptyMap(), Collections.emptyMap());
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
		Objects.requireNonNull(version);
		return new KtfmtConfig(version);
	}

	public class KtfmtConfig {
		final String version;
		Style style;
		KtfmtFormattingOptions options;

		private final ConfigurableStyle configurableStyle = new ConfigurableStyle();

		KtfmtConfig(String version) {
			this.version = Objects.requireNonNull(version);
			this.style = Style.DEFAULT;
			addStep(createStep());
		}

		private ConfigurableStyle style(Style style) {
			this.style = style;
			replaceStep(createStep());
			return configurableStyle;
		}

		public ConfigurableStyle dropboxStyle() {
			return style(Style.DROPBOX);
		}

		public ConfigurableStyle googleStyle() {
			return style(Style.GOOGLE);
		}

		public ConfigurableStyle kotlinlangStyle() {
			return style(Style.KOTLINLANG);
		}

		public void configure(Consumer<KtfmtFormattingOptions> optionsConfiguration) {
			this.configurableStyle.configure(optionsConfiguration);
		}

		private FormatterStep createStep() {
			return KtfmtStep.create(version, provisioner(), style, options);
		}

		public class ConfigurableStyle {

			public void configure(Consumer<KtfmtFormattingOptions> optionsConfiguration) {
				KtfmtFormattingOptions ktfmtFormattingOptions = new KtfmtFormattingOptions();
				optionsConfiguration.accept(ktfmtFormattingOptions);
				options = ktfmtFormattingOptions;
				replaceStep(createStep());
			}
		}
	}

	/** Adds the specified version of <a href="https://github.com/cqfn/diKTat">diktat</a>. */
	public DiktatConfig diktat(String version) {
		Objects.requireNonNull(version, "version");
		return new DiktatConfig(version);
	}

	public DiktatConfig diktat() {
		return diktat(DiktatStep.defaultVersionDiktat());
	}

	public class DiktatConfig {

		private final String version;
		private FileSignature config;

		DiktatConfig(String version) {
			this.version = version;
			addStep(createStep());
		}

		public DiktatConfig configFile(Object file) throws IOException {
			// Specify the path to the configuration file
			if (file == null) {
				this.config = null;
			} else {
				this.config = FileSignature.signAsList(getProject().file(file));
			}
			replaceStep(createStep());
			return this;
		}

		private FormatterStep createStep() {
			return DiktatStep.createForScript(version, provisioner(), config);
		}
	}

	@Override
	protected void setupTask(SpotlessTask task) {
		if (target == null) {
			target = parseTarget(GRADLE_KOTLIN_DSL_FILE_EXTENSION);
		}
		super.setupTask(task);
	}
}
