/*
 * Copyright 2022-2026 DiffPlug
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
package com.diffplug.spotless.glue.ktfmt;

import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.kotlinx.ktfmt.cli.EditorConfigResolver;
import org.jetbrains.kotlinx.ktfmt.format.FileType;
import org.jetbrains.kotlinx.ktfmt.format.Formatter;
import org.jetbrains.kotlinx.ktfmt.format.FormattingOptions;
import org.jetbrains.kotlinx.ktfmt.format.KotlinCode;

import com.diffplug.spotless.FormatterFunc;

public final class KtfmtFormatterFunc implements FormatterFunc {

	@Nonnull
	private final KtfmtStyle style;

	@Nullable private final KtfmtFormattingOptions ktfmtFormattingOptions;

	private final boolean enableEditorConfig;

	public KtfmtFormatterFunc() {
		this(KtfmtStyle.META, null);
	}

	public KtfmtFormatterFunc(@Nonnull KtfmtStyle style) {
		this(style, null);
	}

	public KtfmtFormatterFunc(@Nullable KtfmtFormattingOptions ktfmtFormattingOptions) {
		this(KtfmtStyle.META, ktfmtFormattingOptions);
	}

	public KtfmtFormatterFunc(@Nonnull KtfmtStyle style, @Nullable KtfmtFormattingOptions ktfmtFormattingOptions) {
		this(style, ktfmtFormattingOptions, false);
	}

	public KtfmtFormatterFunc(@Nonnull KtfmtStyle style, @Nullable KtfmtFormattingOptions ktfmtFormattingOptions, boolean enableEditorConfig) {
		this.style = style;
		this.ktfmtFormattingOptions = ktfmtFormattingOptions;
		this.enableEditorConfig = enableEditorConfig;
	}

	@Nonnull
	@Override
	public String apply(@Nonnull String input) throws Exception {
		return format(input, FileType.REGULAR, null);
	}

	@Nonnull
	@Override
	public String apply(@Nonnull String input, @Nonnull File file) throws Exception {
		FileType fileType = file.getName().endsWith("." + FileType.SCRIPT.getExtension()) ? FileType.SCRIPT : FileType.REGULAR;
		return format(input, fileType, file);
	}

	private String format(String input, FileType fileType, @Nullable File file) throws Exception {
		return Formatter.format(createFormattingOptions(file), KotlinCode.Companion.from(input, fileType));
	}

	/**
	 * Resolves the options in order of increasing precedence: the style, the {@code .editorconfig} properties
	 * (when enabled and the file is known), and the explicitly configured options.
	 */
	private FormattingOptions createFormattingOptions(@Nullable File file) throws Exception {
		FormattingOptions formattingOptions = switch (style) {
		case META -> Formatter.META_FORMAT;
		case GOOGLE -> Formatter.GOOGLE_FORMAT;
		case KOTLIN_LANG -> Formatter.KOTLINLANG_FORMAT;
		default -> throw new IllegalStateException("Unknown formatting option " + style);
		};

		if (enableEditorConfig && file != null) {
			formattingOptions = EditorConfigResolver.INSTANCE.resolveFormattingOptions(file, formattingOptions);
		}

		if (ktfmtFormattingOptions == null) {
			return formattingOptions;
		}

		FormattingOptions.Builder builder = formattingOptions.toBuilder();
		ktfmtFormattingOptions.getMaxWidth().ifPresent(builder::maxWidth);
		ktfmtFormattingOptions.getBlockIndent().ifPresent(builder::blockIndent);
		ktfmtFormattingOptions.getContinuationIndent().ifPresent(builder::continuationIndent);
		ktfmtFormattingOptions.getTrailingCommaManagementStrategy()
				.map(KtfmtTrailingCommaManagementStrategy::toFormatterTrailingCommaManagementStrategy)
				.ifPresent(builder::trailingCommaManagementStrategy);
		ktfmtFormattingOptions.getRemoveUnusedImports().ifPresent(builder::removeUnusedImports);
		return builder.build();
	}
}
