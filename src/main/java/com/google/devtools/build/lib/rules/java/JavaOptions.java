// Copyright 2014 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.devtools.build.lib.rules.java;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.devtools.build.lib.analysis.config.BuildConfiguration.LabelConverter;
import com.google.devtools.build.lib.analysis.config.BuildConfiguration.LabelListConverter;
import com.google.devtools.build.lib.analysis.config.BuildConfiguration.LabelMapConverter;
import com.google.devtools.build.lib.analysis.config.BuildConfiguration.StrictDepsConverter;
import com.google.devtools.build.lib.analysis.config.BuildConfiguration.StrictDepsMode;
import com.google.devtools.build.lib.analysis.config.FragmentOptions;
import com.google.devtools.build.lib.cmdline.Label;
import com.google.devtools.build.lib.rules.java.JavaConfiguration.ImportDepsCheckingLevel;
import com.google.devtools.build.lib.rules.java.JavaConfiguration.JavaClasspathMode;
import com.google.devtools.build.lib.rules.java.JavaConfiguration.JavaOptimizationMode;
import com.google.devtools.build.lib.rules.java.JavaConfiguration.OneVersionEnforcementLevel;
import com.google.devtools.common.options.EnumConverter;
import com.google.devtools.common.options.Option;
import com.google.devtools.common.options.OptionDocumentationCategory;
import com.google.devtools.common.options.OptionEffectTag;
import com.google.devtools.common.options.OptionMetadataTag;
import com.google.devtools.common.options.TriState;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Command-line options for building Java targets */
public class JavaOptions extends FragmentOptions {
  /** Converter for the --java_classpath option. */
  public static class JavaClasspathModeConverter extends EnumConverter<JavaClasspathMode> {
    public JavaClasspathModeConverter() {
      super(JavaClasspathMode.class, "Java classpath reduction strategy");
    }
  }

  /**
   * Converter for the --java_optimization_mode option.
   */
  public static class JavaOptimizationModeConverter extends EnumConverter<JavaOptimizationMode> {
    public JavaOptimizationModeConverter() {
      super(JavaOptimizationMode.class, "Java optimization strategy");
    }
  }

  /** Converter for the --experimental_one_version_enforcement option */
  public static class OneVersionEnforcementLevelConverter
      extends EnumConverter<OneVersionEnforcementLevel> {
    public OneVersionEnforcementLevelConverter() {
      super(OneVersionEnforcementLevel.class, "Enforcement level for Java One Version violations");
    }
  }

  /** Converter for the --experimental_import_deps_checking option */
  public static class ImportDepsCheckingLevelConverter
      extends EnumConverter<ImportDepsCheckingLevel> {
    public ImportDepsCheckingLevelConverter() {
      super(
          ImportDepsCheckingLevel.class,
          "Enforcement level for the dependency checking for import targets.");
    }
  }

  @Option(
      name = "javabase",
      defaultValue = "@bazel_tools//tools/jdk:jdk",
      converter = LabelConverter.class,
      documentationCategory = OptionDocumentationCategory.UNCATEGORIZED,
      effectTags = {OptionEffectTag.UNKNOWN},
      help =
          "JAVABASE used for the JDK invoked by Blaze. This is the "
              + "java_runtime which will be used to execute "
              + "external Java commands.")
  public Label javaBase;

  @Option(
    name = "java_toolchain",
    defaultValue = "@bazel_tools//tools/jdk:toolchain",
    converter = LabelConverter.class,
    documentationCategory = OptionDocumentationCategory.UNCATEGORIZED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help = "The name of the toolchain rule for Java."
  )
  public Label javaToolchain;

  @Option(
    name = "host_java_toolchain",
    defaultValue = "@bazel_tools//tools/jdk:toolchain",
    converter = LabelConverter.class,
    documentationCategory = OptionDocumentationCategory.UNCATEGORIZED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help = "The Java toolchain used to build tools that are executed during a build."
  )
  public Label hostJavaToolchain;

  @Option(
      name = "host_javabase",
      defaultValue = "@bazel_tools//tools/jdk:host_jdk",
      converter = LabelConverter.class,
      documentationCategory = OptionDocumentationCategory.UNCATEGORIZED,
      effectTags = {OptionEffectTag.UNKNOWN},
      help =
          "JAVABASE used for the host JDK. This is the java_runtime which is used to execute "
              + "tools during a build.")
  public Label hostJavaBase;

  @Option(
    name = "javacopt",
    allowMultiple = true,
    defaultValue = "",
    documentationCategory = OptionDocumentationCategory.UNCATEGORIZED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help = "Additional options to pass to javac."
  )
  public List<String> javacOpts;

  @Option(
    name = "jvmopt",
    allowMultiple = true,
    defaultValue = "",
    documentationCategory = OptionDocumentationCategory.UNCATEGORIZED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help =
        "Additional options to pass to the Java VM. These options will get added to the "
            + "VM startup options of each java_binary target."
  )
  public List<String> jvmOpts;

  @Option(
    name = "use_ijars",
    defaultValue = "true",
    documentationCategory = OptionDocumentationCategory.UNCATEGORIZED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help =
        "If enabled, this option causes Java compilation to use interface jars. "
            + "This will result in faster incremental compilation, "
            + "but error messages can be different."
  )
  public boolean useIjars;

  @Deprecated
  @Option(
    name = "use_src_ijars",
    defaultValue = "false",
    documentationCategory = OptionDocumentationCategory.UNDOCUMENTED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help = "No-op. Kept here for backwards compatibility."
  )
  public boolean useSourceIjars;

  @Option(
    name = "java_header_compilation",
    defaultValue = "true",
    documentationCategory = OptionDocumentationCategory.UNCATEGORIZED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help = "Compile ijars directly from source.",
    oldName = "experimental_java_header_compilation"
  )
  public boolean headerCompilation;

  // TODO(cushon): delete flag after removing from global .blazerc
  @Deprecated
  @Option(
    name = "experimental_optimize_header_compilation_annotation_processing",
    defaultValue = "false",
    documentationCategory = OptionDocumentationCategory.UNDOCUMENTED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help = "This flag is a noop and scheduled for removal."
  )
  public boolean optimizeHeaderCompilationAnnotationProcessing;

  @Option(
    name = "java_deps",
    defaultValue = "true",
    documentationCategory = OptionDocumentationCategory.UNCATEGORIZED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help = "Generate dependency information (for now, compile-time classpath) per Java target."
  )
  public boolean javaDeps;

  @Option(
    name = "java_classpath",
    allowMultiple = false,
    defaultValue = "javabuilder",
    converter = JavaClasspathModeConverter.class,
    documentationCategory = OptionDocumentationCategory.UNCATEGORIZED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help = "Enables reduced classpaths for Java compilations.",
    oldName = "experimental_java_classpath"
  )
  public JavaClasspathMode javaClasspath;

  @Option(
    name = "java_debug",
    defaultValue = "null",
    expansion = {
      "--test_arg=--wrapper_script_flag=--debug",
      "--test_output=streamed",
      "--test_strategy=exclusive",
      "--test_timeout=9999",
      "--nocache_test_results"
    },
    documentationCategory = OptionDocumentationCategory.UNCATEGORIZED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help =
        "Causes the Java virtual machine of a java test to wait for a connection from a "
            + "JDWP-compliant debugger (such as jdb) before starting the test. Implies "
            + "-test_output=streamed."
  )
  public Void javaTestDebug;

  @Option(
    name = "strict_java_deps",
    allowMultiple = false,
    defaultValue = "default",
    converter = StrictDepsConverter.class,
    documentationCategory = OptionDocumentationCategory.INPUT_STRICTNESS,
    effectTags = {OptionEffectTag.BUILD_FILE_SEMANTICS, OptionEffectTag.EAGERNESS_TO_EXIT},
    help =
        "If true, checks that a Java target explicitly declares all directly used "
            + "targets as dependencies.",
    oldName = "strict_android_deps"
  )
  public StrictDepsMode strictJavaDeps;

  @Option(
    name = "experimental_fix_deps_tool",
    defaultValue = "add_dep",
    documentationCategory = OptionDocumentationCategory.UNDOCUMENTED,
    effectTags = {OptionEffectTag.BUILD_FILE_SEMANTICS},
    help = "Specifies which tool should be used to resolve missing dependencies."
  )
  public String fixDepsTool;

  // TODO(bazel-team): This flag should ideally default to true (and eventually removed). We have
  // been accidentally supplying JUnit and Hamcrest deps to java_test targets indirectly via the
  // BazelTestRunner, and setting this flag to true fixes that behaviour.
  @Option(
    name = "explicit_java_test_deps",
    defaultValue = "false",
    documentationCategory = OptionDocumentationCategory.UNCATEGORIZED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help =
        "Explicitly specify a dependency to JUnit or Hamcrest in a java_test instead of "
            + " accidentally obtaining from the TestRunner's deps. Only works for bazel right now."
  )
  public boolean explicitJavaTestDeps;

  @Option(
    name = "experimental_testrunner",
    defaultValue = "false",
    documentationCategory = OptionDocumentationCategory.UNDOCUMENTED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help =
        "Use the experimental test runner in bazel which runs the tests under a separate "
            + "classloader. We must set the --explicit_java_test_deps flag with this to ensure "
            + "the test targets have their dependencies right."
  )
  public boolean experimentalTestRunner;

  @Option(
    name = "javabuilder_top",
    defaultValue = "null",
    documentationCategory = OptionDocumentationCategory.UNDOCUMENTED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help = "No-op. Kept here for backwards compatibility."
  )
  public String javaBuilderTop;

  @Option(
    name = "singlejar_top",
    defaultValue = "null",
    documentationCategory = OptionDocumentationCategory.UNDOCUMENTED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help = "No-op. Kept here for backwards compatibility."
  )
  public String singleJarTop;

  @Option(
    name = "genclass_top",
    defaultValue = "null",
    documentationCategory = OptionDocumentationCategory.UNDOCUMENTED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help = "No-op. Kept here for backwards compatibility."
  )
  public String genClassTop;

  @Option(
    name = "ijar_top",
    defaultValue = "null",
    documentationCategory = OptionDocumentationCategory.UNDOCUMENTED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help = "No-op. Kept here for backwards compatibility."
  )
  public String iJarTop;

  @Option(
    name = "java_langtools",
    defaultValue = "null",
    documentationCategory = OptionDocumentationCategory.UNDOCUMENTED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help = "No-op. Kept here for backwards compatibility."
  )
  public String javaLangtoolsJar;

  @Option(
    name = "javac_bootclasspath",
    defaultValue = "null",
    documentationCategory = OptionDocumentationCategory.UNDOCUMENTED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help = "No-op. Kept here for backwards compatibility."
  )
  public String javacBootclasspath;

  @Option(
    name = "javac_extdir",
    defaultValue = "null",
    documentationCategory = OptionDocumentationCategory.UNDOCUMENTED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help = "No-op. Kept here for backwards compatibility."
  )
  public String javacExtdir;

  @Option(
    name = "host_java_launcher",
    defaultValue = "null",
    converter = LabelConverter.class,
    documentationCategory = OptionDocumentationCategory.UNCATEGORIZED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help = "The Java launcher used by tools that are executed during a build."
  )
  public Label hostJavaLauncher;

  @Option(
    name = "java_launcher",
    defaultValue = "null",
    converter = LabelConverter.class,
    documentationCategory = OptionDocumentationCategory.UNCATEGORIZED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help =
        "The Java launcher to use when building Java binaries. "
            + "The \"launcher\" attribute overrides this flag. "
  )
  public Label javaLauncher;

  @Option(
    name = "proguard_top",
    defaultValue = "null",
    converter = LabelConverter.class,
    documentationCategory = OptionDocumentationCategory.UNCATEGORIZED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help =
        "Specifies which version of ProGuard to use for code removal when building a Java "
            + "binary."
  )
  public Label proguard;

  @Option(
    name = "extra_proguard_specs",
    allowMultiple = true,
    defaultValue = "", // Ignored
    converter = LabelConverter.class,
    documentationCategory = OptionDocumentationCategory.UNDOCUMENTED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help =
        "Additional Proguard specs that will be used for all Proguard invocations.  Note that "
            + "using this option only has an effect when Proguard is used anyway."
  )
  public List<Label> extraProguardSpecs;

  /**
   * Comma-separated list of Mnemonic=label pairs of optimizers to run in the given order, treating
   * {@code Proguard} specially by substituting in the relevant Proguard binary automatically. All
   * optimizers must understand the same flags as Proguard.
   */
  @Option(
    name = "experimental_bytecode_optimizers",
    defaultValue = "Proguard",
    converter = LabelMapConverter.class,
    documentationCategory = OptionDocumentationCategory.UNDOCUMENTED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help = "Do not use."
  )
  public Map<String, Label> bytecodeOptimizers;

  @Option(
    name = "translations",
    defaultValue = "auto",
    documentationCategory = OptionDocumentationCategory.UNCATEGORIZED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help =
        "Translate Java messages; bundle all translations into the jar " + "for each affected rule."
  )
  public TriState bundleTranslations;

  @Option(
    name = "message_translations",
    defaultValue = "",
    allowMultiple = true,
    documentationCategory = OptionDocumentationCategory.UNCATEGORIZED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help = "The message translations used for translating messages in Java targets."
  )
  public List<String> translationTargets;

  @Option(
    name = "check_constraint",
    allowMultiple = true,
    defaultValue = "",
    documentationCategory = OptionDocumentationCategory.UNCATEGORIZED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help = "Check the listed constraint."
  )
  public List<String> checkedConstraints;

  @Option(
    name = "java_optimization_mode",
    defaultValue = "legacy",
    converter = JavaOptimizationModeConverter.class,
    documentationCategory = OptionDocumentationCategory.UNDOCUMENTED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help = "Applies desired link-time optimizations to Java binaries and tests."
  )
  public JavaOptimizationMode javaOptimizationMode;

  @Option(
    name = "legacy_bazel_java_test",
    defaultValue = "false",
    documentationCategory = OptionDocumentationCategory.UNDOCUMENTED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help = "Use the legacy mode of Bazel for java_test."
  )
  public boolean legacyBazelJavaTest;

  @Option(
    name = "strict_deps_java_protos",
    defaultValue = "false",
    documentationCategory = OptionDocumentationCategory.UNDOCUMENTED,
    effectTags = {OptionEffectTag.BUILD_FILE_SEMANTICS, OptionEffectTag.EAGERNESS_TO_EXIT},
    help =
        "When 'strict-deps' is on, .java files that depend on classes not declared in their rule's "
            + "'deps' fail to build. In other words, it's forbidden to depend on classes obtained "
            + "transitively. When true, Java protos are strict regardless of their 'strict_deps' "
            + "attribute."
  )
  public boolean strictDepsJavaProtos;

  // TODO(twerth): Remove flag after it's turned on globally.
  @Option(
      name = "experimental_proto_generated_strict_deps",
      defaultValue = "false",
      documentationCategory = OptionDocumentationCategory.UNDOCUMENTED,
      effectTags = {OptionEffectTag.BUILD_FILE_SEMANTICS, OptionEffectTag.EAGERNESS_TO_EXIT},
      help = "Enables strict deps mode for the java compilation of proto generated Java code.")
  public boolean protoGeneratedStrictDeps;

  @Option(
      name = "experimental_enable_java_proto_exports",
      defaultValue = "false",
      documentationCategory = OptionDocumentationCategory.UNDOCUMENTED,
      effectTags = {OptionEffectTag.BUILD_FILE_SEMANTICS, OptionEffectTag.EAGERNESS_TO_EXIT},
      help =
          "Enables exports forwarding for proto_library targets depended on by "
              + "java_proto_library targets.")
  public boolean isJavaProtoExportsEnabled;

  @Option(
    name = "experimental_java_header_compilation_disable_javac_fallback",
    defaultValue = "false",
    documentationCategory = OptionDocumentationCategory.UNDOCUMENTED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help =
        "If --java_header_compilation is set, report diagnostics from turbine instead of falling "
            + " back to javac. Diagnostics will be produced more quickly, but may be less helpful."
  )
  public boolean headerCompilationDisableJavacFallback;

  @Option(
    name = "experimental_one_version_enforcement",
    defaultValue = "OFF",
    converter = OneVersionEnforcementLevelConverter.class,
    documentationCategory = OptionDocumentationCategory.UNDOCUMENTED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help =
        "When enabled, enforce that a java_binary rule can't contain more than one version "
            + "of the same class file on the classpath. This enforcement can break the build, or "
            + "can just result in warnings."
  )
  public OneVersionEnforcementLevel enforceOneVersion;

  @Option(
    name = "experimental_import_deps_checking",
    defaultValue = "OFF",
    converter = ImportDepsCheckingLevelConverter.class,
    documentationCategory = OptionDocumentationCategory.INPUT_STRICTNESS,
    effectTags = {OptionEffectTag.LOADING_AND_ANALYSIS},
    help =
        "When enabled, check whether the dependencies of an aar_import are complete. "
            + "This enforcement can break the build, or can just result in warnings."
  )
  public ImportDepsCheckingLevel importDepsCheckingLevel;

  @Option(
    name = "one_version_enforcement_on_java_tests",
    defaultValue = "true",
    documentationCategory = OptionDocumentationCategory.UNDOCUMENTED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help =
        "When enabled, and with experimental_one_version_enforcement set to a non-NONE value,"
            + " enforce one version on java_test targets. This flag can be disabled to improve"
            + " incremental test performance at the expense of missing potential one version"
            + " violations."
  )
  public boolean enforceOneVersionOnJavaTests;

  @Option(
    name = "experimental_allow_runtime_deps_on_neverlink",
    defaultValue = "true",
    documentationCategory = OptionDocumentationCategory.UNDOCUMENTED,
    effectTags = { OptionEffectTag.BUILD_FILE_SEMANTICS },
    metadataTags = { OptionMetadataTag.EXPERIMENTAL },
    help =
        "Flag to help transition from allowing to disallowing runtime_deps on neverlink"
            + " Java archives. The depot needs to be cleaned up to roll this out by default."
  )
  public boolean allowRuntimeDepsOnNeverLink;

  @Option(
    name = "jplPropagateCcLinkParamsStore",
    defaultValue = "false",
    documentationCategory = OptionDocumentationCategory.UNDOCUMENTED,
    effectTags = {OptionEffectTag.AFFECTS_OUTPUTS, OptionEffectTag.LOADING_AND_ANALYSIS},
    metadataTags = {OptionMetadataTag.INCOMPATIBLE_CHANGE},
    help = "Roll-out flag for making java_proto_library propagate CcLinkParamsStore. DO NOT USE."
  )
  public boolean jplPropagateCcLinkParamsStore;

  // Plugins are built using the host config. To avoid cycles we just don't propagate
  // this option to the host config. If one day we decide to use plugins when building
  // host tools, we can improve this by (for example) creating a compiler configuration that is
  // used only for building plugins.
  @Option(
    name = "plugin",
    converter = LabelListConverter.class,
    allowMultiple = true,
    defaultValue = "",
    documentationCategory = OptionDocumentationCategory.UNCATEGORIZED,
    effectTags = {OptionEffectTag.UNKNOWN},
    help = "Plugins to use in the build. Currently works with java_plugin."
  )
  public List<Label> pluginList;

  @Override
  public FragmentOptions getHost() {
    JavaOptions host = (JavaOptions) getDefault();

    host.javaBase = hostJavaBase;
    host.jvmOpts = ImmutableList.of("-XX:ErrorFile=/dev/stderr");

    host.javacOpts = javacOpts;
    host.javaToolchain = hostJavaToolchain;

    host.javaLauncher = hostJavaLauncher;

    // Java builds often contain complicated code generators for which
    // incremental build performance is important.
    host.useIjars = useIjars;
    host.headerCompilation = headerCompilation;

    host.javaDeps = javaDeps;
    host.javaClasspath = javaClasspath;

    host.strictJavaDeps = strictJavaDeps;
    host.fixDepsTool = fixDepsTool;

    host.enforceOneVersion = enforceOneVersion;
    host.importDepsCheckingLevel = importDepsCheckingLevel;
    // java_test targets can be used as a host tool, Ex: as a validating tool on a genrule.
    host.enforceOneVersionOnJavaTests = enforceOneVersionOnJavaTests;
    host.allowRuntimeDepsOnNeverLink = allowRuntimeDepsOnNeverLink;

    host.jplPropagateCcLinkParamsStore = jplPropagateCcLinkParamsStore;

    host.protoGeneratedStrictDeps = protoGeneratedStrictDeps;

    return host;
  }

  @Override
  public Map<String, Set<Label>> getDefaultsLabels() {
    Map<String, Set<Label>> result = new HashMap<>();
    result.put("JDK", ImmutableSet.of(javaBase, hostJavaBase));
    result.put("JAVA_TOOLCHAIN", ImmutableSet.of(javaToolchain));

    return result;
  }
}
