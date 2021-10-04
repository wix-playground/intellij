licenses(["notice"])  # Apache 2.0

load(
    "//aspect/testing/rules:intellij_aspect_test_fixture.bzl",
    "intellij_aspect_test_fixture",
)

proto_library(
    name = "a_proto",
    srcs = ["a.proto"],
    import_prefix = "test",
    strip_import_prefix = "/" + package_name(),
)

proto_library(
    name = "b_proto",
    srcs = ["b.proto"],
    deps = [":a_proto"],
)

intellij_aspect_test_fixture(
    name = "fixture",
    deps = [":b_proto"],
)

java_test(
    name = "ProtoLibraryTest",
    srcs = ["ProtoLibraryTest.java"],
    data = [
        ":fixture",
    ],
    deps = [
        "//aspect/testing:BazelIntellijAspectTest",
        "//aspect/testing:guava",
        "//aspect/testing/rules:IntellijAspectTest",
        "//aspect/testing/rules:intellij_aspect_test_fixture_java_proto",
        "//intellij_platform_sdk:test_libs",
        "//proto:intellij_ide_info_java_proto",
        "@junit//jar",
    ],
)
