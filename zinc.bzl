load("@bazel_tools//tools/build_defs/repo:jvm.bzl", "jvm_maven_import_external")

def dependencies():
    jvm_maven_import_external(
        name = "org_scala_sbt_compiler_bridge_2_12",
        artifact = "org.scala-sbt:compiler-bridge_2.12:1.3.4",
        artifact_sha256 = "3ee60ce1e0850a04facc5596cbbccf4bd422e73e40a586af944b115438c4c2ad",
        srcjar_sha256 = "24cd30dcb37c2b24f962118f49489c98a66b49600cfd7fbb9eab68475ddd56a2",
        deps = [
            "@com_google_protobuf_protobuf_java",
            "@org_scala_sbt_compiler_interface",
        ],
        fetch_sources = True,
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "org_scala_sbt_compiler_interface",
        artifact = "org.scala-sbt:compiler-interface:1.3.4",
        artifact_sha256 = "b552b04dbe8e7dce1d79b3365b59e6b71acc640089760511e969b03dd5969661",
        srcjar_sha256 = "c17d0c3e8fffefd77c66a7bac1134387a759476c37edee63055e0a58fc655ae9",
        deps = [
            "@com_google_protobuf_protobuf_java",
            "@org_scala_sbt_util_interface",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "org_scala_sbt_io_2_12",
        artifact = "org.scala-sbt:io_2.12:1.3.0",
        artifact_sha256 = "df60541da3c7c70eeacd90c846b8c5997de61384a72ead773590409a9ba02470",
        srcjar_sha256 = "50a1e9cb38c9f8e918faa46b46a04691d05bcd2b3d39bfa7721d6bfbe3f4674b",
        deps = [
            "@com_swoval_file_tree_views",
            "@net_java_dev_jna_jna",
            "@net_java_dev_jna_jna_platform",
            "@org_scala_lang_scala_library",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "org_scala_sbt_launcher_interface",
        artifact = "org.scala-sbt:launcher-interface:1.0.0",
        artifact_sha256 = "11ab8f0e2c035c90f019e4f5780ee57de978b7018d34e8f020eb88aa8b14af25",
        srcjar_sha256 = "ca2de13465aee529ebed512ecc1a214e521f436e9a2219042777b32a3cfcf287",
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "org_scala_sbt_sbinary_2_12",
        artifact = "org.scala-sbt:sbinary_2.12:0.5.0",
        artifact_sha256 = "03eff29f0e73ac1d2101c008664aee33ff3eb05fc138ac0ba0bbdc54a7a3b64d",
        srcjar_sha256 = "16295c7720e1bf06d1296784bcc972c17ddc91c4fd26800674ca0b089d6bcfd6",
        deps = [
            "@org_scala_lang_modules_scala_xml_2_12",
            "@org_scala_lang_scala_library",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "org_scala_sbt_util_control_2_12",
        artifact = "org.scala-sbt:util-control_2.12:1.3.0",
        artifact_sha256 = "b91642135a6a08fb3f911fc03ff4e3075a4d3a579966a1e50cc72f6355a23929",
        srcjar_sha256 = "6891fadb836a77e3db6d354ccd44f1e2926163da1aa1ac2447e8ca8fd15a4fac",
        deps = [
            "@org_scala_lang_scala_library",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "org_scala_sbt_util_interface",
        artifact = "org.scala-sbt:util-interface:1.3.0",
        artifact_sha256 = "89028234b4621ac92761676a00e2e47598fcf5232a9bb994a7ed6dee94eb5aa2",
        srcjar_sha256 = "469b23bde6234973f9b0e8afcf2e4ef0a7747f11000235f50038cab1cbe0ed02",
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "org_scala_sbt_util_logging_2_12",
        artifact = "org.scala-sbt:util-logging_2.12:1.3.0",
        artifact_sha256 = "f23a67634c5fcbe41b82cd02e53c5cf8545fb9ac29e14101d1540f8c33d120c2",
        srcjar_sha256 = "3160304a3cf24900515f0e7fd39f8d2749287353869e941f67b8245ba90f42b9",
        deps = [
            "@com_eed3si9n_sjson_new_core_2_12",
            "@com_eed3si9n_sjson_new_scalajson_2_12",
            "@com_lmax_disruptor",
            "@jline_jline",
            "@org_apache_logging_log4j_log4j_api",
            "@org_apache_logging_log4j_log4j_core",
            "@org_scala_lang_scala_library",
            "@org_scala_lang_scala_reflect",
            "@org_scala_sbt_io_2_12",
            "@org_scala_sbt_util_interface",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "org_scala_sbt_util_relation_2_12",
        artifact = "org.scala-sbt:util-relation_2.12:1.3.0",
        artifact_sha256 = "6397d2212446dbe999d8e0a02ce07827987c24b575f143dbbcba428fb73fbd0a",
        srcjar_sha256 = "8ec50461f0c0cfef50b3edacd3dc7c7fc1684f7ad965c121c561ab7c919dac78",
        deps = [
            "@org_scala_lang_scala_library",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "org_scala_sbt_zinc_2_12",
        artifact = "org.scala-sbt:zinc_2.12:1.3.4",
        artifact_sha256 = "22735543354ea1f04f31c58097347bf10f8e7b7c60127091d4814aecd73115d1",
        srcjar_sha256 = "4b12de9881e13d8a308242a593d666ef15c359d67c37d0980138f2d322d9ed69",
        deps = [
            "@com_google_protobuf_protobuf_java",
            "@org_scala_lang_scala_library",
            "@org_scala_sbt_zinc_classfile_2_12",
            "@org_scala_sbt_zinc_compile_core_2_12",
            "@org_scala_sbt_zinc_core_2_12",
            "@org_scala_sbt_zinc_persist_2_12",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "org_scala_sbt_zinc_apiinfo_2_12",
        artifact = "org.scala-sbt:zinc-apiinfo_2.12:1.3.4",
        artifact_sha256 = "af1e2b70393bebf2ee9db9c97f7f22bb62f53adee361c35c8ef462ece72237a9",
        srcjar_sha256 = "7a00449ab9c1ba53165f1bf94c7eff534ed6df64596941a367ab10f1e4cf39c8",
        deps = [
            "@com_google_protobuf_protobuf_java",
            "@org_scala_lang_scala_library",
            "@org_scala_sbt_compiler_bridge_2_12",
            "@org_scala_sbt_compiler_interface",
            "@org_scala_sbt_zinc_classfile_2_12",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "org_scala_sbt_zinc_classfile_2_12",
        artifact = "org.scala-sbt:zinc-classfile_2.12:1.3.4",
        artifact_sha256 = "2f31f57c6b76de0c320d9eb2f56641ca5b241d17d0673b367b53688548b1d07a",
        srcjar_sha256 = "cc703f951bd406c6670b146198060a0363cacc8c85c2b2ddf677fafab45697fa",
        deps = [
            "@com_google_protobuf_protobuf_java",
            "@org_scala_lang_scala_library",
            "@org_scala_sbt_compiler_interface",
            "@org_scala_sbt_io_2_12",
            "@org_scala_sbt_util_logging_2_12",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "org_scala_sbt_zinc_classpath_2_12",
        artifact = "org.scala-sbt:zinc-classpath_2.12:1.3.4",
        artifact_sha256 = "107610bbb481d95a08a8e4e7f8b8ebf0d42c3ef9cfacaee17d46692b92c5fbba",
        srcjar_sha256 = "c50cf3cd43ace34afb016ffa076d39259777d8befb41f47ffdf99b4641d9dcc5",
        deps = [
            "@com_google_protobuf_protobuf_java",
            "@org_scala_lang_scala_compiler",
            "@org_scala_lang_scala_library",
            "@org_scala_sbt_compiler_interface",
            "@org_scala_sbt_io_2_12",
            "@org_scala_sbt_launcher_interface",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "org_scala_sbt_zinc_compile_core_2_12",
        artifact = "org.scala-sbt:zinc-compile-core_2.12:1.3.4",
        artifact_sha256 = "020349ad0eb76b69e92428753b731b23a8593c579a8c09a35aee43cfb5816a5e",
        srcjar_sha256 = "3610a98b8bf19216baa4af8c3796187cee226271b670cdc37faf0a13c0b271f5",
        deps = [
            "@com_google_protobuf_protobuf_java",
            "@org_scala_lang_modules_scala_parser_combinators_2_12",
            "@org_scala_lang_scala_library",
            "@org_scala_sbt_compiler_interface",
            "@org_scala_sbt_io_2_12",
            "@org_scala_sbt_launcher_interface",
            "@org_scala_sbt_util_control_2_12",
            "@org_scala_sbt_util_logging_2_12",
            "@org_scala_sbt_zinc_apiinfo_2_12",
            "@org_scala_sbt_zinc_classfile_2_12",
            "@org_scala_sbt_zinc_classpath_2_12",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "org_scala_sbt_zinc_core_2_12",
        artifact = "org.scala-sbt:zinc-core_2.12:1.3.4",
        artifact_sha256 = "3bac5ccaad0d218d15a7611403b3012a5e2d53ca446514ba942f2984bba38742",
        srcjar_sha256 = "21a1d676f2a21a444f708f794947b8dfbfa07f9cba7ea49233fe84c53a62da19",
        deps = [
            "@com_google_protobuf_protobuf_java",
            "@com_trueaccord_scalapb_scalapb_runtime_2_12",
            "@org_scala_lang_scala_library",
            "@org_scala_sbt_compiler_interface",
            "@org_scala_sbt_io_2_12",
            "@org_scala_sbt_util_logging_2_12",
            "@org_scala_sbt_util_relation_2_12",
            "@org_scala_sbt_zinc_apiinfo_2_12",
            "@org_scala_sbt_zinc_classpath_2_12",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "org_scala_sbt_zinc_persist_2_12",
        artifact = "org.scala-sbt:zinc-persist_2.12:1.3.4",
        artifact_sha256 = "faef7c45663e1c889f27da958c378cfe5e3cbe0dbb40ec4a14229bda666cf18f",
        srcjar_sha256 = "8d4cb5bb384c8f5d85a78ed2e58c00ae8f4ef129b99f0b4e59470fb765e99b6a",
        deps = [
            "@com_google_protobuf_protobuf_java",
            "@com_trueaccord_scalapb_scalapb_runtime_2_12",
            "@org_scala_lang_scala_library",
            "@org_scala_sbt_sbinary_2_12",
            "@org_scala_sbt_zinc_core_2_12",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "com_trueaccord_scalapb_scalapb_runtime_2_12",
        artifact = "com.trueaccord.scalapb:scalapb-runtime_2.12:0.6.0",
        artifact_sha256 = "7921c157a5d0c4852d6ee99c728cf77c148ce6d36280dfcb6b58d1fa90d17f8d",
        srcjar_sha256 = "ed9b75d56698da090ead2ee1f464157225a4c6117d4adb31d2947809fb1f4da8",
        deps = [
            "@com_google_protobuf_protobuf_java",
            "@com_lihaoyi_fastparse_2_12",
            "@com_trueaccord_lenses_lenses_2_12",
            "@org_scala_lang_scala_library",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "com_trueaccord_lenses_lenses_2_12",
        artifact = "com.trueaccord.lenses:lenses_2.12:0.4.12",
        artifact_sha256 = "7cedcbc3125ad3f156466d6f3aec24b7fe6954cdc54a426ea089b4a46cd84c1c",
        srcjar_sha256 = "2eed83e6a00d9dbfdcb367a28ca4a7d2080b0adb1dbabfe4892bef79e8b39aef",
        deps = [
            "@org_scala_lang_scala_library",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "com_swoval_file_tree_views",
        artifact = "com.swoval:file-tree-views:2.1.3",
        artifact_sha256 = "7e8157f873f4c4d5463d69c0d1c297cccfa8361d2364f512bcfe0868e47ea41c",
        srcjar_sha256 = "f776a0d45ddc559dd7242d16fb0cfe4038cf003bf7ca6b7f7bcd339219ea8070",
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "com_eed3si9n_shaded_scalajson_2_12",
        artifact = "com.eed3si9n:shaded-scalajson_2.12:1.0.0-M4",
        artifact_sha256 = "264051c330fca00fe57d4b4cb767c1f6b359a5603f79f63562832125c7055a40",
        srcjar_sha256 = "73400e3c769019b0ea5f5f5f94e61a1ebbc3d9b6667c455524b15967a0f4e550",
        deps = [
            "@org_scala_lang_scala_library",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "com_eed3si9n_sjson_new_core_2_12",
        artifact = "com.eed3si9n:sjson-new-core_2.12:0.8.3",
        artifact_sha256 = "2cbba080a09f5322fdda29f3ecf73d2add55ce2e3c123780916c9282727ab221",
        srcjar_sha256 = "e05ab53e307793d09e181b4f0662e3f040af4ca0c169f67902be16ad4d3b209c",
        deps = [
            "@org_scala_lang_scala_library",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "com_eed3si9n_sjson_new_scalajson_2_12",
        artifact = "com.eed3si9n:sjson-new-scalajson_2.12:0.8.3",
        artifact_sha256 = "928a452967e90336f4b893c23471bef46c0471eda6c301371d927f693957ce18",
        srcjar_sha256 = "f75378d242e3a54a57e1bb23c4589cffd38cd84fff18991e0e8cf37009d0802e",
        deps = [
            "@com_eed3si9n_shaded_scalajson_2_12",
            "@com_eed3si9n_sjson_new_core_2_12",
            "@org_scala_lang_scala_library",
            "@org_spire_math_jawn_parser_2_12",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "org_spire_math_jawn_parser_2_12",
        artifact = "org.spire-math:jawn-parser_2.12:0.10.4",
        artifact_sha256 = "c617fdde8c5b7646b1bedc4f6f565e85aa83b157ea93977fcdc4056b823aadb2",
        srcjar_sha256 = "7601c166db3328c7f63a6388f637ddaf567448b622df167666526b5daefb751c",
        deps = [
            "@org_scala_lang_scala_library",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "org_scala_lang_scala_library",
        artifact = "org.scala-lang:scala-library:2.12.13",
        artifact_sha256 = "1bb415cff43f792636556a1137b213b192ab0246be003680a3b006d01235dd89",
        srcjar_sha256 = "d299cc22829c08bc595a1d4378d7ad521babb6871ca2eab623d55b80c9307653",
        tags = [
            "no-index",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "org_scala_lang_modules_scala_parser_combinators_2_12",
        artifact = "org.scala-lang.modules:scala-parser-combinators_2.12:1.1.2",
        artifact_sha256 = "24985eb43e295a9dd77905ada307a850ca25acf819cdb579c093fc6987b0dbc2",
        srcjar_sha256 = "8fbe3fa9e748f24aa6d6868c0c2be30d41a02d20428ed5429bb57a897cb756e3",
        deps = [
            "@org_scala_lang_scala_library",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "net_java_dev_jna_jna",
        artifact = "net.java.dev.jna:jna:5.12.1",
        artifact_sha256 = "91a814ac4f40d60dee91d842e1a8ad874c62197984403d0e3c30d39e55cf53b3",
        srcjar_sha256 = "28f933201a478ff5882fb151cc2a79db68d961934f1d73d35ab8991bf0ed188a",
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "net_java_dev_jna_jna_platform",
        artifact = "net.java.dev.jna:jna-platform:5.12.1",
        artifact_sha256 = "8ce969116cac95bd61b07a8d5e07174b352e63301473caac72c395e3c08488d2",
        srcjar_sha256 = "7e476c950332a6f21c31533325d21f28b290fcbfef82b5c30cac1db53db29f6e",
        deps = [
            "@net_java_dev_jna_jna",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "com_lihaoyi_sourcecode_2_12",
        artifact = "com.lihaoyi:sourcecode_2.12:0.1.7",
        artifact_sha256 = "f07d79f0751ac275cc09b92caf3618f0118d153da7868b8f0c9397ce93c5f926",
        srcjar_sha256 = "7d0bb8c2de326485ffb02da7fca2fedd814b1ece6a86e1325594d593e5eaba63",
        deps = [
            "@org_scala_lang_scala_library",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "com_lihaoyi_fastparse_2_12",
        artifact = "com.lihaoyi:fastparse_2.12:2.1.3",
        artifact_sha256 = "e8b831a843c0eb5105d42e4b6febfc772b3aed3a853a899e6c8196e9ecc057df",
        srcjar_sha256 = "d7d33157d9dc83d3dd2cec6b4835bede4ce7b7d5e0bef6a08ff6c9e0efe78dd9",
        deps = [
            "@com_lihaoyi_sourcecode_2_12",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "org_scala_lang_modules_scala_xml_2_12",
        artifact = "org.scala-lang.modules:scala-xml_2.12:2.0.1",
        artifact_sha256 = "e36c0cdca62ddd4ea998db47936ab6a2f56854ec69c059a291f725ad5070538b",
        srcjar_sha256 = "fb138664d3fefb7d34c1a8ef0c8cb448ba8d6f53b9b30ad1cf9d268e293141d2",
        deps = [
            "@org_scala_lang_scala_library",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "jline_jline",
        artifact = "jline:jline:0.9.94",
        artifact_sha256 = "d8df0ffb12d87ca876271cda4d59b3feb94123882c1be1763b7faf2e0a0b0cbb",
        srcjar_sha256 = "e2efd2f925e717bb7e88997feb48c7ba2dfd02261051474b728eae58d38ae78b",
        deps = [
            #       "@junit_junit",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )

    jvm_maven_import_external(
        name = "com_lmax_disruptor",
        artifact = "com.lmax:disruptor:3.4.0",
        artifact_sha256 = "e6f084c3a61b5b0b58c80a606120d503acf3708ede2d6a61d7dce67ff6d42c1a",
        srcjar_sha256 = "722d0aee7b658c3324170be320a7af61b2b995776f5330778bfbba78fe410174",
        server_urls = ["https://repo1.maven.org/maven2"],
    )


    jvm_maven_import_external(
        name = "org_apache_logging_log4j_log4j_api",
        artifact = "org.apache.logging.log4j:log4j-api:2.17.1",
        artifact_sha256 = "b0d8a4c8ab4fb8b1888d0095822703b0e6d4793c419550203da9e69196161de4",
        srcjar_sha256 = "198c1e77d61a46fc08d323e1931cd20b430d8a4114a17658b64d15ddb2d902b0",
        server_urls = ["https://repo1.maven.org/maven2"],
    )


    jvm_maven_import_external(
        name = "org_apache_logging_log4j_log4j_core",
        artifact = "org.apache.logging.log4j:log4j-core:2.17.1",
        artifact_sha256 = "c967f223487980b9364e94a7c7f9a8a01fd3ee7c19bdbf0b0f9f8cb8511f3d41",
        srcjar_sha256 = "51cc2d5e9eedb7eca77f4fb19d38d2c97084c818013e14a1b2c8d38aea627792",
        deps = [
            "@org_apache_logging_log4j_log4j_api",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )


    jvm_maven_import_external(
        name = "org_scala_lang_scala_compiler",
        artifact = "org.scala-lang:scala-compiler:2.12.13",
        artifact_sha256 = "ea971e004e2f15d3b7569eee8b559f220e23b9993e688bbe986f97938d1dc9f9",
        srcjar_sha256 = "2229b671c1f481ef52bcba19bab859330366fa915cabca7e06c01add02e5cc21",
        deps = [
            "@org_scala_lang_modules_scala_xml_2_12",
            "@org_scala_lang_scala_library",
            "@org_scala_lang_scala_reflect",
        ],
        tags = [
            "no-index",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )


    jvm_maven_import_external(
        name = "org_scala_lang_scala_reflect",
        artifact = "org.scala-lang:scala-reflect:2.12.13",
        artifact_sha256 = "2bd46318d87945e72eb186a7b5ea496c43cf8f0aabc6ff11b3e7962f8635e669",
        srcjar_sha256 = "1226f12c7d368b76e8ed43fad1cf93a5516efaaa0f75f865fddb818bb06d9256",
        deps = [
            "@org_scala_lang_scala_library",
        ],
        tags = [
            "no-index",
        ],
        server_urls = ["https://repo1.maven.org/maven2"],
    )
