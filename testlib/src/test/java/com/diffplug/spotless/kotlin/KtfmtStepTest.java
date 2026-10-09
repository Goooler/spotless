/*
 * Copyright 2016-2026 DiffPlug
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
package com.diffplug.spotless.kotlin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.diffplug.spotless.Formatter;
import com.diffplug.spotless.FormatterStep;
import com.diffplug.spotless.ResourceHarness;
import com.diffplug.spotless.SerializableEqualityTester;
import com.diffplug.spotless.StepHarness;
import com.diffplug.spotless.StepHarnessWithFile;
import com.diffplug.spotless.TestProvisioner;

class KtfmtStepTest extends ResourceHarness {
	@Test
	void behavior() throws Exception {
		FormatterStep step = KtfmtStep.create(TestProvisioner.mavenCentral());
		StepHarness.forStep(step).testResource("kotlin/ktfmt/basic.dirty", "kotlin/ktfmt/basic.clean");
	}

	@Test
	void behaviorWithOptions() {
		KtfmtStep.KtfmtFormattingOptions options = new KtfmtStep.KtfmtFormattingOptions();
		options.setMaxWidth(100);
		FormatterStep step = KtfmtStep.create(KtfmtStep.defaultVersion(), TestProvisioner.mavenCentral(), KtfmtStep.Style.GOOGLE, options);
		StepHarness.forStep(step).testResource("kotlin/ktfmt/basic.dirty", "kotlin/ktfmt/basic.clean");
	}

	@Test
	void behaviorWithOptions_0_61() {
		KtfmtStep.KtfmtFormattingOptions options = new KtfmtStep.KtfmtFormattingOptions();
		options.setMaxWidth(100);
		FormatterStep step = KtfmtStep.create("0.61", TestProvisioner.mavenCentral(), KtfmtStep.Style.GOOGLE, options);
		StepHarness.forStep(step).testResource("kotlin/ktfmt/basic.dirty", "kotlin/ktfmt/basic.clean");
	}

	@Test
	void behavior_0_62() throws Exception {
		FormatterStep step = KtfmtStep.create("0.62", TestProvisioner.mavenCentral());
		StepHarness.forStep(step).testResource("kotlin/ktfmt/basic.dirty", "kotlin/ktfmt/basic.clean");
	}

	@Test
	void behaviorWithOptions_0_62() {
		KtfmtStep.KtfmtFormattingOptions options = new KtfmtStep.KtfmtFormattingOptions();
		options.setMaxWidth(100);
		FormatterStep step = KtfmtStep.create("0.62", TestProvisioner.mavenCentral(), KtfmtStep.Style.GOOGLE, options);
		StepHarness.forStep(step).testResource("kotlin/ktfmt/basic.dirty", "kotlin/ktfmt/basic.clean");
	}

	@Test
	void behavior_0_64() throws Exception {
		FormatterStep step = KtfmtStep.create("0.64", TestProvisioner.mavenCentral());
		StepHarness.forStep(step).testResource("kotlin/ktfmt/basic.dirty", "kotlin/ktfmt/basic.clean");
	}

	@Test
	void behaviorWithOptions_0_64() {
		KtfmtStep.KtfmtFormattingOptions options = new KtfmtStep.KtfmtFormattingOptions();
		options.setMaxWidth(100);
		FormatterStep step = KtfmtStep.create("0.64", TestProvisioner.mavenCentral(), KtfmtStep.Style.GOOGLE, options);
		StepHarness.forStep(step).testResource("kotlin/ktfmt/basic.dirty", "kotlin/ktfmt/basic.clean");
	}

	@Test
	void behaviorWithOptions_0_53() {
		KtfmtStep.KtfmtFormattingOptions options = new KtfmtStep.KtfmtFormattingOptions();
		options.setMaxWidth(100);
		FormatterStep step = KtfmtStep.create("0.53", TestProvisioner.mavenCentral(), KtfmtStep.Style.GOOGLE, options);
		StepHarness.forStep(step).testResource("kotlin/ktfmt/basic.dirty", "kotlin/ktfmt/basic.clean");
	}

	@Test
	void behaviorWithOptions_0_56() {
		KtfmtStep.KtfmtFormattingOptions options = new KtfmtStep.KtfmtFormattingOptions();
		options.setMaxWidth(100);
		FormatterStep step = KtfmtStep.create("0.56", TestProvisioner.mavenCentral(), KtfmtStep.Style.GOOGLE, options);
		StepHarness.forStep(step).testResource("kotlin/ktfmt/basic.dirty", "kotlin/ktfmt/basic.clean");
	}

	@Test
	@EnabledForJreRange(max = JRE.JAVA_24)
	void dropboxStyle_0_16() throws Exception {
		KtfmtStep.KtfmtFormattingOptions options = new KtfmtStep.KtfmtFormattingOptions();
		FormatterStep step = KtfmtStep.create("0.16", TestProvisioner.mavenCentral(), KtfmtStep.Style.DROPBOX, options);
		StepHarness.forStep(step).testResource("kotlin/ktfmt/basic.dirty", "kotlin/ktfmt/basic-dropboxstyle.clean");
	}

	@Test
	@EnabledForJreRange(max = JRE.JAVA_24)
	void dropboxStyle_0_18() throws Exception {
		KtfmtStep.KtfmtFormattingOptions options = new KtfmtStep.KtfmtFormattingOptions();
		FormatterStep step = KtfmtStep.create("0.18", TestProvisioner.mavenCentral(), KtfmtStep.Style.DROPBOX, options);
		StepHarness.forStep(step).testResource("kotlin/ktfmt/basic.dirty", "kotlin/ktfmt/basic-dropboxstyle.clean");
	}

	@Test
	@EnabledForJreRange(max = JRE.JAVA_24)
	void dropboxStyle_0_22() throws Exception {
		KtfmtStep.KtfmtFormattingOptions options = new KtfmtStep.KtfmtFormattingOptions();
		FormatterStep step = KtfmtStep.create("0.22", TestProvisioner.mavenCentral(), KtfmtStep.Style.DROPBOX, options);
		StepHarness.forStep(step).testResource("kotlin/ktfmt/basic.dirty", "kotlin/ktfmt/basic-dropboxstyle.clean");
	}

	@Test
	void dropboxStyle_0_50() throws Exception {
		KtfmtStep.KtfmtFormattingOptions options = new KtfmtStep.KtfmtFormattingOptions();
		FormatterStep step = KtfmtStep.create("0.50", TestProvisioner.mavenCentral(), KtfmtStep.Style.DROPBOX, options);
		StepHarness.forStep(step).testResource("kotlin/ktfmt/basic.dirty", "kotlin/ktfmt/basic-dropboxstyle.clean");
	}

	@Test
	void behaviorWithTrailingCommas() throws Exception {
		KtfmtStep.KtfmtFormattingOptions options = new KtfmtStep.KtfmtFormattingOptions();
		options.setTrailingCommaManagementStrategy(KtfmtStep.TrailingCommaManagementStrategy.COMPLETE);
		FormatterStep step = KtfmtStep.create("0.49", TestProvisioner.mavenCentral(), KtfmtStep.Style.DROPBOX, options);
		StepHarness.forStep(step).testResource("kotlin/ktfmt/trailing-commas.dirty", "kotlin/ktfmt/trailing-commas.clean");
	}

	@Test
	void behaviorWithTrailingCommaManagementStrategyOnlyAdd() {
		KtfmtStep.KtfmtFormattingOptions options = new KtfmtStep.KtfmtFormattingOptions();
		options.setTrailingCommaManagementStrategy(KtfmtStep.TrailingCommaManagementStrategy.ONLY_ADD);
		FormatterStep step = KtfmtStep.create("0.58", TestProvisioner.mavenCentral(), KtfmtStep.Style.KOTLINLANG, options);
		StepHarness.forStep(step).testResource(
				"kotlin/ktfmt/trailing-commas-only-add.dirty",
				"kotlin/ktfmt/trailing-commas-only-add.clean");
	}

	@Test
	void trailingCommaManagementStrategyOnlyAddUnsupportedBefore_0_57() {
		KtfmtStep.KtfmtFormattingOptions options = new KtfmtStep.KtfmtFormattingOptions();
		options.setTrailingCommaManagementStrategy(KtfmtStep.TrailingCommaManagementStrategy.ONLY_ADD);
		var step = KtfmtStep.create("0.56", TestProvisioner.mavenCentral(), KtfmtStep.Style.KOTLINLANG, options);

		assertThatThrownBy(() -> StepHarness.forStep(step))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("ONLY_ADD");
	}

	@Test
	void behaviorWithEditorConfig() {
		setFile(".editorconfig").toLines("root = true", "[*.{kt,kts}]", "max_line_length = 120");
		FormatterStep step = KtfmtStep.create(KtfmtStep.defaultVersion(), TestProvisioner.mavenCentral(), null, null, rootFolder());
		StepHarnessWithFile.forStep(this, step).testResource("test.kt", "kotlin/ktfmt/max-width.dirty", "kotlin/ktfmt/max-width.clean");
	}

	@Test
	void behaviorWithEditorConfig_0_61() {
		setFile(".editorconfig").toLines("root = true", "[*.{kt,kts}]", "max_line_length = 120");
		FormatterStep step = KtfmtStep.create("0.61", TestProvisioner.mavenCentral(), null, null, rootFolder());
		StepHarnessWithFile.forStep(this, step).testResource("test.kt", "kotlin/ktfmt/max-width.dirty", "kotlin/ktfmt/max-width.clean");
	}

	@Test
	void optionsTakePrecedenceOverEditorConfig() {
		setFile(".editorconfig").toLines("root = true", "[*.{kt,kts}]", "max_line_length = 40");
		KtfmtStep.KtfmtFormattingOptions options = new KtfmtStep.KtfmtFormattingOptions();
		options.setMaxWidth(120);
		FormatterStep step = KtfmtStep.create(KtfmtStep.defaultVersion(), TestProvisioner.mavenCentral(), KtfmtStep.Style.META, options, rootFolder());
		StepHarnessWithFile.forStep(this, step).testResource("test.kt", "kotlin/ktfmt/max-width.dirty", "kotlin/ktfmt/max-width.clean");
	}

	@Test
	void editorConfigIgnoredWhenDisabled() throws Exception {
		setFile(".editorconfig").toLines("root = true", "[*.{kt,kts}]", "max_line_length = 120");
		File file = setFile("test.kt").toResource("kotlin/ktfmt/max-width.dirty");
		FormatterStep step = KtfmtStep.create(KtfmtStep.defaultVersion(), TestProvisioner.mavenCentral(), KtfmtStep.Style.META, null, null);

		// with the default max width of 100 instead of the `.editorconfig` one, the long line gets wrapped
		String formatted = step.format(getTestResource("kotlin/ktfmt/max-width.dirty"), file);
		assertThat(formatted).isNotEqualTo(getTestResource("kotlin/ktfmt/max-width.clean"));
	}

	@Test
	void findEditorConfigFilesStopsAtRoot() throws IOException {
		setFile(".editorconfig").toLines("root = true", "[*]", "indent_size = 4");
		File middle = setFile("a/.editorconfig").toLines("# comment", "ROOT = TRUE", "[*.kt]", "indent_size = 2");
		File inner = setFile("a/b/.editorconfig").toLines("[*.kt]", "root = true", "max_line_length = 120");
		setFile("a/b/c/placeholder").toContent("");

		assertThat(KtfmtStep.findEditorConfigFiles(new File(rootFolder(), "a/b/c"))).containsExactly(inner, middle);
		assertThat(KtfmtStep.findEditorConfigFiles(new File(rootFolder(), "a/b"))).containsExactly(inner, middle);
	}

	@Test
	void findEditorConfigFilesStopsAtRootWithBom() throws IOException {
		setFile(".editorconfig").toLines("root = true");
		File withBom = setFile("a/.editorconfig").toLines("\uFEFFroot = true", "[*.kt]", "indent_size = 2");

		assertThat(KtfmtStep.findEditorConfigFiles(new File(rootFolder(), "a"))).containsExactly(withBom);
	}

	@Test
	void findEditorConfigFilesToleratesMalformedUtf8() throws IOException {
		setFile(".editorconfig").toLines("root = true");
		File malformed = newFile("a/.editorconfig");
		Files.createDirectories(malformed.toPath().getParent());
		// `\u00E9` encoded in ISO-8859-1, which is malformed UTF-8
		Files.write(malformed.toPath(), "# caf\u00E9\nroot = true\n".getBytes(StandardCharsets.ISO_8859_1));

		assertThat(KtfmtStep.findEditorConfigFiles(new File(rootFolder(), "a"))).containsExactly(malformed);
	}

	@ParameterizedTest
	@ValueSource(strings = {"0.61", "0.65"})
	void editorConfigRequiresFile(String version) {
		FormatterStep step = KtfmtStep.create(version, TestProvisioner.mavenCentral(), null, null, rootFolder());

		assertThatThrownBy(() -> step.format("fun main() {}\n", Formatter.NO_FILE_SENTINEL))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("StepHarnessWithFile");
	}

	@Test
	void editorConfigUnsupportedBefore_0_60() {
		var step = KtfmtStep.create("0.59", TestProvisioner.mavenCentral(), null, null, rootFolder());

		assertThatThrownBy(() -> StepHarness.forStep(step))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("0.60");
	}

	@Test
	void editorConfigChangesAreTracked() {
		FormatterStep withoutFile = KtfmtStep.create(KtfmtStep.defaultVersion(), TestProvisioner.mavenCentral(), null, null, rootFolder());
		// the state is computed lazily, so compute it before the `.editorconfig` changes
		withoutFile.hashCode();

		File editorConfig = setFile(".editorconfig").toLines("root = true", "[*.{kt,kts}]", "max_line_length = 120");
		FormatterStep withFile = KtfmtStep.create(KtfmtStep.defaultVersion(), TestProvisioner.mavenCentral(), null, null, rootFolder());
		withFile.hashCode();
		assertThat(withFile).isNotEqualTo(withoutFile);

		setFile(".editorconfig").toLines("root = true", "[*.{kt,kts}]", "max_line_length = 100");
		// FileSignature caches the signatures until the last modified time changes
		editorConfig.setLastModified(editorConfig.lastModified() + 2000);
		FormatterStep withChangedFile = KtfmtStep.create(KtfmtStep.defaultVersion(), TestProvisioner.mavenCentral(), null, null, rootFolder());
		assertThat(withChangedFile).isNotEqualTo(withFile);
	}

	@Test
	void equality() throws Exception {
		new SerializableEqualityTester() {
			String version = "0.18";
			@Nullable File editorConfigDir = null;

			@Override
			protected void setupTest(API api) {
				// same version == same
				api.areDifferentThan();
				// change the version, and it's different
				version = KtfmtStep.defaultVersion();
				api.areDifferentThan();
				// enable editorconfig, and it's different
				editorConfigDir = rootFolder();
				api.areDifferentThan();

			}

			@Override
			protected FormatterStep create() {
				String finalVersion = this.version;
				return KtfmtStep.create(finalVersion, TestProvisioner.mavenCentral(), null, null, editorConfigDir);
			}
		}.testEquals();
	}
}
