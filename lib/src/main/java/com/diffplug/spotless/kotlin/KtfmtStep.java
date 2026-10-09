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

import static com.diffplug.spotless.kotlin.KtfmtStep.Style.DEFAULT;
import static com.diffplug.spotless.kotlin.KtfmtStep.Style.DROPBOX;
import static com.diffplug.spotless.kotlin.KtfmtStep.Style.META;
import static com.diffplug.spotless.kotlin.KtfmtStep.TrailingCommaManagementStrategy.ONLY_ADD;

import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.diffplug.spotless.FileSignature;
import com.diffplug.spotless.FormatterFunc;
import com.diffplug.spotless.FormatterStep;
import com.diffplug.spotless.JarState;
import com.diffplug.spotless.Provisioner;
import com.diffplug.spotless.ThrowingEx;

/**
 * Wraps up <a href="https://github.com/Kotlin/ktfmt">ktfmt</a> as a FormatterStep.
 */
public final class KtfmtStep implements Serializable {
	@Serial
	private static final long serialVersionUID = 2L;

	private static final String NAME = "ktfmt";
	/**
	 * Since 0.65, ktfmt is published under the {@code org.jetbrains.kotlinx} group with the {@code org.jetbrains.kotlinx.ktfmt} package.
	 */
	private static final String MAVEN_COORDINATE = "org.jetbrains.kotlinx:ktfmt:";
	private static final String MAVEN_COORDINATE_LEGACY = "com.facebook:ktfmt:";
	private static final String EDITOR_CONFIG_FILE_NAME = ".editorconfig";
	private static final String UTF8_BOM = "\uFEFF";
	private static final Pattern EDITOR_CONFIG_ROOT = Pattern.compile("root\\s*=\\s*true");

	private final String version;
	/**
	 * Option that allows to apply formatting options to perform a 4-space block and continuation indent.
	 */
	@Nullable private final Style style;
	@Nullable private final KtfmtFormattingOptions options;
	/**
	 * When non-null, the style's configuration is overridden with the {@code .editorconfig} properties supported by ktfmt,
	 * and the {@code .editorconfig} files from this directory up to the root one are tracked.
	 */
	@Nullable private final File editorConfigDir;
	/**
	 * The jar that contains the formatter.
	 */
	private final JarState.Promised jarState;

	private KtfmtStep(String version,
			JarState.Promised jarState,
			@Nullable Style style,
			@Nullable KtfmtFormattingOptions options,
			@Nullable File editorConfigDir) {
		this.version = Objects.requireNonNull(version, "version");
		this.style = style;
		this.options = options;
		this.editorConfigDir = editorConfigDir;
		this.jarState = Objects.requireNonNull(jarState, "jarState");
	}

	/**
	 * Used to allow multiple style option through formatting options and since when is each of them available.
	 *
	 * @see <a href="https://github.com/Kotlin/ktfmt/blob/v0.51/core/src/main/java/com/Kotlin/ktfmt/format/Formatter.kt#L45-L68">ktfmt source</a>
	 */
	public enum Style {
		// @formatter:off
		DEFAULT("DEFAULT_FORMAT", "0.0", "0.50"),
		META("META_FORMAT", "0.51"),
		DROPBOX("DROPBOX_FORMAT", "0.16", "0.50"),
		GOOGLE("GOOGLE_FORMAT", "0.19"),
		KOTLINLANG("KOTLINLANG_FORMAT", "0.21"),
		;
		// @formatter:on

		private final String format;
		private final String since;
		private final @Nullable String until;

		Style(String format, String since) {
			this.format = format;
			this.since = since;
			this.until = null;
		}

		Style(String format, String since, @Nullable String until) {
			this.format = format;
			this.since = since;
			this.until = until;
		}

		String getFormat() {
			return format;
		}

		String getSince() {
			return since;
		}

		/**
		 * Last version (inclusive) that supports this style
		 */
		@Nullable String getUntil() {
			return until;
		}
	}

	public enum TrailingCommaManagementStrategy {
		/**
		 * Do not manage trailing commas at all, only format what is already present.
		 */
		NONE,
		/**
		 * <p>
		 * Only add trailing commas when necessary, but do not remove them.
		 * </p>
		 * <p>
		 * Lists that cannot fit on one line will have trailing commas inserted.
		 * Trailing commas can to be used to "hint" ktfmt that the list should be broken to multiple lines
		 * </p>
		 */
		ONLY_ADD,
		/**
		 * <p>
		 * Fully manage trailing commas, adding and removing them where necessary.
		 * </p>
		 * <p>
		 * Lists that cannot fit on one line will have trailing commas inserted.
		 * Lists that span multiple lines will have them removed. Manually inserted trailing commas
		 * cannot be used as a hint to force breaking lists to multiple lines.
		 * </p>
		 */
		COMPLETE,
	}

	public static class KtfmtFormattingOptions implements Serializable {

		@Serial
		private static final long serialVersionUID = 1L;

		@Nullable private Integer maxWidth;

		@Nullable private Integer blockIndent;

		@Nullable private Integer continuationIndent;

		@Nullable private Boolean removeUnusedImports;

		@Nullable private TrailingCommaManagementStrategy trailingCommaManagementStrategy;

		public KtfmtFormattingOptions() {}

		public KtfmtFormattingOptions(
				@Nullable Integer maxWidth,
				@Nullable Integer blockIndent,
				@Nullable Integer continuationIndent,
				@Nullable Boolean removeUnusedImports,
				@Nullable TrailingCommaManagementStrategy trailingCommaManagementStrategy) {
			this.maxWidth = maxWidth;
			this.blockIndent = blockIndent;
			this.continuationIndent = continuationIndent;
			this.removeUnusedImports = removeUnusedImports;
			this.trailingCommaManagementStrategy = trailingCommaManagementStrategy;
		}

		public void setMaxWidth(int maxWidth) {
			this.maxWidth = maxWidth;
		}

		public void setBlockIndent(int blockIndent) {
			this.blockIndent = blockIndent;
		}

		public void setContinuationIndent(int continuationIndent) {
			this.continuationIndent = continuationIndent;
		}

		public void setRemoveUnusedImports(boolean removeUnusedImports) {
			this.removeUnusedImports = removeUnusedImports;
		}

		public void setTrailingCommaManagementStrategy(TrailingCommaManagementStrategy trailingCommaManagementStrategy) {
			this.trailingCommaManagementStrategy = trailingCommaManagementStrategy;
		}
	}

	/**
	 * Creates a step which formats everything - code, import order, and unused imports.
	 */
	public static FormatterStep create(Provisioner provisioner) {
		return create(defaultVersion(), provisioner);
	}

	/**
	 * Creates a step which formats everything - code, import order, and unused imports.
	 */
	public static FormatterStep create(String version, Provisioner provisioner) {
		return create(version, provisioner, null, null);
	}

	/**
	 * Creates a step which formats everything - code, import order, and unused imports.
	 */
	public static FormatterStep create(String version, Provisioner provisioner, @Nullable Style style, @Nullable KtfmtFormattingOptions options) {
		return create(version, provisioner, style, options, null);
	}

	/**
	 * Creates a step which formats everything - code, import order, and unused imports.
	 * <p>
	 * When {@code editorConfigDir} is non-null, the {@code .editorconfig} files applying to each formatted file are resolved the same
	 * way as ktfmt's {@code --enable-editorconfig} flag, and the supported properties ({@code max_line_length}, {@code indent_size},
	 * {@code ij_continuation_indent_size}, {@code ktfmt_trailing_comma_management_strategy}, ...) override the style.
	 * The explicitly configured {@code options} still take precedence over them. Requires ktfmt 0.60 or later.
	 * <p>
	 * The {@code .editorconfig} files in {@code editorConfigDir} and its parent directories, up to the first one declaring
	 * {@code root = true}, are part of the step's state, so changing them invalidates the up-to-date checks.
	 * {@code .editorconfig} files in its subdirectories are still applied, but not tracked.
	 */
	public static FormatterStep create(String version, Provisioner provisioner, @Nullable Style style, @Nullable KtfmtFormattingOptions options, @Nullable File editorConfigDir) {
		Objects.requireNonNull(version, "version");
		Objects.requireNonNull(provisioner, "provisioner");
		return FormatterStep.create(NAME,
				new KtfmtStep(version, JarState.promise(() -> JarState.from(mavenCoordinate(version) + version, provisioner)), style, options, editorConfigDir),
				KtfmtStep::equalityState,
				State::createFormat);
	}

	public static String defaultVersion() {
		return KotlinBuildConfig.VERSION_KTFMT;
	}

	private static String mavenCoordinate(String version) {
		return isLegacyPackage(version) ? MAVEN_COORDINATE_LEGACY : MAVEN_COORDINATE;
	}

	/**
	 * Versions before 0.65 use the {@code com.facebook} group and package, so they can't use the glue code compiled against the latest ktfmt.
	 */
	private static boolean isLegacyPackage(String version) {
		return BadSemver.version(version) < BadSemver.version(0, 65);
	}

	private State equalityState() throws IOException {
		FileSignature editorConfigFiles = editorConfigDir == null ? null : FileSignature.signAsList(findEditorConfigFiles(editorConfigDir));
		return new State(version, jarState.get(), style, options, editorConfigFiles);
	}

	/**
	 * Finds the {@code .editorconfig} files applying to {@code dir}, from the nearest one up to the first one declaring {@code root = true}.
	 */
	static List<File> findEditorConfigFiles(File dir) throws IOException {
		List<File> files = new ArrayList<>();
		for (File current = dir.getAbsoluteFile(); current != null; current = current.getParentFile()) {
			File editorConfig = new File(current, EDITOR_CONFIG_FILE_NAME);
			if (editorConfig.isFile()) {
				files.add(editorConfig);
				if (isRootEditorConfig(editorConfig)) {
					break;
				}
			}
		}
		return files;
	}

	private static boolean isRootEditorConfig(File editorConfig) throws IOException {
		// decode leniently, since only ASCII matters here and a malformed file mustn't fail the build
		String content = new String(Files.readAllBytes(editorConfig.toPath()), StandardCharsets.UTF_8);
		// the UTF-8 BOM is decoded as a character, and isn't removed by trim()
		if (content.startsWith(UTF8_BOM)) {
			content = content.substring(1);
		}
		for (String line : content.lines().toList()) {
			String trimmed = line.trim();
			if (trimmed.startsWith("[")) {
				// `root` is only allowed in the preamble, before the first section
				return false;
			}
			if (EDITOR_CONFIG_ROOT.matcher(trimmed.toLowerCase(Locale.ROOT)).matches()) {
				return true;
			}
		}
		return false;
	}

	private static final class State implements Serializable {
		@Serial
		private static final long serialVersionUID = 2L;
		private final String version;
		@Nullable private final Style style;
		@Nullable private final KtfmtFormattingOptions options;
		/**
		 * Non-null when the {@code .editorconfig} support is enabled.
		 */
		@Nullable private final FileSignature editorConfigFiles;
		private final JarState jarState;

		State(String version,
				JarState jarState,
				@Nullable Style style,
				@Nullable KtfmtFormattingOptions options,
				@Nullable FileSignature editorConfigFiles) {
			this.version = version;
			this.options = options;
			this.style = style;
			this.editorConfigFiles = editorConfigFiles;
			this.jarState = jarState;
			validateStyle();
			validateOptions();
		}

		FormatterFunc createFormat() throws Exception {
			final boolean enableEditorConfig = editorConfigFiles != null;
			// ktfmt caches the parsed `.editorconfig` files statically, so a classloader is allocated per `.editorconfig` signature to pick up their changes.
			final ClassLoader classLoader = enableEditorConfig
					? jarState.getClassLoader(new ArrayList<>(List.of(jarState, editorConfigFiles)))
					: jarState.getClassLoader();

			if (isLegacyPackage(version)) {
				return new KtfmtFormatterFuncCompat(version, style, options, enableEditorConfig, classLoader).getFormatterFunc();
			}

			final Class<?> formatterFuncClass = classLoader.loadClass("com.diffplug.spotless.glue.ktfmt.KtfmtFormatterFunc");
			final Class<?> ktfmtStyleClass = classLoader.loadClass("com.diffplug.spotless.glue.ktfmt.KtfmtStyle");
			final Class<?> ktfmtFormattingOptionsClass = classLoader.loadClass("com.diffplug.spotless.glue.ktfmt.KtfmtFormattingOptions");
			final Class<?> ktfmtTrailingCommaManagmentStrategyClass = classLoader.loadClass("com.diffplug.spotless.glue.ktfmt.KtfmtTrailingCommaManagementStrategy");

			if (style == null && options == null && !enableEditorConfig) {
				final Constructor<?> constructor = formatterFuncClass.getConstructor();
				return (FormatterFunc) constructor.newInstance();
			}

			final Object ktfmtStyle = Enum.valueOf((Class<? extends Enum>) ktfmtStyleClass, getKtfmtStyleOption(style == null ? META : style));
			if (options == null && !enableEditorConfig) {
				final Constructor<?> constructor = formatterFuncClass.getConstructor(ktfmtStyleClass);
				return (FormatterFunc) constructor.newInstance(ktfmtStyle);
			}

			final Constructor<?> optionsConstructor = ktfmtFormattingOptionsClass.getConstructor(
					Integer.class, Integer.class, Integer.class, Boolean.class, ktfmtTrailingCommaManagmentStrategyClass);

			Object ktfmtFormattingOptions = null;
			if (options != null) {
				final Object ktfmtTrailingCommaManagementStrategy = options.trailingCommaManagementStrategy == null
						? null
						: Enum.valueOf((Class<? extends Enum>) ktfmtTrailingCommaManagmentStrategyClass, options.trailingCommaManagementStrategy.name());
				ktfmtFormattingOptions = optionsConstructor.newInstance(
						options.maxWidth, options.blockIndent, options.continuationIndent, options.removeUnusedImports, ktfmtTrailingCommaManagementStrategy);
			}

			final Constructor<?> constructor = formatterFuncClass.getConstructor(ktfmtStyleClass, ktfmtFormattingOptionsClass, boolean.class);
			final FormatterFunc formatterFunc = (FormatterFunc) constructor.newInstance(ktfmtStyle, ktfmtFormattingOptions, enableEditorConfig);
			// the `.editorconfig` files are resolved from the file's path, so reject the NO_FILE_SENTINEL
			return enableEditorConfig ? (FormatterFunc.NeedsFile) formatterFunc::apply : formatterFunc;
		}

		private void validateOptions() {
			if (editorConfigFiles != null && BadSemver.version(version) < BadSemver.version(0, 60)) {
				throw new IllegalStateException("Ktfmt `.editorconfig` support is available from version 0.60 (current version: %s)".formatted(version));
			}

			if (BadSemver.version(version) < BadSemver.version(0, 11)) {
				if (options != null) {
					throw new IllegalStateException("Ktfmt formatting options supported for version 0.11 and later");
				}
				return;
			}

			if (BadSemver.version(version) < BadSemver.version(0, 17)) {
				if (options != null && options.removeUnusedImports != null) {
					throw new IllegalStateException("Ktfmt formatting option `removeUnusedImports` supported for version 0.17 and later");
				}
			}

			if (BadSemver.version(version) < BadSemver.version(0, 57)) {
				if (options != null && options.trailingCommaManagementStrategy == ONLY_ADD) {
					throw new IllegalStateException("Value ONLY_ADD for Ktfmt formatting option `trailingCommaManagementStrategy` supported for version 0.57 and later");
				}
			}
		}

		private void validateStyle() {
			if (style == null) {
				return;
			}

			if (BadSemver.version(version) < BadSemver.version(style.since)) {
				throw new IllegalStateException("The style %s is available from version %s (current version: %s)".formatted(style.name(), style.since, version));
			}
			if (style.until != null && BadSemver.version(version) > BadSemver.version(style.until)) {
				throw new IllegalStateException("The style %s is no longer available from version %s (current version: %s)".formatted(style.name(), style.until, version));
			}
		}

		/**
		 * @param style
		 * @return com.diffplug.spotless.glue.ktfmt.KtfmtStyle enum value name
		 */
		private String getKtfmtStyleOption(Style style) {
			switch (style) {
			case META:
				return "META";
			case GOOGLE:
				return "GOOGLE";
			case KOTLINLANG:
				return "KOTLIN_LANG";
			default:
				throw new IllegalStateException("Unsupported style: " + style);
			}
		}
	}

	/**
	 * Reflection-based formatter for ktfmt versions before 0.65, which use the {@code com.facebook.ktfmt} package.
	 */
	private static final class KtfmtFormatterFuncCompat {
		private static final String PACKAGE = "com.facebook.ktfmt";

		/**
		 * The <code>format</code> method is available in the link below.
		 *
		 * @see <a href="https://github.com/Kotlin/ktfmt/blob/v0.51/core/src/main/java/com/Kotlin/ktfmt/format/Formatter.kt#L78-L94">ktfmt source</a>
		 */
		static final String FORMATTER_METHOD = "format";

		private final String version;
		private final Style style;
		private final KtfmtFormattingOptions options;
		private final boolean enableEditorConfig;
		private final ClassLoader classLoader;

		public KtfmtFormatterFuncCompat(String currentVersion, @Nullable Style style, @Nullable KtfmtFormattingOptions options, boolean enableEditorConfig, ClassLoader classLoader) {
			this.version = currentVersion;
			this.style = style;
			this.options = options;
			this.enableEditorConfig = enableEditorConfig;
			this.classLoader = classLoader;
		}

		public FormatterFunc getFormatterFunc() {
			if (enableEditorConfig) {
				return (FormatterFunc.NeedsFile) (input, file) -> {
					try {
						return applyFormat(input, file);
					} catch (InvocationTargetException e) {
						throw ThrowingEx.unwrapCause(e);
					}
				};
			}
			return input -> {
				try {
					return applyFormat(input, null);
				} catch (InvocationTargetException e) {
					throw ThrowingEx.unwrapCause(e);
				}
			};
		}

		protected String applyFormat(String input, @Nullable File file) throws Exception {
			Class<?> formatterClass = getFormatterClazz();
			if (style == null && options == null && !enableEditorConfig || style == DEFAULT) {
				Method formatterMethod = formatterClass.getMethod(FORMATTER_METHOD, String.class);
				return (String) formatterMethod.invoke(formatterClass, input);
			} else {
				Method formatterMethod = formatterClass.getMethod(FORMATTER_METHOD, getFormattingOptionsClazz(), String.class);
				Object formattingOptions = getCustomFormattingOptions(formatterClass, file);
				return (String) formatterMethod.invoke(formatterClass, formattingOptions, input);
			}
		}

		private Object getCustomFormattingOptions(Class<?> formatterClass, @Nullable File file) throws Exception {
			Object formattingOptions = getFormattingOptionsFromStyle(formatterClass);
			if (enableEditorConfig && file != null) {
				// The explicitly configured options below are applied on top of the ones resolved from `.editorconfig`.
				Class<?> editorConfigResolverClass = classLoader.loadClass(PACKAGE + ".cli.EditorConfigResolver");
				Object editorConfigResolver = editorConfigResolverClass.getField("INSTANCE").get(null);
				formattingOptions = editorConfigResolverClass.getMethod("resolveFormattingOptions", File.class, getFormattingOptionsClazz())
						.invoke(editorConfigResolver, file, formattingOptions);
			}
			Class<?> formattingOptionsClass = formattingOptions.getClass();

			if (options != null) {
				if (BadSemver.version(version) < BadSemver.version(0, 17)) {
					formattingOptions = formattingOptions.getClass().getConstructor(int.class, int.class, int.class).newInstance(
							/* maxWidth = */ Objects.requireNonNullElse(options.maxWidth, (Integer) formattingOptionsClass.getMethod("getMaxWidth").invoke(formattingOptions)),
							/* blockIndent = */ Objects.requireNonNullElse(options.blockIndent, (Integer) formattingOptionsClass.getMethod("getBlockIndent").invoke(formattingOptions)),
							/* continuationIndent = */ Objects.requireNonNullElse(options.continuationIndent, (Integer) formattingOptionsClass.getMethod("getContinuationIndent").invoke(formattingOptions)));
				} else if (BadSemver.version(version) < BadSemver.version(0, 19)) {
					formattingOptions = formattingOptions.getClass().getConstructor(int.class, int.class, int.class, boolean.class, boolean.class).newInstance(
							/* maxWidth = */ Objects.requireNonNullElse(options.maxWidth, (Integer) formattingOptionsClass.getMethod("getMaxWidth").invoke(formattingOptions)),
							/* blockIndent = */ Objects.requireNonNullElse(options.blockIndent, (Integer) formattingOptionsClass.getMethod("getBlockIndent").invoke(formattingOptions)),
							/* continuationIndent = */ Objects.requireNonNullElse(options.continuationIndent, (Integer) formattingOptionsClass.getMethod("getContinuationIndent").invoke(formattingOptions)),
							/* removeUnusedImports = */ Objects.requireNonNullElse(options.removeUnusedImports, (Boolean) formattingOptionsClass.getMethod("getRemoveUnusedImports").invoke(formattingOptions)),
							/* debuggingPrintOpsAfterFormatting = */ (Boolean) formattingOptionsClass.getMethod("getDebuggingPrintOpsAfterFormatting").invoke(formattingOptions));
				} else if (BadSemver.version(version) < BadSemver.version(0, 47)) {
					Class<?> styleClass = classLoader.loadClass(formattingOptionsClass.getName() + "$Style");
					formattingOptions = formattingOptions.getClass().getConstructor(styleClass, int.class, int.class, int.class, boolean.class, boolean.class).newInstance(
							/* style = */ formattingOptionsClass.getMethod("getStyle").invoke(formattingOptions),
							/* maxWidth = */ Objects.requireNonNullElse(options.maxWidth, (Integer) formattingOptionsClass.getMethod("getMaxWidth").invoke(formattingOptions)),
							/* blockIndent = */ Objects.requireNonNullElse(options.blockIndent, (Integer) formattingOptionsClass.getMethod("getBlockIndent").invoke(formattingOptions)),
							/* continuationIndent = */ Objects.requireNonNullElse(options.continuationIndent, (Integer) formattingOptionsClass.getMethod("getContinuationIndent").invoke(formattingOptions)),
							/* removeUnusedImports = */ Objects.requireNonNullElse(options.removeUnusedImports, (Boolean) formattingOptionsClass.getMethod("getRemoveUnusedImports").invoke(formattingOptions)),
							/* debuggingPrintOpsAfterFormatting = */ (Boolean) formattingOptionsClass.getMethod("getDebuggingPrintOpsAfterFormatting").invoke(formattingOptions));
				} else if (BadSemver.version(version) < BadSemver.version(0, 51)) {
					Class<?> styleClass = classLoader.loadClass(formattingOptionsClass.getName() + "$Style");
					formattingOptions = formattingOptions.getClass().getConstructor(styleClass, int.class, int.class, int.class, boolean.class, boolean.class, boolean.class).newInstance(
							/* style = */ formattingOptionsClass.getMethod("getStyle").invoke(formattingOptions),
							/* maxWidth = */ Objects.requireNonNullElse(options.maxWidth, (Integer) formattingOptionsClass.getMethod("getMaxWidth").invoke(formattingOptions)),
							/* blockIndent = */ Objects.requireNonNullElse(options.blockIndent, (Integer) formattingOptionsClass.getMethod("getBlockIndent").invoke(formattingOptions)),
							/* continuationIndent = */ Objects.requireNonNullElse(options.continuationIndent, (Integer) formattingOptionsClass.getMethod("getContinuationIndent").invoke(formattingOptions)),
							/* removeUnusedImports = */ Objects.requireNonNullElse(options.removeUnusedImports, (Boolean) formattingOptionsClass.getMethod("getRemoveUnusedImports").invoke(formattingOptions)),
							/* debuggingPrintOpsAfterFormatting = */ (Boolean) formattingOptionsClass.getMethod("getDebuggingPrintOpsAfterFormatting").invoke(formattingOptions),
							/* manageTrailingCommas = */ Objects.requireNonNullElse(getManageTrailingCommasFrom(options.trailingCommaManagementStrategy), (Boolean) formattingOptionsClass.getMethod("getManageTrailingCommas").invoke(formattingOptions)));
				} else if (BadSemver.version(version) < BadSemver.version(0, 57)) {
					formattingOptions = formattingOptions.getClass().getConstructor(int.class, int.class, int.class, boolean.class, boolean.class, boolean.class).newInstance(
							/* maxWidth = */ Objects.requireNonNullElse(options.maxWidth, (Integer) formattingOptionsClass.getMethod("getMaxWidth").invoke(formattingOptions)),
							/* blockIndent = */ Objects.requireNonNullElse(options.blockIndent, (Integer) formattingOptionsClass.getMethod("getBlockIndent").invoke(formattingOptions)),
							/* continuationIndent = */ Objects.requireNonNullElse(options.continuationIndent, (Integer) formattingOptionsClass.getMethod("getContinuationIndent").invoke(formattingOptions)),
							/* manageTrailingCommas = */ Objects.requireNonNullElse(getManageTrailingCommasFrom(options.trailingCommaManagementStrategy), (Boolean) formattingOptionsClass.getMethod("getManageTrailingCommas").invoke(formattingOptions)),
							/* removeUnusedImports = */ Objects.requireNonNullElse(options.removeUnusedImports, (Boolean) formattingOptionsClass.getMethod("getRemoveUnusedImports").invoke(formattingOptions)),
							/* debuggingPrintOpsAfterFormatting = */ (Boolean) formattingOptionsClass.getMethod("getDebuggingPrintOpsAfterFormatting").invoke(formattingOptions));
				} else if (BadSemver.version(version) < BadSemver.version(0, 63)) {
					Class<?> trailingCommaManagementStrategyClass = getTrailingCommaManagementStrategyClazz();
					Object trailingCommaManagementStrategy = options.trailingCommaManagementStrategy == null
							? formattingOptionsClass.getMethod("getTrailingCommaManagementStrategy").invoke(formattingOptions)
							: Enum.valueOf((Class<? extends Enum>) trailingCommaManagementStrategyClass, options.trailingCommaManagementStrategy.name());
					formattingOptions = formattingOptions.getClass().getConstructor(int.class, int.class, int.class, trailingCommaManagementStrategyClass, boolean.class, boolean.class).newInstance(
							/* maxWidth = */ Objects.requireNonNullElse(options.maxWidth, (Integer) formattingOptionsClass.getMethod("getMaxWidth").invoke(formattingOptions)),
							/* blockIndent = */ Objects.requireNonNullElse(options.blockIndent, (Integer) formattingOptionsClass.getMethod("getBlockIndent").invoke(formattingOptions)),
							/* continuationIndent = */ Objects.requireNonNullElse(options.continuationIndent, (Integer) formattingOptionsClass.getMethod("getContinuationIndent").invoke(formattingOptions)),
							/* trailingCommaManagementStrategy = */ trailingCommaManagementStrategy,
							/* removeUnusedImports = */ Objects.requireNonNullElse(options.removeUnusedImports, (Boolean) formattingOptionsClass.getMethod("getRemoveUnusedImports").invoke(formattingOptions)),
							/* debuggingPrintOpsAfterFormatting = */ (Boolean) formattingOptionsClass.getMethod("getDebuggingPrintOpsAfterFormatting").invoke(formattingOptions));
				} else {
					// FormattingOptions.Builder is available since 0.63, and keeps the options not managed here (e.g. preserveLambdaBreaks) from the style.
					Object builder = formattingOptionsClass.getMethod("toBuilder").invoke(formattingOptions);
					Class<?> builderClass = builder.getClass();
					if (options.maxWidth != null) {
						builderClass.getMethod("maxWidth", int.class).invoke(builder, options.maxWidth);
					}
					if (options.blockIndent != null) {
						builderClass.getMethod("blockIndent", int.class).invoke(builder, options.blockIndent);
					}
					if (options.continuationIndent != null) {
						builderClass.getMethod("continuationIndent", int.class).invoke(builder, options.continuationIndent);
					}
					if (options.trailingCommaManagementStrategy != null) {
						Class<?> trailingCommaManagementStrategyClass = getTrailingCommaManagementStrategyClazz();
						builderClass.getMethod("trailingCommaManagementStrategy", trailingCommaManagementStrategyClass).invoke(builder,
								Enum.valueOf((Class<? extends Enum>) trailingCommaManagementStrategyClass, options.trailingCommaManagementStrategy.name()));
					}
					if (options.removeUnusedImports != null) {
						builderClass.getMethod("removeUnusedImports", boolean.class).invoke(builder, options.removeUnusedImports);
					}
					formattingOptions = builderClass.getMethod("build").invoke(builder);
				}
			}

			return formattingOptions;
		}

		private Object getFormattingOptionsFromStyle(Class<?> formatterClass) throws Exception {
			Style style = this.style;
			if (style == null) {
				if (BadSemver.version(version) < BadSemver.version(0, 51)) {
					style = DEFAULT;
				} else {
					style = META;
				}
			}
			if (BadSemver.version(version) < BadSemver.version(0, 19)) {
				if (style != DROPBOX) {
					throw new IllegalStateException("Invalid style " + style + " for version " + version);
				}
				Class<?> formattingOptionsCompanionClazz = classLoader.loadClass(PACKAGE + ".FormattingOptions$Companion");
				Object companion = formattingOptionsCompanionClazz.getConstructors()[0].newInstance((Object) null);
				Method formattingOptionsMethod = formattingOptionsCompanionClazz.getDeclaredMethod("dropboxStyle");
				return formattingOptionsMethod.invoke(companion);
			} else {
				return formatterClass.getField(style.getFormat()).get(null);
			}
		}

		private Class<?> getFormatterClazz() throws Exception {
			Class<?> formatterClazz;
			if (BadSemver.version(version) >= BadSemver.version(0, 31)) {
				formatterClazz = classLoader.loadClass(PACKAGE + ".format.Formatter");
			} else {
				formatterClazz = classLoader.loadClass(PACKAGE + ".FormatterKt");
			}
			return formatterClazz;
		}

		private Class<?> getFormattingOptionsClazz() throws Exception {
			Class<?> formattingOptionsClazz;
			if (BadSemver.version(version) >= BadSemver.version(0, 31)) {
				formattingOptionsClazz = classLoader.loadClass(PACKAGE + ".format.FormattingOptions");
			} else {
				formattingOptionsClazz = classLoader.loadClass(PACKAGE + ".FormattingOptions");
			}
			return formattingOptionsClazz;
		}

		private Class<?> getTrailingCommaManagementStrategyClazz() throws Exception {
			return classLoader.loadClass(PACKAGE + ".format.TrailingCommaManagementStrategy");
		}

		private @Nullable Boolean getManageTrailingCommasFrom(
				@Nullable TrailingCommaManagementStrategy trailingCommaManagementStrategy) {
			if (trailingCommaManagementStrategy == null) {
				return null;
			}

			return switch (trailingCommaManagementStrategy) {
			case NONE, ONLY_ADD -> false;
			case COMPLETE -> true;
			};
		}
	}
}
