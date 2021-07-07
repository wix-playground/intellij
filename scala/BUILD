load(
    "//build_defs:build_defs.bzl",
    "intellij_plugin",
    "intellij_plugin_library",
    "optional_plugin_xml",
    "stamped_plugin_xml",
)
load(
    "//testing:test_defs.bzl",
    "intellij_integration_test_suite",
    "intellij_unit_test_suite",
)

licenses(["notice"])  # Apache 2.0

_FAST_BUILD_SCALAC_SRCS = ["src/com/google/idea/blaze/scala/fastbuild/ScalaCompiler.java"]

# This interface is used by two classloaders (the plugin's and the compiler's) whose only shared
# classes are those from the JRE, so it can't have any dependencies at all!
java_library(
    name = "fast_build_scalac_interface",
    srcs = _FAST_BUILD_SCALAC_SRCS,
)

_FAST_BUILD_SCALAC_IMPL_SRCS = [
    "src/com/google/idea/blaze/scala/fastbuild/ScalaCompilerImpl.java",
]

java_library(
    name = "scala",
    srcs = glob(
        ["src/**/*.java"],
        exclude = _FAST_BUILD_SCALAC_SRCS + _FAST_BUILD_SCALAC_IMPL_SRCS
    ),
    visibility = ["//visibility:public"],
    deps = [
        ":fast_build_scalac_interface",
        "//base",
        "//intellij_platform_sdk:jsr305",
        "//intellij_platform_sdk:plugin_api",
        "//java",
        "//proto:proto_deps",
        "//sdkcompat",
        "//third_party/scala",
    ],
)

java_library(
    name = "fast_build_scalac",
    srcs = _FAST_BUILD_SCALAC_SRCS + _FAST_BUILD_SCALAC_IMPL_SRCS,
    visibility = [
        "//aswb:__pkg__",
        "//ijwb:__pkg__",
        "//scala/com/google/devtools/intellij/blaze/plugin:__subpackages__",
    ],
    deps = [],
)

optional_plugin_xml(
    name = "optional_xml",
    module = "org.intellij.scala",
    plugin_xml = "src/META-INF/scala-contents.xml",
)

intellij_plugin_library(
    name = "plugin_library",
    optional_plugin_xmls = [":optional_xml"],
    plugin_xmls = ["src/META-INF/blaze-scala.xml"],
    visibility = ["//visibility:public"],
    deps = [":scala"],
)

stamped_plugin_xml(
    name = "scala_plugin_xml",
    plugin_id = "com.google.idea.blaze.scala",
    plugin_name = "com.google.idea.blaze.scala",
)

intellij_plugin(
    name = "scala_integration_test_plugin",
    testonly = 1,
    plugin_xml = ":scala_plugin_xml",
    deps = [
        ":plugin_library",
        "//base:plugin_library",
        "//java:plugin_library",
    ],
)

intellij_unit_test_suite(
    name = "unit_tests",
    srcs = glob(["tests/unittests/**/*.java"]),
    test_package_root = "com.google.idea.blaze",
    deps = [
        ":scala",
        "//base",
        "//base:unit_test_utils",
        "//common/experiments",
        "//common/experiments:unit_test_utils",
        "//intellij_platform_sdk:jsr305",
        "//intellij_platform_sdk:plugin_api_for_tests",
        "//intellij_platform_sdk:test_libs",
        "//java",
        "//third_party/scala:scala_for_tests",
        "@junit//jar",
    ],
)

intellij_integration_test_suite(
    name = "integration_tests",
    srcs = glob(["tests/integrationtests/**/*.java"]),
    required_plugins = "org.intellij.scala,com.google.idea.blaze.scala",
    test_package_root = "com.google.idea.blaze.scala",
    runtime_deps = [":scala_integration_test_plugin"],
    deps = [
        ":scala",
        "//base",
        "//base:integration_test_utils",
        "//base:unit_test_utils",
        "//intellij_platform_sdk:plugin_api_for_tests",
        "//intellij_platform_sdk:test_libs",
        "//java",
        "//third_party/scala:scala_for_tests",
        "@junit//jar",
    ],
)
